package org.openflexo.pamela.securitypatterns.authenticator.persistance.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.RestrictiveSerializationException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.SerializationPolicy;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.securitypatterns.authenticator.model.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model.ISubject;

import junit.framework.TestCase;

/*
	* Kind of tests being done in this class, see yet to be tested at the end of the file :
	* Round-trip: OK
*/
public class SerializationTests extends TestCase {

	private File file;
	private PamelaModelFactory pamelaModelFactory;

	private IAuthenticator authenticator;
	private ISubject subject;

	@Override
	@Before
	public void setUp() throws IOException, ModelDefinitionException {
		file = File.createTempFile("PAMELA-TestSerialization", ".xml");

		clearModelEntityLibrary(); // TODO is this necessary ?

		PamelaMetaModel context = PamelaMetaModelLibrary.retrieveMetaModel(ISubject.class, IAuthenticator.class);
		/**
		 * TODO as mentionned in {@link AuthenticatorPatternDefinition}
		 *
		 */
		pamelaModelFactory = new PamelaModelFactory(context);

		authenticator = (IAuthenticator) pamelaModelFactory.newInstance(IAuthenticator.class);
		authenticator.init();
		authenticator.setName("Bob");

		subject = (ISubject) pamelaModelFactory.newInstance(ISubject.class, authenticator, ISubject.AUTH_INFO);
		// subject.init(authenticator, ISubject.AUTH_INFO); //TODO is the authenticator given redundant with the newInstance above ?
		// TODO which other value could be demonstrated for serialization ?
	}

	protected void clearModelEntityLibrary() {
		PamelaMetaModelLibrary.clearCache();// TODO watchout this has been added while debugging an exception
											// raised by SerializationTests.
		ModelEntityLibrary.clear();
	}

	@Override
	public void tearDown() {
		// file.delete();
	}

