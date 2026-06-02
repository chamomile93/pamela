package org.openflexo.pamela.test.tests3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in overriden getter context
 * 
 * @author sylvain
 * 
 */
public class PamelaCoreTests3 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {

		try {
			PamelaModelFactory mySpecializedContainerContentFactory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(MySpecializedContainer.class,
					MySpecializedContents.class));

			ModelEntity<MyContainer> myContainerEntity = mySpecializedContainerContentFactory.getPamelaMetaModel().getModelEntity(MyContainer.class);
			ModelEntity<MySpecializedContainer> mySpecializedContainerEntity = mySpecializedContainerContentFactory.getPamelaMetaModel().getModelEntity(
					MySpecializedContainer.class);
			ModelEntity<MyContents> myContentsEntity = mySpecializedContainerContentFactory.getPamelaMetaModel().getModelEntity(MyContents.class);
			ModelEntity<MySpecializedContents> mySpecializedContentsEntity = mySpecializedContainerContentFactory.getPamelaMetaModel().getModelEntity(
					MySpecializedContents.class);

			assertNotNull(myContainerEntity);
			assertNotNull(mySpecializedContainerEntity);
			assertNotNull(myContentsEntity);
			assertNotNull(mySpecializedContentsEntity);
			//TODO what other test could be done ?

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

		PamelaModelFactory mySpecializedContainerContentFactory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(MySpecializedContainer.class,
				MySpecializedContents.class));

		MySpecializedContainer mySpecializedContainer = mySpecializedContainerContentFactory.newInstance(MySpecializedContainer.class);
		MySpecializedContents mySpecializedContents = mySpecializedContainerContentFactory.newInstance(MySpecializedContents.class);

		try {
			mySpecializedContainer.setContents(mySpecializedContents);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertEquals(mySpecializedContents, mySpecializedContainer.getContents());

	}

}
