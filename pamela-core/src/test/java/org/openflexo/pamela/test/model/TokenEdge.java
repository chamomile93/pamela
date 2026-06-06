package org.openflexo.pamela.test.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.test.model.impl.TokenEdgeImpl;

@ModelEntity
@XMLElement(xmlTag = "TokenEdge")
@ImplementationClass(TokenEdgeImpl.class)
public interface TokenEdge extends Edge {

	@Override
	@Getter(PROCESS)
	public FlexoProcess getProcess();

}
