---
sidebar_position: 2
---

# Model at runtime computation

PAMELA model at runtime is computed dynamically, working on the classpath of launched java application,
<!-- TODO fix the usage of "PAMELA model" as "Pamela Meta Model" ? -->
<!-- TODO idf how is this working on the classpath of launched java application ? -->
and starting from a simple java interface (or a collection of java interfaces) which is/are PAMELA-annotated.
<!-- TODO confused is this referring to "user defined interface" or "pamela defined interface" ? -->
From a mathematical point of view,
<!-- TODO the term "mathematical might be innapropriate -->
internal representation of the underlying model is a graph whose vertex are PAMELA *ModelEntities* (annotated java interface), and edges are either inheritance links or reference links (a property whose type is another *ModelEntity*).
<!-- TODO maybe add `![PamelaMetaModel](/images/PamelaMetaModel.png)` for illustrative purpose ? see issues related about this picture in the overview -->
<!-- TODO add "parentEntities" after "inheritance links" -->
<!-- TODO add "entityType" after "reference links" -->
<!-- TODO rename "property" with "ModelProperty" as still in use in the image and in the code ? -->
`@Imports` and `@Import` annotations allows to include some other *ModelEntities* in the model. On the contrary, an annotation attribute `@Getter(...ignoreType=true)` allows to ignore the link.
<!-- TODO idf "link" refer to ? the inheritance link ? the reference links ? both ? something else ? -->
<!-- TODO why mention this anyway ? -->
In that context, PAMELA model computation is a graph closure computation, starting from a collection of vertices.
<!-- TODO why is this a graph closure computation ? -->

A PAMELA model at runtime is represented by a `ModelContext`.
<!-- TODO "ModelContext" does not hold anymore, what is it refering to ? -->
PAMELA instances (instances of *ModelEntity*) are handled through the use of `ModelFactory`,
<!-- TODO "ModelFactory" does not hold anymore, what is it refering to ? -->
which is instantiated from a `ModelContext`.
<!-- TODO "ModelContext" does not hold anymore, what is it refering to ? -->
<!-- TODO fix ModelContext and ModelFactory does not hold anymore, maybe it's PamelaMetaModel resp. PamelaModelFactory -->

PAMELA model closure computation on-the-fly provides an interesting approach to deal with model fragmentation.
<!-- TODO confused by the term "fragmentation" ? -->