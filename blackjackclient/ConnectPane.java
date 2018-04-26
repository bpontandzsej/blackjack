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
    private Label namesLabel;

    public ConnectPane(boolean input, String names){
        setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-font: 16px sans-serif;");
        setAlignment(Pos.CENTER);
        setVgap(10);
        if(names == ""){
            namesLabel = new Label("Varakozas a szerverre...");
            add(namesLabel, 1, 1);
        } else {
            String[] nameArray = names.split("#");
            namesLabel = new Label("Csatlakozott:\n");
            for(int i = 0; i<nameArray.length; i++){
                if(nameArray[i].length()!=0){
                    namesLabel.setText(namesLabel.getText() + nameArray[i] + "\n");
                }
            }
            add(namesLabel, 1, 2);
            if(input){
                Label nickname = new Label("Felhasznalonev: ");
                add(nickname, 0, 0);

                nicknameInput = new TextField();
                nicknameInput.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\sa-zA-Z0-9.,-_!*")) {
                        nicknameInput.setText(newValue.replaceAll("[^\\sa-zA-Z0-9.,-_!]", ""));
                    }
                });
                add(nicknameInput, 1, 0);

                connectButton = new Button("Csatlakozas");
                add(connectButton, 2, 0);
            }
        }                
    }

    public void setNamesLabelText(String s){
        namesLabel.setText(s);
    }

    public Button getConnectButton(){
        return connectButton;
    }

    public String getNicknameInput(){
        return nicknameInput.getText();
    }

    public void setWaitingText(){
        connectButton.setVisible(false);
        connectingText.setText("Varakozas a tobbiekre...");
    }
}