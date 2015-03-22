package activiti.lab;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

import java.util.Date;

public enum Reminder {

    Day90("90 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder90";
        }

        public boolean isTaskDefKey(String defKey) {
            return taskDefKey().equals(defKey);
        }

        public String serviceTaskId() {
            return "reminder90";
        }

        public boolean isServiceTaskId(String id) {
            return serviceTaskId().equals(id);
        }

        public int gatewayValue() {
            return 90;
        }

        public String catchErrorId() {
            return "catchError90";
        }
    },

    Day60("60 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder60";
        }

        public boolean isTaskDefKey(String defKey) {
            return taskDefKey().equals(defKey);
        }

        public String serviceTaskId() {
            return "reminder60";
        }

        public int gatewayValue() {
            return 60;
        }

        public String catchErrorId() {
            return "catchError60";
        }

        public boolean isServiceTaskId(String id) {
            return serviceTaskId().equals(id);
        }
    },

    Day30("30 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder30";
        }

        public boolean isTaskDefKey(String defKey) {
            return taskDefKey().equals(defKey);
        }

        public String serviceTaskId() {
            return "reminder30";
        }

        public boolean isServiceTaskId(String id) {
            return serviceTaskId().equals(id);
        }

        public int gatewayValue() {
            return 30;
        }

        public String catchErrorId() {
            return "catchError30";
        }

    };

    private String text;

    private Reminder(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public abstract String taskDefKey();

    public abstract boolean isTaskDefKey(String defKey);

    public abstract String serviceTaskId();

    public abstract boolean isServiceTaskId(String id);

    public abstract int gatewayValue();

    public abstract String catchErrorId();

    public String getISO8601DateFormat(Date date) {
        Assert.notNull(date);
        DateTime dateTime = new DateTime(date);
        return dateTime.toString("YYYY-MM-dd") + "T" + dateTime.toString("HH:mm:ss");
    }

}
