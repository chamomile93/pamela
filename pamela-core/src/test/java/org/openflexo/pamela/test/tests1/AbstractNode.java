package org.openflexo.pamela.test.tests1;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter.Cardinality;

@ModelEntity(isAbstract = true) // TODO why debug of a MyNode during SerializationTests brings me first here
								// when instance creation by factory ?
public interface AbstractNode extends WorkFlowObject {

	public static final String OUTGOING_EDGES = "outgoingEdges";
	public static final String INCOMING_EDGES = "incomingEdges";

	public static final String MASTER_ANNOTATION = "masterAnnotation"; // TODO what's the purpose of this ?
	public static final String OTHER_ANNOTATIONS = "otherAnnotations"; // TODO what's the purpose of this ?

	// Like an empty constructor. We don't want to force to use the one-arg init
	// method
	@Override
	@Initializer
	public AbstractNode init();

	// Conveninent method to automatically create an abstract node with a name
	@Override
	@Initializer
	public AbstractNode init(@Parameter(TestModelObject.NAME) String name);

	// Conveninent method to automatically create an abstract node with a name & a
	// flexoID
	@Initializer
	public AbstractNode init(@Parameter(TestModelObject.FLEXO_ID) String flexoID,
			@Parameter(TestModelObject.NAME) String name);

	@Override
	public boolean delete(Object... context);

	@Getter(value = INCOMING_EDGES, cardinality = Cardinality.LIST, inverse = Edge.END_NODE)
	@XMLElement(context = "Incoming")
	@Embedded(closureConditions = { Edge.START_NODE }, deletionConditions = { Edge.START_NODE })
	/* TODO idf , the closureConditions is probably what's mentionned in the
	// document of @Embedded, that I don't understand.
	// i don't seem to be used by the Edge like described in the documentation
	* it seems that the "closureCondition" is declaring that "Edge."
	*/
	@CloningStrategy(StrategyType.CLONE)
	public List<Edge> getIncomingEdges();

	@Setter(INCOMING_EDGES)
	public void setIncomingEdges(List<Edge> edges);

	@Adder(INCOMING_EDGES)
	public void addToIncomingEdges(Edge edge);

	@Remover(INCOMING_EDGES)
	public void removeFromIncomingEdges(Edge edge);

	@Getter(value = OUTGOING_EDGES, cardinality = Cardinality.LIST, inverse = Edge.START_NODE)
	@XMLElement(context = "Outgoing", primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded(closureConditions = { Edge.END_NODE }, deletionConditions = { Edge.END_NODE })
	/* TODO idf , the closureConditions is probably what's mentionned in the
	// document of @Embedded, that I don't understand.
	// i don't seem to be used by the Edge like described in the documentation
	*/
	public List<Edge> getOutgoingEdges();

	@Setter(OUTGOING_EDGES)
	public void setOutgoingEdges(List<Edge> edges);

	@Adder(OUTGOING_EDGES)
	public void addToOutgoingEdges(Edge edge);

	@Remover(OUTGOING_EDGES)
	public void removeFromOutgoingEdges(Edge edge);


	@Getter(value = PROCESS, inverse = FlexoProcess.NODES)
	@Override
	public FlexoProcess getProcess();

	@Getter(MASTER_ANNOTATION)
	@XMLAttribute
	@Embedded
	public WorkFlowAnnotation getMasterAnnotation();

	@Setter(MASTER_ANNOTATION)
	public void setMasterAnnotation(WorkFlowAnnotation a);

	@Getter(value = OTHER_ANNOTATIONS, cardinality = Cardinality.LIST)
	@XMLElement()
	@Embedded
	public List<WorkFlowAnnotation> getOtherAnnotations();

	@Setter(OTHER_ANNOTATIONS)
	public void setOtherAnnotations(List<WorkFlowAnnotation> as);

	@Adder(OTHER_ANNOTATIONS)
	public void addToOtherAnnotations(WorkFlowAnnotation a);

	@Remover(OTHER_ANNOTATIONS)
	public void removeFromOtherAnnotations(WorkFlowAnnotation a);
}
