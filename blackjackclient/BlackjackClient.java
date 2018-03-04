package blackjackclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import blackjackclient.BlackjackClientConnect;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class BlackjackClient extends Application{

    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;

    private Stage stage;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primarystage){
        try{      
            connectToServer();
            stage = primarystage;
            stage.setTitle("Blackjack - Connect");

            BlackjackClientConnect connectPane = new BlackjackClientConnect();
            connectPane.getConnecButton().setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    connectPane.setWaitingText();
                    sendMSG(connectPane.getNicknameInput());    
                }
            });
            Scene scene = new Scene(connectPane, 500, 300);
            stage.setScene(scene);
            stage.show();
        } catch(UnknownHostException e){
            System.out.println("Nem letezo host");
        } catch(IOException e){
            System.out.println("Hiba tortent a socket letrehozasa soran");
        }        
    }

    private void connectToServer() throws UnknownHostException, IOException{
        clientSocket = new Socket("localhost", 1234);
        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());
               
    }

    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }

}