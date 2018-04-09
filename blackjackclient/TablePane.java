package blackjackclient;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
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
import java.util.Collections;
import java.util.GregorianCalendar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TablePane extends Pane{
    private Pane chatPane;
    private TextField betInput;
    private Button sendBet;
    private Button cardBTN;
    private Button stopBTN;
    private Button sendChat;
    private TextField chatInput;
    private String myId;
    private Label messages;
    private ScrollPane scrollMessages;

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
    }

    private Pane createChatPane(ArrayList<String> chatArray){
        chatPane = new Pane();
        chatPane.setStyle("-fx-background-color: gray;");
        setSize(chatPane, 200, 300);
        chatPane.relocate(0, 0);

        
        String s = "";
        for(String line : chatArray){
            s += line + "\n";
        }

        messages = new Label(s);
        messages.setWrapText(true);
        messages.setMinWidth(200);
        messages.setPrefWidth(200);
        messages.setMaxWidth(200);

        scrollMessages = new ScrollPane(messages);
        scrollMessages.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollMessages.setVbarPolicy(ScrollBarPolicy.NEVER);
        
        setSize(scrollMessages, 200, 270);
        scrollMessages.relocate(0, 0);
        scrollMessages.setVvalue(1.0);

        chatInput = new TextField();
        setSize(chatInput, 150, 30);
        chatInput.relocate(0, 270);

        sendChat = new Button("Send");
        setSize(sendChat, 50, 30);
        sendChat.relocate(150, 270);

        chatPane.getChildren().addAll(scrollMessages, chatInput, sendChat);

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

        Slider betSlider = new Slider(0, maxBet, 0);
        if(maxBet > 100){ 
            betSlider.setValue(100);
        } else {    
            betSlider.setValue(maxBet);
        }
        betSlider.setShowTickLabels(true);
        betSlider.setShowTickMarks(true);
        betSlider.setMajorTickUnit(100);

        setSize(betSlider, 200, 50);
        betSlider.relocate(0, 0);

        betInput = new TextField();
        setSize(betInput, 200, 50);
        betInput.relocate(0, 50);

        betSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                betInput.setText(Integer.toString(new_val.intValue())); 
            }
        });

        betInput.setText(Integer.toString((int)Math.round(betSlider.getValue())));

        sendBet = new Button("Send");
        setSize(sendBet, 200, 100);
        sendBet.relocate(0, 100);
        format(sendBet, "green", "transparent", 0);

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
        String s = chatInput.getText();
        chatInput.setText("");
        return s;
    }

    public void updateChat(ArrayList<String> chatArray){
        String s = "";
        for(String line : chatArray){
            s += line + "\n";
        }
        messages.setText(s);
        scrollMessages.setVvalue(1.0);
    }

    private Pane createTurnPane(){
        Pane turnPane = new Pane();
        setSize(turnPane, 200, 200);
        turnPane.relocate(0, 300);

        cardBTN = new Button("Card");
        setSize(cardBTN, 200, 100);
        cardBTN.relocate(0, 0);
        format(cardBTN, "green", "transparent", 0);

        stopBTN = new Button("Stop");
        setSize(stopBTN, 200, 100);
        stopBTN.relocate(0, 100);
        format(stopBTN, "red", "transparent", 0);

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
        dealerPane.setStyle("-fx-background-color: #0c4;");
        setSize(dealerPane, 900, 200);
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

    private HBox createPlayersPane(String playersState){
        HBox playersPane = new HBox();
        playersPane.setAlignment(Pos.CENTER);
        String[] players = playersState.split("#");
        int i;
        for(i=0; i<players.length; i++){
            Pane player = createPlayer(players[i]);
            playersPane.getChildren().add(player);
        }

        ObservableList<Node> workingCollection = FXCollections.observableArrayList(playersPane.getChildren());
        Collections.reverse(workingCollection);
        playersPane.getChildren().setAll(workingCollection);
        
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
            nextIcon.setFitHeight(20); 
            nextIcon.setFitWidth(20);

            nextPane.getChildren().add(nextIcon);
        }

        Pane onlyPlayer = new Pane();
        setSize(onlyPlayer, 150, 275);
        onlyPlayer.relocate(0, 25);
        format(onlyPlayer, "transparent", "black", 5);        

        HBox betPane = new HBox(5);
        setSize(betPane, 150, 25);
        betPane.relocate(0, 0);
        betPane.setAlignment(Pos.CENTER);

        ImageView tokenIcon = new ImageView(new Image("/blackjackclient/media/token.png"));
        tokenIcon.setFitHeight(20); 
        tokenIcon.setFitWidth(20);

        Label bet = new Label(data[4]);

        betPane.getChildren().addAll(tokenIcon, bet);

        Label name = new Label("[" + Integer.toString(Integer.parseInt(data[0])+1) + "] " + data[1]);
        setSize(name, 150, 25);
        name.relocate(0, 25);
        name.setAlignment(Pos.CENTER);

        HBox moneyPane = new HBox(5);
        setSize(moneyPane, 150, 25);
        moneyPane.relocate(0, 50);
        moneyPane.setAlignment(Pos.CENTER);

        ImageView moneyIcon = new ImageView(new Image("/blackjackclient/media/money.png"));
        moneyIcon.setFitHeight(20); 
        moneyIcon.setFitWidth(20);

        Label money = new Label(data[3]);

        moneyPane.getChildren().addAll(moneyIcon, money);

        HBox sumPane = new HBox(5);
        if(!data[6].equals("0")){
            setSize(sumPane, 150, 25);
            sumPane.relocate(0, 75);
            sumPane.setAlignment(Pos.CENTER);

            ImageView sumIcon = new ImageView(new Image("/blackjackclient/media/sum.png"));
            sumIcon.setFitHeight(20); 
            sumIcon.setFitWidth(20);

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
        
        Pane cardsPane = new Pane();
        cardsPane.setMinWidth(150);
        cardsPane.setPrefWidth(150);
        cardsPane.setMaxWidth(150);
        //cardsPane.setAlignment(Pos.CENTER);
        if(data[5].length()>0){
            createCardsPane(cardsPane, data[5], true);
        }

        ScrollPane scrollCardsPane = new ScrollPane(cardsPane);
        scrollCardsPane.setStyle("-fx-background-color: transparent;");
        scrollCardsPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollCardsPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        setSize(scrollCardsPane, 150, 175);
        format(scrollCardsPane, "transparent", "transparent", 5);
        scrollCardsPane.relocate(0, 100);
        
        if(myId.equals(data[0])){
            format(name, "silver", "black", 12);
        }
        onlyPlayer.getChildren().addAll(betPane, name, moneyPane, sumPane, scrollCardsPane);
        playerPane.getChildren().addAll(nextPane, onlyPlayer);    
        
        return playerPane;
    }

    private void createCardsPane(Pane cardsPane, String cardsString, boolean player){
        if(cardsString.length()>0){
            String[] cards = cardsString.split(" ");
            for(int i=0; i<cards.length; i++){
                BorderPane card = createCard(cards[i]);
                card.relocate(50, i*30);
                cardsPane.getChildren().add(card);
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
        format(cardPane, "white", "black", 3);
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

    private void format(javafx.scene.layout.Region obj, String bgcolor, String brcolor, int round){
        obj.setStyle("-fx-background-color: " + bgcolor + "; -fx-background-radius: " + Integer.toString(round) + "; -fx-border-color: " + brcolor + "; -fx-border-radius: " + Integer.toString(round) + ";");
    }

    
}