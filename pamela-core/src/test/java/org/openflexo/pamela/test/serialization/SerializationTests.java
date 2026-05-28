package org.openflexo.pamela.test.serialization;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.RestrictiveSerializationException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.SerializationPolicy;
import org.openflexo.pamela.test.AbstractPAMELATest;
import org.openflexo.pamela.test.tests1.FlexoProcess;
import org.openflexo.pamela.test.tests1.MyNode;
import org.openflexo.toolbox.Duration;
import org.openflexo.toolbox.Duration.DurationUnit;
import org.openflexo.toolbox.FileFormat;

/*
	* Kind of tests being done in this class, see yet to be tested at the end of the file :
	* Round-trip: OK
*/
public class SerializationTests extends AbstractPAMELATest {

	private static final String NODE_NAME = "aNode";
	private static final String PROPERTY_VALUE = "aValue";
	private static final Duration DURATION_VALUE = new Duration(10, DurationUnit.SECONDS);
	private static final FileFormat FILEFORMAT_VALUE = FileFormat.JAR;
	private File file;
	private PamelaModelFactory aFlexoProcessPamelaModelFactory;
	private PamelaModelFactory aMyNodePamelaModelFactory;
	/*
	 * TODO at debug I can see that these have two different PamelaMetaModel
	 * references, is it blocking ?
	 *
	 */
	private FlexoProcess process;
	private MyNode node;

	@Override
	@Before
	public void setUp() throws IOException, ModelDefinitionException {
		file = File.createTempFile("PAMELA-TestSerialization", ".xml");

		clearModelEntityLibrary(); // TODO is this necessary ?

		aFlexoProcessPamelaModelFactory = new PamelaModelFactory(FlexoProcess.class);
		aMyNodePamelaModelFactory = new PamelaModelFactory(MyNode.class);
		/*
		 * // TODO why not use `Node` defined in this "package" ? not sure it's really
		 * // important, and would be less confusing to depend on something "outside"
		 * this
		 * // test
		 */
		process = (FlexoProcess) aFlexoProcessPamelaModelFactory.newInstance(FlexoProcess.class);
		process.init();
		/**
		 * // TODO raising an exception when running class-level tests but not on single
		 * // test ?
		 * // TODO why at debug this is not raising an exception ?
		 *
		 * org.openflexo.pamela.exceptions.ModelExecutionException: Could not find
		 * initializer for method public abstract
		 * org.openflexo.pamela.test.tests1.TestModelObject
		 * org.openflexo.pamela.test.tests1.TestModelObject.init(). Make sure that
		 * org.openflexo.pamela.test.tests1.TestModelObject is annotated with
		 * ModelEntity and has been imported.
		 * at
		 * org.openflexo.pamela.model.ModelEntity.getInitializers(ModelEntity.java:995)
		 * at
		 * org.openflexo.pamela.factory.ProxyMethodHandler._invoke(ProxyMethodHandler.
		 * java:502)
		 * at org.openflexo.pamela.factory.ProxyMethodHandler.invoke(ProxyMethodHandler.
		 * java:356)
		 * at org.openflexo.pamela.test.tests1.FlexoProcessImpl_$$_jvst9e_0.init(
		 * FlexoProcessImpl_$$_jvst9e_0.java)
		 * at org.openflexo.pamela.test.serialization.SerializationTests.setUp(
		 * SerializationTests.java:57)
		 */
		node = (MyNode) aMyNodePamelaModelFactory.newInstance(MyNode.class);
		node.init(NODE_NAME);
		/**
		 * // TODO raising an exception when running class-level tests but not on single
		 * // test ?
		 *
		 * org.openflexo.pamela.exceptions.ModelExecutionException:
		 * ModelExecutionException raised because of exception ModelExecutionException
		 * message: Could not find initializer for method public abstract
		 * org.openflexo.pamela.test.tests1.AbstractNode
		 * org.openflexo.pamela.test.tests1.AbstractNode.init(). Make sure that
		 * org.openflexo.pamela.test.tests1.AbstractNode is annotated with ModelEntity
		 * and has been imported.
		 * at org.openflexo.pamela.factory.PamelaModelFactory.newInstance(
		 * PamelaModelFactory.java:472)
		 * at org.openflexo.pamela.factory.PamelaModelFactory.newInstance(
		 * PamelaModelFactory.java:442)
		 * at org.openflexo.pamela.test.serialization.SerializationTests.setUp(
		 * SerializationTests.java:58)
		 */
		node.setMyProperty(PROPERTY_VALUE);
		node.setMyDuration(DURATION_VALUE);
		node.setMyFileformat(FILEFORMAT_VALUE);
		node.setMyLevel(Level.ALL);
	}

