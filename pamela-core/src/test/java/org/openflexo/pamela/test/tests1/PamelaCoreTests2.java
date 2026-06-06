package org.openflexo.pamela.test.tests1;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.function.ThrowingRunnable;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.UnitializedEntityException;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.pamela.factory.EmbeddingType;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.test.AbstractPAMELATest;
import org.openflexo.pamela.test.model.AbstractNode;
import org.openflexo.pamela.test.model.ActivityNode;
import org.openflexo.pamela.test.model.Edge;
import org.openflexo.pamela.test.model.EndNode;
import org.openflexo.pamela.test.model.FlexoProcess;
import org.openflexo.pamela.test.model.StartNode;
import org.openflexo.pamela.test.model.TokenEdge;
import org.openflexo.pamela.test.model.WKFAnnotation;
import org.openflexo.toolbox.FileUtils;

/**
 * Basic tests regarding a sample PAMELA model
 *
 * @author sylvain
 *
 */
public class PamelaCoreTests2 extends AbstractPAMELATest {

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
		file = File.createTempFile("PamelaCoreTests1", ".xml");

		clearModelEntityLibrary();
		if (factory == null) {
			pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(FlexoProcess.class);
			factory = new PamelaModelFactory(pamelaMetaModel);
		}
		aProcessInitialized = factory.newInstance(FlexoProcess.class);
		aProcessInitialized.init("234XX");
		aProcessInitialized.setName("NewProcess");
		aProcessInitialized.setFoo(8);

