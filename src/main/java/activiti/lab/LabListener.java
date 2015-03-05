package activiti.lab;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LabListener implements TaskListener, ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(LabListener.class);


    // task listener
    @Override
    public void notify(DelegateTask delegateTask) {

        DelegateExecution taskExecution = delegateTask.getExecution();
        String bizKey = taskExecution.getProcessBusinessKey();
        String processId = taskExecution.getProcessInstanceId();
        String taskId = delegateTask.getId();
        String taskDefKey = delegateTask.getTaskDefinitionKey();
        String eventName = delegateTask.getEventName();
        StringBuilder sb = new StringBuilder();
        sb.append("bizKey=").append(bizKey)
                .append(",taskId=").append(taskId)
                .append(",taskDefKey=").append(taskDefKey)
                .append(",processId=").append(processId);

        if ("create".equals(eventName)) {
            log.info("create: {}", sb.toString());
        } else if ("complete".equals(eventName)) {
            log.info("complete: {}", sb.toString());
        }
    }


    // execution listener
    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {


        ExecutionEntity thisEntity = (ExecutionEntity) delegateExecution;
        ExecutionEntity superExecEntity = thisEntity.getSuperExecution();
        String eventName = delegateExecution.getEventName();

        if (superExecEntity == null) {
            // get the business key of the main process
            log.info("main process: eventName={}, bizKey={}, procDefId={}", eventName, thisEntity.getBusinessKey(), thisEntity.getProcessDefinitionId());
            // used by designatedReviews output
            // thisEntity.setVariable(AllRvs, true);

        } else {
            // in a sub-process so get the BusinessKey variable set by the caller.
            String key = (String) superExecEntity.getVariable("BusinessKey");
            boolean hasAppendix = (Boolean) superExecEntity.getVariable("hasAppendix");

            log.info("sub-process: eventName={}, bizKey={}, procDefId={}, hasAppendix={}",
                    eventName, key, thisEntity.getProcessDefinitionId(), hasAppendix);

            thisEntity.setVariable("BusinessKey", key);

            // for get task by business key
            thisEntity.setBusinessKey(key);

            //
            Map<String, Object> processMap = new HashMap<String, Object>();
            processMap.put("appendixA", true);   // used in sub-process, has A
            processMap.put("appendixB", true);   // used in sub-process, has B
            thisEntity.setVariables(processMap);
        }
    }
}
