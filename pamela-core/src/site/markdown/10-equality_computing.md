---
sidebar_position: 11
---

# Equality computing support

PAMELA framework additionnaly offers various interesting features in the context of object graph manipulations, such as equality computation, visiting patterns, and diff/merge support with differential updating.

Object graph comparison features are offered by `AccessibleProxyObject` base API, with the method `equalsObject(Object)`. Semantics if this equality computation is performed on the whole object graphs (the one represented by invoked instance and the opposite one), regarding properties considered as persistent (non derived or ignored properties).
<!-- TODO confused this the above sentence -->
The two object graphs are considered as equals if and only if they expose the same graph topology regarding properties to consider, and if all matching pair of objects are equal. To be equals the two objects should have all their property values equals with following condition :

- if values are PAMELA object, they should be equals according to PAMELA semantics (the one we are about to describe);
<!-- TODO confused by the term referenced by "PAMELA object", would a `PamelaMetaModel` class be referenced by this term ? ModelEntity and ModelProperty seems to be refered the main "objects" -->
- if values are convertable to `String`, their conversion to string must match;
- otherwise, they should be equals according to Java language semantics.
<!-- TODO confused about the equality refered to by "Java language semantics"  -->
<!-- TODO the implementation seems to be in `pamela-core/src/main/java/org/openflexo/pamela/factory/ProxyMethodHandler.java#equalsObjects` -->