	@Test
	public void testExtensiveSerializationWithZeroAuthenticatorAndOneSubjectSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.EXTENSIVE;

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(subject, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);
		}
		// NotExpected
		catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + subject.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testExtensiveSerializationWithOneAuthenticatorAndZeroSubjectSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.EXTENSIVE;

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(authenticator, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);
		}
		// NotExpected
		catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + authenticator.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testExtensiveSerializationWithOneAuthenticatorAndOneSubjectSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.EXTENSIVE;
		//TODO change this to the subject instead of the AuthInfo()
		authenticator.addUser(subject);

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(authenticator, fos, policy, true);
			// EXPECT
			assertTrue(file.length() > 0);
		}
		// NotExpected
		catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + authenticator.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRestrictiveSerializationWithOneAuthenticatorAndZeroSubjectsSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.RESTRICTIVE;

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pamelaModelFactory.serialize(authenticator, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);

			// NotExpected
		} catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + authenticator.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	// TODO perhaps this test does not make much sense as the type ISubject is know from the IAuthenticator at compile time from the PamelaMetaModelLibrary and not serialized alone. This need to be thought over and is not the current priority.
	// @Test
	// public void testRestrictiveSerializationOfOneAuthenticatorWithOneSubjectFails() throws Exception {
	// 	// GIVEN
	// 	SerializationPolicy policy = SerializationPolicy.RESTRICTIVE;
	// 	authenticator.addUser(subject);

	// 	// THEN
	// 	try (FileOutputStream fos = new FileOutputStream(file)) {
	// 		pamelaModelFactory.serialize(subject, fos, policy, true);

	// 		// TODO the error has changed to a test failure as the serizalization happens to
	// 		// write the subject when it should not ?
	// 		// TODO FIX this raises an exception because of a null Converter ~ not defined
	// 		// for the type of IAuthenticator or supertype. And I don't follow how a
	// 		// Converter is defined and retrieved by TypeUtils. For this test I might need
	// 		// to understand how TypeUtils, and Types defined in Connie package, is made to
	// 		// work. I found a class that defines some type for Java Primitive types, but I
	// 		// don't understand how it works for complex types, and I haven't found
	// 		// documentation about this yet.

	// 		// EXPECT
	// 		String message = policy.toString() + " serialization should not be allowed for "
	// 				+ subject.getClass().getName();
	// 		fail(message);
	// 		assertThrows(RestrictiveSerializationException.class, () -> {
	// 			pamelaModelFactory.serialize(subject, new FileOutputStream(file), policy, true);
	// 		});
	// 	} catch (RestrictiveSerializationException e) {
	// 	} catch (Exception e) {
	// 		// NotExpected
	// 		fail(e.getMessage());
	// 	}
	// }

	// @Test
	// public void testSerializationExtensiveFails() throws Exception {
	// // TODO does the following holds ?
	// // process.addToNodes(createNode());
	// // assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);
	// }

	// @Test
	// public void testSerializationRestrictiveSucceed() throws Exception {
	// // TODO is there a case where this succeed ?
	// // assertSerializationFails(process, SerializationPolicy.RESTRICTIVE);
	// }

	// @Test
	// public void testDeserializationExtensiveSucceed() throws Exception {
	// flexoProcessFactory = new PamelaModelFactory(FlexoProcess.class);
	// assertRestrictiveDeserializationFails();

	// TODO move deserialization tests to the DeserializationTests class
	// process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // Keep the file serialized with the extensive policy so the round-trip stays
	// // stable.
	// assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);

	// TODO move deserialization tests to the DeserializationTests class
	// flexoProcessFactory = new PamelaModelFactory(FlexoProcess.class);
	// process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// Assert.assertNull(flexoProcessFactory.getModelContext().getModelEntity(MyNode.class));
	// Assert.assertNotNull(myNodeFactory.getModelContext().getModelEntity(MyNode.class));
	// validateBasicModelContext(flexoProcessFactory.getModelContext());
	// validateBasicModelContext(myNodeFactory.getModelContext());
	// Assert.assertEquals(1, process.getNodes().size());
	// AbstractNode aNode = process.getNodeNamed(NODE_NAME);
	// Assert.assertTrue(aNode instanceof MyNode);
	// Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	// }

	// @Test
	// public void testDeserializationExtensiveFails() throws Exception {
	// // TODO is there a failing case? the following instructions still holds ?
	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// // assertRestrictiveDeserializationFails();

	// TODO move deserialization tests to the DeserializationTests class
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // // Keep the file serialized with the extensive policy so the round-trip
	// stays
	// // stable.
	// // assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);

	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// TODO move deserialization tests to the DeserializationTests class
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // Assert.assertNull(factory.getModelContext().getModelEntity(MyNode.class));
	// //
	// Assert.assertNotNull(factory2.getModelContext().getModelEntity(MyNode.class));
	// // validateBasicModelContext(factory.getModelContext());
	// // validateBasicModelContext(factory2.getModelContext());
	// // Assert.assertEquals(1, process.getNodes().size());
	// // AbstractNode aNode = process.getNodeNamed(NODE_NAME);
	// // Assert.assertTrue(aNode instanceof MyNode);
	// // Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	// }

	// @Test
	// public void testDeserializationRestrictiveFails() throws Exception {
	// // TODO is there a failing case? the following instructions holds ?
	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// // assertRestrictiveDeserializationFails();

	// TODO move deserialization tests to the DeserializationTests class
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // // Keep the file serialized with the extensive policy so the round-trip
	// stays
	// // stable.
	// // assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);

	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // Assert.assertNull(factory.getModelContext().getModelEntity(MyNode.class));
	// //
	// Assert.assertNotNull(factory2.getModelContext().getModelEntity(MyNode.class));
	// // validateBasicModelContext(factory.getModelContext());
	// // validateBasicModelContext(factory2.getModelContext());
	// // Assert.assertEquals(1, process.getNodes().size());
	// // AbstractNode aNode = process.getNodeNamed(NODE_NAME);
	// // Assert.assertTrue(aNode instanceof MyNode);
	// // Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	// }

	// @Test
	// public void testDeserializationRestrictiveSucceed() throws Exception {
	// // TODO is there a passing case? the following instructions holds ?
	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// // assertRestrictiveDeserializationFails();

	// TODO move deserialization tests to the DeserializationTests class
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // // Keep the file serialized with the extensive policy so the round-trip
	// stays
	// // stable.
	// // assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);

	// // factory = new PamelaModelFactory(FlexoProcess.class);
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);

	// // Assert.assertNull(factory.getModelContext().getModelEntity(MyNode.class));
	// //
	// Assert.assertNotNull(factory2.getModelContext().getModelEntity(MyNode.class));
	// // validateBasicModelContext(factory.getModelContext());
	// // validateBasicModelContext(factory2.getModelContext());
	// // Assert.assertEquals(1, process.getNodes().size());
	// // AbstractNode aNode = process.getNodeNamed(NODE_NAME);
	// // Assert.assertTrue(aNode instanceof MyNode);
	// // Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	// }

	// private void assertRestrictiveDeserializationFails() throws Exception {
	// try (FileInputStream fis = new FileInputStream(file)) {
	// flexoProcessFactory.deserialize(fis, DeserializationPolicy.RESTRICTIVE);
	// Assert.fail(
	// "Restrictive deserialization should not allow the deserialization of a " +
	// MyNode.class.getName());
	// } catch (RestrictiveDeserializationException e) {
	// // Expected
	// }
	// }

	// private FlexoProcess deserializeProcess(DeserializationPolicy policy) throws
	// Exception {
	// try (FileInputStream fis = new FileInputStream(file)) {
	// return (FlexoProcess) flexoProcessFactory.deserialize(fis, policy);
	// } catch (RestrictiveDeserializationException e) {
	// Assert.fail("Extensive deserialization should allow the deserialization of a
	// " + MyNode.class.getName());
	// return null;
	// }
	// }

	/*
	 * TODO things to test:
	 * // Type/contract: Verify classes meant to be serializable implement
	 * Serializable (or required interface) and have stable serialVersionUID when
	 * applicable
	 * // Object graph identity & cycles: Serialize graphs with shared references
	 * and cyclic references; assert object identity preserved (a == b for shared
	 * refs) and no stack overflow.
	 * // Transient / defaults / optional fields: Ensure transient fields are not
	 * persisted, defaults are applied on read, and optional/missing fields
	 * deserialize safely.
	 * // Schema evolution (backwards compatibility): Test older serialized data
	 * against current classes and current serialized data against older class
	 * versions (where possible). Assert no data loss for compatible changes and
	 * graceful handling for incompatible ones.
	 * // Forward-compatibility / new fields: Deserialize containing extra/new
	 * fields (e.g., XML) and verify unknown fields are ignored or handled per spec.
	 * // Malformed / invalid input handling: Feed truncated/corrupted streams and
	 * unexpected types; assert proper exceptions, no resource leaks or crashes.
	 * // Version/Cross-JVM compatibility: Round-trip between JVM versions (or
	 * different runtime builds) and across platforms if relevant; assert
	 * compatibility for on-disk or networked serialized data.
	 * // Custom serializer behavior: If using custom (de)serializers — e.g.,
	 * Jackson, XStream, protobuf, or Pamela-specific — test hooks
	 * (readResolve/writeReplace), custom adapters, and annotated rules.
	 * // Reference security / deserialization hardening: Test against known
	 * gadget/attack patterns if deserializing untrusted input; verify
	 * allowlists/blocklists and safe object construction
	 * // Performance & memory: Serialize/deserialize very large graphs and measure
	 * time and memory; assert acceptable throughput and no OOMs.
	 * // Concurrency: Serialize/deserialize same objects concurrently from multiple
	 * threads; assert thread-safety or document required external synchronization.
	 * // Fuzz / randomized testing: Generate random valid/invalid data to discover
	 * edge cases.
	 * // Persistence/integration: Write serialized blobs to the real persistence
	 * layer (files, DB, network), reload across app restarts, and assert integrity.
	 */
}
