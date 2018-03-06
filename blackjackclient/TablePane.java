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

public class TablePane extends Pane{
    private Pane chatPane;

    public TablePane(String state){
        /*String [] blocks = state.split("$");
        GridPane chat = new GridPane();
        chat.setPrefSize(200, 200);
        Text messages = new Text("fdfdsfsf\ngdgggsgs\ngdfgdg");
        chat.add(messages, 0, 0, 2, 1);
        TextField input = new TextField();
        chat.add(input, 0, 1);
        Button send = new Button("Send");
        send.setPrefSize(50, 20);
        chat.add(send, 1, 1);
        

        //chat
        add(chat, 0, 0);

        String [] dealerBlocks = blocks[0].split(";");
        BorderPane dealer = new BorderPane();
        dealer.setPrefSize(900, 200);
        Text dealerName = new Text(dealerBlocks[0]);
        dealer.setTop(dealerName);

        Text cards = new Text(dealerBlocks[1]);
        dealer.setCenter(cards);

        Text dealerMoney = new Text(dealerBlocks[2]);
        dealer.setBottom(dealerMoney);




        //dealer
        add(dealer, 1, 0);

        //action panel
        add(, 0, 1);

        //players
        //add(, 1, 1);*/



        getChildren().add(createChatPane());
        getChildren().add(createBetPane());
        getChildren().add(createDealerPane());
        getChildren().add(createPlayersPane());
    }

    private Pane createChatPane(){
        chatPane = new Pane();
        setSize(chatPane, 200, 300);
        chatPane.setStyle("-fx-background-color: green;");
        chatPane.relocate(0, 0);
        
        Text messages = new Text("-Chat chat chat\n-Megfogtad a kilin\n-Chat chat chat");
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
        setSize(betPane, 900, 200);
        betPane.setStyle("-fx-background-color: blue;");
        betPane.relocate(200, 0);

        return betPane;
    }

    private Pane createTurnPane(){
        return null;
    }

    private Pane createDealerPane(){
        Pane dealerPane = new Pane();
        setSize(dealerPane, 200, 200);
        dealerPane.setStyle("-fx-background-color: red;");
        dealerPane.relocate(0, 300);

        return dealerPane;
    }

    private Pane createPlayersPane(){
        Pane playersPane = new Pane();
        setSize(playersPane, 900, 300);
        playersPane.setStyle("-fx-background-color: yellow;");
        playersPane.relocate(200, 200);

        return playersPane;
    }

    private void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    
}