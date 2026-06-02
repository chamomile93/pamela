package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represents a {@link PropertyImplementation} which is settable and of SINGLE cardinality
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
//TODO might need to understand this, I came here while trying to understand the test of InitializePropertyTest. I am not familiar with this concept yet
public interface SinglePropertyImplementation<I, T> extends SettablePropertyImplementation<I, T> {

	@Override
	public T get() throws ModelDefinitionException;

	@Override
	public void set(T aValue) throws ModelDefinitionException;

	@Override
	public void update(T aValue) throws ModelDefinitionException;
}
