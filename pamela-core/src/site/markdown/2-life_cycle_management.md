---
sidebar_position: 3
---

# PAMELA objects life-cycle management

A Metamodel computation is represented by a `ModelContext` and uses a `ModelFactory` built with that model context to handle instances of that metamodel.
<!-- TODO fix ModelContext and ModelFactory does not hold anymore, maybe it's PamelaMetaModel resp. PamelaModelFactory -->
The `ModelFactory` is responsible of the life-cycle of instances of metamodel (construction and destruction of Java instances). `@Initializer` annotation allows to define parametered constructors for *ModelEntity* instances.

`AccessibleProxyObject` is a Java interface providing utilities methods which are interpreted by internal PAMELA interpreter. This includes calls to internal code execution, such as `performSuperSetter(String,Object)` which represent a call to internal setter of a property identified by the supplied String value.
<!-- TODO confused by "a call to internal setter of a property identified" -->

Following figure illustrates life-cycle of objects beeing instantiated as PAMELA instances.
<!-- TODO rename "PAMELA" instances as "PamelaMetaModel" instances ? -->

![LifeCycle](/images/LifeCycle.png)
<!-- TODO sed "triggers"/"invoke" ? -->
<!-- TODO mention that "isCreating" has no "concrete" existence ? -->
<!-- TODO sed "new"/"newInstance" ? -->
<!-- TODO sed "isCreating" with what is closed in the code ? -->
<!-- TODO sed "build" with what is closed in the code ? -->
<!-- TODO sed "Alive/Saved" with what is closed in the code ? -->
<!-- TODO sed "save" with what is closed in the code ? -->
<!-- TODO sed "edit" with what is closed in the code ? -->
<!-- TODO sed "Modified" with what is closed in the code ? -->

The `ModelFactory` initiates creation and triggers right constructor during a phase where the object is in `isCreating` status.
<!-- TODO fix ModelContext and ModelFactory does not hold anymore, maybe it's PamelaMetaModel resp. PamelaModelFactory -->

Modifications of objects are internally tracked by PAMELA interpreter which manages `modified` status, according to containment semantics as presented further (a contained object modification implies object flagged as modified, and implied container flagged as modified too).
<!-- TODO sed "modified" with what is closed in the code ? is it a field ? a local variable in a method ? etc. -->
Saving object graph brings back object status in `Alive/Saved` status.

Since calls to any features of model objects are dispatched by the internal interpreter, PAMELA runtime offers a multi-level undo/redo stack tooling. When enabled, this scheme allows to store and manage an edition model composed of atomic edits. Following figure presents atomic edits metamodel for a fine-grained model modification tracking system.

![AtomicEditMetaModel](/images/AtomicEditMetaModel.png)
<!-- TODO fix, why images in this folder are duplicated in a subfolder which contains more pictures ? -->
<!-- TODO why not mention that this is extending the java.swing `public interface UndoableEdit` ? -->
That mechanism provides undo/redo features, virtually unlimited. For performance reasons, we can set a maximum depth for undo/redo operations.

`DeletableProxyObject` provides delete (and undelete) features to *ModelEntity* instances.
<!-- TODO why mention this ? -->
<!-- TODO why doesn't appear in the above picture ? -->
Deletion features are performed using a context which is a graph closure computation for all the objects which have to be deleted. PAMELA also offers undelete feature which allow to resurrect a deleted object. Deleted objects are still resurectable until they are still in the undo/redo stack. A deleted object who is leaving scope of maximum depth of undo/redo stack is destroyed. Object is fully dereferenced, ready for garbage collecting, and cannot be resurrected again.