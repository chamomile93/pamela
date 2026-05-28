package org.openflexo.pamela;

@FunctionalInterface
public interface RuntimeMethod {

	Object invoke(Object receiver, Object... args) throws Throwable;
}