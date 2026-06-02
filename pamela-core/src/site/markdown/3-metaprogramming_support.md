---
sidebar_position: 4
---

# Meta-programming support

Following figure represents PAMELA metamodel. 

![PamelaMetaModel](/images/PamelaMetaModel.png)

<!-- TODO fix ? the file is ~/pamela/src/site/resources/images/PamelaMetaModel.png maybe this is "map" differently when "building" -->
<!-- TODO see other fixes elsewhere in the index/overview -->
Concept of *ModelProperty* represent access to a value (simple or with multiple cardinality).
<!-- TODO confused about how this "represent access to a value" -->
<!-- TODO confused why mention "cardinality" in the first place ? -->
<!-- TODO confused why mention "simple" which should be "single" according to code -->
This read-access is implicitely implemented using `get:` protocol.
<!-- TODO confused by this "protocol" I've seen somewhere that this string is used in the code and still not familiar with -->
Similarly, ModelEntity `properties` may expose the following protocols used to define semantics of the *ModelProperty* :

- `get:` read-access to a data. This protocol is implemented by all kind of *ModelProperties*.
<!-- TODO why speaks of "kind of" there's only one "kind of" ModelProperty ? there's no subclasses of this concept in this version  -->
- `set:` write-access to a data. This protocol is implemented by *ModelProperties* implementing `SettablePropertyImplementation` API.
<!-- TODO idf the interface -->
- `add:` and `add:AtIndex:` add-access to a multiple cardinality data. These protocols are implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
<!-- TODO idf the mention to "multiple cardinality" why does not work with "single" ? i remember that a single refer to a simple variable -->
<!-- TODO idf the interface -->
<!-- TODO idf both property -->
- `reindex:To:` reindex a value to a given index in a multiple cardinality data. This protocol is implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
<!-- TODO idf this property -->
- `remove:` remove-access to a multiple cardinality data. This protocol is implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
<!-- TODO idf this property -->
- `delete:` and `undelete:` delete/undelete protocols, implemented by all *ModelProperties*
<!-- TODO as with the previous mentions, why speaks of "all" there's only one ModelProperty ? there's no subclasses of this concept in this version  -->

Unless specified in `@PropertyImplementation` PAMELA annotations, default implementation is provided for the PAMELA interpreter.
<!-- TODO idf this property -->
This default implementation, also called the *ModelProperty* standard semantics, is encoded in correctly identified classes which are extendable to provide specific *ModelProperty* semantics.

PAMELA allows programmers to define their custom implementation for a given set of properties, by providing a class implementing `@PropertyImplementation` API and some protocol implementation (depending on the nature of the *ModelProperty*).
<!-- TODO confused by the term "nature" -->
<!-- TODO confused by "implementing... and some protocol", for instance in `MyPropertyImplementation` below, the term "protocol" probably refers to "set" ? -->
```java
public class MyStrangePropertyImplementation extends DefaultSinglePropertyImplementation<Concept, String> {
	...
	@Override
	public void set(String aValue) throws ... {...}
}
```

Following excerpt of code illustrate use of two custom property implementations, with specific semantics.

<!-- TODO fix the example which does not hold anymore, the new example might be `MyStrangePropertyImplementation` -->
```java
@ModelEntity
public interface MyConcept {

	static final String VALUE = "value";
	static final String SUB_CONCEPTS = "someSubConcepts";

	@Getter(value = VALUE)
	@PropertyImplementation(MyStrangePropertyImplementation.class)
	String getValue();

	@Setter(VALUE)
	public void setValue(String value);

	@Getter(value = SUB_CONCEPTS, cardinality = Cardinality.LIST)
	@PropertyImplementation(MyListCardinalityPropertyImplementation.class)
	@Embedded
	List<MySubConcept> getSubConcepts();

	@Adder(SUB_CONCEPTS)
	void addToSubConcepts(MySubConcept subConcept);

	@Remover(SUB_CONCEPTS)
	void removeFromSubConcepts(MySubConcept subConcept);
}
```

```java
public class MyStrangePropertyImplementation extends DefaultSinglePropertyImplementation<Concept, String> {

	public MyStrangePropertyImplementation(ProxyMethodHandler<Concept> handler, ModelProperty<Concept> property)
			throws InvalidDataException {
		super(handler, property);
	}

    // Implements set: protocol, with a 'concatenate' semantics
	@Override
	public void set(String aValue) throws ModelDefinitionException {
		String oldValue = get();
		if (get() != null) {
			super.set(aValue + get());
		}
		else {
			super.set(aValue);
		}
	}
}
```

```java
public class MyListCardinalityPropertyImplementation<I, T> extends AbstractPropertyImplementation<I, List<T>>
		implements MultiplePropertyImplementation<I, T> {
	...
	// Implements get: protocol
	public List<T> get() {
		...
	}
	// Implements add: protocol
	public void addTo(T aValue) throws ModelDefinitionException {
		...
	}
	// Implements remove: protocol
	public void removeFrom(T aValue) {
		...
	}
}
```

Such mechanisms are really usefull to control and fine-tune code implementation. Suppose for example that you have a code used in monothreading context: all your multiple cardinality properties will rely for example on a `ArrayList` implementation. Instead of replacing all occurences of `ArrayList` by `Vector` (multi-thread safe version of Java `List`) in generated code, the developer has just one modification to do, in an adequate `PropertyImplementation`.
<!-- TODO idf this example and the relevance -->