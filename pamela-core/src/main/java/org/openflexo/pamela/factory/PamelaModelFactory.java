/**
 *
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 *
 * This file is part of Pamela-core, a component of the software infrastructure
 * developed at Openflexo.
 *
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or
 *          combining it with software containing parts covered by the terms
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.pamela.factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.exceptions.MissingImplementationException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelInitializer;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.xml.XMLSaxDeserializer;
import org.openflexo.pamela.xml.XMLSerializer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
//TODO this is important to define a PamelaModelFactory 
import javassist.util.proxy.ProxyObject;

/**
 * The {@link PamelaModelFactory} is responsible for creating new instances of
 * PAMELA entities.<br>
 *
 * This class should be considered stateless, regarding to the state of handled
 * instances.<br>
 *
 * Note that a {@link PamelaModelFactory} might refer to an
 * {@link EditingContext}. When so, new instances are automatically registered
 * in
 * this {@link EditingContext}.
 *
 * @author sylvain
 *
 */
public class PamelaModelFactory {

	private Class<?> defaultModelClass = Object.class;
	private Class<? extends List> listImplementationClass = Vector.class;

	private final Map<Class, PAMELAProxyFactory> proxyFactories;
	private final StringEncoder stringEncoder;
	private final PamelaMetaModel pamelaMetaModel;

	private PamelaMetaModel extendedContext;

	// TODO idf what is this concept of EditingContext
	private EditingContext editingContext;

	// Stores on-the-fly generated classes to proxy the targeted implementation
	// class, but in the right package
	// TODO idf why the value is map of classes as key and value ? shouldn't be
	// <class,proxy> ? why the nested structure ?
	private static Map<Class, Map<Class, Class>> implementationProxyClasses = new HashMap<>();

	public Map<Class, PAMELAProxyFactory> getProxyFactories() {
		return proxyFactories;
	}

	public class PAMELAProxyFactory<I> extends ProxyFactory {
		// TODO i might need to get into this proxy implementation for the runtime
		// scenario
		// TODO idf why we need to define this. What is separating from the main class ?
		private final ModelEntity<I> modelEntity;
		private boolean locked = false;
		private boolean overridingSuperClass = false;

		public PAMELAProxyFactory(ModelEntity<I> aModelEntity, PamelaMetaModel context)
				throws ModelDefinitionException {
			super();
			this.modelEntity = aModelEntity;
			setFilter(new MethodFilter() {
				@Override
				public boolean isHandled(Method method) {

					if (Modifier.isAbstract(method.getModifiers()))
						return true;
					if (method.getName().equals("toString")) {
						return true;
					}

					if (aModelEntity.getJMLMethodDefinition(method) != null) {
						return true;
					}

					if (context.isMethodInvolvedInPattern(method)) {
						return true;
					}

					/*
					 * if (context.getPatternContext().getRelatedPatternsFromClass(aModelEntity.
					 * getImplementedInterface()).size() > 0) {
					 * try {
					 * aModelEntity.getImplementedInterface().getMethod(method.getName(),
					 * method.getParameterTypes());
					 * return true;
					 * } catch (NoSuchMethodException e) {
					 * }
					 * }
					 */

					// :TODO perf issue ??? Check this !
					if (modelEntity.getPropertyForMethod(method) != null) {
						return true;
					}
					return false;
					// Old code
					/*
					 * return Modifier.isAbstract(method.getModifiers()) ||
					 * method.getName().equals("toString")
					 * && method.getParameterTypes().length == 0 && method.getDeclaringClass() ==
					 * Object.class;
					 */
				}
			});
			Class<?> implementingClass = modelEntity.getImplementingClass();

			if (implementingClass == null && modelEntity.isSimplePamelaInstrumentation()) {
				// Special case for a Pamela entity defined for a basic Java class
				implementingClass = modelEntity.getImplementedInterface();
				super.setSuperclass(modelEntity.getImplementedInterface());
			} else {
				if (implementingClass == null) {
					implementingClass = defaultModelClass;
				}
				super.setSuperclass(implementingClass);
				Class<?>[] interfaces = { modelEntity.getImplementedInterface() };
				setInterfaces(interfaces);
			}

		}

		public Class<?> getOverridingSuperClass() {
			if (overridingSuperClass) {
				return getSuperclass();
			} else {
				return null;
			}
		}

