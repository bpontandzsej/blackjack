package blackjackclient;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import sun.security.ssl.SSLContextImpl.TLS10Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.Object;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class TablePane extends Pane{
    private Pane chatPane;
    private TextField betInput;
    private Button sendBet;
    private Button cardBTN;
    private Button stopBTN;
    private Button sendChat;
    private TextField chatInput;
    private String myId;

    public TablePane(String state, String whichPane, ArrayList<String> chatArray, String myId){
        String[] dealerAndPlayers = state.split("@");
        this.myId = myId;
        getChildren().add(createChatPane(chatArray));
        switch(whichPane){
            case "bet":
                getChildren().add(createBetPane(dealerAndPlayers[1]));
            break;

            case "turn":
                getChildren().add(createTurnPane());
            break;
        }
        
        getChildren().add(createDealerPane(dealerAndPlayers[0]));
        getChildren().add(createPlayersPane(dealerAndPlayers[1]));
        /*if(state.indexOf("x:x") == -1){
            try{
                Thread.sleep(1000);
            } catch(Exception e){

            }
            
        }*/
    }

    private Pane createChatPane(ArrayList<String> chatArray){
        chatPane = new Pane();
        setSize(chatPane, 200, 300);
        chatPane.setStyle("-fx-background-color: green;");
        chatPane.relocate(0, 0);
        
        String s = "";
        for(String line : chatArray){
            s += line + "\n";
        }

        Text messages = new Text(s);
        messages.relocate(0, 0);

        chatInput = new TextField();
        setSize(chatInput, 150, 30);
        chatInput.relocate(0, 270);

        sendChat = new Button("Send");
        setSize(sendChat, 50, 30);
        sendChat.relocate(150, 270);

        chatPane.getChildren().addAll(messages, chatInput, sendChat);


        return chatPane;
    }

    private Pane createBetPane(String playersState){
        int maxBet = 0;
        String[] players = playersState.split("#");
        for(int i=0; i<players.length; i++){
            if(players[i].substring(0, 1).equals(myId)){
                String[] data = players[i].split(";");
                maxBet = Integer.parseInt(data[3]);
            }
        }
        Pane betPane = new Pane();
        setSize(betPane, 200, 200);
        betPane.relocate(0, 300);

        Slider betSlider = new Slider();
        betSlider.setMin(0);
        betSlider.setMax(maxBet);
        if(maxBet > 100){ 
            betSlider.setValue(100);
        } else {    
            betSlider.setValue(maxBet);
        }
        betSlider.setShowTickLabels(true);
        betSlider.setShowTickMarks(true);
        betSlider.setMajorTickUnit(100);
        betSlider.setMinorTickCount(10);
        betSlider.setBlockIncrement(10);

        

        setSize(betSlider, 200, 50);
        betSlider.relocate(0, 0);

        betInput = new TextField();
        setSize(betInput, 200, 50);
        betInput.relocate(0, 50);

        betSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                   betInput.setText(Integer.toString(new_val.intValue())); 
            }
        });

        betInput.setText(Integer.toString((int)Math.round(betSlider.getValue())));

        sendBet = new Button("Send");
        setSize(sendBet, 200, 100);
        sendBet.relocate(0, 100);

        betPane.getChildren().addAll(betSlider, betInput, sendBet);

        return betPane;
    }

    public String getBet(){
        return betInput.getText();
    }

    public Button getBetButton(){
        return sendBet;
    }

    public Button getSendChat(){
        return sendChat;
    }

    public String getChatMessage(){
        return chatInput.getText();
    }

    private Pane createTurnPane(){
        Pane turnPane = new Pane();
        
        setSize(turnPane, 200, 200);
        turnPane.setStyle("-fx-background-color: brown;");
        turnPane.relocate(0, 300);

        cardBTN = new Button("Card");
        setSize(cardBTN, 200, 100);
        cardBTN.relocate(0, 0);

        stopBTN = new Button("Stop");
        setSize(stopBTN, 200, 100);
        stopBTN.relocate(0, 100);

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

        HBox moneyPane = new HBox(5);
        setSize(moneyPane, 900, 25);
        moneyPane.relocate(0, 25);
        moneyPane.setAlignment(Pos.CENTER);

        ImageView moneyIcon = new ImageView(new Image("/blackjackclient/media/money.png"));
        moneyIcon.setFitHeight(25); 
        moneyIcon.setFitWidth(25);

        Label money = new Label(data[0]);

        moneyPane.getChildren().addAll(moneyIcon, money);

        HBox sumPane = new HBox(5);
        if(!data[3].equals("0")){
            setSize(sumPane, 900, 25);
            sumPane.relocate(0, 150);
            sumPane.setAlignment(Pos.CENTER);

            ImageView sumIcon = new ImageView(new Image("/blackjackclient/media/sum.png"));
            sumIcon.setFitHeight(25); 
            sumIcon.setFitWidth(25);

            Label sum = new Label();
            if(data[3].equals("21")){
                sum.setText("BLACKJACK");
            } else {
                sum.setText(data[3]);
                
            }

            sumPane.getChildren().addAll(sumIcon, sum);
        }
        

        HBox cardsPane = new HBox(5);
        cardsPane.setAlignment(Pos.CENTER);
        setSize(cardsPane, 900, 100);
        cardsPane.relocate(0, 50);
        if(data[1].length()>0){
            createCardsPane(cardsPane, data[1], false);
        }

        dealerPane.getChildren().addAll(moneyPane, cardsPane, sumPane);

        return dealerPane;
    }

    private Pane createPlayersPane(String playersState){
        Pane playersPane = new Pane();
        String[] players = playersState.split("#");
        int i;
        for(i=0; i<players.length; i++){
            Pane player = createPlayer(players[i]);
            player.relocate((5-i)*150, 0);
            playersPane.getChildren().add(player);
        }
        for(int j = i; j<6; j++){
            Pane blankPane = new Pane();
            setSize(blankPane, 150, 300);
            blankPane.relocate((5-i)*150, 0);
            playersPane.getChildren().add(blankPane);
        }
        

        setSize(playersPane, 900, 300);
        playersPane.relocate(200, 200);

        return playersPane;
    }

    private Pane createPlayer(String player){
        String[] data = player.split(";");
        Pane playerPane = new Pane();
        setSize(playerPane, 150, 300);

        HBox nextPane = new HBox();
        setSize(nextPane, 150, 25);
        nextPane.relocate(0, 0);
        nextPane.setAlignment(Pos.CENTER);

        if(data[2].equals("1")){
            ImageView nextIcon = new ImageView(new Image("/blackjackclient/media/next.png"));
            nextIcon.setFitHeight(25); 
            nextIcon.setFitWidth(25);

            nextPane.getChildren().add(nextIcon);
        }        

        HBox betPane = new HBox(5);
        setSize(betPane, 150, 25);
        betPane.relocate(0, 25);
        betPane.setAlignment(Pos.CENTER);

        ImageView tokenIcon = new ImageView(new Image("/blackjackclient/media/token.png"));
        tokenIcon.setFitHeight(25); 
        tokenIcon.setFitWidth(25);

        Label bet = new Label(data[4]);

        betPane.getChildren().addAll(tokenIcon, bet);

        Label name = new Label("[" + Integer.toString(Integer.parseInt(data[0])+1) + "] " + data[1]);
        setSize(name, 150, 25);
        name.relocate(0, 50);
        name.setAlignment(Pos.CENTER);

        HBox moneyPane = new HBox(5);
        setSize(moneyPane, 150, 25);
        moneyPane.relocate(0, 75);
        moneyPane.setAlignment(Pos.CENTER);

        ImageView moneyIcon = new ImageView(new Image("/blackjackclient/media/money.png"));
        moneyIcon.setFitHeight(25); 
        moneyIcon.setFitWidth(25);

        Label money = new Label(data[3]);

        moneyPane.getChildren().addAll(moneyIcon, money);

        HBox sumPane = new HBox(5);
        if(!data[6].equals("0")){
            setSize(sumPane, 150, 25);
            sumPane.relocate(0, 100);
            sumPane.setAlignment(Pos.CENTER);

            ImageView sumIcon = new ImageView(new Image("/blackjackclient/media/sum.png"));
            sumIcon.setFitHeight(25); 
            sumIcon.setFitWidth(25);

            Label sum = new Label();
            if(data[7].equals("21")){
                sum.setText("BLACKJACK");
            } else {
                if(data[2].equals("2")){
                    sum.setText(data[7]);
                } else {
                    sum.setText(data[6]);
                }
            }            

            sumPane.getChildren().addAll(sumIcon, sum);
        }
        

        VBox cardsPane = new VBox(5);
        cardsPane.setMinWidth(150);
        cardsPane.setPrefWidth(150);
        cardsPane.setMaxWidth(150);
        cardsPane.setAlignment(Pos.CENTER);
        if(data[5].length()>0){
            createCardsPane(cardsPane, data[5], true);
        }
        ScrollPane scrollCardsPane = new ScrollPane(cardsPane);
        scrollCardsPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollCardsPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        setSize(scrollCardsPane, 150, 175);
        scrollCardsPane.relocate(0, 125);
        

        if(myId.equals(data[0])){
            name.setStyle("-fx-background-color: gray;");
        }
        playerPane.getChildren().addAll(nextPane, betPane, name, moneyPane, sumPane, scrollCardsPane);
        
        /*switch(data[2]){
            case "0":
                playerPane.setStyle("-fx-background-color: yellow;");
            break;
            case "1":
                playerPane.setStyle("-fx-background-color: pink;");
            break;
            case "2":
                playerPane.setStyle("-fx-background-color: green;");
            break;
        }*/       
        
        return playerPane;
    }

    private void createCardsPane(Pane cardsPane, String cardsString, boolean player){
        if(cardsString.length()>0){
            String[] cards = cardsString.split(" ");
            if(player){
                for(int i=cards.length-1; i>=0; i--){
                    cardsPane.getChildren().add(createCard(cards[i]));
                }
            } else {
                for(int i=0; i<cards.length; i++){
                    cardsPane.getChildren().add(createCard(cards[i]));
                }
            }
            
        }
        
    }

    private void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    private BorderPane createCard(String cardString){
        BorderPane cardPane = new BorderPane();
        cardPane.setStyle("-fx-background-color: orange;");
        String[] card = cardString.split(":");
        setSize(cardPane, 50, 70);
        
        if(!cardString.equals("x:x")){
            String number = "";
            if(Integer.parseInt(card[1])>1 && Integer.parseInt(card[1])<11){
                number = card[1];
            } else {
                switch(card[1]){
                    case "1":
                        number = "A";
                    break;
                    case "11":
                        number = "J";
                    break;
                    case "12":
                        number = "Q";
                    break;
                    case "13":
                        number = "K";
                    break;
                }
            }
            Label tl = new Label(number);
            Label br = new Label(number);
    
            Image color = null;
    
            switch(card[0]){
                case "h":                 
                    color = new Image("/blackjackclient/media/hearts.png");
                break;
                case "s":
                    color = new Image("/blackjackclient/media/spades.png");
                break;
                case "d":
                    color = new Image("/blackjackclient/media/diamonds.png");
                break;
                case "c":
                    color = new Image("/blackjackclient/media/clubs.png");
                break;
            }
    
            ImageView c = new ImageView(color);
            c.setFitHeight(25); 
            c.setFitWidth(25); 
            
            cardPane.setTop(tl);
            cardPane.setCenter(c);
            cardPane.setBottom(br);
            
            cardPane.setAlignment(tl, Pos.TOP_LEFT);
            cardPane.setAlignment(c, Pos.CENTER);
            cardPane.setAlignment(br, Pos.BOTTOM_RIGHT);
            cardPane.setMargin(tl, new Insets(5, 5, 5, 5));
            cardPane.setMargin(br, new Insets(5, 5, 5, 5));
        }
        

        return cardPane;
    }

    
}