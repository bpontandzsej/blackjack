package blackjackclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    private Socket chatClientSocket;
    private PrintWriter chatSender;
    private Scanner chatReceiver;

    private ConnectPane connectPane;
    private TablePane tablePane;

    private Stage stage;
    private boolean running;

    static ArrayList<String> chatArray;

    private String myName;
    private String myId;

    private String lastState;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primarystage){
            chatArray = new ArrayList<String>();
            stage = primarystage;
            stage.setTitle("Blackjack - Connect");
            stage.setResizable(false);
            connectPane = new ConnectPane();
            Scene scene = new Scene(connectPane, 500, 300);
            stage.setScene(scene);
            stage.show();
            try{
                connectToServer();
                sendMSG("");
                String names = getMSG();
                connectPane.getConnectButton().setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        if(connectPane.getNicknameInput().length()>0){
                            
                            if(names.indexOf("#" + connectPane.getNicknameInput() + "#") == -1){
                                sendMSG(connectPane.getNicknameInput());
                                connectPane.setWaitingText();
                                Thread inputChecker = new Thread(){
                                    public void run(){
                                        while(running){
                                            inbox(getMSG());
                                        }
                                    }
                                };
                                inputChecker.start();
                            }

                            
                            /*getMSG();
                            createChatThread();*/
                            
                            
                        }
                        myName = connectPane.getNicknameInput();
                        
                        //myId = getMSG();
                    }
                });
            } catch(UnknownHostException e){
                System.out.println("Nem letezo host");
            } catch(IOException e){
                System.out.println("Hiba tortent a socket letrehozasa soran");
            }
            
            
    }

    /*private void createChatThread(){
        clientSocket = new Socket("localhost", 1234);
        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());
    }*/

    private void connectToServer() throws UnknownHostException, IOException{
        running = true;
        clientSocket = new Socket("localhost", 1234);
        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());

        chatClientSocket = new Socket("localhost", 1235);
        chatSender = new PrintWriter(chatClientSocket.getOutputStream(), true);
        chatReceiver = new Scanner(chatClientSocket.getInputStream());
        Thread chatHandler = new Thread(){
            @Override
            public void run(){
                String msg = getChatMSG();
                while(running){
                    inbox(msg);
                    msg = getChatMSG();
                }
            }
        };
        chatHandler.start();
    }

    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }

    private void sendChatMSG(String msg){
        chatSender.println(msg);
    }

    private String getChatMSG(){
        return chatReceiver.nextLine();
    }

    private void inbox(String msg){
        if(msg.substring(0, 6).equals("_svms_")){
            chatArray.add(msg.substring(6));
            inbox(lastState);
        } else {
            switch(msg.substring(0, 6)){
                case "_id___":
                    myId = msg.substring(6);
                    sendMSG("");
                break;
                case "_strt_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createBlankTableScene();
                        }
                    });
                    sendMSG("");
                    //lastState = msg;
                    
                break;
                case "_stat_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "", chatArray);
                        }
                    });
                    sendMSG("");
                    //lastState = msg;
                break;
                case "_bet__":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "bet", chatArray);
                        }
                    });
                    //lastState = msg;
                break;
                case "_turn_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "turn", chatArray);
                        }
                    });
                    //lastState = msg;
                break;
                case "_chat_": 
                    chatArray.add(msg.substring(6));
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane.updateChat(chatArray);
                        }
                    });                    
                    //inbox(lastState);
                break;
                case "_bye__":
                    
                break;
            }
            if(!msg.substring(0, 6).equals("_chat_")) lastState = msg;
        }
        
    }

    private void createBlankTableScene(){
        Pane pane = new Pane();
        Scene scene = new Scene(pane, 1100, 500);
        stage.setScene(scene);
    }

    private void createTableScene(String state, String whichPane, ArrayList<String> chatArray){
        tablePane = new TablePane(state, whichPane, chatArray, myId);
        switch(whichPane){
            case "bet":
                tablePane.getBetButton().setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        String s = tablePane.getBet();
                        if(s.matches("[0-9]+")){
                            if(Integer.parseInt(s)>0){
                                sendMSG(s);
                            }
                        }
                    }
                });
            break;

            case "turn":
                tablePane.getCardButton().setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        sendMSG("#card");    
                    }
                });
                tablePane.getStopButton().setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        sendMSG("#stop");    
                    }
                });
            break;
        }

        tablePane.getSendChat().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                sendChatMSG("_chat_[" + myName + "] " + tablePane.getChatMessage());
            }
        });

        Scene scene = new Scene(tablePane, 1100, 500);
        stage.setScene(scene);
    }



}