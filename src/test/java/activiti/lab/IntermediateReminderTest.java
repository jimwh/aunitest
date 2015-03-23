package activiti.lab;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateReminderTest {

    static final Logger log = LoggerFactory.getLogger(IntermediateReminderTest.class);
    static final String ProcessDefKey = "intermediateReminder";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"activiti/lab/intermediateReminder.bpmn20.xml"})
    public void test() {
        log.info("codeName={}", Reminder.Day30.getText());
        log.info("name={}", Reminder.Day30.name());
        log.info("taskDefKey={}", Reminder.Day30.taskDefKey());
        log.info("serviceTaskId={}", Reminder.Day30.serviceTaskId());
        log.info("toString={}", Reminder.Day30.toString());
        Date date = new Date();
        log.info("date={}", date);
        log.info("ISO8601DateFormat={}", Reminder.Day30.getISO8601DateFormat(date));
        //normal();
        userCancel();
    }

    public void normal() {
        String bizKey = "my-bizKey";
        Map<String, Object>map=new HashMap<String, Object>();
        map.put("remindDate", new Date());

        map.put("START_GATEWAY", Reminder.Day30.gatewayValue() );
        ProcessInstance instance=starProcess(bizKey, map, Reminder.Day30.name() );
        Assert.assertNotNull(instance);
        //
        Assert.assertTrue(hasTask(bizKey, Reminder.Day30.taskDefKey()));

        List<Job> timerList=activitiRule.getManagementService()
                .createJobQuery()
                .processInstanceId(instance.getProcessInstanceId())
                .list();
        Assert.assertNotNull(timerList);

        instance = activitiRule.getRuntimeService().createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey).singleResult();
        Assert.assertNull(instance);
        //
        printHistoricReminder(bizKey, Reminder.Day30);
        printHistoricTaskInstance(bizKey, Reminder.Day30);
    }

    HistoricProcessInstance getHistoricProcessInstanceByBizKeyAndInstanceName(String bizKey, String name) {
        return activitiRule.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(name)
                .singleResult();
    }

    void printHistoricReminder(String bizKey, Reminder reminder) {
        HistoricProcessInstance pi=
                getHistoricProcessInstanceByBizKeyAndInstanceName(bizKey, reminder.name());

        HistoricActivityInstance hai=
                activitiRule.getHistoryService()
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(pi.getId())
                        .activityId( reminder.serviceTaskId() )
                        .singleResult();
        if( hai != null ) {
            log.info("endTime={},serviceTaskId={},activityName={},activityType={}, duration={}",
                    hai.getEndTime(),
                    hai.getActivityId(),
                    hai.getActivityName(),
                    hai.getActivityType(),
                    hai.getDurationInMillis());
        }
        //
        HistoricActivityInstance catchError=
                activitiRule.getHistoryService()
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(pi.getId())
                        .activityId( reminder.catchErrorId() )
                        .singleResult();
        if( catchError != null ) {
            log.info("endTime={},serviceTaskId={},activityName={},activityType={}, duration={}",
                    catchError.getEndTime(),
                    catchError.getActivityId(),
                    catchError.getActivityName(),
                    catchError.getActivityType(),
                    catchError.getDurationInMillis());
        }
    }

    boolean hasTask(String bizKey, String taskDefKey) {
        return activitiRule.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey)
                .singleResult() != null;
    }

    void printHistoricTaskInstance(String bizKey, Reminder reminder) {
        HistoricProcessInstance pi=
                getHistoricProcessInstanceByBizKeyAndInstanceName(bizKey, reminder.name());

        List<HistoricTaskInstance>hsList=activitiRule.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(pi.getId())
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        for(HistoricTaskInstance hs: hsList) {
            log.info("taskDefKey={}, taskName={}, deleltedOrCompleted={}",
                    hs.getTaskDefinitionKey(), hs.getName(), hs.getDeleteReason());
        }
    }


    public void userCancel() {
        String bizKey = "my-bizKey";
        Map<String, Object>map=new HashMap<String, Object>();
        map.put("START_GATEWAY", Reminder.Day30.gatewayValue() );
        DateTime dateTime=new DateTime();
        map.put("remindDate", Reminder.Day30.getISO8601DateFormat( dateTime.plusDays(1).toDate()) );
        ProcessInstance instance=starProcess(bizKey, map, Reminder.Day30.name());
        Assert.assertNotNull(instance);
        //
        Task userTask=activitiRule.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey(Reminder.Day30.taskDefKey())
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
            log.info("activityId={}, activityType={}", hai.getActivityId(), hai.getActivityType());
        }
        ///////////////////////////////////////////////////////

        printHistoricReminder(bizKey, Reminder.Day30);
        printHistoricTaskInstance(bizKey, Reminder.Day30);
    }


    ProcessInstance starProcess(String bizKey, Map<String, Object> map, String instanceName) {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessDefKey, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), instanceName);
        return instance;
    }
}
