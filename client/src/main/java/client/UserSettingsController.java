package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import service.ServiceMessages;

public class UserSettingsController {

    @FXML
    public TextField loginField;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea textArea;

    private Controller controller;
    private String nickname;
    private String login;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        nicknameField.setText(this.nickname);
    }

    public void setLogin(String login) {
        this.login = login;
        loginField.setText(this.login);
    }

    @FXML
    public void clickBtnApply(ActionEvent actionEvent) {
        if(nickname.equals(nicknameField.getText())){
            return;
        }
        controller.updateNickname(login, nicknameField.getText());
    }

    public void regStatus(String result) {
        if (result.equals(ServiceMessages.CHANGE_NICKNAME_OK)) {
            textArea.appendText("Изменение никнейма прошло успешно\n");
            controller.setLoginUser(nicknameField.getText());
            nickname = nicknameField.getText();
        } else {
            textArea.appendText("Изменение не получилось. Никнейм занят\n");
        }
    }
}
