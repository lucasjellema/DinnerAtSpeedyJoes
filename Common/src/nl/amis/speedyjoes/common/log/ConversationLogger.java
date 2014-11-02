package nl.amis.speedyjoes.common.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversationLogger {
    
    List<LogEntry> logEntries = new ArrayList<LogEntry>();
    public ConversationLogger() {
        super();
    }
    private long startTime;
    public void start() {
        startTime = new Date().getTime();
    }
    public int stop() {
        return (new Long((new Date().getTime() - startTime))).intValue();
    }
    
    public void enterLog(String component, int level, String description, int durationInMiliSeconds) {
        logEntries.add(new LogEntry(component, level, description, durationInMiliSeconds));
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }
}
