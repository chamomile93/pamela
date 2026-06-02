package org.openflexo.pamela.model.property;

import java.beans.PropertyChangeSupport;

import org.openflexo.pamela.factory.IProxyMethodHandler;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.PamelaModelFactory.PAMELAProxyFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Base abstract class providing property implementation
 * 
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 * @param <M>
 *            internal memory adressable for a given entity instance and property
 */
//TODO might need to understand this, I came here while trying to understand the test of InitializePropertyTest. I am not familiar with this concept yet
//TODO might need to understand this, I came here while trying to understand the test of InitializePropertyTest. I am not familiar with this concept yet
public abstract class AbstractPropertyImplementation<I, T> implements PropertyImplementation<I, T> {

	private final ProxyMethodHandler<I> handler;
	private final ModelProperty<I> property;

	public AbstractPropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property) {
		this.handler = handler;
		this.property = property;
	}

	protected ProxyMethodHandler<I> getHandler() {
		return handler;
	}

	@Override
	public I getObject() {
		return getHandler().getObject();
	}

	public PamelaModelFactory getModelFactory() {
		return getHandler().getModelFactory();
	}

	final public ModelEntity<I> getModelEntity() {
		return getHandler().getModelEntity();
	}

	public PAMELAProxyFactory<I> getPamelaProxyFactory() {
		return getHandler().getPamelaProxyFactory();
	}

	@Override
	public ModelProperty<I> getProperty() {
		return property;
	}

	protected Object getDebugValue() {
		return null;
	}

	@Override
	public String toString() {
		//TODO this was added by copilot after a suggestion during a debug session
		StringBuilder sb = new StringBuilder();
		sb.append(getModelEntity().getImplementedInterface().getSimpleName());
		sb.append('.');
		sb.append(getProperty().getPropertyIdentifier());
		sb.append("=");
		Object debugValue = getDebugValue();
		sb.append(String.valueOf(debugValue));
		if (debugValue != null) {
			sb.append(" (");
			sb.append(debugValue.getClass().getSimpleName());
			sb.append(')');
		}
		return sb.toString();
	}

	protected void firePropertyChange(String propertyIdentifier, Object oldValue, Object value) {
		if (getObject() instanceof HasPropertyChangeSupport && !getHandler().isDeleting()) {
			PropertyChangeSupport propertyChangeSupport = ((HasPropertyChangeSupport) getObject()).getPropertyChangeSupport();
			if (propertyChangeSupport != null) {
				propertyChangeSupport.firePropertyChange(propertyIdentifier, oldValue, value);
			}
		}
	}

	protected static boolean isEqual(Object oldValue, Object newValue) {
		return IProxyMethodHandler.isEqual(oldValue, newValue);

	}

}
