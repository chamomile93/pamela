---
sidebar_position: 6
---

# Behind the scene

## Runtime considerations

The aforementioned models are executed at runtime as a combination of two components:
<!-- TODO idf "aformentioned models" -->
<!-- TODO idf why this is a "combination" of anything ? -->
1. plain Java byte-code, as the result of the basic compilation of source code; and
<!-- TODO idf the "plain Java byte-code -->
2. an embedded PAMELA interpreter, executing semantics reflected by *ModelEntity* and *ModelProperty* declarations (together with custom annotations where available).
<!-- TODO idf the "embedded PAMELA interpreter", the first term is seems confusing, the second and third term seems misleading -->

The main idea of the approach is to override the Java dynamic binding (the scheme used to bind method call to method body, which is not done by the compiler but at runtime).

<!-- TODO maybe add references to the :
- Java Language Specification :
  - “12.2. Loading of Classes and Interfaces” ([“Chapter 12. Execution”](zotero://select/groups/5473375/items/QAI3H6WR)) ([snapshot](zotero://open-pdf/groups/5473375/items/EZINLB9M?sel=div%3Anth-child(7)%20%3E%20div%3Afirst-child%20%3E%20div%20%3E%20div%20%3E%20h2&annotation=QD5DZDDM))
- Java Virtual Machine Specification :
  - “Chapter 5. Loading, Linking, and Initializing” ([“Chapter 5. Loading, Linking, and Initializing”](zotero://select/groups/5473375/items/54KPZUQN)) ([snapshot](zotero://open-pdf/groups/5473375/items/QRK2NIG8?sel=h1&annotation=9492CEDD))
-->

Invoking a method on an object which is part of a PAMELA model,

<!-- TODO what is meant by "part of PAMELA model" ? -->

causes the real implementation to be called when existing (more precisely dispatch code execution between all provided implementations), or the required interpretation according to underlying model to be executed.

The PAMELA interpreter intercepts any method call to an instance of a *ModelEntity* and conditionally branches code execution.

<!-- TODO what is exactly refered to by "conditionally branches" ? -->
<!-- TODO idf the "PAMELA interpreter" -->

- If the accessed method is part of a *ModelProperty* (a getter, or a setter, etc..), and no custom implementation is defined neither in the class declared as implementation, nor in a class declared as partial implementation in the context of traits, then execution is delegated to the related property implementation (a generic code provided by the PAMELA interpreter).

<!-- TODO idf why distinguish between "custom implementation" which is "defined in the class declared as implementation" with "in a class declared as partial implementation in the context of traits" ? to me this is the same. -->

<!-- TODO idf "generic code provided" this might refer to different part of the code and not a specifc place that is well defined, would this be interesting to point to some code that might refer to this "provided" ? -->

<!-- TODO idf the "PAMELA interpreter" -->

- If the accessed method is defined in a class declared as implementation, or in a class declared as partial implementation, then this method is executed (with a scheme preventing method declaration clashes).

<!-- TODO idf "scheme preventing method declaration clashes", I vaguely remember a code that does something similar -->

-  The PAMELA API through the `AccessibleProxyObject` interface also provides access to the generic behaviour (super implementation), allowing the developer to define an overriding composition.

<!-- TODO idf how "through the AccessibleProxyObject" which "generic behaviour"" is given "access" -->
<!-- TODO code example of this "to define an overriding composition" ? -->

This general scheme provides also an extension point allowing to instrument the code, which is used for other features such as notification management, undo/redo stack management, assertion checking at runtime (support for *Design by Contract*, as in JML, and dynamic code weaving in the context of *Aspect Programming*).

<!-- TODO idf "general scheme" -->
<!-- TODO idf "notification management" -->
<!-- TODO idf "undo/redo stack" -->
<!-- TODO idf "assertion checking" -->
<!-- TODO idf "assertion checking" -->

This composition of an interpreter (interpreting both standard and specific semantics) and compiled code offers many benefits:

<!-- TODO confused by "composition of an interpreter" with "compiled code" -->
<!-- TODO confused by "standard" "semantics" -->
<!-- TODO confused by "specific" "semantics" -->
<!-- TODO confused by "" "semantics" -->

- Strong coupling between model and code
- Strong typing is kept, and required checks are performed by the Java compiler
- The PAMELA framework provides interpretation of model@runtime
- No need to generate POJO (plain old Java objects), as their execution follow the standard semantics (less code, less bugs)
- Custom implementation are provided if needed, using usual Java extension points
- It offers a way to intercept method calls and instrument the code
- Assertions checking at runtime
- Dynamic code weaving at runtime (aspect programming without compilation)
<!-- TODO assuming aspect programming is done by compilation, i.e. in general, and I don't remember this as being a definition that holds in the general case -->

[//]: # (@Sylvain, uniformise la forme des points certains sont des phrases et pas d'autres)

## Exposed API at design time

The model-code integration we advocate requires facilities to encode metadata in source code. This requires an annotation-enabled language. Such a language supports the attribute-oriented programming if its grammar allows adding custom declarative tags to annotate standard program elements. Java programming language from version 1.5 is a good candidate with the support of annotations.

<!-- TODO maybe list alternatives which demonstrate that PAMELA could be ported to another language -->

[//]: # (@Sylvain, déjà lu le paragraphe précédent)

API exposed to the developer mainly consists of:

1. a set of annotations;
<!-- TODO example ? -->
1. a set of unimplemented Java interfaces exposing the required features.
<!-- TODO example ? -->

The package `org.openflexo.pamela.annotations` package exposes a set of annotations, some were presented in [Common annotations](./annotations.md).

The package `org.openflexo.pamela` contains following feature-related Java interfaces:

- `AccessibleProxyObject` is the interface that PAMELA objects should extend in order to benefit from base features such as generic default implementation, containment management, notification, object graph comparison and diff/merge, visiting patterns, etc.
- `CloneableProxyObject` exposes features related to cloning.
- `DeletableProxyObject` exposes features related to deletion management.
- `SpecifiableProxyObject` exposes dynamic assertion checking features in the context of JML (contract management) use.

<!-- TODO what about `public interface KeyValueCoding` ? -->
<!-- TODO perhaps putting my "RuntimeMethod" is not correct -->

Generic design patterns API, used in the context of aspect programming is exposed in the `org.openflexo.pamela.patterns` package.

<!-- TODO such as ? `PatternDefinition` etc. -->

A plug-in architecture allows to enrich model with specific design patterns.

<!-- TODO how could we illustrate this ? -->

Some basic design patterns are released with PAMELA 1.6.x in the context of security (`Authenticator`, `Authorization`, `SingleAccessPoint`, `Owner`) exposed in `pamela-security-patterns`.

## The PAMELA interpreter

<!-- TODO this seems to explain, or define what could be understood by the term "PAMELA interpreter" mentionned above and before ? -->

The package `org.openflexo.pamela.factory` contains PAMELA interpreter implementation. The core of this interpreter is implemented in the class `ProxyMethodHandler`.

From a technical point of view, PAMELA implementation uses *javassist* reflection library, providing a `MethodHandler` mechanism, which is a way to override the Java dynamic binding.

<!-- TODO which part of the Java specification would be more appropriate ?
- “5. Loading, Linking, and Initializing” ([“The Java® Virtual Machine Specification”](zotero://select/groups/5473375/items/LRTIHGKS)) ([snapshot](zotero://open-pdf/groups/5473375/items/LD6URYFC?sel=div%3Alast-child%20%3E%20dl%20%3E%20dt%3Anth-child(9)%20%3E%20span%20%3E%20a&annotation=I856UKWE))
- “2.6.3. Dynamic Linking” ([“The Java® Virtual Machine Specification”](zotero://select/groups/5473375/items/LRTIHGKS)) ([snapshot](zotero://open-pdf/groups/5473375/items/LD6URYFC?sel=dd%3Anth-child(9)%20%3E%20dl%20%3E%20dt%3Anth-child(3)%20%3E%20span%20%3E%20a&annotation=ENJG8KGG))
- “12.2. Loading of Classes and Interfaces” ([“Chapter 12. Execution”](zotero://select/groups/5473375/items/QAI3H6WR)) ([snapshot](zotero://open-pdf/groups/5473375/items/EZINLB9M?sel=div%3Anth-child(7)%20%3E%20div%3Afirst-child%20%3E%20div%20%3E%20div%20%3E%20h2&annotation=QD5DZDDM)) -->

Invoking a method on an object which is part of a PAMELA model,
<!-- TODO is it possible to invoke a method on an object which is not a "part of PAMELA model" while still using any of the PAMELA model code in any way ? yes you can, this would probably raise a `ModelDefinitionException`  in many cases. -->
causes the real implementation to be called when existing (more precisely dispatch code execution between all provided implementations),
<!-- TODO idf "between all provided implementations" what is refered ? how does this work ? -->
or the required interpretation according to underlying model to be executed.
<!-- TODO idf the emphasis which seems to be put with "required", why this last part differs from the previous part ? it seems to be saying something in case there's "no implementation, we use the 'default' " -->
This provides also an extension point allowing to instrument the code, which is used for other features such as undo/redo stack management, and assertion checking at run-time (support for Design by Contract, aka JML).
<!-- TODO confused by "provides an extension point" to which part is refered to, how it's implemented -->

[//]: # (@Sylvain, déjà lu la fin du paragraphe précédent)

The PAMELA framework is 100% pure Java (> 1.5), compilable by a standard Java compiler and
executable in a standard Java Virtual Machine.

<!-- TODO why is this mentionned ? how could it have been implemented without 100% java ? which non standard Java compiler exists and make it not compilable ? which non standard JVM exists and that make it not executable ? -->

