package blackjackclient;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.lang.Object;
import blackjackclient.CardPane;;

public class TablePane extends Pane{
    private Pane chatPane;
    private TextField betInput;
    private Button sendBet;
    private Button cardBTN;
    private Button stopBTN;

    public TablePane(String state, String whichPane){
        String[] dealerAndPlayers = state.split("@");

        getChildren().add(createChatPane());
        switch(whichPane){
            case "bet":
                getChildren().add(createBetPane());
            break;

            case "turn":
            getChildren().add(createTurnPane());
            break;
        }
        
        getChildren().add(createDealerPane(dealerAndPlayers[0]));
        getChildren().add(createPlayersPane(dealerAndPlayers[1]));
        if(state.indexOf("x:x") == -1){
            try{
                Thread.sleep(1000);
            } catch(Exception e){

            }
            
        }
    }

    private Pane createChatPane(){
        chatPane = new Pane();
        setSize(chatPane, 200, 300);
        chatPane.setStyle("-fx-background-color: green;");
        chatPane.relocate(0, 0);
        
        Text messages = new Text("");
        messages.relocate(0, 0);

        TextField input = new TextField();
        setSize(input, 150, 30);
        input.relocate(0, 270);

        Button send = new Button("Send");
        setSize(send, 50, 30);
        send.relocate(150, 270);

        chatPane.getChildren().addAll(messages, input, send);


        return chatPane;
    }

    private Pane createBetPane(){
        Pane betPane = new Pane();
        setSize(betPane, 200, 200);
        betPane.setStyle("-fx-background-color: blue;");
        betPane.relocate(0, 300);

        betInput = new TextField();
        setSize(betInput, 150, 30);
        betInput.relocate(0, 0);

        sendBet = new Button("Send");
        setSize(sendBet, 50, 30);
        sendBet.relocate(150, 50);

        betPane.getChildren().addAll(betInput, sendBet);

        return betPane;
    }

    public String getBet(){
        return betInput.getText();
    }

    public Button getBetButton(){
        return sendBet;
    }

    private Pane createTurnPane(){
        Pane turnPane = new Pane();
        setSize(turnPane, 200, 200);
        turnPane.setStyle("-fx-background-color: brown;");
        turnPane.relocate(0, 300);

        cardBTN = new Button("Card");
        setSize(cardBTN, 50, 30);
        cardBTN.relocate(0, 0);

        stopBTN = new Button("Stop");
        setSize(stopBTN, 50, 30);
        stopBTN.relocate(0, 50);

        turnPane.getChildren().addAll(cardBTN, stopBTN);

        return turnPane;
    }

    public Button getCardButton(){
        return cardBTN;
    }
    
    public Button getStopButton(){
        return stopBTN;
    }

    private Pane createDealerPane(String dealerState){
        String[] data = dealerState.split(";");
        Pane dealerPane = new Pane();
        setSize(dealerPane, 900, 200);
        dealerPane.setStyle("-fx-background-color: red;");
        dealerPane.relocate(200, 0);

        Label sum = new Label(data[3]);
        setSize(sum, 150, 25);
        sum.relocate(0, 0);

        Label cards = new Label(data[1]);
        setSize(cards, 150, 25);
        cards.relocate(0, 25);

        dealerPane.getChildren().addAll(cards, sum);

        return dealerPane;
    }

    private Pane createPlayersPane(String playersState){
        Pane playersPane = new Pane();
        String[] players = playersState.split("#");
        int i;
        for(i=0; i<players.length; i++){
            Pane player = createPlayer(players[i]);
            player.relocate(i*150, 0);
            playersPane.getChildren().add(player);
        }
        for(int j = i; j<6; j++){
            Pane blankPane = new Pane();
            setSize(blankPane, 150, 300);
            blankPane.relocate(i*150, 0);
            playersPane.getChildren().add(blankPane);
        }
        

        setSize(playersPane, 900, 300);
        playersPane.setStyle("-fx-background-color: yellow;");
        playersPane.relocate(200, 200);

        return playersPane;
    }

    private Pane createPlayer(String player){
        String[] data = player.split(";");
        Pane playerPane = new Pane();
        setSize(playerPane, 150, 300);
        Label bet = new Label(data[4]);
        setSize(bet, 150, 25);
        bet.relocate(0, 0);

        Label name = new Label("[" + data[0] + "]" + data[1]);
        setSize(name, 150, 25);
        name.relocate(0, 25);

        Label money = new Label(data[3]);
        setSize(money, 150, 25);
        money.relocate(0, 50);

        Label sum = new Label(data[6]);
        setSize(sum, 150, 25);
        sum.relocate(0, 75);

        Label cards = new Label(data[5]);
        setSize(cards, 150, 25);
        cards.relocate(0, 100);

        playerPane.getChildren().addAll(bet, name, money, sum, cards);


        return playerPane;
    }

    private void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    
}