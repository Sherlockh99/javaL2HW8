package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HistoryMy implements HistoryService{
    private FileWriter fileWriter;

    private static String getHistoryFilenameByLogin(String login) {
        return "history/history_" + login + ".txt";
    }

    @Override
    public void start(String login) {

    }

    @Override
    public String getLast100LinesOfHistory(String login) {
        String pathName = getHistoryFilenameByLogin(login);

        File file = new File(pathName);

        if(file.exists()){
            StringBuilder sb = new StringBuilder();
            try {
                List<String> history = Files.readAllLines(Paths.get(pathName));
                int start = 1;
                if(history.size()>100){
                    start = history.size()-100;
                }
                for (int i = start; i < history.size(); i++) {
                    //textArea.appendText(ss.get(i-1)+"\n");
                    sb.append(history.get(i)).append(System.lineSeparator());
                }
                //textArea.setText(Files.readAllLines(Paths.get(pathName)).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }else{
            return "";
        }
    }

    @Override
    public void stop(){
        if(fileWriter != null){
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
