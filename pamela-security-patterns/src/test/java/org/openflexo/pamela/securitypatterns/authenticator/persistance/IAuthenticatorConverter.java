package org.openflexo.pamela.securitypatterns.authenticator.persistance;

import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

public class IAuthenticatorConverter extends Converter<IAuthenticator> {

	public IAuthenticatorConverter() {
		super(IAuthenticator.class);
	}

	@Override
	public IAuthenticator convertFromString(String value, PamelaModelFactory factory) {
		IAuthenticator auth = factory.newInstance(IAuthenticator.class);
		// TODO FIX as the value received is empty, instead of expected "Bob"
		auth.setName(value);
		System.out.println("IAuthenticatorConverter.convertFromString=" + value);
		/*
		 * try {
		 * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 * DocumentBuilder db = dbf.newDocumentBuilder();
		 * 
		 * Document doc = db.parse(
		 * new ByteArrayInputStream(
		 * value.getBytes(StandardCharsets.UTF_8)));
		 * 
		 * Element root = doc.getDocumentElement();
		 * 
		 * String name =
		 * root.getElementsByTagName("name")
		 * .item(0)
		 * .getTextContent();
		 * 
		 * IAuthenticator auth =
		 * factory.newInstance(IAuthenticator.class);
		 * 
		 * auth.setName(name);
		 * 
		 * return auth;
		 * }
		 * catch (Exception e) {
		 * throw new RuntimeException(e);
		 * }
		 */
		return auth;
	}

	@Override
	public String convertToString(IAuthenticator value) {
		if (value != null) {
			return value.toString(); // TODO change this for something better I am not sure this does a great job as
										// is
		}
		return null;
	}
}
