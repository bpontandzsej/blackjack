package blackjackclient;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class BlackjackClientConnect extends GridPane{
    private TextField nicknameInput;
    private Button connectButton;
    private Text connectingText;

    public BlackjackClientConnect(){
        setAlignment(Pos.CENTER);

        Label nickname = new Label("Nickname: ");
        add(nickname, 0, 0);

        nicknameInput = new TextField();
        add(nicknameInput, 1, 0);

        connectButton = new Button("Connect");
        add(connectButton, 2, 0);

        connectingText = new Text();
        add(connectingText, 1, 1, 2, 1);
    }

    public Button getConnecButton(){
        return connectButton;
    }

    public String getNicknameInput(){
        return nicknameInput.getText();
    }

    public void setWaitingText(){
        connectButton.setVisible(false);
        connectingText.setText("Waiting for others...");
    }
}