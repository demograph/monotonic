# Combining delta-CRDT and data-flow operations

In this document we explore, and aim to specify, a way of combining delta-CRDTs with appropriate data-flow operations such as applied in Lasp.

## Definition of Lasp ORSet

The example lattice that is used to demonstrate dataflow in Lasp is a particular implementation of the ORSet.
Essentially, this ORSet is a `Map[V, (A, R)]` where `V` represents the value which may be in the set. Whether it _is_ a set member is dictated
by the combination of add-set `A` and remove-set `R`. Every time some `v in V` is added, a unique number is added to `A`. Every time it is
removed, all existing elements in `A` are copied to `R`. Effectively, a value `v` is member of the ORSet if its respective `A` is a strict
subset of its `R`.

## Definition of Delta ORSet

The delta ORSet is a so called causal-CRDT, which is more complicated than the one used in Lasp. We will have a look at the add-wins
ORSet (AWSet from now on), as this has equivalent semantics to the Lasp ORSet. There are two notable high-level design differences:

1. The AWSet is delta-enabled, which means that its mutating operations return a delta-state, which can be merged into the original state
to effectuate it
2. The AWSet is tombstone-less, i.e. deleted elements don't leave behind state.

An AWSet is a pair consisting of a map `M: DotMap[V, DotSet]` and a causal context `C`. Similar to the Lasp ORSet, set-membership is defined as
the keys of `M`, different from that design is that one does not have to look at the respective (DotSet) value. As this design does not leave
behind tombstones (any data) for deleted elements, just checking the keys is sufficient.

The DotSet performs a similar role to `A` in Lasp's ORSet, keeping track of unique elements that indicate unique events of adding the respective
element to the set. Dissimilar to Lasp's `A`, a DotSet is not meant to contain arbitrary unique values that indicate such add-events. Instead,
it is a Set of dots, which is a pair of host-identifier and event-sequence-id (with respect to that host's unique sequence of produced events).

The CausalContext `C` is also simply a DotSet. Where the DotSet value in values of `M` only contains the dots that relate to the adding of the respective
element, `C` contains all the dots that relate to any key-value pair of `M`. Although DotSet features set-semantics, in practice `C` can be
compacted to a version vector, maintaining the highest event-sequence-id seen for each host-id.

Now we are ready to understand deletion and addition. Addition of `e` simply constructs a delta: `Map(e -> d)`, together with a (delta!) causal-context
that contains both `d` and all previously existing dots for value `e`. `d` here is the next available unique-event-identifier on the host where the event
is triggered. Removal also creates a causal-context from all the element-specific dots (this time without adding a new dot) and its element map is just
the empty map.

Most of the magic lies in the definition of join for DotStores (DotSet and DotMap being two examples, DotFun being another not discussed here). The
CausalContext will always contain all the dots representing events that affected the datastructure. This is important for the tombstone-less design,
joining an element-removal will truly delete the dots from the element-specific DotSet. No longer having the data in the element-specific DotSet means
that another filled DotSet looks like it contains events not previously processed. However, given that we maintain `C` with a host-dedicated sequence of
event identifiers, we can differentiate between cancelled-additions-events and new-addition events.

## Definition of Lasp data-flows

Here we will explain how Lasp dataflows are defined on top of the Lasp ORSet, and try to distill their functioning so that it can be copied to `AWSet`

### Functorial map on ORSet

Informally, `ORSet.map(f)` applies `f` to all keys of the `ORSet` and doesn't do much with the metadata `A` and `R` in the happy case. However, if `f`
is surjective then two distinct keys may in fact form one key after application of `f`. In that case, it simply performs a pointwise join of `A` and `R`
for each key mapping to the same key after application of `f`.

Example:
```scala
val intMap = ORMap(
    -1 -> (Set(1), Set.empty),
    0 -> (Set(2), Set(2)),
    1 -> (Set(3, 4), Set(3, 4))
)

intMap.map(abs)
// should be ORMap(
  0 -> (Set(2), Set(2)),
  1 -> (Set(1, 3, 4), Set(3, 4))
)
```

In the above example, we have a set where elements -1, 0 and 1 have been added at some point in time, even though 0 and 1 are currently considered deleted. After
application of the absolute function, 1 is considered to be a member of the resulting set, as -1 was considered member of the input set.

### Functorial map on AWSet

As map does not affect the nature of the identifiers in ORSet, we can assume it does not affect the CausalContext `C`, thus we only need to consider the effect
on `M: DotMap[V, DotSet]`. The translation is trivial, apply `f` to each element, and join DotSet values of elements whose `f`-application makes them equivalent.

In order to compute the delta `awDelta` from input delta `inputDelta`, we then:
- apply `f` to `inputDelta`, obtaining a `applicableDelta`
- for `e` keys in `applicableDelta`, obtain current DotSets and merge them with the DotSet delta for each element in `applicableDelta`


### Filter on ORSet

Filtering allows the application of a predicate to each element `e` in the ORSet. If the predicate is not satisfied, all event-identifiers for `e` are moved
from `A` to `R`, i.e. the element gets deleted in the resulting set.

## Filter on AWSet

Deletion of `e` is implemented as removal of `e`from `M`. Given that filtering is equivalent to removal for ORSet, this should suffice for AWSet.

## Fold on ORSet

Lasp defines a fold combinator for functions `f` that are associative, commutative and with an inverse `f'` (i.e. CommutativeGroup). For each element `e`, apply
`f` to `e` `|A|` times and `f'` to `e` `|R|` times; then finally, combine the intermediate results using `f`.

Note that for this to work, `f` must be an endo-function. Also, this definition uses generalized MultiSet semantics. If an element `e` was concurrently added
twice without cancelling deletion, `f(e)` will be represented twice in the fold-result. I.e. a set-size computation would count this element twice; many concurrent
deletions could lead to negative set-size.

## Fold on AWSet

`MVar[AWSet[E]].fold` should result in `MVar[E]` (assuming a CommutativeGroup is defined for `E`). Definition of fold for addition is simple for AWSet:

For each element `e` in the delta, apply `f` (from the CommutativeGroup) on `e` `DotSet` times.

Removal is significantly more complicated. The delta does not contain the actual element `e`, just the CausalContext `C` with all the dots that are considered cancelled.
We may use these dots to retrieve the actual value for `e`, only that value may not be available locally yet. Not sure if that matters for consistency, but certainly
we cannot produce inverted elements until the value is available, i.e. this requires causal delivery/processing. Also, the current datastructure doesn't lend itself
well for obtaining the elements based on the dots alone, we would have to scan all the elements to find the appropriate dots.

An interesting approach may be to enrich the delta with `e` mapping to the empty DotSet, which from a DotMap perspective is equivalent to emptiness, i.e. adding this
shouldn't actually affect the join (it would still perform removal), but prevents having to scan to find the dots and thus immediately produce correct values (at the cost
of higher transmission overhead)

