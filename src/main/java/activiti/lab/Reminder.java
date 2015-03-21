package activiti.lab;

import org.joda.time.DateTime;

import java.util.Date;

public enum Reminder {

    Day90("90 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder90";
        }

        public String activityId() {
            return "reminder90";
        }

        public int gatewayValue() { return 90; }
        public String catchErrorId() { return "catchError90"; }
    },

    Day60("60 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder60";
        }

        public String activityId() {
            return "reminder60";
        }
        public int gatewayValue() { return 60; }

        public String catchErrorId() { return "catchError60"; }
    },

    Day30("30 Day Reminder") {
        public String taskDefKey() {
            return "cancelReminder30";
        }

        public String activityId() {
            return "reminder30";
        }
        public int gatewayValue() { return 30; }

        public String catchErrorId() { return "catchError30"; }

    };

    private String text;

    private Reminder(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public abstract String taskDefKey();

    public abstract String activityId();

    public abstract int gatewayValue();

    public abstract String catchErrorId();

    public String getISO8601DateFormat(Date date){
        if( date==null ) return null;
        DateTime dateTime=new DateTime(date);
        return dateTime.toString("YYYY-MM-dd") + "T" + dateTime.toString("HH:mm:ss");
    }
}
