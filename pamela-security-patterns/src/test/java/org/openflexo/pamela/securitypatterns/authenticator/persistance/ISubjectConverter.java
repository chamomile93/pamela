package org.openflexo.pamela.securitypatterns.authenticator.persistance;

import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

public class ISubjectConverter extends Converter<ISubject> {

	public ISubjectConverter() {
		super(ISubject.class);
	}

	@Override
	public ISubject convertFromString(String value, PamelaModelFactory factory) {
		ISubject subject = factory.newInstance(ISubject.class);
		//TODO
		System.out.println("ISubjectConverter.convertFromString="+value);
		return null;
	}

	@Override
	public String convertToString(ISubject value) {
		if (value != null) {
			return value.toString();
		}
		return null;
	}
}
