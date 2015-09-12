package activiti.lab;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricTaskInstance;
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

import java.util.*;

public class SkipTest {

    static final Logger log = LoggerFactory.getLogger(SkipTest.class);
    static final String ProcessDefKey = "testSkip";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"activiti/lab/testSkip.bpmn20.xml"})
    public void test() {

        Set<String> set = new TreeSet<String>();
        set.add("Morgan Freemen (rscl1002)");
        set.add("Andrew Thompson (at2582)");
        set.add("Yueping Yu (yy229)");
        set.add("Tom Cargie (rscl1001)");
        for (String name : set) {
            log.info("name={}", name);
        }

        // List<Foo> listFoo;
        // List<Map<String, Object>> listMap;
        // change List<Foo> to Map<String, Foo>
        //
        DateTime dateTime = new DateTime(1429070400000L);
        log.info(dateTime.toString());

        String bizKey = "my-bizKey";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        map.put("skip", true);
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessDefKey,
                bizKey, map);
        Assert.assertNotNull(instance);
        //
        log.info("hasMyTask={}", hasTask(bizKey, "myTask"));

        List<Task> userTaskList = activitiRule.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("myTask")
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(instance.getProcessInstanceId())
                .list();

        Assert.assertNotNull(userTaskList);
        for (Task task : userTaskList) {
            activitiRule.getTaskService().complete(task.getId());
        }
        printHistoricTaskInstance(bizKey);

        instance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .processDefinitionKey(ProcessDefKey)
                .active()
                .singleResult();
        Assert.assertNull(instance);
    }

    boolean hasTask(String bizKey, String taskDefKey) {
        return !activitiRule.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey)
                .list().isEmpty();
    }

    void printHistoricTaskInstance(String bizKey) {

        List<HistoricTaskInstance> hsList = activitiRule.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        for (HistoricTaskInstance hs : hsList) {
            log.info("endTime={}, taskDefKey={}, taskName={}, deleltedOrCompleted={}",
                    hs.getEndTime(),
                    hs.getTaskDefinitionKey(), hs.getName(), hs.getDeleteReason());
        }
    }

}
