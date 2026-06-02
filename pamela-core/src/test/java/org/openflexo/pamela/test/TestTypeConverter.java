package org.openflexo.pamela.test;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.WildcardTypeImpl.DefaultWildcardType;
import org.openflexo.pamela.converter.TypeConverter;

public class TestTypeConverter extends AbstractPAMELATest {
	// TODO I am looking at this class to probably get an understanding of how this
	// concept of TypeConverter could be use to define a Converter for the
	// IAuthenticator

	private TypeConverter typeConverter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		// TODO what factories could be given if any ?
		typeConverter = new TypeConverter(null);

		/*
		 * new File("/tmp").mkdirs();
		 * pamelaMetaModel = new PamelaMetaModel(FlexoProcess.class);
		 * factory = new PamelaModelFactory(pamelaMetaModel);
		 */
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	public void testConvertStringClassFromFQNisStringClassSucceed() throws Exception {
		assertEquals(String.class, typeConverter.convertFromString("java.lang.String", null));
	}

	public void testConvertListClassFromFQNisListClassSucceed() throws Exception {
		assertEquals(List.class, typeConverter.convertFromString("java.util.List", null));
	}

	public void testConvertParameterizedClassFromFQNisListOfStringClassSucceed() throws Exception {
		assertEquals(new ParameterizedTypeImpl(List.class, String.class),
				typeConverter.convertFromString("java.util.List<java.lang.String>", null));
	}

	public void testConvertParameterizedClassFromFQNisParameterizedClassSucceed() throws Exception {
		assertEquals(
				new ParameterizedTypeImpl(Map.class, String.class,
						new ParameterizedTypeImpl(Map.class, String.class, Object.class)),
				typeConverter.convertFromString(
						"java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Object>>", null));
	}

	public void testConvertWildcardClassFromUpperBoundWildcardStringSucceed() throws Exception {
		assertEquals(DefaultWildcardType.makeUpperBoundWilcard(Object.class),
				typeConverter.convertFromString("? extends java.lang.Object", null));
	}

	public void testConvertDefaultWildcardClassFromWildcardStringSucceed() throws Exception {
		assertEquals(new DefaultWildcardType(), typeConverter.convertFromString("?", null));
	}

	public void testConvertParameterizedClassWithWildcardTypeClassFromUpperBoundWildcardStringSucceed() throws Exception {
		assertEquals(
				new ParameterizedTypeImpl(Map.class, DefaultWildcardType.makeUpperBoundWilcard(Object.class),
						DefaultWildcardType.makeUpperBoundWilcard(
								new ParameterizedTypeImpl(List.class, new DefaultWildcardType()))),
				typeConverter.convertFromString(
						"java.util.Map<? extends java.lang.Object, ? extends java.util.List<?>>", null));
	}

}