		@Override
		public void setSuperclass(Class clazz) {
			if (getSuperclass() != clazz) {
				if (locked) {
					throw new IllegalStateException(
							"ProxyFactory for " + modelEntity + " is locked. Super-class can no longer be modified.");
				}
			}
			overridingSuperClass = true;
			super.setSuperclass(clazz);
			locked = true;
		}

		/**
		 * Internally used to set a proxy base implementation class in the right package
		 *
		 * @param clazz
		 */
		private void setProxySuperClass(Class clazz) {
			super.setSuperclass(clazz);
		}

		public PamelaModelFactory getModelFactory() {
			return PamelaModelFactory.this;
		}

		public ModelEntity<I> getModelEntity() {
			return modelEntity;
		}

		/*
		 * public I newInstance(Object... args) throws IllegalArgumentException,
		 * NoSuchMethodException, InstantiationException,
		 * IllegalAccessException, InvocationTargetException, ModelDefinitionException {
		 * if (modelEntity.isAbstract()) {
		 * throw new InstantiationException(modelEntity +
		 * " is declared as an abstract entity, cannot instantiate it");
		 * }
		 * locked = true;
		 * ProxyMethodHandler<I> handler = new ProxyMethodHandler<>(this,
		 * getEditingContext());
		 *
		 * if (args == null) {
		 * args = new Object[0];
		 * }
		 *
		 * I returned = null;
		 * if (modelEntity.isSimplePamelaInstrumentation()) {
		 * Class<?>[] paramTypesArray = new Class<?>[args.length];
		 * for (int I = 0; I < args.length; i++) {
		 * paramTypesArray[i] = args[i].getClass();
		 * }
		 * returned = (I) create(paramTypesArray, args, handler);
		 * handler.setObject(returned);
		 * }
		 * else {
		 * returned = (I) create(new Class<?>[0], new Object[0], handler);
		 * handler.setObject(returned);
		 * if (args.length > 0 || modelEntity.hasInitializers()) {
		 * Class<?>[] types = new Class<?>[args.length];
		 * for (int i = 0; i < args.length; i++) {
		 * Object o = args[i];
		 * if (isProxyObject(o)) {
		 * ModelEntity<?> modelEntity = getModelEntityForInstance(o);
		 * types[i] = modelEntity.getImplementedInterface();
		 * }
		 * else {
		 * types[i] = o != null ? o.getClass() : null;
		 * }
		 * }
		 * ModelInitializer initializerForArgs =
		 * modelEntity.getInitializerForArgs(types);
		 * if (initializerForArgs != null) {
		 * handler.initializing = true;
		 * try {
		 * initializerForArgs.getInitializingMethod().invoke(returned, args);
		 * } finally {
		 * handler.initializing = false;
		 * handler.initialized = true;
		 * }
		 * }
		 * else {
		 * if (args.length > 0) {
		 * StringBuilder sb = new StringBuilder();
		 * for (Class<?> c : types) {
		 * if (sb.length() > 0) {
		 * sb.append(',');
		 * }
		 * sb.append(c != null ? c.getName() : "<null>");
		 *
		 * }
		 * throw new NoSuchMethodException("Could not find any initializer with args " +
		 * sb.toString());
		 * }
		 * }
		 * }
		 * }
		 *
		 * // looks for property to initialize
		 * for (ModelProperty<? super I> property : modelEntity.getPropertyIterable()) {
		 * if (property.getInitialize() != null) {
		 * handler.invokeSetter(property,
		 * PamelaModelFactory.this.newInstance(property.getType()));
		 * }
		 * }
		 *
		 * objectHasBeenCreated(returned, modelEntity.getImplementedInterface());
		 * return returned;
		 * }
		 */

		/**
		 * Creates a new model instance.
		 * This is a hidden wrapper for {@link #newInstance}.
		 * The wrapper name is meant to accomodate myself with the multiple overloading
		 * of {@link #newInstance}.
		 *
		 * @param args the arguments to pass to the initializer of the created instance
		 * @return the created instance
		 * @throws IllegalArgumentException
		 * @throws NoSuchMethodException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws ModelDefinitionException  if the model definition is invalid
		 */
		private I newInstanceOfModelEntity(Object... args)
				throws IllegalArgumentException, NoSuchMethodException, InstantiationException,
				IllegalAccessException, InvocationTargetException, ModelDefinitionException {
			return newInstance(args);
		}

