# API re-design

One can connect arbitrary numbers of Subscribers to an MVar. An MVar
may have a number of Subscriptions on other MVars.

Some prototypical MVars we aim to make available:

- UserMVar[A] - Updatable by the user.
  - Synchronously: Updatable connects directly to the AtomicReference that the UserMVar wraps
  - Asynchronously: Updatable publishes a message to an MVars subscription
- MapMVar[A] - A derived MVar using some deterministic function applied to an input MVar
- ProductMVar[A] - An MVar derived from a fixed number of MVars,

MVar thus features:
- update(elem: A): Unit (only available on UserMVar)
- map[B](f: A => B): MVar[B]
- product[B](other: MVar[B]): MVar[(A, B)]

# Anatomy of MVar implementations

## MVar

The monotonic variable interface, which exposes sample

- Exposes "publisher" interface to package
- Instantiated with an implementation-dependent number of Subscriptions
- For any new subscriber:
    - Replay the current state as a series of deltas (0/1 is permitted)
    - Replay evolutions as a series of deltas

## RWMVar

A RWMVar[A] is an MVar that can be updated by the user, i.e. there is no consistency relationship
to be maintained by the execution context.

Responsibilities:
    - Consume updates by the user
    - Update the MVar with the updates
    - Broadcast the elements to Subscribers of the UserMVar.

## ROMVar

A read-only MVar, which means that it is updated through a process; updating the variable is not
user-exposed functionality.

## MapMVar

A MapMVar[B] (extends ROMVar[B]) is created from an MVar[A] and a deterministic function f: A => B

Responsibilities
    - Consume elements produced by MVar[A]
    - Transform such elements with the given function f
    - Update the MVar with those elements
    - Broadcast elements of the Merge to Subscribers

## ProductMVar

A ProductMVar[A, B] (extends ROMVar[(A, B)]) is created for two MVar's: MVar[A] and MVar[B], in that order.

Responsibilities:
    - Map MVar[A] elements to (A, B.bottom)
    - Map MVar[B] elements to (A.bottom, B)
    - Merge mapped outputs to (A, B)
    - Update the MVar with elements of the Merge
    - Broadcast elements of the Merge to Subscribers


# Anatomy of the execution context

# Misc

## Requirements

For the sake of monitoring / debugging, it would be nice to be able to render the graph, and be able to access the state of each stage/node.

This requires an explicit representation of the computational graph, which we:
1. Can take as the definition, based upon which we create an in-memory pipeline
2. Can derive from a running graph

(1) has my preference, but not without guaranteeing typesafety.

## Current problems

* Working with full-state and delta-state CRDTs (in particular, dealing with delta-state CRDTs combined with dataflow operations)
* Specification implementation: Akka (Streams + Distributed Data)
* Working with digestable lattices
