package blackjackclient;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TablePane extends Pane{
    private Pane chatPane;
    private Pane dealerPane;
    private Pane playersPane;
    private Pane actionPane;
    private Pane turnPaneChild;
    private TextField betInput;
    private Button sendBet;
    private Button skipBet;
    private Button cardBTN;
    private Button stopBTN;
    private Button sendChat;
    private TextField chatInput;
    private String myId;
    private Label messages;
    private Label odds;
    private Pane remainingCard;
    private Pane remainingBet;
    private ScrollPane scrollMessages;
    private CheckBox helpCheckBox;
    private String currentOdds = "";

    public TablePane(String myId){
        this.myId = myId;
        getChildren().add(createChatPane());
        getChildren().add(createActionPane());       
        getChildren().add(createDealerPane());
        getChildren().add(createPlayerPane());
        turnPaneChild = createTurnPane();
    }

    private Pane createChatPane(){
        chatPane = new Pane();
        format(chatPane, "#333", "transparent", 0, 0);
        setSize(chatPane, 200, 300);
        chatPane.relocate(0, 0);

        messages = new Label();
        messages.setStyle("-fx-font-size: 14px; -fx-font-color: white;");
        messages.setWrapText(true);
        messages.setMinWidth(200);
        messages.setPrefWidth(200);
        messages.setMaxWidth(200);

        scrollMessages = new ScrollPane(messages);
        scrollMessages.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollMessages.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollMessages.setVbarPolicy(ScrollBarPolicy.NEVER);
        
        setSize(scrollMessages, 200, 270);
        scrollMessages.relocate(0, 0);

        chatInput = new TextField();
        
        format(chatInput, "#ccc", "black", 1, 5);
        setSize(chatInput, 150, 30);
        chatInput.relocate(0, 270);

        ImageView sendIcon = new ImageView(new Image("/blackjackclient/media/message.png"));
        sendIcon.setFitHeight(25); 
        sendIcon.setFitWidth(25);

        sendChat = new Button();
        sendChat.setGraphic(sendIcon);
        setSize(sendChat, 50, 30);
        sendChat.relocate(150, 270);

        chatInput.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                sendChat.fire();
                ev.consume(); 
            }
        });

        chatPane.getChildren().addAll(scrollMessages, chatInput, sendChat);

        return chatPane;
    }

    private Pane createActionPane(){
        actionPane = new Pane();
        format(actionPane, "#aca", "transparent", 0, 0);
        setSize(actionPane, 200, 200);
        actionPane.relocate(0, 300);

        return actionPane;
    }

    private Pane createDealerPane(){
        dealerPane = new Pane();
        setSize(dealerPane, 900, 200);     
        dealerPane.relocate(200, 0);

        helpCheckBox = new CheckBox("Segitseg");
        helpCheckBox.relocate(0, 0);
        helpCheckBox.setSelected(true);
        helpCheckBox.setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-border-color: black;");
        helpCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    cardBTN.setText("Lapot kerek (" + currentOdds + "%)");
                } else {
                    cardBTN.setText("Lapot kerek");
                }
            }
        });
                        
        return dealerPane;
    }

    private Pane createPlayerPane(){
        playersPane = new Pane();
        setSize(playersPane, 900, 300);
        playersPane.relocate(200, 200);

        return playersPane;
    }

    public void updateChatPane(ArrayList<String> chatArray){
        String s = "";
        for(String line : chatArray){
            s += line + System.lineSeparator();
        }
        messages.setText(s + System.lineSeparator());
        scrollMessages.setVvalue(1.0);
    }

    public Button getSendChat(){
        return sendChat;
    }

    public String getChatMessage(){
        String s = chatInput.getText();
        chatInput.setText("");
        return s;
    }

    public void updateActionPane(String whichPane, String state, String odds){
        actionPane.getChildren().clear();
        switch(whichPane){
            case "_bet__":
                String[] dealerAndPlayers = state.split("@");
                actionPane.getChildren().add(createBetPane(dealerAndPlayers[1]));
            break;
            case "_turn_":
                currentOdds = odds;
                actionPane.getChildren().add(turnPaneChild);
                if(helpCheckBox.isSelected()){
                    cardBTN.setText("Lapot kerek (" + odds + "%)");
                } else {
                    cardBTN.setText("Lapot kerek");
                }
            break;
            default:
            break;
        }
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
        final int finalMaxBet = maxBet;
        Pane betPaneChild = new Pane();
        setSize(betPaneChild, 200, 200);
        betPaneChild.relocate(0, 0);

        remainingBet = new Pane();
        format(remainingBet, "#48f", "transparent", 0, 5);
        setSize(remainingBet, 200, 10);
        remainingBet.relocate(0, 0);

        Slider betSlider = new Slider(0, maxBet, 0);
        if(maxBet > 100){ 
            betSlider.setValue(100);
        } else {    
            betSlider.setValue(maxBet);
        }
        betSlider.setShowTickLabels(true);
        betSlider.setShowTickMarks(true);
        betSlider.setMajorTickUnit(100);

        setSize(betSlider, 180, 50);
        betSlider.relocate(10, 10);

        betInput = new TextField();
        format(betInput, "transparent", "transparent", 0, 0);
        betInput.setAlignment(Pos.CENTER);
        setSize(betInput, 200, 30);
        betInput.relocate(0, 60);

        betSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                betInput.setText(Integer.toString(new_val.intValue())); 
            }
        });

        betInput.setText(Integer.toString((int)Math.round(betSlider.getValue())));
        betInput.textProperty().addListener((observable, oldValue, newValue) -> {
            int l = Integer.toString(finalMaxBet).length();
            
            if(!newValue.matches("\\d*")) {
                betInput.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                if(betInput.getText().length()>l){
                    betInput.setText(betInput.getText().substring(0, l));
                } else {
                    if(betInput.getText().length()>0){
                        if(Integer.parseInt(newValue)>finalMaxBet){
                            betInput.setText(Integer.toString(finalMaxBet));
                        }
                    }
                }               
            }
            if(betInput.getText().length()>0){
                betSlider.setValue(Integer.parseInt(betInput.getText()));
            }
        });

        ImageView betIcon = new ImageView(new Image("/blackjackclient/media/bet.png"));
        betIcon.setFitHeight(25); 
        betIcon.setFitWidth(25);

        sendBet = new Button("Tetet teszek", betIcon);
        setSize(sendBet, 200, 50);
        sendBet.relocate(0, 100);
        format(sendBet, "#7c7", "black", 1, 5);

        ImageView skipIcon = new ImageView(new Image("/blackjackclient/media/stop.png"));
        skipIcon.setFitHeight(25); 
        skipIcon.setFitWidth(25);

        skipBet = new Button("Kihagyom a kort", skipIcon);
        setSize(skipBet, 200, 50);
        skipBet.relocate(0, 150);
        format(skipBet, "#c77", "black", 1, 5);

        betPaneChild.getChildren().addAll(remainingBet, betSlider, betInput, sendBet, skipBet);

        return betPaneChild;
    }

    public String getBet(){
        return betInput.getText();
    }

    public Button getBetButton(){
        return sendBet;
    }
    
    public Button getSkipBetButton(){
        return skipBet;
    }

    private Pane createTurnPane(){
        turnPaneChild = new Pane();
        setSize(turnPaneChild, 200, 200);
        turnPaneChild.relocate(0, 0);

        ImageView cardIcon = new ImageView(new Image("/blackjackclient/media/card.png"));
        cardIcon.setFitHeight(25); 
        cardIcon.setFitWidth(25);

        cardBTN = new Button("Lapot kerek", cardIcon);
        setSize(cardBTN, 200, 95);
        cardBTN.relocate(0, 10);
        format(cardBTN, "#7c7", "black", 1, 5);

        remainingCard = new Pane();
        format(remainingCard, "#48f", "transparent", 0, 5);
        setSize(remainingCard, 200, 10);
        remainingCard.relocate(0, 0);

        ImageView stopIcon = new ImageView(new Image("/blackjackclient/media/stop.png"));
        stopIcon.setFitHeight(25); 
        stopIcon.setFitWidth(25);

        stopBTN = new Button("Megallok", stopIcon);
        setSize(stopBTN, 200, 95);
        stopBTN.relocate(0, 105);
        format(stopBTN, "#c77", "black", 1, 5);

        turnPaneChild.getChildren().addAll(cardBTN, remainingCard, stopBTN);

        return turnPaneChild;
    }

    public void setCardRemaining(int l){
        setSize(remainingCard, l*2, 10);
    }

    public void setBetRemaining(int l){
        setSize(remainingBet, l*2, 10);
    }

    public Label getOddsLabel(){
        return odds;
    }

    public Button getCardButton(){
        return cardBTN;
    }
    
    public Button getStopButton(){
        return stopBTN;
    }

    public void updateDealerPane(String dealerState){
        dealerPane.getChildren().clear();
        String[] data = dealerState.split(";");
        Pane dealerPaneChild = new Pane();
        setSize(dealerPaneChild, 900, 200);
        dealerPaneChild.relocate(0, 0);

        HBox moneyPane = new HBox(5);
        moneyPane.setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-border-color: black;");
        setSize(moneyPane, 900, 25);
        moneyPane.relocate(0, 25);
        moneyPane.setAlignment(Pos.CENTER);

        ImageView moneyIcon = new ImageView(new Image("/blackjackclient/media/money.png"));
        moneyIcon.setFitHeight(25); 
        moneyIcon.setFitWidth(25);

        Label money = new Label(data[0]);

        moneyPane.getChildren().addAll(moneyIcon, money);

        HBox sumPane = new HBox(5);
        sumPane.setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-border-color: black;");
        setSize(sumPane, 900, 25);
        sumPane.relocate(0, 150);
        if(!data[3].equals("0")){
            
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
        
        dealerPaneChild.getChildren().addAll(helpCheckBox, moneyPane, cardsPane, sumPane);
        dealerPane.getChildren().add(dealerPaneChild);

    }

    public void updatePlayerPane(String playersState){
        playersPane.getChildren().clear();
        HBox playerPaneChild = new HBox();
        setSize(playerPaneChild, 900, 300);
        playerPaneChild.relocate(0, 0);
        playerPaneChild.setAlignment(Pos.CENTER);
        String[] players = playersState.split("#");
        int i;
        for(i=0; i<players.length; i++){
            Pane player = createPlayer(players[i]);
            playerPaneChild.getChildren().add(player);
        }

        ObservableList<Node> workingCollection = FXCollections.observableArrayList(playerPaneChild.getChildren());
        Collections.reverse(workingCollection);
        playerPaneChild.getChildren().setAll(workingCollection);
        
        playersPane.getChildren().add(playerPaneChild);
    }

    private void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    private void format(javafx.scene.layout.Region obj, String bgcolor, String brcolor, int brwidth, int round){
        obj.setStyle("-fx-background-color: " + bgcolor + "; -fx-background-radius: " + Integer.toString(round) + "; -fx-border-width: " + Integer.toString(brwidth) + "; -fx-border-color: " + brcolor + "; -fx-border-radius: " + Integer.toString(round) + ";");
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
        if(myId.equals(data[0])){
            onlyPlayer.setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-border-color: black;");
        } else {
            onlyPlayer.setStyle("-fx-background-image: url('/blackjackclient/media/othertable.png'); -fx-border-color: black;");
        }         

        HBox betPane = new HBox(5);
        setSize(betPane, 150, 25);
        betPane.relocate(0, 0);
        betPane.setAlignment(Pos.CENTER);

        ImageView tokenIcon = new ImageView(new Image("/blackjackclient/media/token.png"));
        tokenIcon.setFitHeight(20); 
        tokenIcon.setFitWidth(20);

        Label bet = new Label(data[4]);

        betPane.getChildren().addAll(tokenIcon, bet);

        Label name = new Label(data[1]);
        setSize(name, 150, 25);
        name.relocate(0, 25);
        name.setAlignment(Pos.CENTER);
        format(name, "#6a6", "black", 1, 0);

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
        format(cardsPane, "transparent", "transparent", 0, 0);
        cardsPane.setMinWidth(150);
        cardsPane.setPrefWidth(150);
        cardsPane.setMaxWidth(150);
        if(data[5].length()>0){
            createCardsPane(cardsPane, data[5], true);
        }

        ScrollPane scrollCardsPane = new ScrollPane(cardsPane);
        scrollCardsPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollCardsPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollCardsPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        setSize(scrollCardsPane, 150, 175);
        
        scrollCardsPane.relocate(0, 100);
        scrollCardsPane.setVvalue(1.0);
        
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

    private BorderPane createCard(String cardString){
        BorderPane cardPane = new BorderPane();
        
        String[] card = cardString.split(":");
        setSize(cardPane, 50, 70);
        
        if(cardString.equals("x:x")){
            format(cardPane, "#666", "black", 3, 3);
        } else {
            String number = "";
            format(cardPane, "white", "black", 1, 3);
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
            tl.setTextFill(Color.BLACK);
            br.setTextFill(Color.BLACK);
    
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

    /*public boolean needHelp(){
        return helpCheckBox.isSelected();
    }*/
}