		public I newInstance(Object... args)
				throws IllegalArgumentException, NoSuchMethodException, InstantiationException,
				IllegalAccessException, InvocationTargetException, ModelDefinitionException {
			// TODO this seems to be the base definition for creating an instance of a class
			// probably annotated with @ModelEntity
			if (modelEntity.isAbstract()) {
				throw new InstantiationException(
						modelEntity + " is declared as an abstract entity, cannot instantiate it");
			}
			locked = true;
			ProxyMethodHandler<I> handler = new ProxyMethodHandler<>(this, getEditingContext());

			if (args == null) {
				args = new Object[0];
			}

			I returned = null;
			if (modelEntity.isSimplePamelaInstrumentation()) {
				Class<?>[] paramTypesArray = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					paramTypesArray[i] = args[i].getClass();
				}
				returned = (I) create(paramTypesArray, args, handler);
				handler.setObject(returned);
			} else {

				// Java 11 security issue
				// If the base implementation class is not in the same package than the
				// implemented interface, it fails
				// The workaround is to generate (or reuse) on the fly a proxy super classes (in
				// the right package !)
				if (!modelEntity.getImplementedInterface().getPackage().equals(getSuperclass().getPackage())) {
					Class<?> implementationClass = retrieveProxyImplementationClass(
							modelEntity.getImplementedInterface(), getSuperclass());
					setProxySuperClass(implementationClass);
				}

				returned = (I) create(new Class<?>[0], new Object[0], handler);
				handler.setObject(returned);
				if (args.length > 0 || modelEntity.hasInitializers()) {
					Class<?>[] types = new Class<?>[args.length];
					for (int i = 0; i < args.length; i++) {
						Object o = args[i];
						if (isProxyObject(o)) {
							ModelEntity<?> modelEntity = getModelEntityForInstance(o);
							types[i] = modelEntity.getImplementedInterface();
						} else {
							types[i] = o != null ? o.getClass() : null;
						}
					}
					ModelInitializer initializerForArgs = modelEntity.getInitializerForArgs(types);
					if (initializerForArgs != null) {
						handler.initializing = true;
						try {
							initializerForArgs.getInitializingMethod().invoke(returned, args);
						} finally {
							handler.initializing = false;
							handler.initialized = true;
						}
					} else {
						if (args.length > 0) {
							StringBuilder sb = new StringBuilder();
							for (Class<?> c : types) {
								if (sb.length() > 0) {
									sb.append(',');
								}
								sb.append(c != null ? c.getName() : "<null>");

							}
							throw new NoSuchMethodException(
									"Could not find any initializer with args " + sb.toString());
						}
					}
				}
			}

			// looks for property to initialize
			for (ModelProperty<? super I> property : modelEntity.getPropertyIterable()) {
				if (property.getInitialize() != null) {
					handler.invokeSetter(property, PamelaModelFactory.this.newInstance(property.getType()));
				}
			}

			objectHasBeenCreated(returned, modelEntity.getImplementedInterface());
			return returned;
		}

		/**
		 * Generate (return when already existant) a base implementation class proxying
		 * the declared base implementation class, but in the
		 * right package (the same as the implemented interface)
		 *
		 * @param implementedInterface
		 * @param superClass
		 * @return
		 */
		private Class retrieveProxyImplementationClass(Class<?> implementedInterface, Class<?> superClass) {

			String packageName = implementedInterface.getPackageName();

			Map<Class, Class> map = implementationProxyClasses.get(superClass);
			if (map == null) {
				map = new HashMap<>();
				implementationProxyClasses.put(superClass, map);
			}

			Class returned = map.get(implementedInterface);
			if (returned == null) {
				ClassPool pool = ClassPool.getDefault();
				CtClass ctClass = pool.makeClass(
						packageName + "." + superClass.getSimpleName() + "_" + implementedInterface.getSimpleName()
								+ "_" + "Proxy");
				try {
					ctClass.setSuperclass(pool.get(superClass.getName()));
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					returned = ctClass.toClass(implementedInterface);
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map.put(implementedInterface, returned);
			}
			return returned;
		}

	}

	public PamelaModelFactory(Class<?> baseClass) throws ModelDefinitionException {
		this(PamelaMetaModelLibrary.retrieveMetaModel(baseClass));
		// TODO given this "baseClass" e.g. an interface like FlexoProcess, this is
		// simply initializing a PamelaMetaModel if it doesn't exists yet and dispatch
		// it to the other constructor
	}

