---
sidebar_position: 6
---

# Containment management

As it was previously described, PAMELA metamodel supports containment.

Entities containment is modeled through the use of `@Embedded` annotation, defined on a *ModelProperty*. Underlying semantics states that an instance of a *ModelEntityA* is embedded in a container *ModelEntityB*, if this instance of *ModelEntityA* is present in a the types of the set of *ModelProperty* of *ModelEntityB* of applied to container instance. Resulting containment relationships are internally represented as a directed acyclic graph.
<!-- TODO confused by the plural "relationships", there's only one form of "containment relationship" ? use the singular ? -->

Those relationships are used to support miscellaneous features, such as `modified` status for any of instances involved in an object graph, as well as closure computation, cloning and clipboard operations.
<!-- TODO confused by "those relationships" as well ? does it refer to the "containment" ? or something else -->

## Containment closure computation

In graph theory and combinatorial optimization, a closure of a directed graph is a set of vertices with no outgoing edges. That is, the graph should have no edges that start within the closure and end outside the closure.
<!-- TODO idf -->
PAMELA frameworks relies on a internal scheme which computes the closure of an input graph infered from an explicit set of objects, considered as vertices, and following containment relationships, considered as edges.
<!-- TODO idf -->
This containment closure computation is especially used in cloning support.
<!-- TODO idf -->

## `modified` status support

<!-- TODO maybe add a reference to `- [Life-cycle management](./pamela-core/2-life_cycle_management.md)` -->
Following figure presents life-cycle for any object managed with PAMELA.

![LifeCycle](/images/LifeCycle.png)
<!-- TODO see issues elsewhere -->

Any call to a write-access *ModelProperty* (properties using `set:`, `add:`, `add:AtIndex:`, `remove:`, `delete:` and `undelete:` protocols) implies `modified` status for related object to be set to true.

In the context of containment relationships, this status is propagated along container hierarchy: any object beeing modified implies its container (which may be an unique object or a set of objects) being modified too.

The `modified` status for a given instance may be retrieved using `isModified()` method defined in `AccessibleProxyObject` Java interface.
<!-- TODO maybe indicate that his implemented in `public class ProxyMethodHandler<I> extends IProxyMethodHandler implements MethodHandler, PropertyChangeListener` -->
In the case of a custom method beeing implemented, which has side-effect (impacting its internal data), a `setModified(boolean)` method is offered to the developper to indicates the modification. Saving object graph brings back object status in `Alive/Saved` status: in this case `isModified()` method returns false.
<!-- TODO idf "saving" part refers to in the code? -->