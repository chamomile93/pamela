package org.openflexo.pamela;

/**
 * Addind support for runtime checking, this is first design to injecting java
 * code that will have to be verified using Alloy
 */
// TODO find a better package to put this, given documentation `[Behind the scene](./behind_the_scene.md`) this might not be correct
@FunctionalInterface
public interface RuntimeMethod {

	Object invoke(Object receiver, Object... args) throws Throwable;
}