package activiti.lab;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class CompensateEventTest {

    static final Logger log = LoggerFactory.getLogger(CompensateEventTest.class);
    static final String ProcessDefKey = "lab-process";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"activiti/lab/testCompensateSubprocess.bpmn20.xml"})
    public void testCompensateSubprocess() {

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("compensateProcess");

        assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

        //runtimeService.signal(processInstance.getId());
        //assertProcessEnded(processInstance.getId());
    }


}
