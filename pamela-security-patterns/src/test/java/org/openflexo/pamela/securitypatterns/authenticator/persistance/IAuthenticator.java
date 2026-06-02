package org.openflexo.pamela.securitypatterns.authenticator.persistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DeserializationFinalizer;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.pamela.patterns.PropertyParadigmType;
import org.openflexo.pamela.patterns.annotations.Requires;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

@ModelEntity
@ImplementationClass(IAuthenticator.AuthenticatorImp.class)
@XMLElement(xmlTag = "IAuthenticator")
@Authenticator(patternID = ISubject.PATTERN_ID)
public interface IAuthenticator extends AccessibleProxyObject, DeletableProxyObject, CloneableProxyObject {
	// TODO probably not implement Cloneable ? like with Duration example since in
	// case of
	// IAuthenticator being proxied this seems weird

	String USERS = "users";
	String ID = "id";
	String NAME = "name";

	@StringConverter
	Converter<IAuthenticator> AUTHENTICATOR_CONVERTER = new IAuthenticatorConverter();

	@Initializer
	default void init() {
		setUsers(new ArrayList<>());
	}

	@Initializer
	default void init(String name) {
		setName(name);
		init();
	}

	@XMLElement(xmlTag = "name")
	@Getter(value = NAME, defaultValue = "defaultName")
	@CloningStrategy(StrategyType.CLONE)
	String getName();

	@Setter(NAME)
	void setName(String name);

	@XMLElement(primary = true)
	@Getter(value = USERS, cardinality = Getter.Cardinality.LIST)
	// TODO is this a ModelProperty as the doc says? probably
	// TODO as is this doesn't write anything yet, probably, to get the
	// serialization to works I would need to add a concept of User or ISubject as
	// the type ? probably not, what I need is to define the converter which should
	// process this normally
	List<ISubject> getUsers();

	// TODO define a converter for deserialization
	// @StringConverter
	// Converter<Type<ISubject>> SUBJECT_CONVERTER = new TypeConverter();

	@Setter(USERS)
	void setUsers(List<ISubject> users);

	@Adder(USERS)
	void addUser(ISubject val);

	@Remover(USERS)
	void removeUser(ISubject val);

	@Requires(patternID = ISubject.PATTERN_ID, type = PropertyParadigmType.Java, property = "prout")
	public void aMethodGuardedWithAPrecondition();

	@RequestAuthentication(patternID = ISubject.PATTERN_ID)
	int request(@AuthenticationInformation(patternID = ISubject.PATTERN_ID, paramID = ID) String id);

	int generateFromAuthInfo(String id);

	default int getDefaultToken() {
		return -42;
	}

	@DeserializationInitializer
	public void initializeDeserialization();

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public static abstract class AuthenticatorImp implements IAuthenticator {
		/*
		 * TODO I though about defining this to test equality after serialization but
		 * this might be incorrect for at least one reason:
		 * since PAMELA is doing runtime computation using a Proxy scheme this might be
		 * incompatible.
		 * That's why it's more appropriate to use the equality defined by PAMELA.
		 * Maybe another reason might be added to support or not this case.
		 */

		// private String cachedName;

		// @Override
		// public boolean equals(Object o) {
		// //don't use this method, instead use PAMELA equality
		// //- [Equality computing support](./pamela-core/10-equality_computing.md)
		// if (this == o)
		// return true;
		// if (!(o instanceof IAuthenticator))
		// return false;

		// IAuthenticator other = (IAuthenticator) o;

		// boolean result = false;
		// // result = Objects.equals(this.getName(), other.getName());
		// result = Objects.equals(this.cachedName, ((AuthenticatorImp)
		// other).cachedName);
		// // TODO I have to access the field without using the proxy which creates an
		// // infinite recursion
		// // not use the methods as well
		// // && Objects.equals(getUsers(), other.getUsers())
		// return result;
		// }

		// // TODO
		// @Override
		// public int hashCode() {
		// // return Objects.hash(getName());
		// return Objects.hash(cachedName);
		// }

		@Override
		public int request(String id) {
			if (this.check(id)) {
				return this.generateFromAuthInfo(id);
			}
			return this.getDefaultToken();
		}

		private boolean check(String id) {
			for (ISubject user : this.getUsers()) {
				if (user.getAuthInfo().compareTo(id) == 0)
					return true;
			}
			return false;
		}

		@Override
		public int generateFromAuthInfo(String id) {
			return id.hashCode();
		}

		@Override
		public void aMethodGuardedWithAPrecondition() {
			System.out.println("aMethodGuardedWithAPrecondition");
		}

		public static String DESERIALIZATION_TRACE = "";

		private boolean isDeserializing = false;
		// TODO why is this shadowed ? does it imply that this is not used ?

		// @Override
		// public void setUsers(List<ISubject> users) {
		// if (isDeserializing) {
		// DESERIALIZATION_TRACE += " BEGIN:" + users.size();
		// }
		// performSuperSetter(USERS, users);
		// }

		@Override
		public void setName(String name) {
			performSuperSetter(NAME, name);
		}

		@Override
		public void initializeDeserialization() {
			if (isDeserializing) {
				DESERIALIZATION_TRACE += " BEGIN:" + getName();
			}
			System.out.println("Init deserialization for Authenticator " + getName());
			isDeserializing = true;
		}

		@Override
		// TODO why this method doesn't seem to be called by the framework ? maybe
		// because there's no "childElement"
		public void finalizeDeserialization() {
			isDeserializing = false;
			DESERIALIZATION_TRACE += " END:" + getName(); // + getName();
			// TODO maybe trace users as well ?
			System.out.println("Finalize deserialization for Authenticator " + getName());
		}

	}

}
