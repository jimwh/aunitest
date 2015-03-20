package activiti.lab;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.*;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
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
        Task userTask=activitiRule.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("userTask")
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(instance.getProcessInstanceId())
                .singleResult();
        Assert.assertNotNull(userTask);
        activitiRule.getTaskService().complete(userTask.getId());
        //


        instance = activitiRule.getRuntimeService().createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey).singleResult();
        Assert.assertNull(instance);
        //
        List<HistoricActivityInstance>inList=
                activitiRule.getHistoryService()
                        .createHistoricActivityInstanceQuery()
                        .orderByHistoricActivityInstanceEndTime()
                        .desc()
                        .list();
        for(HistoricActivityInstance hai: inList) {
            log.info("activityId={}", hai.getActivityId());
        }
    }

    public void normal() {
        String bizKey = "my-bizKey";
        ProcessInstance instance=starProcess(bizKey, new HashMap<String, Object>(), "test");
        Assert.assertNotNull(instance);
        //
        List<Job> timerList=activitiRule.getManagementService()
                .createJobQuery()
                .processInstanceId(instance.getProcessInstanceId())
                .list();
        Assert.assertNotNull(timerList);

        try {
            Thread.sleep(66000);
        }catch (InterruptedException e){}

        instance = activitiRule.getRuntimeService().createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey).singleResult();
        Assert.assertNull(instance);
        //
        List<HistoricActivityInstance>inList=
                activitiRule.getHistoryService()
                        .createHistoricActivityInstanceQuery()
                        .orderByHistoricActivityInstanceEndTime()
                        .desc()
                        .list();
        for(HistoricActivityInstance hai: inList) {
            log.info("activityId={}", hai.getActivityId());
        }
    }


    ProcessInstance starProcess(String bizKey, Map<String, Object> map, String instanceName) {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessDefKey, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), instanceName);
        return instance;
    }

}
