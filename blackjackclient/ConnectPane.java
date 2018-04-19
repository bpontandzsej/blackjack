package blackjackclient;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class ConnectPane extends GridPane{
    private TextField nicknameInput;
    private Button connectButton;
    private Text connectingText;

    public ConnectPane(boolean input, String names){
        setAlignment(Pos.CENTER);
        setVgap(10);
        if(names == ""){
            Label namesLabel = new Label("Waiting for the server...");
            add(namesLabel, 1, 1);
        } else {
            String[] nameArray = names.split("#");
            Label namesLabel = new Label("Connected:\n");
            for(int i = 0; i<nameArray.length; i++){
                if(nameArray[i].length()!=0){
                    namesLabel.setText(namesLabel.getText() + nameArray[i] + "\n");
                }
            }
            add(namesLabel, 1, 2);
            if(input){
                Label nickname = new Label("Nickname: ");
                add(nickname, 0, 0);

                nicknameInput = new TextField();
                nicknameInput.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\sa-zA-Z0-9.,-_!*")) {
                        nicknameInput.setText(newValue.replaceAll("[^\\sa-zA-Z0-9.,-_!]", ""));
                    }
                });
                add(nicknameInput, 1, 0);

                connectButton = new Button("Connect");
                add(connectButton, 2, 0);
            }
        }                
    }

    public Button getConnectButton(){
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