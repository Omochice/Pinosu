# Strip Known Tracking Query Parameters from Bookmark URLs

Date: 2026-08-31

## Status

Proposed

## Context

Bookmarks are stored as kind 39701 events ([Kind 39701 for Bookmark Data Model](./kind-39701-bookmark.md)), where the `d` tag holds the URL without scheme and is the identity of the parameterized replaceable event.
URLs shared from browsers and apps frequently carry tracking query parameters such as `utm_source` or `fbclid`.
These parameters do not change which page the URL points to, but they do change the `d` tag, so bookmarking the same page from two different referral paths produces two distinct events.
The duplicates cannot be updated or deleted as one bookmark, and other NIP-B0 clients see them as unrelated entries.

Bookmark events are also published to public relays.
Tracking parameters record which campaign or platform the user came from, so leaving them in the `d` and `r` tags publishes that referral context permanently, and following the `r` tag later re-triggers the same tracking.

## Decision Drivers

Any option has to satisfy three forces.

- **Identity stability**: the same page bookmarked twice must produce the same `d` tag.
- **Content preservation**: query parameters that identify the page content (for example `?v=` on video sites) must survive untouched.
- **Predictability**: the user signs the event, so the URL shown in the posting screen must match what gets signed.

## Considered Options

Three options were weighed against the drivers.

### Option 1: Do nothing

Keep URLs exactly as shared.

**Pros:**

- No risk of altering a URL whose meaning depends on an unrecognized parameter.

**Cons:**

- Duplicate events for the same page persist across referral paths.
- Referral context is published to public relays.

### Option 2: Strip all query parameters

Cut everything from the first `?` on.

**Pros:**

- Maximal deduplication and no list to maintain.

**Cons:**

- Breaks URLs whose query identifies the content itself, such as `youtube.com/watch?v=...`, making the bookmark point to the wrong page.

### Option 3: Strip a fixed list of known tracking parameters

Remove only query keys known to be tracking identifiers, keeping every other parameter.

**Pros:**

- Removes the common tracking noise while leaving content-identifying queries intact.
- The list is small, auditable, and backed by external sources.

**Cons:**

- Parameters not on the list pass through; the list needs occasional maintenance as trackers change.

## Decision

We will strip a fixed list of known tracking query parameter keys from bookmark URLs at write time, in a single shared function under `core/url`, applied both when a URL is extracted from a share intent and when the bookmark event is assembled. The stripped URL feeds both the `d` tag and the `r` tag so the two never diverge. Read paths are left untouched: `d` tags on already-signed events are identifiers and are never rewritten.

### 1. Matching rules

Keys are compared case-insensitively against the query string split on `&`, taking each segment up to its first `=`.
`utm_` matches as a prefix; every other entry matches exactly.
Values are not inspected.
Segments are otherwise preserved byte for byte: no re-encoding, no reordering, no removal of empty segments.
If stripping empties the query, the `?` is dropped.
The fragment and everything before the first `?` are never modified.

**Rationale**: parsing with `java.net.URI` either decodes the query (so `x=a%26b` splits incorrectly) or double-encodes on reassembly, and it throws on the slightly malformed URLs that arrive via share intents.
Plain string handling of the region between the first `?` and the first `#` has none of these failure modes.

### 2. Parameter list

The list is `utm_` as a prefix plus the exact keys `fbclid`, `gclid`, `dclid`, `gbraid`, `wbraid`, `msclkid`, `mc_eid`, `yclid`, `ysclid`, `twclid`, `igshid`, `_hsenc`, `_hsmi`, `__hssc`, `__hstc`, `__hsfp`, `hsctatracking`, `wickedid`, `_openstat`, `mkt_tok`, `oly_anon_id`, `oly_enc_id`, `vero_id`, `__s`.

**Rationale**: every key was verified on 2026-08-31 against at least one of two maintained sources: Firefox's query stripping list (the `query-stripping` collection on Mozilla remote settings, which backs `privacy.query_stripping.strip_list`) and the ClearURLs rules (`rules2.clearurls.xyz`).
`ttclid` was considered but dropped because neither source lists it.
Site-specific rule sets (per-domain rules as ClearURLs ships them) are out of scope; only globally applicable keys are stripped.

### 3. No opt-out, no read-side normalization

Stripping always applies, including when the user re-adds a tracking parameter by hand in the posting screen before signing.
Events fetched from relays are displayed with their `d` and `r` tags as signed.

**Rationale**: a toggle would add a setting for a situation with no identified use case, and rewriting identifiers on the read side would break the correspondence between displayed bookmarks and the replaceable events they belong to.

## Consequences

Bookmarking the same page from different referral paths now converges on one event, and referral context no longer reaches public relays through Pinosu.

Existing events whose `d` tag contains tracking parameters will not match newly created bookmarks for the same page, so historical duplicates remain.
This is no worse than the previous behavior, where no normalization happened at all, and such duplicates can still be deleted manually.

Other NIP-B0 clients that do not strip the same parameters will still produce non-matching `d` tags for the same page.
The list also requires occasional review against its sources; the source names and verification date are recorded here so the review has a starting point.

Scheme removal remains the only other normalization.
Fragments, trailing slashes, and host case are deliberately untouched; changing them is a separate decision.
