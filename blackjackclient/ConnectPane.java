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
            Label namesLabel = new Label("Varakozas a tobbi jatekosra...");
            add(namesLabel, 1, 0);
        } else {
            String[] nameArray = names.split("#");
            Label namesLabel = new Label("Csatlakozott:\n");
            for(int i = 0; i<nameArray.length; i++){
                if(nameArray[i].length()!=0){
                    namesLabel.setText(namesLabel.getText() + nameArray[i] + "\n");
                }
            }
            add(namesLabel, 1, 0);
            if(input){
                Label nickname = new Label("Nickname: ");
                add(nickname, 0, 1);

                nicknameInput = new TextField();
                add(nicknameInput, 1, 1);

                connectButton = new Button("Connect");
                add(connectButton, 2, 1);

                connectingText = new Text();
                add(connectingText, 1, 2, 2, 1);
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