		anotherProcessNotInitialized = factory.newInstance(FlexoProcess.class);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// TODO should reset the library ?
		clearModelEntityLibrary();
		// file.delete();
	}

	/**
	 * We declare here a basic mapping model, and we check that the model
	 * construction is right
	 *
	 * @throws Exception
	 */
	public void testShouldConstructCorrectSimplePamelaMetaModel() throws Exception {
		System.out.println(pamelaMetaModel.debug());
		assertEquals(11, pamelaMetaModel.getEntityCount());

		// why countEntity returns "11" ? I would expect one entity,
		// namely a FlexoProcess that is not correct, since other entity are referenced
		assertFalse(Objects.equals(1, pamelaMetaModel.getEntityCount()));

		validateBasicModelContext(pamelaMetaModel);
	}

	public void testShouldNotRaiseAnExceptionAfterProccesIsInitialized() throws Exception {
		// GIVEN
		FlexoProcess aProcess = factory.newInstance(FlexoProcess.class);
		try {
			aProcess.init("234XX");
			aProcess.getName();
			// WHEN
		} catch (UnitializedEntityException e) {
		}
		// THEN
		// TODO move to another test this has nothing to do with the current
		// assertTrue(aProcess instanceof FlexoProcess);
		// assertEquals("NewProcess", aProcess.getName());
		// assertEquals("234XX", aProcess.getFlexoID());
		// assertEquals(8, aProcess.getFoo());
	}

	public void testShouldRaiseExceptionWhenGetNameBeforeProcessIsInitialized() throws Exception {
		// GIVEN
		try {
			anotherProcessNotInitialized.getName();
			fail("getName() must not be invoke until init() has been called");
		} catch (UnitializedEntityException e) {
		}
	}

	public void testShouldThrowsAnExceptionWhenGetNameBeforeProcessIsInitialized() throws Exception {
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

	public void testShouldThrowsAnExceptionWhenSetNameBeforeProcessIsInitialized() throws Exception {
		// GIVEN
		FlexoProcess anotherProcessNotInitialized = factory.newInstance(FlexoProcess.class);
		Assert.assertThrows(UnitializedEntityException.class,
				new ThrowingRunnable() {
					@Override
					public void run() throws Throwable {
						anotherProcessNotInitialized.setName("NewProcess");
					}
				});
	}

	// TODO rename the test
	public void testInitializedActivityHasDefaultValueAndSettingPropertyWorksAndEmbeddingWorksAndContainmentWorksAndParentsWorks()
			throws Exception {
		ActivityNode activityNode = factory.newInstance(ActivityNode.class);
		activityNode.init();

		assertTrue(activityNode instanceof ActivityNode);
		assertEquals("0000", activityNode.getFlexoID());

		activityNode.setFlexoID("1");
		assertEquals("1", activityNode.getFlexoID());

		activityNode.setName("MyActivity");
		assertEquals("MyActivity", activityNode.getName());

		aProcessInitialized.addToNodes(activityNode);
		assertTrue(aProcessInitialized.getNodes().contains(activityNode));

		assertEquals(aProcessInitialized, activityNode.getProcess());
	}

	// TODO fix
	public void testShouldConstructCorrectProcessWithNodesAndEdgesAndSDemonstrateThatReturnedValueSucceedandSerialize()
			throws Exception {

		ActivityNode activityNode = factory.newInstance(ActivityNode.class);
		activityNode.init();

		StartNode startNode = factory.newInstance(StartNode.class);

		// TODO the following comment might not hold anymore since I split the test
		// TODO why id=3? my expectation was id=2 since this seems to be an internal
		// identifier generated on object creation and this is the second object created
		// after the process itself but here id=2 is an OutgoingTokenEdge being a
		// successor to this node id=3

		startNode.init();
		startNode.setName("Start");
		aProcessInitialized.addToNodes(startNode);

		EndNode endNode = factory.newInstance(EndNode.class);
		endNode.init();
		endNode.setName("End");
		aProcessInitialized.addToNodes(endNode);

		// TODO the following comment might not hold anymore since I split the test
		// TODO id=5 idem, expect id=3
		// maybe this has to do with the way nodes are "serialized" ? doesn't seem to
		// hold,
		// if this was the case that this is DFS then
		// the first incomingTokenEdge not yet created at this point would have id=2
		// but in the xml doesn't have one, that's because it doesn't belong to this
		// activity node but is a "reference" thus the "idref=2" in the xml which refer
		// to the edge with id=2 but "belong to" the startNode above
		// and moreover the node after is id=4; this is normal, since it was affected
		// has "belong to" this "activity node" and there's not "idRef" on this one.
		// that does not follow.

		TokenEdge edge1 = (TokenEdge) factory.newInstance(TokenEdge.class).init(startNode, activityNode);
		edge1.setName("edge1");
		assertEquals(aProcessInitialized, edge1.getProcess());
		startNode.addToOutgoingEdges(edge1);
		assertEquals(aProcessInitialized, edge1.getProcess());
		activityNode.addToIncomingEdges(edge1);
		assertEquals(aProcessInitialized, edge1.getProcess());

		TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);
		// TODO idf why this needs to be cast to the type when the previous invocation
		// did not ?
		// TokenEdge edge2 = (TokenEdge)
		// factory.newInstance(TokenEdge.class).init("edge2", activityNode, endNode);

		// TODO is this redundant with the newInstance constructors with args given per
		// PAMELA semantic ?
		// TODO what could have been the "intention" of this in this test ?
		edge2.setStartNode(activityNode);
		edge2.setEndNode(endNode);

		// Why the actual is not equal the process ? because the "@ReturnedValue" of an
		// Edge says to return the value of the startNode which is "activityNode", which
		// is null.
		assertNotEquals(edge2.getProcess(), aProcessInitialized);

		// TODO the following might not be appropriate to have in the test
		try (FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml")) {
			factory.serialize(aProcessInitialized, fos);
			fos.flush();
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testShouldCompareDeserializationAreEqualsWithNonBreakingChangesOnModel() throws Exception {
		// TODO what is this testing ?
		// TODO might be too long, should be split in several tests,
		// each testing a specific aspect of the model construction and serialization

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		// TODO why ini this one and not the others ?
		// endNode.init();
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		aProcessInitialized.addToNodes(activityNode);
		aProcessInitialized.addToNodes(startNode);
		aProcessInitialized.addToNodes(endNode);

		WKFAnnotation annotation1 = factory.newInstance(WKFAnnotation.class, "Annotation 1");
		WKFAnnotation annotation2 = factory.newInstance(WKFAnnotation.class, "Annotation 2");
		startNode.setMasterAnnotation(annotation1);
		startNode.addToOtherAnnotations(annotation2);

		// TODO why test this here ?
		// assertEquals("MyActivity", activityNode.getName());
		// assertEquals("Start", startNode.getName());
		// assertEquals("End", endNode.getName());
		// assertEquals(activityNode, edge2.getStartNode());
		// assertTrue(activityNode.getOutgoingEdges().contains(edge2));
		// assertEquals(1, startNode.getOutgoingEdges().size());
		// assertEquals(1, activityNode.getOutgoingEdges().size());

		// TODO there is no need to have three serialization as this try to test
		// something else than serialization=deserialization
		
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(aProcessInitialized, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		String anXml = FileUtils.fileContents(file);

		edge2.setStartNode(startNode);

		// TODO idf why test this here ?
		// assertEquals(startNode, edge2.getStartNode());
		// assertTrue(startNode.getOutgoingEdges().contains(edge2));
		// assertEquals(2, startNode.getOutgoingEdges().size());
		// assertEquals(0, activityNode.getOutgoingEdges().size());

		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(aProcessInitialized, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		String anotherXml = FileUtils.fileContents(file);
		assertNotEquals(anXml, anotherXml);

		// TODO that's weird but why not
		activityNode.addToOutgoingEdges(edge2);

		// TODO why test this here ?
		// with the above this should belong to another test case

		// assertEquals(activityNode, edge2.getStartNode());
		// assertFalse(startNode.getOutgoingEdges().contains(edge2));
		// assertTrue(activityNode.getOutgoingEdges().contains(edge2));
		// assertEquals(1, startNode.getOutgoingEdges().size());
		// assertEquals(1, activityNode.getOutgoingEdges().size());

		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(aProcessInitialized, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		String anotherXmlToo = FileUtils.fileContents(file, "UTF-8");

		// TODO are these tests necessary regarding our current test ?
		// TODO maybe split this
		assertNotEquals(anotherXml, anotherXmlToo);
		assertEquals(anXml, anotherXmlToo);

		// TODO why did they named this a test4 ?
		// test4

		try (FileInputStream fis = new FileInputStream(file)) {
			aProcessInitialized = (FlexoProcess) factory.deserialize(fis);
		} catch (Exception e) {
			// TODO fails because procces is not initialized, why ?
			fail(e.getMessage());
		}

		assertTrue(aProcessInitialized instanceof FlexoProcess);
		assertEquals("NewProcess", aProcessInitialized.getName());
		assertEquals("234XX", aProcessInitialized.getFlexoID());
		assertEquals(8, aProcessInitialized.getFoo());

		activityNode = (ActivityNode) aProcessInitialized.getNodeNamed("MyActivity");

		assertNotNull(activityNode);
		assertEquals("MyActivity", activityNode.getName());
		assertTrue(aProcessInitialized.getNodes().contains(activityNode));
		assertEquals(aProcessInitialized, activityNode.getProcess());

		startNode = (StartNode) aProcessInitialized.getNodeNamed("Start");

		assertNotNull(startNode);
		assertNotNull(startNode.getMasterAnnotation());
		assertEquals("Annotation 1", startNode.getMasterAnnotation().getText());
		assertEquals(1, startNode.getOtherAnnotations().size());

		endNode = (EndNode) aProcessInitialized.getNodeNamed("End");

		assertNotNull(endNode);

		edge1 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge1");

		assertNotNull(edge1);

		edge2 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge2");

		assertNotNull(edge2);
		assertEquals(aProcessInitialized, edge1.getProcess());
		assertEquals(aProcessInitialized, edge2.getProcess());
		assertEquals(activityNode, edge2.getStartNode());
		assertTrue(activityNode.getOutgoingEdges().contains(edge2));
		assertEquals(1, startNode.getOutgoingEdges().size());
		assertEquals(1, activityNode.getOutgoingEdges().size());
	}

	public void testDeletionAfterDeserializationShouldSucceed() throws Exception {
		// TODO refactor this test with another one that serialize the appropriate model

		File file = new File("/tmp/foo");
		try (FileInputStream fis = new FileInputStream(file)) {
			aProcessInitialized = (FlexoProcess) factory.deserialize(fis);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		ActivityNode activityNode = (ActivityNode) aProcessInitialized.getNodeNamed("MyActivity");
		StartNode startNode = (StartNode) aProcessInitialized.getNodeNamed("Start");
		WKFAnnotation annotation1 = startNode.getMasterAnnotation();
		WKFAnnotation annotation2 = startNode.getOtherAnnotations().get(0);
		TokenEdge edge1 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge1");
		TokenEdge edge2 = (TokenEdge) aProcessInitialized.getEdgeNamed("edge2");

		assertTrue(activityNode.getIncomingEdges().contains(edge1));

		startNode.delete();

		assertTrue(startNode.isDeleted());
		assertTrue(edge1.isDeleted());
		assertTrue(annotation1.isDeleted());
		assertTrue(annotation2.isDeleted());
		assertNull(aProcessInitialized.getNodeNamed("Start"));
		assertNull(aProcessInitialized.getEdgeNamed("edge1"));
		assertTrue(!activityNode.getIncomingEdges().contains(edge1));
	}

	/**
	 * Testing getEmbeddedObjects()
	 *
	 * @throws Exception
	 */
	public void test6() throws Exception {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		// No embedded objects for a simple node (edges let the closure fail)
		List<Object> embeddedObjects1 = factory.getEmbeddedObjects(startNode, EmbeddingType.CLOSURE);
		System.out.println("Embedded: " + embeddedObjects1);
		assertEquals(0, embeddedObjects1.size());

		// The 3 nodes and the 2 edges belongs to same closure, take them
		List<Object> embeddedObjectsFromProcess = factory.getEmbeddedObjects(process, EmbeddingType.CLOSURE);
		System.out.println("Embedded: " + embeddedObjectsFromProcess);

		assertEquals(5, embeddedObjectsFromProcess.size());
		assertTrue(embeddedObjectsFromProcess.contains(activityNode));
		assertTrue(embeddedObjectsFromProcess.contains(startNode));
		assertTrue(embeddedObjectsFromProcess.contains(endNode));
		assertTrue(embeddedObjectsFromProcess.contains(edge1));
		assertTrue(embeddedObjectsFromProcess.contains(edge2));

		// Computes embedded objects for activity node in the context of process
		// In this case, edge1 and edge2 are also embedded because belonging to supplied
		// context which is the process itself
		List<Object> embeddedObjectsFromActivityNodeOfProcess = factory.getEmbeddedObjects(activityNode,
				EmbeddingType.CLOSURE, process);
		System.out.println("Embedded: " + embeddedObjectsFromActivityNodeOfProcess);

		assertEquals(2, embeddedObjectsFromActivityNodeOfProcess.size());
		assertTrue(embeddedObjectsFromActivityNodeOfProcess.contains(edge1));
		assertTrue(embeddedObjectsFromActivityNodeOfProcess.contains(edge2));

		// Computes embedded objects for activity node in the context of node startNode
		// In this case, edge1 is also embedded because belonging to supplied
		// context which is the opposite node startNode
		List<Object> embeddedObjectsFromActivityNodeOfStartingNode = factory.getEmbeddedObjects(activityNode,
				EmbeddingType.CLOSURE, startNode);
		System.out.println("Embedded: " + embeddedObjectsFromActivityNodeOfStartingNode);

		assertEquals(1, embeddedObjectsFromActivityNodeOfStartingNode.size());
		assertTrue(embeddedObjectsFromActivityNodeOfStartingNode.contains(edge1));
	}

	/**
	 * Testing cloning
	 *
	 * @throws Exception
	 */
	public void test7() throws Exception {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		// TODO observe the result of this test
		File file = File.createTempFile("PAMELA.test7", ".xml");
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		TokenEdge edge2 = factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		FlexoProcess processCopy = (FlexoProcess) process.cloneObject();
		System.out.println("processCopy=" + processCopy);

		assertNotNull(processCopy);
		assertTrue(processCopy instanceof FlexoProcess);
		// TODO: Uncomment next line when FACTORY strategy will be implemented
		// assertEquals("NewProcess1", processCopy.getName());
		assertEquals("234XX", processCopy.getFlexoID());
		assertEquals(8, processCopy.getFoo());

		ActivityNode activityNodeCopy = (ActivityNode) processCopy.getNodeNamed("MyActivity");
		assertNotNull(activityNodeCopy);
		assertEquals("MyActivity", activityNodeCopy.getName());
		assertTrue(processCopy.getNodes().contains(activityNodeCopy));
		assertEquals(processCopy, activityNodeCopy.getProcess());
		StartNode startNodeCopy = (StartNode) processCopy.getNodeNamed("Start");
		assertNotNull(startNodeCopy);
		EndNode endNodeCopy = (EndNode) processCopy.getNodeNamed("End");
		assertNotNull(endNodeCopy);
		TokenEdge edge1Copy = (TokenEdge) processCopy.getEdgeNamed("edge1");
		assertNotNull(edge1Copy);
		TokenEdge edge2Copy = (TokenEdge) processCopy.getEdgeNamed("edge2");
		assertNotNull(edge2Copy);
		assertEquals(processCopy, edge1Copy.getProcess());
		assertEquals(processCopy, edge2Copy.getProcess());
		assertEquals(startNodeCopy, edge1Copy.getStartNode());
		assertEquals(activityNodeCopy, edge1Copy.getEndNode());
		assertEquals(activityNodeCopy, edge2Copy.getStartNode());
		assertEquals(endNodeCopy, edge2Copy.getEndNode());
		assertTrue(activityNodeCopy.getOutgoingEdges().contains(edge2Copy));
		assertEquals(1, startNodeCopy.getOutgoingEdges().size());
		assertEquals(1, activityNodeCopy.getOutgoingEdges().size());

		assertNotSame(edge1, edge1Copy);
		assertNotSame(edge2, edge2Copy);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(processCopy, fos);
			// TODO why after assertion ? does it help init certain fields of the model at
			// compile time ? the same thing is done in test below for cloning, pasting
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			file.delete();
		}
	}

	/**
	 * Testing cloning (with and without context)
	 *
	 * @throws Exception
	 */
	public void test8() throws Exception {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		// Var unused TokenEdge edge1 =
		factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		// Var unused TokenEdge edge2 =
		factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		// Clone activityNode, edge1 and edge2 will be cloned as their
		// related property @CloningStrategy is flagged as CLONE
		ActivityNode activityNodeCopy = (ActivityNode) activityNode.cloneObject();
		System.out.println("activityNodeCopy=" + activityNodeCopy);
		System.out.println(debug(activityNodeCopy));

		assertEquals(1, activityNodeCopy.getIncomingEdges().size());
		TokenEdge edge1Copy = (TokenEdge) activityNodeCopy.getIncomingEdges().get(0);
		assertEquals("edge1", edge1Copy.getName());

		assertEquals(1, activityNodeCopy.getOutgoingEdges().size());
		TokenEdge edge2Copy = (TokenEdge) activityNodeCopy.getOutgoingEdges().get(0);
		assertEquals("edge2", edge2Copy.getName());

		// Clone activityNode in the context of process, edge1 and edge2 will be cloned
		// because they belong to process' context
		ActivityNode activityNodeCopy2 = (ActivityNode) activityNode.cloneObject(process);
		System.out.println("activityNodeCopy2=" + activityNodeCopy2);
		System.out.println(debug(activityNodeCopy2));

		assertEquals(1, activityNodeCopy2.getIncomingEdges().size());
		TokenEdge edge1Copy2 = (TokenEdge) activityNodeCopy2.getIncomingEdges().get(0);
		assertEquals("edge1", edge1Copy2.getName());

		assertEquals(1, activityNodeCopy2.getOutgoingEdges().size());
		TokenEdge edge2Copy2 = (TokenEdge) activityNodeCopy2.getOutgoingEdges().get(0);
		assertEquals("edge2", edge2Copy2.getName());

		// Clone activityNode in the context of startNode, only edge1 will be cloned
		ActivityNode activityNodeCopy3 = (ActivityNode) activityNode.cloneObject(startNode);
		System.out.println("activityNodeCopy3=" + activityNodeCopy3);
		System.out.println(debug(activityNodeCopy3));

		assertEquals(1, activityNodeCopy3.getIncomingEdges().size());
		TokenEdge edge1Copy3 = (TokenEdge) activityNodeCopy3.getIncomingEdges().get(0);
		assertEquals("edge1", edge1Copy3.getName());

		assertEquals(0, activityNodeCopy3.getOutgoingEdges().size());
	}

	/**
	 * Testing copy paste
	 *
	 * @throws Exception
	 */
	public void test9() throws Exception {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		// Var unused TokenEdge edge1 =
		factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		// Var unused TokenEdge edge2 =
		factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		Clipboard clipboard = factory.copy(activityNode);
		System.out.println("Clipboard 1");
		System.out.println(debug(clipboard.getSingleContents()));
		assertTrue(clipboard.isSingleObject());
		assertTrue(clipboard.getSingleContents() instanceof ActivityNode);
		assertEquals("MyActivity", ((ActivityNode) clipboard.getSingleContents()).getName());
		assertEquals(0, ((ActivityNode) clipboard.getSingleContents()).getIncomingEdges().size());
		assertEquals(0, ((ActivityNode) clipboard.getSingleContents()).getOutgoingEdges().size());

		Object pasted = factory.paste(clipboard, process);
		assertNotNull(pasted);
		assertTrue(pasted instanceof ActivityNode);
		System.out.println(debug(process));
		assertEquals(4, process.getNodes().size());
		assertTrue(((List<?>) process.getNodesNamed("MyActivity")).contains(pasted));
		ActivityNode newNode = (ActivityNode) pasted;
		assertEquals(0, newNode.getIncomingEdges().size());
		assertEquals(0, newNode.getOutgoingEdges().size());
		try (FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml")) {
			factory.serialize(process, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Testing copy paste
	 *
	 * @throws Exception
	 */
	public void test10() throws Exception {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		// Var unused TokenEdge edge1 =
		factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		// Var unused TokenEdge edge2 =
		factory.newInstance(TokenEdge.class, "edge2", activityNode, endNode);

		Clipboard clipboard = factory.copy(startNode, activityNode);
		System.out.println("Clipboard");
		System.out.println(debug(clipboard.getMultipleContents()));
		assertFalse(clipboard.isSingleObject());
		assertTrue(clipboard.getMultipleContents() instanceof List);
		assertEquals(2, (clipboard.getMultipleContents()).size());
		assertTrue(clipboard.getMultipleContents().get(0) instanceof StartNode);
		assertTrue(clipboard.getMultipleContents().get(1) instanceof ActivityNode);
		StartNode copiedStartNode = (StartNode) clipboard.getMultipleContents().get(0);
		ActivityNode copiedActivityNode = (ActivityNode) clipboard.getMultipleContents().get(1);
		assertEquals("Start", copiedStartNode.getName());
		assertEquals("MyActivity", copiedActivityNode.getName());
		assertEquals(0, copiedStartNode.getIncomingEdges().size());
		assertEquals(1, copiedStartNode.getOutgoingEdges().size());
		assertEquals(1, copiedActivityNode.getIncomingEdges().size());
		assertEquals(0, copiedActivityNode.getOutgoingEdges().size());
		assertSame(copiedStartNode.getOutgoingEdges().get(0), copiedActivityNode.getIncomingEdges().get(0));

		Object pasted = factory.paste(clipboard, process);
		assertNotNull(pasted);
		assertTrue(pasted instanceof List);

		System.out.println(clipboard.debug());

		System.out.println(debug(process));
		assertEquals(5, process.getNodes().size());
		ActivityNode newActivity = null;
		StartNode newStartNode = null;
		for (Object o : (List<?>) pasted) {
			if (o instanceof ActivityNode) {
				newActivity = (ActivityNode) o;
			} else if (o instanceof StartNode) {
				newStartNode = (StartNode) o;
			}
		}
		assertEquals(1, newActivity.getIncomingEdges().size());
		assertEquals(0, newActivity.getOutgoingEdges().size());
		assertEquals(0, newStartNode.getIncomingEdges().size());
		assertEquals(1, newStartNode.getOutgoingEdges().size());
		assertSame(newStartNode.getOutgoingEdges().get(0), newActivity.getIncomingEdges().get(0));
		try (FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml")) {
			factory.serialize(process, fos);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testModify() {
		// TODO too long, should be split in several tests, each testing a specific
		// aspect of the model construction and serialization
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		assertTrue(process.isModified());
		process.setName("NewProcess");
		assertTrue(process.isModified());
		process.setFoo(8);
		assertTrue(process.isModified());

		serializeObject(process);
		assertFalse(process.isModified());

		ActivityNode activityNode = factory.newInstance(ActivityNode.class);
		assertFalse(activityNode.isModified());
		// Here we verify that if we use an initializer, the object is marked as
		// modified
		ActivityNode activityNode2 = factory.newInstance(ActivityNode.class, "MyActivity");
		assertTrue(activityNode2.isModified());

		process.addToNodes(activityNode);
		assertTrue(process.isModified());
		serializeObject(process);
		assertFalse(process.isModified());
		assertFalse(activityNode.isModified());
		activityNode.setName("Coucou");
		assertTrue(activityNode.isModified());
		assertTrue(process.isModified());// Here we verify the forward state
		serializeObject(process);
		assertFalse(activityNode.isModified());// Here we verify the synch forward state

		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		serializeObject(process);
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		assertTrue(process.isModified());// Here we verify the forward state
		assertTrue(activityNode.isModified());

		activityNode.removeFromIncomingEdges(edge1);

		assertNull(edge1.getEndNode());

		process.removeFromNodes(activityNode);

		serializeObject(process);

		assertFalse(process.isModified());// Here we verify that process has been marked as not-modified (by the
											// serialization mechanism)
		assertTrue(activityNode.isModified()); // And that activity node is no longer synched with its previous
												// container process.
	}

	public void testDeletion() {
		FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("234XX");
		process.setName("NewProcess");
		process.setFoo(8);

		ActivityNode activityNode = factory.newInstance(ActivityNode.class, "MyActivity");
		process.addToNodes(activityNode);
		StartNode startNode = factory.newInstance(StartNode.class, "Start");
		process.addToNodes(startNode);
		EndNode endNode = factory.newInstance(EndNode.class, "End");
		process.addToNodes(endNode);
		TokenEdge edge1 = factory.newInstance(TokenEdge.class, "edge1", startNode, activityNode);
		System.out.println("Deleting " + startNode);
		try {
			startNode.delete();
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
		assertTrue(startNode.isDeleted());
		assertFalse(edge1.isDeleted());
		activityNode.delete();
		assertTrue(edge1.isDeleted());
		assertTrue(activityNode.isDeleted());
	}

	private void serializeObject(AccessibleProxyObject object) {
		try {
			factory.serialize(object, new ByteArrayOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	public String debug(Object o) {
		if (o instanceof AbstractNode) {
			AbstractNode node = (AbstractNode) o;
			StringBuffer returned = new StringBuffer();
			returned.append("------------------- " + o + " -------------------\n");
			List<Edge> inEdges = node.getIncomingEdges();
			if (inEdges != null) {
				for (Object e : inEdges) {
					if (e == null) {
						returned.append("null     Incoming: " + null + "\n");
					} else {
						returned.append(Integer.toHexString(e.hashCode()) + " Incoming: " + e + "\n");
					}
				}
			}
			List<Edge> outEdges = node.getOutgoingEdges();
			if (outEdges != null) {
				for (Object e : outEdges) {
					if (e == null) {
						returned.append("null     Outgoing: " + null + "\n");
					} else {
						returned.append(Integer.toHexString(e.hashCode()) + " Outgoing: " + e + "\n");
					}
				}
			}
			return returned.toString();
		}

		if (o instanceof Edge) {
			Edge edge = (Edge) o;
			StringBuffer returned = new StringBuffer();
			returned.append("------------------- " + edge + " -------------------\n");
			returned.append("From: " + edge.getStartNode() + "\n");
			returned.append("To: " + edge.getEndNode() + "\n");
			return returned.toString();
		}

		if (o instanceof FlexoProcess) {
			FlexoProcess process = (FlexoProcess) o;
			StringBuffer returned = new StringBuffer();
			returned.append("=================== " + process + " ===================\n");
			for (AbstractNode node : process.getNodes()) {
				returned.append(debug(node));
			}
			return returned.toString();
		}

		if (o instanceof List) {
			StringBuffer returned = new StringBuffer();
			for (Object o2 : (List<?>) o) {
				returned.append(debug(o2));
			}
			return returned.toString();
		}

		return o.toString();
	}

}
