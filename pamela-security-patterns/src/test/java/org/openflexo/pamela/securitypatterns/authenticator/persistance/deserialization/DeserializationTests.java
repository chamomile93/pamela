package org.openflexo.pamela.securitypatterns.authenticator.persistance.deserialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.DeserializationPolicy;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.SerializationPolicy;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.securitypatterns.authenticator.model.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model.ISubject;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

import junit.framework.TestCase;

@RunWith(OrderedRunner.class)
public class DeserializationTests extends TestCase {

	private static File file;
	private PamelaModelFactory pamelaModelFactory;

	private IAuthenticator authenticator;
	private ISubject subject;

	@Before
	public void setUp() throws IOException, ModelDefinitionException {
		PamelaMetaModelLibrary.clearCache();
		ModelEntityLibrary.clear();

		file = File.createTempFile("PAMELA-TestDeserialization", ".xml");

		PamelaMetaModel context = PamelaMetaModelLibrary.retrieveMetaModel(ISubject.class, IAuthenticator.class);
		pamelaModelFactory = new PamelaModelFactory(context);
	}

	@After
	public void tearDown() {
		// file.delete();
	}

	@Test
	@TestOrder(1)
	public void testDeserializeOneAuthenticatorSucceed() {
		// GIVEN
		authenticator = (IAuthenticator) pamelaModelFactory.newInstance(IAuthenticator.class);
		authenticator.init();
		authenticator.setName("Bob");

		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(authenticator, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// WHEN

		try (FileInputStream fis = new FileInputStream(file)) {
			IAuthenticator deserializedAuthenticator = (IAuthenticator) pamelaModelFactory.deserialize(fis,
					DeserializationPolicy.EXTENSIVE);

			assertNotNull(deserializedAuthenticator);
			assertEquals(authenticator.getName(), deserializedAuthenticator.getName());
			assertNotSame(authenticator, deserializedAuthenticator);

			// using PAMELA equality see
			// [Equality computing support](./pamela-core/10-equality_computing.md)
			boolean result = authenticator.equalsObject(deserializedAuthenticator);
			assertEquals(true, result);

		} catch (Exception e) {
			// THEN
			fail(e.getMessage());
		}
	}

	@Test
	@TestOrder(1)
	public void testDeserializeOneSubjectSucceed() {
		// GIVEN
		subject = (ISubject) pamelaModelFactory.newInstance(ISubject.class, authenticator, ISubject.AUTH_INFO);

		// WHEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(subject, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			ISubject deserializedSubject = (ISubject) pamelaModelFactory.deserialize(fis,
					DeserializationPolicy.EXTENSIVE);

			// THEN
			assertEquals(subject.getAuthInfo(), deserializedSubject.getAuthInfo());

			// using PAMELA equality see
			// [Equality computing support](./pamela-core/10-equality_computing.md)
			boolean result = subject.equalsObject(deserializedSubject);
			assertEquals(true, result);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	// TODO following tests
	@Test
	@TestOrder(1)
	public void testDeserializeOneAuthenticatorWithOneSubjectSucceed() {
		// GIVEN
		authenticator = (IAuthenticator) pamelaModelFactory.newInstance(IAuthenticator.class);
		authenticator.init();
		authenticator.setName("Bob");
		subject = (ISubject) pamelaModelFactory.newInstance(ISubject.class, authenticator, ISubject.AUTH_INFO);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(authenticator, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// WHEN

		try (FileInputStream fis = new FileInputStream(file)) {
			IAuthenticator deserializedAuthenticator = (IAuthenticator) pamelaModelFactory.deserialize(fis,
					DeserializationPolicy.EXTENSIVE);

			// THEN
			assertNotNull(deserializedAuthenticator);
			assertEquals(authenticator.getName(), deserializedAuthenticator.getName());
			assertNotSame(authenticator, deserializedAuthenticator);

			boolean result = authenticator.equalsObject(deserializedAuthenticator);
			assertEquals(true, result);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
