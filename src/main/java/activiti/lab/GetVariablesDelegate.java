package activiti.lab;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class GetVariablesDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
        Object nrOfCompletedInstances = execution.getVariable("nrOfCompletedInstances");
        Integer variable = SetVariablesDelegate.variablesMap.get(nrOfCompletedInstances);
        Object variableLocal = execution.getVariable("variable");
        if (!variableLocal.equals(variable)) {
            throw new ActivitiIllegalArgumentException("wrong variable passed in to compensation handler");
        }
    }

}
