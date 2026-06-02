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
import org.openflexo.pamela.securitypatterns.authenticator.persistance.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.persistance.ISubject;
import org.openflexo.pamela.securitypatterns.authenticator.persistance.IAuthenticator.AuthenticatorImp;
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
		PamelaMetaModelLibrary.clearCache(); // TODO watchout this has been added while debugging
		ModelEntityLibrary.clear();
		// exception raised by SerializationTests.
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

		System.out.println(pamelaModelFactory.stringRepresentation(authenticator));
		// WHEN

		try (FileInputStream fis = new FileInputStream(file)) {
			IAuthenticator deserializedAuthenticator = (IAuthenticator) pamelaModelFactory.deserialize(fis,
					DeserializationPolicy.EXTENSIVE);

			assertNotNull(deserializedAuthenticator);
			assertEquals(authenticator, deserializedAuthenticator); // by value
			assertNotSame(authenticator, deserializedAuthenticator); // by referenc
			assertEquals(authenticator.getName(), deserializedAuthenticator.getName());
			
			// using PAMELA equality see [Equality computing support](./pamela-core/10-equality_computing.md)
			boolean result = authenticator.equalsObject(deserializedAuthenticator);
			assertEquals(true,result);

			System.out.println(AuthenticatorImp.DESERIALIZATION_TRACE);
		} catch (Exception e) {
			// THEN
			fail(e.getMessage());
		}

		// TODO

		// assertEquals(
		// " BEGIN:Root BEGIN:Node1 BEGIN:Node2 BEGIN:Node21 BEGIN:Node22 BEGIN:Node23
		// BEGIN:Node3 END:Root END:Node1 END:Node2 END:Node21 END:Node22 END:Node23
		// END:Node3",
		// NodeImpl.DESERIALIZATION_TRACE);
	}

	@Test
	@TestOrder(1)
	public void testDeserializeOneSubjectSucceed() {
		// GIVEN
		subject = (ISubject) pamelaModelFactory.newInstance(ISubject.class, authenticator, ISubject.AUTH_INFO);
		subject.init(authenticator, ISubject.AUTH_INFO);
		subject.setIdProof(42);
		// TODO which other value could be demonstrated for serialization ?

		// WHEN

		try (FileInputStream fis = new FileInputStream(file)) {
			// TODO
			// rootNode = (Node) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (Exception e) {
			// THEN
			fail(e.getMessage());
		}

		// TODO
		// System.out.println(NodeImpl.DESERIALIZATION_TRACE);

		// assertEquals(
		// " BEGIN:Root BEGIN:Node1 BEGIN:Node2 BEGIN:Node21 BEGIN:Node22 BEGIN:Node23
		// BEGIN:Node3 END:Root END:Node1 END:Node2 END:Node21 END:Node22 END:Node23
		// END:Node3",
		// NodeImpl.DESERIALIZATION_TRACE);
	}

	@Test
	@TestOrder(1)
	public void testDeserializeOneAuthenticatorWithOneSubjectSucceed() {
		// GIVEN

		// WHEN

		try (FileInputStream fis = new FileInputStream(file)) {
			// TODO
			// rootNode = (Node) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (Exception e) {
			// THEN
			fail(e.getMessage());
		}

		// TODO
		// assertNotNull(rootNode);

		// TODO
		// System.out.println(NodeImpl.DESERIALIZATION_TRACE);

		// assertEquals(
		// " BEGIN:Root BEGIN:Node1 BEGIN:Node2 BEGIN:Node21 BEGIN:Node22 BEGIN:Node23
		// BEGIN:Node3 END:Root END:Node1 END:Node2 END:Node21 END:Node22 END:Node23
		// END:Node3",
		// NodeImpl.DESERIALIZATION_TRACE);
	}
}
