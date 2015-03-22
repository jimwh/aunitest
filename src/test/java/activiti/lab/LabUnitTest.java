package activiti.lab;

import java.util.*;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabUnitTest {

    static final Logger log = LoggerFactory.getLogger(LabUnitTest.class);
    static final String ProcessDefKey = "lab-process";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"activiti/lab/lab-process.bpmn20.xml"})
    public void test() {
        String bizKey = "my-bizKey";
        ProcessInstance instance=starProcess(bizKey, new HashMap<String, Object>(), "test");
        Assert.assertNotNull(instance);
        List<String> activeActivityIds = activitiRule.getRuntimeService()
                .getActiveActivityIds(instance.getId());
        log.info(activeActivityIds.toString());
        Execution execution = activitiRule.getRuntimeService().createExecutionQuery()
                .processInstanceId(instance.getId())
                .activityId("cancelHotel").singleResult();
        if (execution != null) {
            activitiRule.getRuntimeService().signal(execution.getId());
        }
        Task task=getTaskByTaskDefKey(bizKey, "submit");
        Assert.assertNotNull(task);
        activitiRule.getTaskService().complete(task.getId());
        printOpenTaskList(bizKey);
        printOpenActiviti(bizKey);
    }


    List<HistoricTaskInstance> getFromMainProcess(String bizKey) {
        HistoryService historyService = activitiRule.getHistoryService();
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .finished()
                .taskDeleteReason("completed")
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        List<HistoricTaskInstance> list = query.list();
        return list;
    }

    List<HistoricTaskInstance> getFromSubProcess(String bizKey) {
        HistoryService historyService = activitiRule.getHistoryService();
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey("appendix-process") // sub-process def key
                //.processInstanceBusinessKey(bizKey) not for sub process
                .processVariableValueEquals("BusinessKey", bizKey) // use proc var
                .finished()
                .taskDeleteReason("completed")
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        return query.list();
    }

    String getCommentText(String commentId) {
        if (commentId == null) return null;
        Comment comment = activitiRule.getTaskService().getComment(commentId);
        return comment != null ? comment.getFullMessage() : null;
    }


    Task getTaskByTaskDefKey(String bizKey, String defKey) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(defKey)
                .singleResult();
    }

    Task getAssigneeTaskByTaskDefKey(String bizKey, String defKey, String assignee) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(defKey)
                .taskAssignee(assignee)
                .singleResult();
    }

    Task getAssigneeTaskByTaskName(String bizKey, String taskName, String assignee) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskName(taskName)
                .taskAssignee(assignee)
                .singleResult();
    }

    long taskCount(String bizKey) {
        return activitiRule.getTaskService()
                .createTaskQuery()
                .processInstanceBusinessKey(bizKey).count();
    }

    void printOpenTaskList(String bizKey) {
        List<Task> taskList = activitiRule.getTaskService()
                .createTaskQuery().processInstanceBusinessKey(bizKey).list();
        log.info("open task:");
        for (Task task : taskList) {
            log.info("taskDefKey=" + task.getTaskDefinitionKey());
        }
    }

    void printOpenActiviti(String bizKey) {
        List<Execution> executionList = activitiRule.getRuntimeService()
                .createExecutionQuery()
                .processInstanceBusinessKey(bizKey)
                .list();
        log.info("open activity:");
        for (Execution exe: executionList) {
            log.info("serviceTaskId={}", exe.getActivityId() );
        }
    }

    ProcessInstance getProcessInstanceByName(String bizKey, String instanceName) {
        return activitiRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .singleResult();
    }

    ProcessInstance starProcess(String bizKey, Map<String, Object> map, String instanceName) {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessDefKey, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), instanceName);
        return instance;
    }

}
