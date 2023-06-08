import javax.swing.*;

class Alarm {
    public JLabel getNameLabelOfAlarm() {
        return nameLabelOfAlarm;
    }

    public void setNameLabelOfAlarm(JLabel nameLabelOfAlarm) {
        this.nameLabelOfAlarm = nameLabelOfAlarm;
    }

    public JLabel getAlarmTimeLabel() {
        return alarmTimeLabel;
    }

    public void setAlarmTimeLabel(JLabel alarmTimeLabel) {
        this.alarmTimeLabel = alarmTimeLabel;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    private JLabel nameLabelOfAlarm;
    private JLabel alarmTimeLabel;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;

}