	@Override
	public void tearDown() {
		file.delete();
	}

	@Test
	public void testExtensiveSerializationWithOneProcessAndZeroNodesSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.EXTENSIVE;

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			aFlexoProcessPamelaModelFactory.serialize(process, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);
		}
		// NotExpected
		catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + node.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testExtensiveSerializationWithOneProcessAndOneNodeSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.EXTENSIVE;
		process.addToNodes(node);

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			aFlexoProcessPamelaModelFactory.serialize(process, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);
		}
		// NotExpected
		catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + node.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	//TODO an EXTENSIVE fail case showing serialization of two entity with same model mapping
	//TODO an EXTENSIVE fail case showing non-serializable entity (no model mapping)

	//TODO a PERMISSIVE pass case showing serialization of one entity with one model mapping
	//TODO a PERMISSIVE fail case showing serialization of one entity with no model mapping
	//TODO a PERMISSIVE fail case showing serialization of two entity with one model mapping

	@Test
	public void testRestrictiveSerializationWithOneProcessAndZeroNodesSucceed() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.RESTRICTIVE;

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			aFlexoProcessPamelaModelFactory.serialize(process, fos, policy, true);

			// EXPECT
			assertTrue(file.length() > 0);

			// NotExpected
		} catch (RestrictiveSerializationException e) {
			fail(policy.toString() + " serialization should be allowed for " + process.getClass().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRestrictiveSerializationOfOneProcessWithOneNodeFails() throws Exception {
		// GIVEN
		SerializationPolicy policy = SerializationPolicy.RESTRICTIVE;
		process.addToNodes(node);

		// THEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			aFlexoProcessPamelaModelFactory.serialize(process, fos, policy, true);
			
			// EXPECT
			String message = policy.toString() + " serialization should not be allowed for "
					+ node.getClass().getName();
			fail(message);
			assertThrows(RestrictiveSerializationException.class, () -> {
				aFlexoProcessPamelaModelFactory.serialize(process, new FileOutputStream(file), policy, true);
			});
		} catch (RestrictiveSerializationException e) {
		} catch (Exception e) {
			// NotExpected
			fail(e.getMessage());
		}
	}

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

	//TODO move deserialization tests to the DeserializationTests class
	// process = deserializeProcess(DeserializationPolicy.EXTENSIVE);
	
	// // Keep the file serialized with the extensive policy so the round-trip stays
	// // stable.
	// assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);


	//TODO move deserialization tests to the DeserializationTests class
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
	
	//TODO move deserialization tests to the DeserializationTests class
	// // process = deserializeProcess(DeserializationPolicy.EXTENSIVE);
	
	// // // Keep the file serialized with the extensive policy so the round-trip
	// stays
	// // stable.
	// // assertSerializationSucceeds(process, SerializationPolicy.EXTENSIVE);
	
	// // factory = new PamelaModelFactory(FlexoProcess.class);
	//TODO move deserialization tests to the DeserializationTests class
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
	
	//TODO move deserialization tests to the DeserializationTests class
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
	
	//TODO move deserialization tests to the DeserializationTests class
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
