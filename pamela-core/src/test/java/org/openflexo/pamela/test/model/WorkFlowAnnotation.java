package org.openflexo.pamela.test.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

@ModelEntity
public interface WorkFlowAnnotation extends TestModelObject {

	public static final String TEXT = "text";

	@Override
	@Initializer
	public WorkFlowAnnotation init();

	@Override
	@Initializer
	public WorkFlowAnnotation init(@Parameter(TEXT) String text);

	@Getter(TEXT)
	public String getText();

	@Setter(TEXT)
	public void setText(String s);

	@StringConverter
	public static final Converter<WorkFlowAnnotation> CONVERTER = new WKFAnnotationConverter();

	public static class WKFAnnotationConverter extends Converter<WorkFlowAnnotation> {

		public WKFAnnotationConverter() {
			super(WorkFlowAnnotation.class);
		}

		@Override
		public WorkFlowAnnotation convertFromString(String value, PamelaModelFactory factory) throws InvalidDataException {
			return factory.newInstance(WorkFlowAnnotation.class, value);
		}

		@Override
		public String convertToString(WorkFlowAnnotation value) {
			return value.getText();
		}

	}

}
