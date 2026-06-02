package org.openflexo.pamela.securitypatterns.authenticator.persistance;

import org.openflexo.connie.type.CustomType;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DeserializationFinalizer;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;

@ModelEntity
@ImplementationClass(ISubject.SubjectImp.class)
@AuthenticatorSubject(patternID = ISubject.PATTERN_ID)
@XMLElement
public interface ISubject extends AccessibleProxyObject, DeletableProxyObject, CloneableProxyObject, CustomType {
	String PATTERN_ID = "patternID";
	String AUTH_INFO = "auth_info";
	String MANAGER = "manager";
	String ID_PROOF = "id_proof";

	@Initializer
	default void init(IAuthenticator authenticator, String authInfo, int idProof) {
		setManager(authenticator);
		setAuthInfo(authInfo);
		setIdProof(idProof);
	}

	@Initializer
	// TODO idf is this annotation imply that when
	// `pamelaModelFactory.newInstance(..)` must be provided the args that follows ?
	// no, this imply that it will provide a constructor for this. Since
	default void init(IAuthenticator authenticator, String id) {
		setManager(authenticator);
		setAuthInfo(id);
	}

	@Initializer
	default void init(String id) {
		setAuthInfo(id);
	}

	@Initializer
	default void init() {
	}

	@Getter(value = AUTH_INFO, defaultValue = AUTH_INFO)
	@AuthenticationInformation(patternID = PATTERN_ID, paramID = IAuthenticator.ID)
	@XMLAttribute(xmlTag = AUTH_INFO)
	// TODO idf why the default value is not serialized ?
	String getAuthInfo();

	@Setter(AUTH_INFO)
	void setAuthInfo(String val);

	@Getter(value = ID_PROOF, defaultValue = "-1")
	// TODO the defaultValue seems to be ignored while serializing in this case
	// since it's a string and expected is an integer to be serialized ?
	@XMLAttribute(xmlTag = ID_PROOF)
	// TODO idf why the default value is not serialized ?
	int getIDProof();

	@Setter(ID_PROOF)
	@ProofOfIdentitySetter(patternID = PATTERN_ID)
	void setIdProof(int val);

	@Getter(MANAGER)
	@AuthenticatorGetter(patternID = PATTERN_ID)
	@XMLElement(xmlTag = MANAGER) // TODO which value would be better ? an id that ref the manager as with
									// XMLElement ? of something else ?
	@CloningStrategy(StrategyType.CLONE) //TODO this might become tedious to handle when adding a constraint that each subject might have different authenticator ?
	IAuthenticator getManager();

	@Setter(MANAGER)
	void setManager(IAuthenticator val);

	@StringConverter
	Converter<IAuthenticator> IAuthenticatorConverter = new IAuthenticatorConverter();

	@AuthenticateMethod(patternID = PATTERN_ID)
	void authenticate();

	@RequiresAuthentication
	public void thisMethodRequiresToBeAuthenticated();

	@DeserializationInitializer
	public void initializeDeserialization();

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public static abstract class SubjectImp implements ISubject {
		public static String DESERIALIZATION_TRACE = "";

		private boolean isDeserializing = false;

		//TODO probably like IAuthenticator solution
		// @Override
		// public boolean equals(Object o) {
		// 	if (this == o)
		// 		return true;
		// 	if (!(o instanceof ISubject))
		// 		return false;

		// 	ISubject other = (ISubject) o;

		// 	return Objects.equals(getAuthInfo(), other.getAuthInfo())
		// 			&& getIDProof() == other.getIDProof();
		// }

		// @Override
		// public int hashCode() {
		// 	return Objects.hash(getAuthInfo());
		// }

		@Override
		public void initializeDeserialization() {
			System.out.println("Init deserialization for ISubject IDProof=" + getIDProof());
			isDeserializing = true;
		}

		@Override
		public void setIdProof(int val) {
			if (isDeserializing) {
				DESERIALIZATION_TRACE += " BEGIN:" + val;
			}
			performSuperSetter(ID_PROOF, val);
		}

		@Override
		public void finalizeDeserialization() {
			isDeserializing = false;
			DESERIALIZATION_TRACE += " END:" + getIDProof();
			System.out.println("Finalize deserialization for ISubject IDProof=" + getIDProof());
		}

		@Override
		public void thisMethodRequiresToBeAuthenticated() {
			System.out.println("I need to be authenticated to execute this");
		}
	}
}
