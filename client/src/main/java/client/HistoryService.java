package client;

public interface HistoryService {
    void start(String login);
    String getLast100LinesOfHistory(String login);
    void stop();
    void writeLine(String msg);
}
