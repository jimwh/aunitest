package activiti.lab;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelHotel implements JavaDelegate {
    private static final Logger log= LoggerFactory.getLogger(BookHotel.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("cancel hotel...");
    }
}
