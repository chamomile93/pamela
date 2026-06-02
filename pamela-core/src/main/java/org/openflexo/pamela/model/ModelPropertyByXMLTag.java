package org.openflexo.pamela.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openflexo.toolbox.StringUtils;

/**
 * Used in the context of XML serialization/deserialization
 * 
 * @author sylvain
 *
 * @param <I>
 */
@Deprecated(since="Added for testing purposes, to not be used, add not functionality.", forRemoval = true)
// this is a wrapper that name the extended concept with a more appropriate name for me.
public class ModelPropertyByXMLTag<I> extends ModelPropertyXMLTag<I> {

	//TODO use this class instead of ModelPropertyByXMLTag in the rest of the code
	public ModelPropertyByXMLTag(ModelProperty<? super I> property) {
		super(property);
	}

	public ModelPropertyByXMLTag(ModelProperty<? super I> property, ModelEntity<?> accessedEntity) {
		super(property, accessedEntity);
	}

	@Override
	public String toString() {
		return "ModelPropertyByXMLTag_" + getAccessedEntity() + "_" + getProperty() + "/tag=" + getTag();
	}
}
