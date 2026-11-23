# RSS Feed Subscription Storage and Unread Determination

Date: 2026-11-23

## Status

Accepted

## Context

Pinosu is going to show RSS and Atom feeds and let users bookmark their items.
This needs two kinds of state the app has never held: the list of subscribed feeds, and which items are unread.
Bookmarks already live on Nostr relays so that every device of a user sees them, and subscriptions are expected to behave the same way.

No NIP defines an RSS subscription list.
The Nostr feed readers found, Nostr-Feedz and its fork Readstr, use kind 30404, which no NIP registers.

Unread state changes on every item the user opens.
Publishing it as an event would need a signature from the external signer (NIP-55) each time.

## Alternatives Considered

### Option 1: A kind no NIP defines

An unregistered replaceable kind in the NIP-51 list shape, or kind 30404 as Nostr-Feedz and Readstr use it.

#### Pros

- Other clients could adopt the shape.

#### Cons

- The kind may later collide with one a NIP defines.

### Option 2: NIP-51 kind 10054 (favorite podcasts)

#### Pros

- A registered kind that already holds RSS URLs.

#### Cons

- Blog feeds would show up in podcast clients as favorite podcasts.

### Option 3: Unread state published to Nostr

#### Pros

- Read state would follow the user across devices.

#### Cons

- Every read would prompt the external signer, and the event grows with every item read.

### Option 4: "New" as items published after a per-feed last-seen time

#### Pros

- Only one timestamp per feed is stored.

#### Cons

- Feeds with missing or wrong publication dates break it, and single items cannot be marked read or unread.

## Decision

We will publish the subscription list as a NIP-78 event and keep unread state only on the device.

### 1. Subscription list as a public NIP-78 event

The list is a kind 30078 event whose `d` tag names Pinosu and its purpose, with one `url` tag per feed in plaintext.
A feed can be filed under at most one category, written as an optional third element of its `url` tag, such as `["url", "https://example.com/feed.xml", "Tech"]`.
Only Pinosu is expected to read it.

#### Rationale

Avoiding a kind collision decided the choice.
NIP-78 lets an app claim a namespace through its `d` tag instead of a kind number, while every other fitting kind is either unregistered or already means something else.
Being readable by other feed readers does not matter, because sharing subscriptions with them is not a goal.

One category per feed, like folders in Feedly, was chosen over several, because a feed under several categories would repeat its items across category tabs.
Keeping the category inside the feed's own tag, instead of in separate category tags, keeps each feed described by exactly one tag, so a list without categories is still a valid list and a feed cannot be left pointing at a category that no tag defines.

### 2. Unread state on the device

Each item is read or unread, and is identified by the identifier its feed provides.
An item becomes read when opened, bookmarked, or marked read, and can be marked unread again.
Items present at the first fetch of a feed on a device start as read.
Items and their read state are stored only on the device, and are deleted some time after they leave the feed.

#### Rationale

Local read state avoids a signer prompt per read.
Starting existing items as read keeps a new subscription from opening with a backlog, without relying on publication dates.
Deletion waits because feeds often drop and re-add items, which would otherwise turn read items unread.

### 3. Bookmarked items are kind 39701 bookmarks

#### Rationale

A bookmarked feed item is a web bookmark, so it belongs in the existing data model.

### Not Decided Here

- NIP-42 AUTH support: decided when a relay is found to reject the list without it.
- Private subscriptions: decided if a user asks to hide a feed.
- OPML import and export: decided as a separate feature.

## Consequences

### Positive

- Subscriptions follow the user across devices, as bookmarks do.
- No kind number can collide with another app.
- Reading items never prompts the external signer.

### Negative

- Read state does not follow the user across devices, and a second device starts every existing item as read.
- NIP-78 says relays should serve its events only to their author after NIP-42 AUTH, so such relays may hide or refuse the list, and Pinosu has no AUTH support.
- Anyone can see which feeds the user follows.
- A relay that accepts the list without AUTH but serves it only after AUTH can hold a list Pinosu cannot see, and Pinosu may publish a new list over it.

### Mitigations

- Items can be marked unread again by hand.
- Relays that refuse the query are ignored, and the list counts as absent once any other relay answers that it has none.
  Because only Pinosu writes this list and it publishes to every write relay, such an answer means the list most likely does not exist anywhere, while refusing relays would otherwise block a new list forever.
- When no relay answers at all, the user sees an error rather than an empty list, and no list is published over it.
- The user is told the list is public before the first subscription is published.

## References

- [NIP-78: Arbitrary custom app data](https://github.com/nostr-protocol/nips/blob/bec0fc89e75645e77a06d145598246efca844457/78.md)
- [NIP-42: Authentication of clients to relays](https://github.com/nostr-protocol/nips/blob/bec0fc89e75645e77a06d145598246efca844457/42.md)
- [NIP-51: Lists](https://github.com/nostr-protocol/nips/blob/bec0fc89e75645e77a06d145598246efca844457/51.md)
- [Readstr subscription sync](https://github.com/privkeyio/readstr/blob/235ac4bceed65ba7a6484caf78be9aa48cc02a51/docs/SUBSCRIPTION_SYNC.md)
- [Hakkadaikon/pendulum](https://github.com/Hakkadaikon/pendulum), which publishes public kind 30078 data
- [Kind 39701 for Bookmark Data Model](kind-39701-bookmark.md)
