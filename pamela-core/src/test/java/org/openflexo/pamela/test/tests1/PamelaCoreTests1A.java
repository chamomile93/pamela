package org.openflexo.pamela.test.tests1;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.function.ThrowingRunnable;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.UnitializedEntityException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.test.AbstractPAMELATest;
import org.openflexo.pamela.test.model.ActivityNode;
import org.openflexo.pamela.test.model.EndNode;
import org.openflexo.pamela.test.model.FlexoProcess;
import org.openflexo.pamela.test.model.StartNode;
import org.openflexo.pamela.test.model.TokenEdge;
import org.openflexo.pamela.test.model.WKFAnnotation;

/**
 * Basic tests regarding a sample PAMELA model
 *
 * @author chamomille93
 *
 */
public class PamelaCoreTests1A extends AbstractPAMELATest {

	private static final int GIVEN_FOO_VALUE = 42;
	private static final String GIVEN_PROCESS_NAME = "NewProcess";
	private static final String GIVEN_FLEXO_ID = "234XX";
	private File file;
	private PamelaModelFactory factory;
	private PamelaMetaModel pamelaMetaModel;
	private FlexoProcess aProcessInitialized;
	private FlexoProcess anotherProcessNotInitialized;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new File("/tmp").mkdirs();
		PamelaMetaModelLibrary.clearCache();
		ModelEntityLibrary.clear();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {

		file = File.createTempFile("PamelaCoreTests2", ".xml");

		clearModelEntityLibrary();
		pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(FlexoProcess.class);
		factory = new PamelaModelFactory(pamelaMetaModel);

		aProcessInitialized = factory.newInstance(FlexoProcess.class);
		aProcessInitialized.init(GIVEN_FLEXO_ID);
		aProcessInitialized.setName(GIVEN_PROCESS_NAME);
		aProcessInitialized.setFoo(GIVEN_FOO_VALUE);

		anotherProcessNotInitialized = factory.newInstance(FlexoProcess.class);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// TODO is this redundant ?
		clearModelEntityLibrary();
		// file.delete();
	}

	/**
	 * We declare here a basic mapping model, and we check that the model
	 * construction is right
	 *
	 * @throws Exception
	 */
	public void testShouldConstructCorrectSimplePamelaMetaModelSucceed() throws Exception {
		// System.out.println(pamelaMetaModel.debug());
		int expectedEntityCount = 11;
		assertEquals(expectedEntityCount, pamelaMetaModel.getEntityCount());

		// why countEntity returns "11" ? I would expect one entity,
		// namely a FlexoProcess that is not correct, since other entity are referenced
		expectedEntityCount = 1;
		assertFalse(Objects.equals(expectedEntityCount, pamelaMetaModel.getEntityCount()));

		validateBasicModelContext(pamelaMetaModel);
	}

	public void testShouldNotRaiseAnExceptionAfterProccesIsInitializedSucceed() throws Exception {
		// GIVEN
		FlexoProcess aProcess = factory.newInstance(FlexoProcess.class);
		try {
			String givenFlexoId = "234X1";
			aProcess.init(givenFlexoId);
			aProcess.getName();
			// WHEN
		} catch (UnitializedEntityException e) {
		}
	}

	public void testShouldRaiseExceptionWhenGetNameBeforeProcessIsInitializedSucceed() throws Exception {
		// GIVEN
		try {
			anotherProcessNotInitialized.getName();
			fail("getName() must not be invoke until init() has been called");
		} catch (UnitializedEntityException e) {
		}
	}

	public void testShouldThrowsAnExceptionWhenGetNameBeforeProcessIsInitializedSucceed() throws Exception {
		// GIVEN
		FlexoProcess anotherProcessNotInitialized = factory.newInstance(FlexoProcess.class);
		Assert.assertThrows(UnitializedEntityException.class,
				new ThrowingRunnable() {
					@Override
					public void run() throws Throwable {
						anotherProcessNotInitialized.getName();
					}
				});
	}

	public void testShouldThrowsAnExceptionWhenSetNameBeforeProcessIsInitializedSucceed() throws Exception {
		// GIVEN
		FlexoProcess anotherProcessNotInitialized = factory.newInstance(FlexoProcess.class);
		Assert.assertThrows(UnitializedEntityException.class,
				new ThrowingRunnable() {
					@Override
					public void run() throws Throwable {
						anotherProcessNotInitialized.setName(GIVEN_PROCESS_NAME);
					}
				});
	}

