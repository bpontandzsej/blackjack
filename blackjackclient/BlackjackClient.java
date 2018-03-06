package blackjackclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import blackjackclient.ConnectPane;
import blackjackclient.TablePane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class BlackjackClient extends Application{

    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;

    private ConnectPane connectPane;
    private TablePane tablePane;

    private Stage stage;
    private boolean running;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primarystage){
        try{      
            connectToServer();
            stage = primarystage;
            stage.setTitle("Blackjack - Connect");
            stage.setResizable(false);
            connectPane = new ConnectPane();
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
        running = true;
        Thread inputChecker = new Thread(){
            public void run(){
                while(running){
                    inbox(getMSG());
                }
            }
        };
        inputChecker.start();
    }

    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }

    private void inbox(String msg){
        System.out.println(msg);
        //connectPane.setWaitingText();
        switch(msg.substring(0, 6)){
            case "_strt_":
                //need to generate the gui
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        createBlankTableScene();
                    }
                });
                sendMSG("");
                
                
            break;
            case "_stat_":
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        createTableScene(msg.substring(6));
                    }
                });
                sendMSG("");
            break;
            case "_bet__":
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        createTableScene(msg.substring(6));
                    }
                });
            break;
            case "_turn_":
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        createTableScene(msg.substring(6));
                    }
                });
            break;
            case "_chat_":

            break;
            case "_bye__":

            break;
        }
    }

    private void createBlankTableScene(){
        Pane pane = new Pane();
        Scene scene = new Scene(pane, 1100, 500);
        stage.setScene(scene);
    }

    private void createTableScene(String state){
        tablePane = new TablePane(state);
        Scene scene = new Scene(tablePane, 1100, 500);
        stage.setScene(scene);
    }



}