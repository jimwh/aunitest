package activiti.lab;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reminder implements JavaDelegate {
    private static final Logger log= LoggerFactory.getLogger(Reminder.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("reminder...");
    }

    public void m3Reminder(DelegateExecution delegateExecution) {
        // try {} catch {} to handle until done
        log.info("bizKey={}, cancelJob={}",
            delegateExecution.getProcessBusinessKey(),
                delegateExecution.getVariable("cancelJob") );
    }

}
