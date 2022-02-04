package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import service.ServiceMessages;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;
    @FXML
    public HBox userPanel;
    @FXML
    public TextField login;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String loginUser;
    private String nickname;
    private Stage stage;
    private Stage regStage;
    private Stage settingsStage;
    private RegController regController;
    private UserSettingsController userSettingsController;
    //private FileOutputStream fileOut;
    private FileWriter fileWriter;
    String pathName;

    public void setAuthenticated(boolean authenticated){

        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        userPanel.setVisible(authenticated);
        userPanel.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
            loginField.setText("");
            textArea.clear();
            closeFile();
        }else{
            try {
                openFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setTitle(nickname);

        loginUser = loginField.getText();
        login.setText(nickname);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    private void closeFile(){
        if(fileWriter != null){
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void openFile() throws IOException {

        pathName = "client\\history_"+nickname+".txt";
        File file = new File(pathName);
        if(file.exists()){
            try {
                List<String> ss = Files.readAllLines(Paths.get(pathName));
                int start = 1;
                if(ss.size()>100){
                    start = ss.size()-100;
                }
                for (int i = start; i < ss.size(); i++) {
                    textArea.appendText(ss.get(i-1)+"\n");
                }
                //textArea.setText(Files.readAllLines(Paths.get(pathName)).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //try {
            fileWriter = new FileWriter(pathName,true);
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}

    }


    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith(ServiceMessages.AUTH_OK)) {
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith("/reg")) {
                                regController.regStatus(str);
                            }

                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                setAuthenticated(false);
                                break;
                            }
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split(" ");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                            if(str.startsWith(ServiceMessages.CHANGE_NICKNAME)){
                                userSettingsController.regStatus(str);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                            fileWriter.write((str + "\n"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                        if(fileWriter != null){
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clickBtnSendText(ActionEvent actionEvent) {
        if (textField.getText().length() > 0) {
            try {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickBtnAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            //loginUser = loginField.getText().trim();
            String msg = String.format("%s %s %s", ServiceMessages.AUTH,
                    loginField.getText().trim(), passwordField.getText().trim());
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "Magic chat";
        } else {
            title = String.format("Magic chat - %s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w " + receiver + " ");
    }

    public void clickBtnReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regStage = new Stage();
            regStage.setTitle("Magic chat registration");
            regStage.setScene(new Scene(root, 500, 425));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateNickname(String newNickname){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format(ServiceMessages.CHANGE_NICKNAME + " %s", newNickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickBtnSettings(ActionEvent actionEvent) {
        if (settingsStage == null) {
            createSettingStageWindow();
        }
        settingsStage.show();
    }

    private void createSettingStageWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/userSettings.fxml"));
            Parent root = fxmlLoader.load();

            settingsStage = new Stage();
            settingsStage.setTitle("Magic chat settings");
            settingsStage.setScene(new Scene(root, 500, 425));

            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initStyle(StageStyle.UTILITY);

            userSettingsController = fxmlLoader.getController();
            userSettingsController.setController(this);
            userSettingsController.setNickname(nickname);
            userSettingsController.setLogin(loginUser);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
        login.setText(loginUser);
    }
}
