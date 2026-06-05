package org.openflexo.pamela.test.model;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@XMLElement(xmlTag = "StartNode")
public interface StartNode extends EventNode {

}