## Product on ORSet

Product combines two ORSets into a single ORSet representing all possible combinations. The challenge here is to make sure that when an element `e` is removed on one side,
all the elements `p` of the product are removed when `p` contains `e` on that side. For the sake of consistency, this also means taking the product of the metadata.

## Product on AWSet

This requires a change of structure of the datastructure. Before we had the structure:

`AWSet[E] = Causal[DotMap[E,DotSet]]`, which is equivalent to `(DotMap[E, DotSet), CausalContext)`. A Product `p` of two AWSets `a1` and `a2`:

`p = (DotMap[(A1, A2), (DotSet_a1, DotSet_a2)], (CausalContext_a1, CausalContext_a2))`.

An element `e` is a member of a ProductAWSet iif both DotSets that `e` maps to are non-empty.

This requires that the product of two DotStores is also a DotStore, likewise for CausalContext.

## Union on ORSet

In the case of union, every element from each source is represented in the output. In addition, if the element is available in both inputs, their metadata is unioned
for the output element.

## Union on AWSet

Here we are dealing with two distinct causal-contexts, which we cannot just union (host identifiers and the host-event-sequences may overlap, which ruins consistency).
The same holds for the element-specific DotSets. The union `unionedSet` of two AWSets `a1` and `a2`:

`unionedSet = (DotMap[E, DotSet_a1 + DotSet_a2], CausalContext_a1 + CausalContext_a2)`. Where `+` is linear-sum, which tags elements with their source (Left/Right)
so that they can be distinguished in the output. Note: We don't want the prioritizing behavior of linear-sum... It should have more union-like behavior while retaining
source. I.e. in contrast of a product dot-set, where the output-dotset is empty if either input-dotset is, the union should be non-empty if either inputs is non-empty.

## Intersection on ORSet

The definition is quasi-equivalent to product, with the difference that the output set does not contain tuples, but only single elements (essentially the definition is such
that it contains only tuples whose sides are equivalent)

## Intersection on AWSet

The intersection `interSet` of two AWSets `a1` and `a2`:
`interSet = (DotMap[E, (DotSet_a1, DotSet_a2)], (CausalContext_a1, CausalContext_a2))`

## Final thoughts

The main difference between product, union and intersection is in the interpretation of DotStore combinations. In all cases we want to do point-wise joins, but:

- In the product and intersection case, the combined dotstores contain dot-elements for each pair, i.e. this is a real DotStore _product_: `DotStore[A1 x A2]`
(implementation could be `(DotStore[A1], DotStore[A2])` for efficiency)
- In the union case, the combined dotstores contain `Left(x)` elements for each dot `x` in the left input DotStore and `Right(y)` elements for each dot `y` in
the right input. In contrast to the (also tagging) linear-sum, join should still be point-wise. This is a _tagged union_ over DotStore: `DotStore[A1 + A2]`,
`Either` could suffice for this job (we should look at shapeless union types when moving beyond 'arity' 2)


