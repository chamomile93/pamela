package org.openflexo.pamela.test.model;

import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Modify;
import org.openflexo.pamela.annotations.Setter;

@ModelEntity(isAbstract = true)
@Modify(forward = WorkFlowObject.PROCESS, synchWithForward = true)
public interface WorkFlowObject extends TestModelObject {
	/*
	Most likely: "WKF" = "Workflow" (common shorthand — fits `WKFObject` having a `getProcess()`).

	Other possibilities (less likely):
	- "Workforce"
	- "Workflow Kernel/Framework" (project-specific)
	- An initialism from a domain-specific name (check project docs?)
	 */

	public static final String PROCESS = "process";

	@Getter(PROCESS)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoProcess getProcess();

	@Setter(PROCESS)
	public void setProcess(FlexoProcess aProcess);

	@Override
	@Setter(TestModelObject.FLEXO_ID)
	public void setFlexoID(String flexoID);
}
