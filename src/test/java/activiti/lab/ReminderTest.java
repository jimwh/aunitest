package activiti.lab;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReminderTest {

    static final Logger log = LoggerFactory.getLogger(ReminderTest.class);
    static final String ProcessDefKey = "intermediateReminder";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"activiti/lab/intermediateReminder.bpmn20.xml"})
    public void test() {
        String bizKey = "my-bizKey";
        ProcessInstance instance=starProcess(bizKey, new HashMap<String, Object>(), "test");
        Assert.assertNotNull(instance);
        //
        /*
        Task m3ReminderTask = activitiRule.getTaskService()
                .createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey("m3Reminder").singleResult();
        */
        // Assert.assertNotNull(m3ReminderTask);
        //
        // activitiRule.getTaskService().complete(m3ReminderTask.getId());
        //
        try {
            Thread.sleep(140000);
            /*
            m3ReminderTask = activitiRule.getTaskService()
                    .createTaskQuery()
                    .processInstanceBusinessKey(bizKey)
                    .taskDefinitionKey("m3Reminder").singleResult();
            // Assert.assertNull(m3ReminderTask);
            // activitiRule.getTaskService().complete(m3ReminderTask.getId());
            */
            log.info("time out...");
        }catch (InterruptedException e) {

        }

        instance=activitiRule.getRuntimeService().createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey).singleResult();
        Assert.assertNull(instance);

        List<HistoricDetail>list =
                activitiRule.getHistoryService()
                .createHistoricDetailQuery()
                .list();

        for(HistoricDetail h: list) {
            log.info("executionId={}", h.getExecutionId());
        }

        List<HistoricActivityInstance>inList=
                activitiRule.getHistoryService()
                        .createHistoricActivityInstanceQuery()
                        .orderByHistoricActivityInstanceEndTime()
                        .desc()
                        .list();
        for(HistoricActivityInstance h: inList) {
            log.info("fff..."+h.getActivityId());
        }
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
            log.info("activityId={}", exe.getActivityId() );
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