	public PamelaModelFactory(PamelaMetaModel pamelaMetaModel) {
		this.pamelaMetaModel = pamelaMetaModel;
		proxyFactories = new HashMap<>();
		stringEncoder = new StringEncoder(this); // this is used particularly in Deserialization
	}

	@Deprecated(since = "For testing purposes only", forRemoval = true)
	public PamelaMetaModel getPamelaMetaModel() {
		return getModelContext();
	}

	public PamelaMetaModel getModelContext() {
		return pamelaMetaModel;
		// TODO this is initialized at instantiation of a PamelaModelFactory with a
		// given baseClass, for instance the interface FlexoProcess
	}

	// TODO idf why and differences with the rest
	public PamelaMetaModel getExtendedContext() {
		return extendedContext != null ? extendedContext : pamelaMetaModel;
	}

	public <I> I newInstance(ModelEntity<I> modelEntity) {
		return newInstance(modelEntity, (Object[]) null);
	}

	public <I> I newInstance(ModelEntity<I> modelEntity, Object... args) {
		return newInstance(modelEntity.getImplementedInterface(), args);
		// TODO ici j'ai un modelEntity et nons pas l'implémentation d'une Interface
		// comme ci-dessous
	}

	public <I> I newInstance(Class<I> implementedInterface) {
		// TODO ok je demande une nouvelle instance de l'interface de FPI
		// la routine c'est que je n'ai pas d'argument dans le cas de test
		// SerizalizationTest RestrictiveFails
		return newInstance(implementedInterface, (Object[]) null);
	}

	public <I> I newInstance(Class<I> implementedInterface, Object... args) {
		try {
			// this.getModelContext().getPatternContext().enteringConstructor();
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);

			// TODO idf, is this how to get a ProxyObject at runtime ?
			I returned = proxyFactory.newInstanceOfModelEntity(args);

			// TODO idf, this is calling the "base" definition ?
			if (getEditingContext() != null) {
				// TODO stil idf the editingContext concept and it's usage
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager()
							.addEdit(new CreateCommand<>(returned, proxyFactory.getModelEntity(), this));
				}
			}

