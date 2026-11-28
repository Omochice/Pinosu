package io.github.omochice.pinosu.feature.feed.data.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

/**
 * Reduces the HTML body of a feed item to text formatting, tables, and images, with absolute
 * HTTP(S) links and image sources
 */
internal object ItemBodySanitizer {

  private const val ANCHOR = "a"
  private const val HREF = "href"
  private const val IMAGE = "img"
  private const val SRC = "src"
  private const val COLSPAN = "colspan"
  private const val ROWSPAN = "rowspan"

  /**
   * Spans beyond this are dropped rather than kept, because each spanned column or row is laid out
   * even when it holds nothing, so one cell could otherwise blow up the size of the table.
   */
  private const val MAX_SPAN = 20

  /**
   * [Safelist.basic] drops headings and `div`, which would run separate blocks of an article
   * together, tables, which would run their cells together, and images, so they are added.
   *
   * [Safelist.relaxed] is not used, because it also keeps sizing attributes that would fight the
   * reader's own layout, and removing them one by one is longer than adding what is needed.
   */
  private val SAFELIST: Safelist =
      Safelist.basic()
          .addTags("div", "h1", "h2", "h3", "h4", "h5", "h6")
          .addTags("table", "caption", "thead", "tbody", "tfoot", "tr", "th", "td")
          .addAttributes("th", COLSPAN, ROWSPAN)
          .addAttributes("td", COLSPAN, ROWSPAN)
          .addTags(IMAGE)
          .addAttributes(IMAGE, SRC, "alt")
          .addProtocols(IMAGE, SRC, "http", "https")
          .removeProtocols(ANCHOR, HREF, "ftp", "mailto")
          .removeEnforcedAttribute(ANCHOR, "rel")

  /**
   * Sanitize the HTML body of an item
   *
   * @param html Body as HTML
   * @param base URL that relative links and image sources in [html] are relative to
   * @return The sanitized body, or null when neither text nor an image is left
   */
  fun sanitize(html: String, base: String): SanitizedBody? {
    val dirty = Jsoup.parseBodyFragment(html)
    resolveLinks(dirty, base)
    resolveImages(dirty, base)
    val clean = Cleaner(SAFELIST).clean(dirty)
    limitSpans(clean)
    clean.outputSettings().prettyPrint(false)
    val body = clean.body()
    return SanitizedBody(body.html(), body.hasText()).takeIf {
      it.hasText || body.selectFirst(IMAGE) != null
    }
  }

  /**
   * Links are resolved by [LinkResolver] rather than by the cleaner, so that the same rules as for
   * item links decide which schemes survive, and a `base` element in the body cannot move the base.
   */
  private fun resolveLinks(document: Document, base: String) {
    document.select("$ANCHOR[$HREF]").forEach { anchor ->
      val resolved = LinkResolver.resolveAgainst(base, anchor.attr(HREF).trim())
      if (resolved == null) {
        anchor.removeAttr(HREF)
      } else {
        anchor.attr(HREF, resolved)
      }
    }
  }

  /**
   * Images are resolved like links, by the same rules [ItemImageFinder] uses, so that a body image
   * that is also the image of the item has the same URL and can be recognised as such.
   *
   * An empty source is not resolved, because it would resolve to the page itself.
   */
  private fun resolveImages(document: Document, base: String) {
    document.select(IMAGE).forEach { image ->
      val resolved =
          image
              .attr(SRC)
              .trim()
              .takeIf { it.isNotEmpty() }
              ?.let { LinkResolver.resolveAgainst(base, it) }
      if (resolved == null) {
        image.remove()
      } else {
        image.attr(SRC, resolved)
      }
    }
  }

  private fun limitSpans(document: Document) {
    listOf(COLSPAN, ROWSPAN).forEach { attribute ->
      document.select("[$attribute]").forEach { cell ->
        val span = cell.attr(attribute).trim().toIntOrNull()?.takeIf { it in 1..MAX_SPAN }
        if (span == null) {
          cell.removeAttr(attribute)
        } else {
          cell.attr(attribute, span.toString())
        }
      }
    }
  }
}

/**
 * HTML body of a feed item reduced by [ItemBodySanitizer]
 *
 * @property html Sanitized HTML
 * @property hasText Whether any text is left, as opposed to images alone
 */
internal data class SanitizedBody(val html: String, val hasText: Boolean)
