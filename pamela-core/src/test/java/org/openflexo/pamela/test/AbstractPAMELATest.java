/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.pamela.test;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelEntityLibrary;
import org.openflexo.pamela.test.model.AbstractNode;
import org.openflexo.pamela.test.model.FlexoProcess;
import org.openflexo.pamela.test.model.StartNode;
import org.openflexo.pamela.test.model.TestModelObject;
import org.openflexo.pamela.test.model.TokenEdge;
import org.openflexo.pamela.test.model.WKFObject;

import junit.framework.TestCase;

public abstract class AbstractPAMELATest extends TestCase {

	//TODO pull up the factory and pamelametamodel as they are becoming more general

	/**
	 * Little hack to access the library clear() method. This is only for testing purposes.
	 */
	protected void clearModelEntityLibrary() {
		ModelEntityLibrary.clear();
		PamelaMetaModelLibrary.clearCache();
	}

	protected void validateBasicModelContext(PamelaMetaModel pamelaMetaModel) throws ModelDefinitionException {
		ModelEntity<TestModelObject> aTestModelObjectEntity = pamelaMetaModel.getModelEntity(TestModelObject.class);
		ModelEntity<FlexoProcess> aProcessEntity = pamelaMetaModel.getModelEntity(FlexoProcess.class);
		ModelEntity<AbstractNode> aAbstractNodeEntity = pamelaMetaModel.getModelEntity(AbstractNode.class);
		ModelEntity<StartNode> aStartNodeEntity = pamelaMetaModel.getModelEntity(StartNode.class);
		ModelEntity<TokenEdge> aTokenEdgeEntity = pamelaMetaModel.getModelEntity(TokenEdge.class);
		ModelEntity<WKFObject> aWorkFlowObjectEntity = pamelaMetaModel.getModelEntity(WKFObject.class);

		assertNotNull(aProcessEntity);
		assertNotNull(aAbstractNodeEntity);
		assertNotNull(aStartNodeEntity);
		assertNotNull(aTokenEdgeEntity);
		assertNotNull(aWorkFlowObjectEntity);

		assertNotNull(aTestModelObjectEntity.getModelProperty(TestModelObject.FLEXO_ID));
		assertNotNull(aTestModelObjectEntity.getModelProperty(TestModelObject.NAME));
		
		assertNull(aTestModelObjectEntity.getModelProperty(TestModelObject.DELETED));

		// see that properties are inherited
		assertNotNull(aProcessEntity.getModelProperty(FlexoProcess.FLEXO_ID));
		assertNotNull(aProcessEntity.getModelProperty(FlexoProcess.NAME));
		
		assertNull(aProcessEntity.getModelProperty(FlexoProcess.DELETED));
		
		assertNotNull(aProcessEntity.getModelProperty(FlexoProcess.PROCESS));
		assertNotNull(aProcessEntity.getModelProperty(FlexoProcess.NODES));
		assertNotNull(aProcessEntity.getModelProperty(FlexoProcess.FOO));

		assertNotNull(aAbstractNodeEntity.getModelProperty(WKFObject.FLEXO_ID));
		assertNotNull(aAbstractNodeEntity.getModelProperty(WKFObject.NAME));
		
		assertNull(aAbstractNodeEntity.getModelProperty(WKFObject.DELETED));
		
		assertNotNull(aAbstractNodeEntity.getModelProperty(WKFObject.PROCESS));
		assertNotNull(aAbstractNodeEntity.getModelProperty(AbstractNode.INCOMING_EDGES));
		assertNotNull(aAbstractNodeEntity.getModelProperty(AbstractNode.OUTGOING_EDGES));
		assertNotNull(aAbstractNodeEntity.getModelProperty(AbstractNode.MASTER_ANNOTATION));
		assertNotNull(aAbstractNodeEntity.getModelProperty(AbstractNode.OTHER_ANNOTATIONS));
		
		assertNotNull(aAbstractNodeEntity.getModelProperty(WKFObject.PROCESS).getInverseProperty(aProcessEntity));
		assertNotNull(aAbstractNodeEntity.getModelProperty(WKFObject.PROCESS).getSetter());
		
		// TODO check that the models have all properties covered
		// TODO property of StartNode
		// TODO property of TokenEdge

		assertNotNull(aWorkFlowObjectEntity.getModelProperty(TestModelObject.FLEXO_ID));
		assertNotNull(aWorkFlowObjectEntity.getModelProperty(TestModelObject.FLEXO_ID).getSetter());

		assertNotNull(aWorkFlowObjectEntity.getModelProperty(WKFObject.PROCESS));
		assertNull(aWorkFlowObjectEntity.getModelProperty(WKFObject.PROCESS).getInverseProperty(aProcessEntity));
		assertNotNull(aWorkFlowObjectEntity.getModelProperty(WKFObject.PROCESS).getSetter());

		assertTrue(aTestModelObjectEntity.getAllDescendants(pamelaMetaModel).contains(aProcessEntity));

		// TODO check that adding this may not break the unit where this method
		// "validateBasicModelContext" is used ?
		assertTrue(aTestModelObjectEntity.getAllDescendants(pamelaMetaModel).contains(aAbstractNodeEntity));
		assertTrue(aTestModelObjectEntity.getAllDescendants(pamelaMetaModel).contains(aStartNodeEntity));
		assertTrue(aTestModelObjectEntity.getAllDescendants(pamelaMetaModel).contains(aTokenEdgeEntity));
		assertTrue(aTestModelObjectEntity.getAllDescendants(pamelaMetaModel).contains(aWorkFlowObjectEntity));
	}
}