	// TODO rename the test
	public void testInitializedActivityHasDefaultValueAndSettingPropertyWorksAndEmbeddingWorksAndContainmentWorksAndParentsSucceed()
			throws Exception {
		ActivityNode activityNode = factory.newInstance(ActivityNode.class);
		activityNode.init();

		assertTrue(activityNode instanceof ActivityNode);
		String expectedDefaultFlexoId = "0000";
		assertEquals(expectedDefaultFlexoId, activityNode.getFlexoID());

		String aFlexoID = "1";
		activityNode.setFlexoID(aFlexoID);
		assertEquals(aFlexoID, activityNode.getFlexoID());

		String activityName = "MyActivity";
		activityNode.setName(activityName);
		assertEquals(activityName, activityNode.getName());

		aProcessInitialized.addToNodes(activityNode);
		assertTrue(aProcessInitialized.getNodes().contains(activityNode));

		assertEquals(aProcessInitialized, activityNode.getProcess());
	}

	// TODO rename and maybe refactor
	public void testShouldConstructCorrectProcessWithNodesAndEdgesAndSDemonstrateThatReturnedValueSucceed()
			throws Exception {

		String givenActivityNodeName = "MyActivity";
		ActivityNode activityNode = factory.newInstance(ActivityNode.class, givenActivityNodeName);
		String givenStartNodeName = "Start";
		StartNode startNode = factory.newInstance(StartNode.class, givenStartNodeName);
		String givenEndNodeName = "End";
		EndNode endNode = factory.newInstance(EndNode.class, givenEndNodeName);
		String givenEdgeName = "edge1";
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, givenEdgeName, startNode, activityNode);
		String givenEdgeAnotherName = "edge2";
		TokenEdge edge2 = factory.newInstance(TokenEdge.class, givenEdgeAnotherName, activityNode, endNode);

		aProcessInitialized.addToNodes(startNode);
		aProcessInitialized.addToNodes(endNode);
		assertEquals(aProcessInitialized, edge1.getProcess());

		startNode.addToOutgoingEdges(edge1);
		assertEquals(aProcessInitialized, edge1.getProcess());

		activityNode.addToIncomingEdges(edge1);
		assertEquals(aProcessInitialized, edge1.getProcess());

		edge2.setStartNode(activityNode);
		edge2.setEndNode(endNode);

