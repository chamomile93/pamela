package org.openflexo.pamela.test.tests4;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in overriden getter/setter context
 * 
 * @author xtof
 * 
 */
// TODO I don't understand why this test exists alone.
// TODO Why there's a need for more than one overriden method for this test ?
// TODO why introduce another method "setContentURI"?
public class PamelaCoreTests4 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {
		// TODO what other test could be done on the factory ?
		// testing nonnull reference is kind of simple
		try {
			PamelaModelFactory myContainerContentsfactory = new PamelaModelFactory(
					PamelaMetaModelLibrary.retrieveMetaModel(MyContainer.class, MyContents.class));

			ModelEntity<MyContainer> myContainerEntity = myContainerContentsfactory.getPamelaMetaModel()
					.getModelEntity(MyContainer.class);
			ModelEntity<MyContents> myContentsEntity = myContainerContentsfactory.getPamelaMetaModel()
					.getModelEntity(MyContents.class);

			assertNotNull(myContainerEntity);
			assertNotNull(myContentsEntity);

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test the diagram factory
	 */
	@Test
	public void testInstanciate() throws Exception {

		PamelaModelFactory myContainerContentsfactory = new PamelaModelFactory(
				PamelaMetaModelLibrary.retrieveMetaModel(MyContainer.class, MyContents.class));

		MyContainer container1 = myContainerContentsfactory.newInstance(MyContainer.class);
		MyContainer container2 = myContainerContentsfactory.newInstance(MyContainer.class);
		((MyContainerImpl) container1).setFactory(myContainerContentsfactory);
		((MyContainerImpl) container2).setFactory(myContainerContentsfactory);

		try {
			container1.setContents("Bonjour");

			container1.setContents("Au revoir");

			container1.setContents("A demain");

			container2.setContentURI("Content://Je suis méchant");

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// TODO is the following comment still relevant ?
		// TODO : this should be assertTrue!!

		assertTrue(container1.getContents().equals("A demain"));

		assertTrue(container2.getContents().equals("Je suis méchant"));

		System.out.println("Et au final je dis:" + container1.getContents());
		System.out.println("Et au final je dis:" + container2.getContents());

	}

}
