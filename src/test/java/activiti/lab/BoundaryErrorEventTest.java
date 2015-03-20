package activiti.lab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class BoundaryErrorEventTest  {

    private static final Logger log = LoggerFactory.getLogger(BoundaryErrorEventTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Deployment(resources = {"activiti/lab/reviewSalesLead.bpmn20.xml"})
    public void testReviewSalesLeadProcess() {
        TaskService taskService=activitiRule.getTaskService();
        RuntimeService runtimeService=activitiRule.getRuntimeService();
        // After starting the process, a task should be assigned to the 'initiator' (normally set by GUI)
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("details", "very interesting");
        variables.put("customerName", "Alfresco");
        String procId = runtimeService.startProcessInstanceByKey("reviewSaledLead", variables).getId();
        Task task = taskService.createTaskQuery().taskDefinitionKey("provideNewSalesLead").singleResult();
        log.info(task.getTaskDefinitionKey());
        assertEquals("Provide new sales lead", task.getName());

        // After completing the task, the review subprocess will be active
        taskService.complete(task.getId());
        Task ratingTask = taskService.createTaskQuery().taskDefinitionKey("reviewCustomerRating").singleResult();
        log.info(ratingTask.getTaskDefinitionKey());
        assertEquals("Review customer rating", ratingTask.getName());
        Task profitabilityTask = taskService.createTaskQuery().taskDefinitionKey("reviewProfitability").singleResult();
        log.info(profitabilityTask.getTaskDefinitionKey());
        assertEquals("Review profitability", profitabilityTask.getName());

        // Complete the management task by stating that not enough info was provided
        // This should throw the error event, which closes the subprocess
        variables = new HashMap<String, Object>();
        variables.put("notEnoughInformation", true);
        taskService.complete(profitabilityTask.getId(), variables);

        // The 'provide additional details' task should now be active
        Task provideDetailsTask = taskService.createTaskQuery().taskDefinitionKey("provideAdditionalDetails").singleResult();
        log.info(provideDetailsTask.getTaskDefinitionKey());
        assertEquals("Provide additional details", provideDetailsTask.getName());

        // Providing more details (ie. completing the task), will activate the subprocess again
        taskService.complete(provideDetailsTask.getId());
        List<Task> reviewTasks = taskService.createTaskQuery().orderByTaskName().asc().list();
        assertEquals("Review customer rating", reviewTasks.get(0).getName());
        assertEquals("Review profitability", reviewTasks.get(1).getName());

        // Completing both tasks normally ends the process
        taskService.complete(reviewTasks.get(0).getId());
        variables.put("notEnoughInformation", false);
        taskService.complete(reviewTasks.get(1).getId(), variables);
        //assertProcessEnded(procId);

        List<HistoricTaskInstance>hsList=activitiRule.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        for(HistoricTaskInstance hs: hsList) {
            log.info(hs.getTaskDefinitionKey());
        }
    }

}