		assertNotEquals(edge2.getProcess(), aProcessInitialized);
	}

	public void testDeserializationEqualsSerializationSucceed() throws Exception {
		// GIVEN
		String givenActivityNodeName = "MyActivity";
		ActivityNode activityNode = factory.newInstance(ActivityNode.class, givenActivityNodeName);
		String givenStartNodeName = "Start";
		StartNode startNode = factory.newInstance(StartNode.class, givenStartNodeName);
		String givenEndNodeName = "End";
		EndNode endNode = factory.newInstance(EndNode.class, givenEndNodeName);
		String givenEdgeName = "edge1";
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, givenEdgeName, startNode, activityNode);
		String givenEdgeAnotherName = "edge2";
		TokenEdge edge2 = factory.newInstance(TokenEdge.class, givenEdgeAnotherName, activityNode, endNode);

		aProcessInitialized.addToNodes(activityNode);
		aProcessInitialized.addToNodes(startNode);
		aProcessInitialized.addToNodes(endNode);

		String givenAnnotationName = "Annotation 1";
		WKFAnnotation annotation1 = factory.newInstance(WKFAnnotation.class, givenAnnotationName);
		String givenAnnotationAnotherName = "Annotation 2";
		WKFAnnotation annotation2 = factory.newInstance(WKFAnnotation.class, givenAnnotationAnotherName);

		startNode.setMasterAnnotation(annotation1);
		startNode.addToOtherAnnotations(annotation2);

		// WHEN
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(aProcessInitialized, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			aProcessInitialized = (FlexoProcess) factory.deserialize(fis);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// THEN
		activityNode = (ActivityNode) aProcessInitialized.getNodeNamed(givenActivityNodeName);
		startNode = (StartNode) aProcessInitialized.getNodeNamed(givenStartNodeName);
		endNode = (EndNode) aProcessInitialized.getNodeNamed(givenEndNodeName);
		edge1 = (TokenEdge) aProcessInitialized.getEdgeNamed(givenEdgeName);
		edge2 = (TokenEdge) aProcessInitialized.getEdgeNamed(givenEdgeAnotherName);

		assertTrue(aProcessInitialized instanceof FlexoProcess);
		assertEquals(GIVEN_PROCESS_NAME, aProcessInitialized.getName());
		assertEquals(GIVEN_FLEXO_ID, aProcessInitialized.getFlexoID());
		assertEquals(GIVEN_FOO_VALUE, aProcessInitialized.getFoo());

		assertNotNull(activityNode);
		assertEquals(givenActivityNodeName, activityNode.getName());
		assertTrue(aProcessInitialized.getNodes().contains(activityNode));
		assertEquals(aProcessInitialized, activityNode.getProcess());

		assertNotNull(startNode);
		assertNotNull(startNode.getMasterAnnotation());
		assertEquals(givenAnnotationName, startNode.getMasterAnnotation().getText());

		int expectedAnnotationCount = 1;
		assertEquals(expectedAnnotationCount, startNode.getOtherAnnotations().size());

		assertNotNull(endNode);
		assertNotNull(edge1);

		int expectedEdgeCount = 1;
		assertNotNull(edge2);
		assertEquals(aProcessInitialized, edge1.getProcess());
		assertEquals(aProcessInitialized, edge2.getProcess());
		assertEquals(activityNode, edge2.getStartNode());
		assertTrue(activityNode.getOutgoingEdges().contains(edge2));
		assertEquals(expectedEdgeCount, startNode.getOutgoingEdges().size());
		assertEquals(expectedEdgeCount, activityNode.getOutgoingEdges().size());
	}

	// public void testDeletionAfterDeserializationShouldSucceed() throws Exception
	// {
	// // TODO refactor this test with another one that serialize the appropriate
	// model

	// File file = new File("/tmp/foo");
	// try (FileInputStream fis = new FileInputStream(file)) {
	// aProcessInitialized = (FlexoProcess) factory.deserialize(fis);
	// } catch (Exception e) {
	// fail(e.getMessage());
	// }

	// ActivityNode activityNode = (ActivityNode)
	// aProcessInitialized.getNodeNamed("MyActivity");
	// StartNode startNode = (StartNode) aProcessInitialized.getNodeNamed("Start");
	// WKFAnnotation annotation1 = startNode.getMasterAnnotation();
	// WKFAnnotation annotation2 = startNode.getOtherAnnotations().get(0);
	// TokenEdge edge1 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge1");
	// TokenEdge edge2 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge2");

	// assertTrue(activityNode.getIncomingEdges().contains(edge1));

	// startNode.delete();

	// assertTrue(startNode.isDeleted());
	// assertTrue(edge1.isDeleted());
	// assertTrue(annotation1.isDeleted());
	// assertTrue(annotation2.isDeleted());
	// assertNull(aProcessInitialized.getNodeNamed("Start"));
	// assertNull(aProcessInitialized.getEdgeNamed("edge1"));
	// assertTrue(!activityNode.getIncomingEdges().contains(edge1));
	// }

	// /**
	// * Testing getEmbeddedObjects()
	// *
	// * @throws Exception
	// */
	// public void test6() throws Exception {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode,
	// activityNode);
	// TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode,
	// endNode);

	// // No embedded objects for a simple node (edges let the closure fail)
	// List<Object> embeddedObjects1 = factory.getEmbeddedObjects(startNode,
	// EmbeddingType.CLOSURE);
	// System.out.println("Embedded: " + embeddedObjects1);
	// assertEquals(0, embeddedObjects1.size());

	// // The 3 nodes and the 2 edges belongs to same closure, take them
	// List<Object> embeddedObjectsFromProcess = factory.getEmbeddedObjects(process,
	// EmbeddingType.CLOSURE);
	// System.out.println("Embedded: " + embeddedObjectsFromProcess);

	// assertEquals(5, embeddedObjectsFromProcess.size());
	// assertTrue(embeddedObjectsFromProcess.contains(activityNode));
	// assertTrue(embeddedObjectsFromProcess.contains(startNode));
	// assertTrue(embeddedObjectsFromProcess.contains(endNode));
	// assertTrue(embeddedObjectsFromProcess.contains(edge1));
	// assertTrue(embeddedObjectsFromProcess.contains(edge2));

	// // Computes embedded objects for activity node in the context of process
	// // In this case, edge1 and edge2 are also embedded because belonging to
	// supplied
	// // context which is the process itself
	// List<Object> embeddedObjectsFromActivityNodeOfProcess =
	// factory.getEmbeddedObjects(activityNode,
	// EmbeddingType.CLOSURE, process);
	// System.out.println("Embedded: " + embeddedObjectsFromActivityNodeOfProcess);

	// assertEquals(2, embeddedObjectsFromActivityNodeOfProcess.size());
	// assertTrue(embeddedObjectsFromActivityNodeOfProcess.contains(edge1));
	// assertTrue(embeddedObjectsFromActivityNodeOfProcess.contains(edge2));

	// // Computes embedded objects for activity node in the context of node
	// startNode
	// // In this case, edge1 is also embedded because belonging to supplied
	// // context which is the opposite node startNode
	// List<Object> embeddedObjectsFromActivityNodeOfStartingNode =
	// factory.getEmbeddedObjects(activityNode,
	// EmbeddingType.CLOSURE, startNode);
	// System.out.println("Embedded: " +
	// embeddedObjectsFromActivityNodeOfStartingNode);

	// assertEquals(1, embeddedObjectsFromActivityNodeOfStartingNode.size());
	// assertTrue(embeddedObjectsFromActivityNodeOfStartingNode.contains(edge1));
	// }

	// /**
	// * Testing cloning
	// *
	// * @throws Exception
	// */
	// public void test7() throws Exception {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// // TODO observe the result of this test
	// File file = File.createTempFile("PAMELA.test7", ".xml");
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode,
	// activityNode);
	// TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode,
	// endNode);

	// FlexoProcess processCopy = (FlexoProcess) process.cloneObject();
	// System.out.println("processCopy=" + processCopy);

	// assertNotNull(processCopy);
	// assertTrue(processCopy instanceof FlexoProcess);
	// // TODO: Uncomment next line when FACTORY strategy will be implemented
	// // assertEquals("NewProcess1", processCopy.getName());
	// assertEquals("234XX", processCopy.getFlexoID());
	// assertEquals(8, processCopy.getFoo());

	// ActivityNode activityNodeCopy = (ActivityNode)
	// processCopy.getNodeNamed("MyActivity");
	// assertNotNull(activityNodeCopy);
	// assertEquals("MyActivity", activityNodeCopy.getName());
	// assertTrue(processCopy.getNodes().contains(activityNodeCopy));
	// assertEquals(processCopy, activityNodeCopy.getProcess());
	// StartNode startNodeCopy = (StartNode) processCopy.getNodeNamed("Start");
	// assertNotNull(startNodeCopy);
	// EndNode endNodeCopy = (EndNode) processCopy.getNodeNamed("End");
	// assertNotNull(endNodeCopy);
	// TokenEdge edge1Copy = (TokenEdge) processCopy.getEdgeNamed("edge1");
	// assertNotNull(edge1Copy);
	// TokenEdge edge2Copy = (TokenEdge) processCopy.getEdgeNamed("edge2");
	// assertNotNull(edge2Copy);
	// assertEquals(processCopy, edge1Copy.getProcess());
	// assertEquals(processCopy, edge2Copy.getProcess());
	// assertEquals(startNodeCopy, edge1Copy.getStartNode());
	// assertEquals(activityNodeCopy, edge1Copy.getEndNode());
	// assertEquals(activityNodeCopy, edge2Copy.getStartNode());
	// assertEquals(endNodeCopy, edge2Copy.getEndNode());
	// assertTrue(activityNodeCopy.getOutgoingEdges().contains(edge2Copy));
	// assertEquals(1, startNodeCopy.getOutgoingEdges().size());
	// assertEquals(1, activityNodeCopy.getOutgoingEdges().size());

	// assertNotSame(edge1, edge1Copy);
	// assertNotSame(edge2, edge2Copy);
	// try (FileOutputStream fos = new FileOutputStream(file)) {
	// factory.serialize(processCopy, fos);
	// // TODO why after assertion ? does it help init certain fields of the model
	// at
	// // compile time ? the same thing is done in test below for cloning, pasting
	// } catch (FileNotFoundException e) {
	// fail(e.getMessage());
	// } catch (IOException e) {
	// fail(e.getMessage());
	// } finally {
	// file.delete();
	// }
	// }

	// /**
	// * Testing cloning (with and without context)
	// *
	// * @throws Exception
	// */
	// public void test8() throws Exception {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// // Var unused TokenEdge edge1 =
	// factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
	// // Var unused TokenEdge edge2 =
	// factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

	// // Clone activityNode, edge1 and edge2 will be cloned as their
	// // related property @CloningStrategy is flagged as CLONE
	// ActivityNode activityNodeCopy = (ActivityNode) activityNode.cloneObject();
	// System.out.println("activityNodeCopy=" + activityNodeCopy);
	// System.out.println(debug(activityNodeCopy));

	// assertEquals(1, activityNodeCopy.getIncomingEdges().size());
	// TokenEdge edge1Copy = (TokenEdge) activityNodeCopy.getIncomingEdges().get(0);
	// assertEquals("edge1", edge1Copy.getName());

	// assertEquals(1, activityNodeCopy.getOutgoingEdges().size());
	// TokenEdge edge2Copy = (TokenEdge) activityNodeCopy.getOutgoingEdges().get(0);
	// assertEquals("edge2", edge2Copy.getName());

	// // Clone activityNode in the context of process, edge1 and edge2 will be
	// cloned
	// // because they belong to process' context
	// ActivityNode activityNodeCopy2 = (ActivityNode)
	// activityNode.cloneObject(process);
	// System.out.println("activityNodeCopy2=" + activityNodeCopy2);
	// System.out.println(debug(activityNodeCopy2));

	// assertEquals(1, activityNodeCopy2.getIncomingEdges().size());
	// TokenEdge edge1Copy2 = (TokenEdge)
	// activityNodeCopy2.getIncomingEdges().get(0);
	// assertEquals("edge1", edge1Copy2.getName());

	// assertEquals(1, activityNodeCopy2.getOutgoingEdges().size());
	// TokenEdge edge2Copy2 = (TokenEdge)
	// activityNodeCopy2.getOutgoingEdges().get(0);
	// assertEquals("edge2", edge2Copy2.getName());

	// // Clone activityNode in the context of startNode, only edge1 will be cloned
	// ActivityNode activityNodeCopy3 = (ActivityNode)
	// activityNode.cloneObject(startNode);
	// System.out.println("activityNodeCopy3=" + activityNodeCopy3);
	// System.out.println(debug(activityNodeCopy3));

	// assertEquals(1, activityNodeCopy3.getIncomingEdges().size());
	// TokenEdge edge1Copy3 = (TokenEdge)
	// activityNodeCopy3.getIncomingEdges().get(0);
	// assertEquals("edge1", edge1Copy3.getName());

	// assertEquals(0, activityNodeCopy3.getOutgoingEdges().size());
	// }

	// /**
	// * Testing copy paste
	// *
	// * @throws Exception
	// */
	// public void test9() throws Exception {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// // Var unused TokenEdge edge1 =
	// factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
	// // Var unused TokenEdge edge2 =
	// factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

	// Clipboard clipboard = factory.copy(activityNode);
	// System.out.println("Clipboard 1");
	// System.out.println(debug(clipboard.getSingleContents()));
	// assertTrue(clipboard.isSingleObject());
	// assertTrue(clipboard.getSingleContents() instanceof ActivityNode);
	// assertEquals("MyActivity", ((ActivityNode)
	// clipboard.getSingleContents()).getName());
	// assertEquals(0, ((ActivityNode)
	// clipboard.getSingleContents()).getIncomingEdges().size());
	// assertEquals(0, ((ActivityNode)
	// clipboard.getSingleContents()).getOutgoingEdges().size());

	// Object pasted = factory.paste(clipboard, process);
	// assertNotNull(pasted);
	// assertTrue(pasted instanceof ActivityNode);
	// System.out.println(debug(process));
	// assertEquals(4, process.getNodes().size());
	// assertTrue(((List<?>) process.getNodesNamed("MyActivity")).contains(pasted));
	// ActivityNode newNode = (ActivityNode) pasted;
	// assertEquals(0, newNode.getIncomingEdges().size());
	// assertEquals(0, newNode.getOutgoingEdges().size());
	// try (FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml")) {
	// factory.serialize(process, fos);
	// } catch (FileNotFoundException e) {
	// fail(e.getMessage());
	// } catch (IOException e) {
	// fail(e.getMessage());
	// }
	// }

	// /**
	// * Testing copy paste
	// *
	// * @throws Exception
	// */
	// public void test10() throws Exception {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// // Var unused TokenEdge edge1 =
	// factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
	// // Var unused TokenEdge edge2 =
	// factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

	// Clipboard clipboard = factory.copy(startNode, activityNode);
	// System.out.println("Clipboard");
	// System.out.println(debug(clipboard.getMultipleContents()));
	// assertFalse(clipboard.isSingleObject());
	// assertTrue(clipboard.getMultipleContents() instanceof List);
	// assertEquals(2, (clipboard.getMultipleContents()).size());
	// assertTrue(clipboard.getMultipleContents().get(0) instanceof StartNode);
	// assertTrue(clipboard.getMultipleContents().get(1) instanceof ActivityNode);
	// StartNode copiedStartNode = (StartNode)
	// clipboard.getMultipleContents().get(0);
	// ActivityNode copiedActivityNode = (ActivityNode)
	// clipboard.getMultipleContents().get(1);
	// assertEquals("Start", copiedStartNode.getName());
	// assertEquals("MyActivity", copiedActivityNode.getName());
	// assertEquals(0, copiedStartNode.getIncomingEdges().size());
	// assertEquals(1, copiedStartNode.getOutgoingEdges().size());
	// assertEquals(1, copiedActivityNode.getIncomingEdges().size());
	// assertEquals(0, copiedActivityNode.getOutgoingEdges().size());
	// assertSame(copiedStartNode.getOutgoingEdges().get(0),
	// copiedActivityNode.getIncomingEdges().get(0));

	// Object pasted = factory.paste(clipboard, process);
	// assertNotNull(pasted);
	// assertTrue(pasted instanceof List);

	// System.out.println(clipboard.debug());

	// System.out.println(debug(process));
	// assertEquals(5, process.getNodes().size());
	// ActivityNode newActivity = null;
	// StartNode newStartNode = null;
	// for (Object o : (List<?>) pasted) {
	// if (o instanceof ActivityNode) {
	// newActivity = (ActivityNode) o;
	// } else if (o instanceof StartNode) {
	// newStartNode = (StartNode) o;
	// }
	// }
	// assertEquals(1, newActivity.getIncomingEdges().size());
	// assertEquals(0, newActivity.getOutgoingEdges().size());
	// assertEquals(0, newStartNode.getIncomingEdges().size());
	// assertEquals(1, newStartNode.getOutgoingEdges().size());
	// assertSame(newStartNode.getOutgoingEdges().get(0),
	// newActivity.getIncomingEdges().get(0));
	// try (FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml")) {
	// factory.serialize(process, fos);
	// } catch (FileNotFoundException e) {
	// fail(e.getMessage());
	// } catch (IOException e) {
	// fail(e.getMessage());
	// }
	// }

	// public void testModify() {
	// // TODO too long, should be split in several tests, each testing a specific
	// // aspect of the model construction and serialization
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// assertTrue(process.isModified());
	// process.setName("NewProcess");
	// assertTrue(process.isModified());
	// process.setFoo(8);
	// assertTrue(process.isModified());

	// serializeObject(process);
	// assertFalse(process.isModified());

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class);
	// assertFalse(activityNode.isModified());
	// // Here we verify that if we use an initializer, the object is marked as
	// // modified
	// ActivityNode activityNode2 = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// assertTrue(activityNode2.isModified());

	// process.addToNodes(activityNode);
	// assertTrue(process.isModified());
	// serializeObject(process);
	// assertFalse(process.isModified());
	// assertFalse(activityNode.isModified());
	// activityNode.setName("Coucou");
	// assertTrue(activityNode.isModified());
	// assertTrue(process.isModified());// Here we verify the forward state
	// serializeObject(process);
	// assertFalse(activityNode.isModified());// Here we verify the synch forward
	// state

	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// serializeObject(process);
	// TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode,
	// activityNode);
	// assertTrue(process.isModified());// Here we verify the forward state
	// assertTrue(activityNode.isModified());

	// activityNode.removeFromIncomingEdges(edge1);

	// assertNull(edge1.getEndNode());

	// process.removeFromNodes(activityNode);

	// serializeObject(process);

	// assertFalse(process.isModified());// Here we verify that process has been
	// marked as not-modified (by the
	// // serialization mechanism)
	// assertTrue(activityNode.isModified()); // And that activity node is no longer
	// synched with its previous
	// // container process.
	// }

	// public void testDeletion() {
	// FlexoProcess process = factory.newInstance(FlexoProcess.class);
	// process.init("234XX");
	// process.setName("NewProcess");
	// process.setFoo(8);

	// ActivityNode activityNode = factory.newInstance(ActivityNode.class,
	// "MyActivity");
	// process.addToNodes(activityNode);
	// StartNode startNode = factory.newInstance(StartNode.class, "Start");
	// process.addToNodes(startNode);
	// EndNode endNode = factory.newInstance(EndNode.class, "End");
	// process.addToNodes(endNode);
	// TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode,
	// activityNode);
	// System.out.println("Deleting " + startNode);
	// try {
	// startNode.delete();
	// } catch (Throwable t) {
	// t.printStackTrace();
	// fail();
	// }
	// assertTrue(startNode.isDeleted());
	// assertFalse(edge1.isDeleted());
	// activityNode.delete();
	// assertTrue(edge1.isDeleted());
	// assertTrue(activityNode.isDeleted());
	// }

	// private void serializeObject(AccessibleProxyObject object) {
	// try {
	// factory.serialize(object, new ByteArrayOutputStream());
	// } catch (Exception e) {
	// e.printStackTrace();
	// fail(e.getMessage());
	// }

	// }

	// public String debug(Object o) {
	// if (o instanceof AbstractNode) {
	// AbstractNode node = (AbstractNode) o;
	// StringBuffer returned = new StringBuffer();
	// returned.append("------------------- " + o + " -------------------\n");
	// List<Edge> inEdges = node.getIncomingEdges();
	// if (inEdges != null) {
	// for (Object e : inEdges) {
	// if (e == null) {
	// returned.append("null Incoming: " + null + "\n");
	// } else {
	// returned.append(Integer.toHexString(e.hashCode()) + " Incoming: " + e +
	// "\n");
	// }
	// }
	// }
	// List<Edge> outEdges = node.getOutgoingEdges();
	// if (outEdges != null) {
	// for (Object e : outEdges) {
	// if (e == null) {
	// returned.append("null Outgoing: " + null + "\n");
	// } else {
	// returned.append(Integer.toHexString(e.hashCode()) + " Outgoing: " + e +
	// "\n");
	// }
	// }
	// }
	// return returned.toString();
	// }

	// if (o instanceof Edge) {
	// Edge edge = (Edge) o;
	// StringBuffer returned = new StringBuffer();
	// returned.append("------------------- " + edge + " -------------------\n");
	// returned.append("From: " + edge.getStartNode() + "\n");
	// returned.append("To: " + edge.getEndNode() + "\n");
	// return returned.toString();
	// }

	// if (o instanceof FlexoProcess) {
	// FlexoProcess process = (FlexoProcess) o;
	// StringBuffer returned = new StringBuffer();
	// returned.append("=================== " + process + " ===================\n");
	// for (AbstractNode node : process.getNodes()) {
	// returned.append(debug(node));
	// }
	// return returned.toString();
	// }

	// if (o instanceof List) {
	// StringBuffer returned = new StringBuffer();
	// for (Object o2 : (List<?>) o) {
	// returned.append(debug(o2));
	// }
	// return returned.toString();
	// }

	// return o.toString();
	// }

}
