package blackjackclient;

import java.util.Timer;
import java.util.TimerTask;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class ConnectPane extends EditablePane{
    private TextField nicknameInput;
    private Button connectButton;
    private Text connectingText;
    private Label namesLabel;
    private Pane remainingName;

    public ConnectPane(boolean input, String names){
        setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-font: 16px sans-serif; -fx-font-weight: bold;");
        /*setAlignment(Pos.CENTER);
        setVgap(10);*/
        if(names == ""){
            namesLabel = new Label("Varakozas a szerverre...");
            namesLabel.setAlignment(Pos.CENTER);
            //add(namesLabel, 1, 1);
            namesLabel.relocate(150, 235);
            getChildren().add(namesLabel);
        } else {
            String[] nameArray = names.split("#");
            namesLabel = new Label("Csatlakozott:\n");
            for(int i = 0; i<nameArray.length; i++){
                if(nameArray[i].length()!=0){
                    namesLabel.setText(namesLabel.getText() + nameArray[i] + "\n");
                }
            }
            //center.add(namesLabel, 1, 2);
            if(input){
                remainingName = new Pane();
                remainingName.relocate(150, 220);
                format(remainingName, "#48f", "transparent", 0, 5);
                setSize(remainingName, 200, 10);
                //center.add(remainingName, 1, 0);
                Label nickname = new Label("Felhasznalonev: ");
                //center.add(nickname, 0, 1);
                nickname.relocate(150, 190);

                nicknameInput = new TextField();
                nicknameInput.setAlignment(Pos.CENTER);
                nicknameInput.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(nicknameInput.getText().length()>10){
                        nicknameInput.setText(oldValue);
                    } else {
                        if (!newValue.matches("\\sa-zA-Z0-9*")) {
                            nicknameInput.setText(newValue.replaceAll("[^\\sa-zA-Z0-9]", ""));
                        }
                    }
                    
                });
                //center.add(nicknameInput, 1, 1);
                nicknameInput.relocate(150, 230);
                setSize(nicknameInput, 200, 30);

                connectButton = new Button("Csatlakozas");

                nicknameInput.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
                    if (ev.getCode() == KeyCode.ENTER) {
                        connectButton.fire();
                        ev.consume(); 
                    }
                });
                //center.add(connectButton, 2, 1);
                connectButton.relocate(150, 265);
                setSize(connectButton, 200, 30);
                namesLabel.relocate(150, 300);
                getChildren().addAll(remainingName, namesLabel, nickname, nicknameInput, connectButton);
            } else {
                namesLabel.relocate(150, 235);
                getChildren().add(namesLabel);
            }
            
            
        }                
    }

    public void setNamesLabelText(String s){
        namesLabel.setText(s);
    }

    public void setNameRemaining(int l){
        setSize(remainingName, l*2, 10);
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