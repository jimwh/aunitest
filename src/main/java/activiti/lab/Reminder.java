package activiti.lab;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Reminder {
    private static final Logger log = LoggerFactory.getLogger(Reminder.class);

    @Autowired
    BookFlight book;

    public void expirationReminder(DelegateExecution delegateExecution) throws Exception {
        int retries = 0;
        while (true) {
            try {
                log.info("bizKey={}, currentActivityId={}",
                        delegateExecution.getProcessBusinessKey(),
                        delegateExecution.getCurrentActivityId()
                );
                book.bookFlight("foome");
                log.info("booked ...");
                return;
            } catch (Exception e) {
                if (retries == 3) {
                    throw new BpmnError("BusinessExceptionOccurred");
                }
                retries += 1;
            }
            //
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
            }
        }
    }
}

/*
public class Reminder implements JavaDelegate {
    private static final Logger log= LoggerFactory.getLogger(Reminder.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("reminder...");
    }

    public void expirationReminder(DelegateExecution delegateExecution) {
        // try {} catch {} to handle until done
        log.info("bizKey={}, cancelJob={}",
            delegateExecution.getProcessBusinessKey(),
                delegateExecution.getVariable("cancelJob") );
    }

}
*/