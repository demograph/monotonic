MonotonicMap
============

This project is comprised of the MonotonicMap interface and an
in-memory implementation.

# Api
This is the specification of the MonotonicMap interface. Intuitively,
this sits between a key-value store and a message broker. We will
explore here their similarities to expose use cases for MonotonicMap.

A KeyValue allows the persistent one-on-one association between pairs
of key and value. `put` is usually implemented in a last-writer-wins
manner, and replaces the last value, if one existed for the given key,
with a new value. A `get` allows one to retrieve the latest written
value for the supplied key.

Similarly, a message broker allows the association between keys
(in the form of either topics or queues) and values. This association is
typically modeled as a key mapping to a possibly unbounded sequence of
values, which appear one at a time through a `put`-like operation. A
`get`-like operation simply polls for available values and retrieves
them in FIFO order. (Of course, more commonly, one would implement this
in an event-driven fashion, but this is to expose high-level
similarities)

Despite some high-level similarities, the technologies are used for
vastly different purposes. KeyValue stores are durable, whereas message
brokers will usually prune older messages, given that the value
sequence can grow without bound. Message brokers provide decoupling
between sources/publishers of events and consumers for those events,
whereas multiple producers to the same key in a KeyValue store kind of
seem at odds with another, requiring something like last-writer-wins to
resolve conflicts between (concurrent) updates. KeyValue stores more
generally, represent accumulated state of the past. Message brokers
represent events as they occur, ready to be handled incrementally.

MonotonicMap, as mentioned, kind of sits in between the two. It obtains
interesting advantages of both, adds some cool stuff of its own, but
certainly also introduces its own constraints.

MonotonicMap, like the above, introduces something like `put` and `get`.
`put` now registers a delta with a given key, and `get` retrieves a
stream of deltas. The first delta returned may represent all past state
up to that point, consecutive deltas may come as writes occur.

There is a special requirement for the types of values
that are supported by MonotonicMap. They must support a monotonic
merge operation, i.e. they must effectively be CRDTs. In order to be
efficient, they should also be delta-CRDTs. Limiting values to be of the
delta-CRDT class is a significant restriction, but that class is still
full of useful, composable datatypes (one can come close to modeling
datastructures as rich as json). This restriction buys us the power of
being able to merge any two deltas into another delta, and thus we gain
order-independence, a valuable property in highly distributed systems.
We also gain the ability to trade latency for efficiency, by collecting
deltas in mini-batches of respectively smaller or larger size.

MonotonicMap reflects both the properties of a durable KeyValue store,
with its accumulated past state (all past deltas merged together) and
the properties of a real-time message broker (effects of events
represented as deltas of the state). It supports decoupling in that
a key can have any number of consumers, none of which needs to be known
by a publisher (in essence, although I think there are many benefits to
a point-to-point model managed by an infrastructural layer, more on that
later)

The primary use case? Well, my primary use case is real-time interactive
collaborative applications. What is yours?

# Mem - Implementation
Nothing fancy, a straightforward implementation that works, to
facilitate the test-driven developer.

Notable details:

- `write(key, element)` returns a Publisher that will produce a `Persisted` once the
written element is stored in memory. It will also produce a `Propagated`
for each Reader that consumes the element written. The stream never
terminates (MonotonicMap is not aware of whether additional Readers will
subscribe in the future). Cancelling the subscription leads to its
proper cleanup.

- `read(key)` returns a Publisher that produces deltas from an internal
buffer, one dedicated for each Reader. The buffer will merge any write
occurring while there is no demand, and propagate that element as soon
as the Reader signals its demand. As long as there is demand, all writes
(to the given key) are propagated opportunistically. The very first
element produced will reflect the entire state up to the point that
demand was first signaled; no optimization takes place in the form of
splitting it up in bite-size chunks (if you are creating a `Set` that
fills your entire memory, that `Set` will be passed to the Reader).

