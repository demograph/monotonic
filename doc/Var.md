Variable programming
====================

# Introduction

We have gone through a transition of using immutable data instead of mutable data. This is due to negative publicity around using variables, most of which is warranted.

- Concurrent access to mutable datastructures is complicated to do in a way that is not prone to race conditions or other unintended effects
- When sharing data between two separate modules in your codebase, a variable has to be made visible on the highest level of the two modules' respective height. Often also exposing the variable to modules that
shouldn't be touching the data; in the limit introducing completely global variables.
- One might want to take control of some of the complexities by introducing a separation between readers and writers. This requires distinct interfaces for every datastructure. i.e. an IntWriter and IntReader, or DoubleWriter and DoubleReader

One convenience of variables is that for some problems they feel more intuitive than their immutable counterpart, and may be more performant on typical computer architectures as well.

One way of solving these issues is reifying variables as a `Var`, making explicit the fact that it can be `set`d and `sample`d at any point in time:

- Access to the variable is no longer done through its scope; the variable can be passed around as an object
- What modules receive the power to write or read can be easily controlled using the more general `VarWriter` and `VarReader` interfaces
- Having wrapped an arbitrary variable datastructure in our `Var`, we can implement its accessors in a concurrency-safe and performant manner, i.e. `AtomicReference`

Additional advantages that can be gained through this abstraction:

- functorial `map` to derive `Var`s from a base `Var` using an arbitrary function
- `inflate`/`zip` as a means of lifting the datastructure in a heterogeneous list, or expanding the list
- applicative `ap` as a means of deriving new `Var`s from multiple inputs, not unlike the `Signal` pattern in functional reactive programming.
- producing a `ReactivePublisher` such that all updates to the variable can be explicitly observed.

Note that each of these can be implemented in a concurrency-safe and performant way using Akka Streams (Lightbend's reactive-streams implementation)

# Monotonic Variables

We will refer to the subset of datastructures whose operations are monotonic as monotonic datastructures. Their operations inflate the datastructure in a way that enables eventual consistency. For every
such datastructure a `merge` operation exists that is `commutative`, `associative` and `idempotent`, i.e. it is well behaved under re-ordering of operations or multiple executions thereof, which frequently
occur in a network setting.

Monotonic variables add another layer of safety, warranting their use in a truly global context. Here they are not limited to being global on a single machine, but they can be shared between _all_ machines,
with the guarantee that all updates that occur will eventually be reflected on all other machines sharing that variable, assuming those machines have moments of healthy operation where they can receive and
aid in the propagation of those updates. The speed with which such replication occurs is dependent on a number of factors, such as the number of replicas, the health of said replicas, and the efficiency of
the replication mechanism under those conditions. The advantage of monotonic variables is that replication can be implemented as an independent responsibility, separate from the application programmer that
queries and/or updates the variables.