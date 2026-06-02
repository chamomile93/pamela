package org.openflexo.pamela.test.persistance.deserialization;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.DeserializationPolicy;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.SerializationPolicy;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.test.persistance.Node;
import org.openflexo.pamela.test.persistance.Node.NodeImpl;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class DeserializationTests {

	private static File file;
	private static PamelaMetaModel pamelaMetaModelLibrary;
	private static PamelaModelFactory factory;

	@BeforeClass
	public static void setUpClass() throws IOException, ModelDefinitionException {
		PamelaMetaModelLibrary.clearCache(); // TODO watchout this has been added by copilot while debugging an
												// exception raised by SerializationTests.
		ModelEntityLibrary.clear();
		file = File.createTempFile("PAMELA-TestDeserialization", ".xml");

		pamelaMetaModelLibrary = PamelaMetaModelLibrary.retrieveMetaModel(Node.class);
		factory = new PamelaModelFactory(pamelaMetaModelLibrary);
	}

	@AfterClass
	public static void tearDownClass() {
		// file.delete();
	}

	@Test
	@TestOrder(1)
	public void testInitializeAModel() {

		Assert.assertNotNull(factory.getPamelaMetaModel().getModelEntity(Node.class));

		Node rootNode = factory.newInstance(Node.class);
		rootNode.setName("Root");

		Node childNode1 = factory.newInstance(Node.class);
		childNode1.setName("Node1");
		rootNode.addToNodes(childNode1);

		Node childNode2 = factory.newInstance(Node.class);
		childNode2.setName("Node2");
		rootNode.addToNodes(childNode2);

		Node childNode3 = factory.newInstance(Node.class);
		childNode3.setName("Node3");
		rootNode.addToNodes(childNode3);

		Node childNode21 = factory.newInstance(Node.class);
		childNode21.setName("Node21");
		childNode2.addToNodes(childNode21);

		Node childNode22 = factory.newInstance(Node.class);
		childNode22.setName("Node22");
		childNode2.addToNodes(childNode22);

		Node childNode23 = factory.newInstance(Node.class);
		childNode23.setName("Node23");
		childNode2.addToNodes(childNode23);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(rootNode, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		System.out.println(factory.stringRepresentation(rootNode));
	}

	@Test
	@TestOrder(2)
	public void testDeserialize() {
		Node rootNode = null;

		try (FileInputStream fis = new FileInputStream(file)) {
			rootNode = (Node) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		assertNotNull(rootNode);

		System.out.println(NodeImpl.DESERIALIZATION_TRACE);

		//TODO c'est original comme manière de tester, est-ce que la raison d'exister de ce "DESERIALIZATION_TRACE" tiens qu'à cette utilisation ?
		assertEquals(
				" BEGIN:Root BEGIN:Node1 BEGIN:Node2 BEGIN:Node21 BEGIN:Node22 BEGIN:Node23 BEGIN:Node3 END:Root END:Node1 END:Node2 END:Node21 END:Node22 END:Node23 END:Node3",
				NodeImpl.DESERIALIZATION_TRACE);

	}
}