			// this.getModelContext().getPatternContext().leavingConstructor();
			getPamelaMetaModel().notifiedNewInstance(returned, getModelEntityForInstance(returned));
			return returned;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ExceptionInInitializerError e) {
			e.getCause().printStackTrace();
			throw new ModelExecutionException(e);
		}
	}

	public <I> I newInstance(Class<I> implementedInterface, boolean useExtended) {
		return _newInstance(implementedInterface, useExtended, (Object[]) null);
	}

	// TODO idf why this seems to duplicate the other newInstance(Class, args) but
	// with "extended"
	public <I> I _newInstance(Class<I> implementedInterface, boolean useExtended, Object... args) {
		try {
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true, useExtended);
			I returned = proxyFactory.newInstanceOfModelEntity(args);
			if (getEditingContext() != null) {
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager()
							.addEdit(new CreateCommand<>(returned, proxyFactory.getModelEntity(), this));
				}
			}
			return returned;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		}
	}

	/*
	 * Unused
	 * private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I>
	 * implementedInterface) throws ModelDefinitionException {
	 * return getProxyFactory(implementedInterface, true);
	 * }
	 */
	private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I> implementedInterface, boolean create)
			throws ModelDefinitionException {
		return getProxyFactory(implementedInterface, create, false);
	}

	// TODO idf this helper method
	private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I> implementedInterface, boolean create,
			boolean useExtended)
			throws ModelDefinitionException {
		// TODO idf why this methods exists, the enclosing class already return the
		// "proxyFactories" attribute ?
		PAMELAProxyFactory<I> proxyFactory = proxyFactories.get(implementedInterface);
		// TODO idf why proxyFactory could be null ?
		if (proxyFactory == null) {
			// TODO in case it's null, then what happens ? in my case it's not extended,
			// then what ?
			ModelEntity<I> entity;
			if (useExtended) {
				entity = getExtendedContext().getModelEntity(implementedInterface);
			} else {
				// TODO then idf "getModelContext()" or it's subsequent call to "ModelEntity()"
				// given the "implementedInterface"
				entity = getPamelaMetaModel(). // TODO this return a "PamelaMetaModel" type, where did I saw this ? I
												// suppose it contains the implementing interface of FlexoProcess given
												// when creating a "PamelaModelFactory"
						getModelEntity(implementedInterface);
				// TODO perhaps I have to look at how "entity" is derived to help understand how
				// it's used in the instantiation of PAMELAProxyFactory
			}
			// TODO then if the entity is null what happens ?
			if (entity == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Debug model context\n");
				Iterator<ModelEntity> it = pamelaMetaModel.getEntities();
				while (it.hasNext()) {
					ModelEntity<?> next = it.next();
					sb.append("> ").append(next).append('\n');
				}
				System.out.println(sb.toString());
				throw new ModelExecutionException("Unknown entity '" + implementedInterface.getName()
						+ "'! Did you forget to import it or to annotated it with @ModelEntity?");
				// TODO idf is this exception different from the one I encountered before ?
			}
			// TODO if it's not null and since I am in the case "create=true" then perhaps I
			// don't get in the above conditional ?
			else {
				if (create) {
					// TODO here "entity" is not null
					proxyFactory = new PAMELAProxyFactory<>(entity, this.getPamelaMetaModel());
					// TODO idf why instantiate PAMELAProxyFactory, given the entity ?
					// TODO idf what's the entity type ? what's the value ? since it's derived from
					// the implemented Interface of a FlexoProcess,
					proxyFactories.put(implementedInterface, proxyFactory);
					// TODO idf why I need to save this in the proxyFactory map,
				}
			}
		}
		return proxyFactory;
	}

	public Class<?> getDefaultModelClass() {
		return defaultModelClass;
	}

	public void setDefaultModelClass(Class<?> defaultModelClass) {
		Class<?> old = defaultModelClass;
		this.defaultModelClass = defaultModelClass;
		for (PAMELAProxyFactory<?> factory : proxyFactories.values()) {
			if (factory.getSuperclass() == old) {
				factory.setSuperclass(defaultModelClass);
			}
		}
	}

	public <I> void setImplementingClassForInterface(Class<? extends I> implementingClass,
			Class<I> implementedInterface)
			throws ModelDefinitionException {
		try {
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
			proxyFactory.setSuperclass(implementingClass);
		} catch (ModelExecutionException e) {
			// OK, we won't add the implementation since the interface is not
			// declared
		}
	}

	public <I> Class<? extends I> getImplementingClassForInterface(Class<I> implementedInterface)
			throws ModelDefinitionException {
		PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
		if (proxyFactory != null) {
			return (Class) proxyFactory.getSuperclass();
		}
		return null;
	}

	public <I> void setImplementingClassForInterface(Class<? extends I> implementingClass,
			Class<I> implementedInterface,
			boolean useExtended) throws ModelDefinitionException {
		PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true, useExtended);
		if (proxyFactory != null) {
			proxyFactory.setSuperclass(implementingClass);
		}
	}

	public <I> void setImplementingClassForEntity(Class<? extends I> implementingClass, ModelEntity<I> entity)
			throws ModelDefinitionException {
		setImplementingClassForInterface(implementingClass, entity.getImplementedInterface());
	}

	<I> void setImplementingClassForEntity(Class<? extends I> implementingClass, ModelEntity<I> entity,
			boolean useExtended)
			throws ModelDefinitionException {
		setImplementingClassForInterface(implementingClass, entity.getImplementedInterface(), useExtended);
	}

	public Class<? extends List> getListImplementationClass() {
		return listImplementationClass;
	}

	public void setListImplementationClass(Class<? extends List> listImplementationClass) {
		this.listImplementationClass = listImplementationClass;
	}

	public boolean isProxyObject(Object object) {
		return object instanceof ProxyObject;
	}

	public <I> ModelEntity<I> getModelEntityForInstance(I object) {
		ProxyMethodHandler<I> handler = getHandler(object);
		if (handler != null) {
			return handler.getModelEntity();
		}
		return null;
	}

	public <I> ProxyMethodHandler<I> getHandler(I object) {
		if (object instanceof ProxyObject) {

			// Vincent: Check this, handler can be of DelegateImplementation
			// Type( in the case of Edition actions containers)
			// ???
			/*
			 * if(((ProxyObject) object).getHandler() instanceof DelegateImplementation){
			 * return ((DelegateImplementation<I>) ((ProxyObject)
			 * object).getHandler()).getMasterMethodHandler();
			 * }
			 */
			if (((ProxyObject) object).getHandler() instanceof ProxyMethodHandler) {
				return (ProxyMethodHandler<I>) ((ProxyObject) object).getHandler();
				/*
				 * TODO why is this casting to ProxyObject ?
				 */
			}
		}
		return null;
	}

	public <I> ModelEntity<I> importClass(Class<I> klass) throws ModelDefinitionException {
		ModelEntity<I> modelEntity = pamelaMetaModel.getModelEntity(klass);
		// TODO idf how do I get here ?
		if (modelEntity == null) {
			extendedContext = new PamelaMetaModel(klass, getExtendedContext());
			modelEntity = extendedContext.getModelEntity(klass);
		}
		return modelEntity;
	}

	public StringEncoder getStringEncoder() {
		return stringEncoder;
	}

	//TODO idf usages of this method ?
	public void addConverter(Converter<?> converter) {
		stringEncoder.addConverter(converter);
	}

	public boolean isEmbedddedIn(Object parentObject, Object childObject, EmbeddingType embeddingType) {
		return getEmbeddedObjects(parentObject, embeddingType).contains(childObject);
	}

	public boolean isEmbedddedIn(Object parentObject, Object childObject, EmbeddingType embeddingType,
			Object... context) {
		return getEmbeddedObjects(parentObject, embeddingType, context).contains(childObject);
	}

	/**
	 * Build and return a List of embedded objects, using meta informations
	 * contained in related class All property should be annotated with
	 * a @Embedded annotation which determine the way of handling this property
	 *
	 * Supplied context is used to determine the closure of objects graph being
	 * constructed during this operation.
	 *
	 * @param root
	 * @return
	 */
	// TODO maybe I need to annotate my IAuthenticator or ISubject with @Embedding ?
	public List<Object> getEmbeddedObjects(Object root, EmbeddingType embeddingType) {
		return getEmbeddedObjects(root, embeddingType, (Object[]) null);
	}

	/**
	 * Build and return a List of embedded objects, using meta informations
	 * contained in related class All property should be annotated with
	 * a @Embedded annotation which determine the way of handling this property
	 *
	 * Supplied context is used to determine the closure of objects graph being
	 * constructed during this operation.
	 *
	 * @param root
	 * @param context
	 * @return
	 */
	public List<Object> getEmbeddedObjects(Object root, EmbeddingType embeddingType, Object... context) {
		if (!isProxyObject(root)) {
			// TODO idf why if not a proxy then is empty
			// does it imply that if we pass something that is not proxied with the
			// javassist.Proxy then we ignore it ?
			// or maybe this could work with other types not directly annotated with the
			// Pamela framework ?
			return Collections.emptyList();
		}

		List<Object> derivedObjectsFromContext = new ArrayList<>();
		if (context != null && context.length > 0) {
			for (Object o : context) {
				derivedObjectsFromContext.add(o);
				derivedObjectsFromContext.addAll(getEmbeddedObjects(o, embeddingType));
			}
		}

		List<Object> returned = new ArrayList<>();
		try {
			appendEmbeddedObjects(root, returned, embeddingType);
		} catch (ModelDefinitionException e) {
			throw new ModelExecutionException(e);
		}
		List<Object> discardedObjects = new ArrayList<>();
		for (int i = 0; i < returned.size(); i++) {
			Object o = returned.get(i);
			if (o instanceof ConditionalPresence) {
				// TODO idf this concept of ConditionalPresence
				boolean allOthersArePresent = true;
				for (Object other : ((ConditionalPresence) o).requiredPresence) {
					// TODO not sure to follow this
					if (!returned.contains(other) && !derivedObjectsFromContext.contains(other)) {
						allOthersArePresent = false;
						break;
					}
				}
				if (allOthersArePresent && !returned.contains(((ConditionalPresence) o).object)) {
					// Closure is fine and object is not already present, add
					// object
					returned.set(i, ((ConditionalPresence) o).object);
				} else {
					// Discard object
					discardedObjects.add(o);
				}
			}
		}
		for (Object o : discardedObjects) {
			returned.remove(o);
		}
		return returned;
	}

	private class ConditionalPresence {
		private final Object object;
		private final List<Object> requiredPresence;

		public ConditionalPresence(Object object, List<Object> requiredPresence) {
			super();
			this.object = object;
			this.requiredPresence = requiredPresence;
		}
	}

	private void appendEmbedded(ModelProperty p, Object root, List<Object> list, Object child,
			EmbeddingType embeddingType)
			throws ModelDefinitionException {
		if (!isProxyObject(child)) {
			return;
		}

		if (p.getEmbedded() == null && p.getComplexEmbedded() == null) {
			// this property is not embedded
			return;
		}

		boolean append = false;
		switch (embeddingType) {
			case CLOSURE:
				append = p.getEmbedded() != null && p.getEmbedded().closureConditions().length == 0
						|| p.getComplexEmbedded() != null && p.getComplexEmbedded().closureConditions().length == 0;
				break;
			case DELETION:
				append = p.getEmbedded() != null && p.getEmbedded().deletionConditions().length == 0
						|| p.getComplexEmbedded() != null && p.getComplexEmbedded().deletionConditions().length == 0;
				break;
		}

		if (append) {
			// There is no condition, just append it
			if (!list.contains(child)) {
				// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
				list.add(child);
				appendEmbeddedObjects(child, list, embeddingType);
			}
		} else {
			List<Object> requiredPresence = new ArrayList<>();
			if (p.getEmbedded() != null) {
				switch (embeddingType) {
					case CLOSURE:
						for (String c : p.getEmbedded().closureConditions()) {
							ModelEntity<?> closureConditionEntity = getModelEntityForInstance(child);
							ModelProperty closureConditionProperty = closureConditionEntity.getModelProperty(c);
							Object closureConditionRequiredObject = getHandler(child)
									.invokeGetter(closureConditionProperty);
							if (closureConditionRequiredObject != null) {
								requiredPresence.add(closureConditionRequiredObject);
							}
						}
						break;
					case DELETION:
						for (String c : p.getEmbedded().deletionConditions()) {
							ModelEntity<?> deletionConditionEntity = getModelEntityForInstance(child);
							ModelProperty deletionConditionProperty = deletionConditionEntity.getModelProperty(c);
							Object deletionConditionRequiredObject = getHandler(child)
									.invokeGetter(deletionConditionProperty);
							if (deletionConditionRequiredObject != null) {
								requiredPresence.add(deletionConditionRequiredObject);
							}
						}
						break;
				}
				if (requiredPresence.size() > 0) {
					ConditionalPresence conditionalPresence = new ConditionalPresence(child, requiredPresence);
					list.add(conditionalPresence);
				} else {
					if (!list.contains(child)) {
						// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
						list.add(child);
						appendEmbeddedObjects(child, list, embeddingType);
					}
				}
			}
			// System.out.println("Embedded in "+father+" : "+child+" conditioned to
			// required presence of "+requiredPresence);
		}
	}

	private void appendEmbeddedObjects(Object root, List<Object> list, EmbeddingType embeddingType)
			throws ModelDefinitionException {
		ProxyMethodHandler handler = getHandler(root);
		ModelEntity modelEntity = handler.getModelEntity();

		Iterator<ModelProperty<?>> properties = modelEntity.getProperties();
		while (properties.hasNext()) {
			ModelProperty<?> p = properties.next();
			switch (p.getCardinality()) {
				case SINGLE:
					Object oValue = handler.invokeGetter(p);
					appendEmbedded(p, root, list, oValue, embeddingType);
					break;
				case LIST:
					List<?> values = (List<?>) handler.invokeGetter(p);
					if (values != null) {
						for (Object o : values) {
							appendEmbedded(p, root, list, o, embeddingType);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	public Clipboard copy(Object... objects)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return new Clipboard(this, objects);
	}

	public Clipboard cut(Object... objects)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		Clipboard returned = new Clipboard(this, objects);
		for (Object o : objects) {
			if (o instanceof DeletableProxyObject) {
				((DeletableProxyObject) o).delete(objects);
			}
		}
		return returned;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in
	 * object monitored by this method handler<br>
	 *
	 * @param clipboard
	 * @param context
	 * @return
	 */
	public boolean isPastable(Clipboard clipboard, Object context) throws ClipboardOperationException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).isPastable(clipboard);

	}

	/**
	 * Paste supplied clipboard in context object<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a
	 * list of objects for a multiple contents)
	 *
	 * @param clipboard
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid: " + context);
		}

		return getHandler(context).paste(clipboard);
	}

	/**
	 * Paste supplied clipboard in context object for supplied property <br>
	 * Return pasted objects (a single object for a single contents clipboard, and a
	 * list of objects for a multiple contents)
	 *
	 * @param clipboard
	 * @param modelProperty
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, ModelProperty<?> modelProperty, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).paste(clipboard, (ModelProperty) modelProperty);
	}

	/**
	 * Paste supplied clipboard in context object for supplied property at specified
	 * pasting point<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a
	 * list of objects for a multiple contents)
	 *
	 * @param clipboard
	 * @param modelProperty
	 * @param pp
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, ModelProperty<?> modelProperty, PastingPoint pp, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).paste(clipboard, (ModelProperty) modelProperty, pp);
	}

	// TODO idf this might be of interest when serializing
	public String stringRepresentation(Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			serialize(object, baos, SerializationPolicy.PERMISSIVE, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return baos.toString();
	}

	public void serialize(Object object, OutputStream os)
			throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			ModelDefinitionException {
		serialize(object, os, SerializationPolicy.PERMISSIVE, true);
	}

	public void serialize(Object object, OutputStream os, SerializationPolicy policy, boolean resetModifiedStatus)
			throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			ModelDefinitionException {
		XMLSerializer serializer = new XMLSerializer(this, policy);

		serializer.serializeDocument(object, os, resetModifiedStatus);
	}

	public Object deserialize(InputStream is) throws Exception {
		return deserialize(is, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(InputStream is, DeserializationPolicy policy) throws Exception {
		XMLSaxDeserializer deserializer = new XMLSaxDeserializer(this, policy);
		return deserializer.deserializeDocument(is);
	}

	public Object deserialize(String input) throws Exception {
		return deserialize(input, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(String input, DeserializationPolicy policy) throws Exception {
		XMLSaxDeserializer deserializer = new XMLSaxDeserializer(this, policy);
		return deserializer.deserializeDocument(input);
	}

	/**
	 * Hook to detect an object creation Default implementation silently returns
	 *
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	public <I> void objectHasBeenCreated(I newlyCreatedObject, Class<I> implementedInterface) {
		// System.out.println("object is being created obj=" + newlyCreatedObject);
		// System.out.println("object is being created implInterface=" +
		// implementedInterface);
	}

	/**
	 * Hook to detect an object deserialization (called just after instance has been
	 * created)<br>
	 * Default implementation silently returns
	 *
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	// TODO this does nothing at the moment maybe I could do something ?
	public <I> void objectIsBeeingDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		System.out.println("object is being deserialized obj=" + newlyCreatedObject);
		System.out.println("object is being deserialized implInterface=" + implementedInterface);
	}

	/**
	 * Hook to detect an object deserialization (called at the end of whole object
	 * graph deserialization)<br>
	 * Default implementation silently returns
	 *
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		System.out.println("object has being deserialized obj=" + newlyCreatedObject);
		System.out.println("object has being deserialized implInterface=" + implementedInterface);
	}

	/**
	 * Return {@link EditingContext} associated with this factory.
	 *
	 * @return
	 */
	public EditingContext getEditingContext() {
		return editingContext;
	}

	/**
	 * Sets {@link EditingContext} associated with this factory.<br>
	 * When not null, new instances created with this factory are automatically
	 * registered in this EditingContext
	 *
	 * @param editingContext
	 */
	public void setEditingContext(EditingContext editingContext) {
		this.editingContext = editingContext;
	}

	/**
	 * Check that this factory contains all required implementation for all
	 * non-abstract entities
	 * 
	 * 
	 * @throws MissingImplementationException
	 *                                        when an implementation was not found
	 */
	// TODO idf the comment and usage of "all" "required" "implementation"
	public void checkMethodImplementations() throws ModelDefinitionException, MissingImplementationException {
		PamelaMetaModel pamelaMetaModel = getPamelaMetaModel();
		MissingImplementationException thrown = null;
		for (Iterator<ModelEntity> it = pamelaMetaModel.getEntities(); it.hasNext();) {
			ModelEntity<?> e = it.next();
			try {
				// TODO this "dispatch" to "ModelEntity"
				e.checkMethodImplementations(this);
			} catch (MissingImplementationException ex) {
				System.err.println("MissingImplementationException: " + ex.getMessage());
				thrown = ex;
			}
		}
		if (thrown != null) {
			throw thrown;
		}
	}

}
