package blackjackclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
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
        Properties clientProperties = getProperties();
        chatArray = new ArrayList<String>();
        stage = primarystage;
        stage.setTitle("Blackjack - Connect");
        stage.setResizable(false);
        connectPane = new ConnectPane(false, "");
        Scene scene = new Scene(connectPane, 500, 300);
        stage.setScene(scene);
        stage.show();
        try{
            connectToServer(clientProperties);
            sendMSG("");
            Thread inputChecker = new Thread(){
                public void run(){
                    while(running){
                        inbox(getMSG());
                    }
                }
            };
            inputChecker.start();
            
        } catch(UnknownHostException e){
            System.out.println("Nem letezo host");
        } catch(IOException e){
            System.out.println("Hiba tortent a socket letrehozasa soran");
        } catch(Exception e){

        } 
    }

    static Properties getProperties(){
        final String propertiesFileName = "blackjackclient/default.properties";

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try {
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            inputForConfig.close();
        } catch (IOException e) {

        } 
        return properties;
    }

    private void connectToServer(Properties clientProperties) throws UnknownHostException, IOException, Exception{
        running = true;
        clientSocket = new Socket(clientProperties.getProperty("host"), Integer.parseInt(clientProperties.getProperty("port")));

        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());

        chatClientSocket = new Socket(clientProperties.getProperty("host"), Integer.parseInt(clientProperties.getProperty("chatport")));
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
        System.out.println(msg);
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
                break;
                case "_stat_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "", chatArray);
                        }
                    });
                    sendMSG("");
                break;
                case "_bet__":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "bet", chatArray);
                        }
                    });
                break;
                case "_turn_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            createTableScene(msg.substring(6), "turn", chatArray);
                        }
                    });
                break;
                case "_chat_": 
                    chatArray.add(msg.substring(6));
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane.updateChat(chatArray);
                        }
                    });
                break;
                case "_bye__":
                    
                break;
                case "_gtnm_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            String names = msg.substring(6);
                            ConnectPane connectPane = new ConnectPane(true, names);
                            Scene scene = new Scene(connectPane, 500, 300);
                            stage.setScene(scene);
                            connectPane.getConnectButton().setOnAction(new EventHandler<ActionEvent>(){
                                @Override
                                public void handle(ActionEvent event) {
                                    if(connectPane.getNicknameInput().length()>0){
                                        if(names.indexOf("#" + connectPane.getNicknameInput() + "#") == -1){
                                            sendMSG(connectPane.getNicknameInput());
                                        }
                                    }
                                    myName = connectPane.getNicknameInput();
                                }
                            });
                        }
                    });
                    
                break;
                case "_nms__":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            String names = msg.substring(6);
                            ConnectPane connectPane = new ConnectPane(false, names);
                            Scene scene = new Scene(connectPane, 500, 300);
                            stage.setScene(scene);
                        }
                    });
                    sendMSG("");
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