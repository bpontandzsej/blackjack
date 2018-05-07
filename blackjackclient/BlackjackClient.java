package blackjackclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BlackjackClient extends Application{
    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;

    private Socket chatClientSocket;
    private PrintWriter chatSender;
    private Scanner chatReceiver;

    private ConnectPane connectPane;
    private TablePane tablePane;
    private MenuPane menuPane;

    private Stage stage;
    private boolean running;

    static ArrayList<String> chatArray;

    private String myName;
    private String myId;

    private String lastState;
    private int betLoop;
    private int turnLoop;
    private int nameLoop;

    private Alert confirm;

    private Timer betTimer = new Timer();
    private Timer turnTimer = new Timer();
    private Timer connectTimer = new Timer();

    private Properties clientProperties;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primarystage){
        clientProperties = getProperties();
        stage = primarystage;
        stage.setScene(new Scene(new Pane(), 500, 500));
        
        stage.getIcons().add(new Image("/blackjackclient/media/sum.png"));
        stage.setTitle("Blackjack");
        stage.setResizable(false);
        
        menuPane = new MenuPane();
        Scene scene = new Scene(menuPane, 500, 500);
        initMenuButtons(clientProperties);
        stage.show();
        stage.setScene(scene);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(running){
                    event.consume();
                    confirm = new Alert(AlertType.CONFIRMATION);
                    confirm.setTitle("Blackjack");
                    confirm.setContentText("Are you sure you want to leave this table?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.get() == ButtonType.OK){
                        turnTimer.cancel();
                        betTimer.cancel();
                        connectTimer.cancel();
                        running = false;
                        try{
                            sendMSG("#close");
                        } catch(Exception e){
                        }
                        
                        showMenu();
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                    } else {
                    }
                } else {
                    closeAll();
                }
           }
        });
    }

    private void initMenuButtons(Properties clientProperties){
        menuPane.getNewGameButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                connectPane = new ConnectPane(false, "");
                Scene scene = new Scene(connectPane, 500, 500);
                stage.setScene(scene);
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                try{
                    connectToServer(clientProperties);
                    sendMSG("");
                    Thread inputChecker = new Thread(){
                        public void run(){
                            while(running){
                                try{
                                    inbox(getMSG());
                                } catch(Exception e){
                                    System.out.println("Megszakadt a kapcsolat a szerverrel");
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            Alert alert = new Alert(AlertType.INFORMATION);
                                            alert.setTitle("Blackjack");
                                            alert.setHeaderText("Server message");
                                            alert.setContentText("You have lost the connection with sthe server...");
                                            alert.showAndWait();
                                            showMenu();
                                        }
                                    });
                                    running = false;
                                }
                            }
                            return;
                        }
                    };
                    inputChecker.start();
                    running = true;
                } catch(UnknownHostException e){
                    System.out.println("Nem letezo host");
                    connectPane.setNamesLabelText("Failed to connect to the server...");
                    alertForFailedConnect();
                    showMenu();
                } catch(IOException e){
                    System.out.println("Hiba tortent a socket letrehozasa soran");
                    connectPane.setNamesLabelText("Failed to connect to the server...");
                    alertForFailedConnect();
                    showMenu();
                } catch(Exception e){
                    System.out.println("Varatlan hiba");
                    connectPane.setNamesLabelText("Failed to connect to the server...");
                    alertForFailedConnect();
                    showMenu();
                }
            }
        });
            
        menuPane.getExitButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                closeAll();
            }
        });
    }

    static Properties getProperties(){
        final String propertiesFileName = "client_config.properties";

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try {
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            inputForConfig.close();
        } catch (IOException e) {
            System.out.println("The config.properties file is missing");
            System.exit(0);
        } 
        return properties;
    }

    private void connectToServer(Properties clientProperties) throws UnknownHostException, IOException, Exception{
        clientSocket = new Socket(clientProperties.getProperty("host"), Integer.parseInt(clientProperties.getProperty("port")));
        chatArray = new ArrayList<String>();
        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());

        chatClientSocket = new Socket(clientProperties.getProperty("host"), Integer.parseInt(clientProperties.getProperty("chatport")));
        chatSender = new PrintWriter(chatClientSocket.getOutputStream(), true);
        chatReceiver = new Scanner(chatClientSocket.getInputStream());
        Thread chatHandler = new Thread(){
            @Override
            public void run(){
                try{
                    String msg = getChatMSG();
                    while(running){
                        inbox(msg);
                        msg = getChatMSG();
                    }
                } catch(NoSuchElementException e){
                    System.out.println("You have lost the connection with the server...");
                }
                return;
            }
        };
        chatHandler.start();
    }

    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG() throws NoSuchElementException{
        return receiver.nextLine();
    }

    private void sendChatMSG(String msg){
        chatSender.println(msg);
    }

    private String getChatMSG() throws NoSuchElementException{
        return chatReceiver.nextLine();
    }

    private void inbox(String msg){
        System.out.println(msg);
            switch(msg.substring(0, 6)){
                case "_id___":
                    myId = msg.substring(6);
                    sendMSG("");
                break;
                case "_svms_":
                    if(running){
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Blackjack");
                                alert.setHeaderText("Server message");
                                alert.setContentText(msg.substring(6));
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask(){
                                    @Override
                                    public void run() {
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                try{
                                                    alert.close();
                                                } catch(Exception e){
                                                    
                                                }
                                                return;
                                            }
                                        });
                                    }
                                }, 10*1000);
                                Optional<ButtonType> result = alert.showAndWait();
                                if(result.isPresent()){
                                    timer.cancel();
                                }
                            }
                        });
                    }
                    sendMSG("");
                break;
                case "_strt_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane = new TablePane(myId);
                            tablePane.setStyle("-fx-background-image: url('/blackjackclient/media/greentable.png'); -fx-font: 16px sans-serif;");
                            tablePane.getSendChat().setOnAction(new EventHandler<ActionEvent>(){
                                @Override
                                public void handle(ActionEvent event) {
                                    String s = tablePane.getChatMessage();
                                    if(s.length()>0){
                                        sendChatMSG("_chat_[" + myName + "] " + s);
                                    }
                                }
                            });
                            tablePane.updateChatPane(chatArray);
                            Scene scene = new Scene(tablePane, 1100, 500);
                            stage.setScene(scene);
                            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                            return;
                        }
                    });
                    sendMSG("");
                break;
                case "_stat_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            updateDealerAndPlayers(msg.substring(6));
                            return;
                        }
                    });
                    sendMSG("");
                break;
                case "_bet__":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane.updateActionPane("_bet__", msg.substring(6), "");
                            updateDealerAndPlayers(msg.substring(6));
                            initBetButtons();
                            betTimer = new Timer();
                            betLoop = 4*30;
                            betTimer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    betLoop--;
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            tablePane.setBetRemaining((betLoop*100/120));
                                            return;
                                        }
                                    });
                                    if(betLoop==0){
                                        sendMSG("#skip");
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                tablePane.updateActionPane("", "", "");
                                                return;
                                            }
                                        });                                    
                                        betTimer.cancel();                                    
                                    }
                                }
                            }, 250, 250);
                            return;
                        }
                    });
                break;
                case "_turn_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane.updateActionPane("_turn_", msg.substring(6), Integer.toString(getOdds(msg.substring(6))));
                            updateDealerAndPlayers(msg.substring(6));
                            initButtons(msg.substring(6));
                            turnTimer = new Timer();
                            turnLoop = 4*30;
                            turnTimer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    turnLoop--;
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            tablePane.setCardRemaining((turnLoop*100/120));
                                            return;
                                        }
                                    });
                                    if(turnLoop==0){
                                        sendMSG("#stop");
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                tablePane.updateActionPane("", "", "");
                                                try{
                                                    confirm.close();
                                                } catch(Exception e){

                                                }                                                
                                                return;
                                            }
                                        });                                    
                                        turnTimer.cancel();                                    
                                    }
                                }
                            }, 250, 250);
                            return;
                        }
                    });
                break;
                case "_chat_":
                    chatArray.add(msg.substring(6));
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            tablePane.updateChatPane(chatArray);
                            return;
                        }
                    });
                break;
                case "_bye__":
                    System.out.println("game is over");
                break;
                case "_gtnm_":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            String names = msg.substring(6);
                            ConnectPane connectPane = new ConnectPane(true, names);
                            Scene scene = new Scene(connectPane, 500, 500);
                            stage.setScene(scene);
                            connectTimer = new Timer();
                            nameLoop = 4*60;
                            connectTimer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    nameLoop--;
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            connectPane.setNameRemaining((nameLoop*100/240));
                                            return;
                                        }
                                    });
                                    if(nameLoop==0){
                                        connectTimer.cancel();
                                        sendMSG("#player");
                                    }
                                }
                            }, 250, 250);
                            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                            connectPane.getConnectButton().setOnAction(new EventHandler<ActionEvent>(){
                                @Override
                                public void handle(ActionEvent event) {
                                    String inputName = connectPane.getNicknameInput();
                                    if(inputName.length()>=3){
                                        if(names.indexOf("#" + inputName + "#") == -1){
                                            connectTimer.cancel();
                                            sendMSG(inputName);
                                        }
                                    }
                                }
                            });
                            return;
                        }
                    });
                break;
                case "_nms__":
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            String names = msg.substring(6);
                            ConnectPane connectPane = new ConnectPane(false, names);
                            Scene scene = new Scene(connectPane, 500, 500);
                            stage.setScene(scene);
                            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                            return;
                        }
                    });
                    sendMSG("");
                break;
                case "_ynm__":
                    myName = msg.substring(6);
                    sendMSG("");
                break;
            }
        if(!msg.substring(0, 6).equals("_chat_")) lastState = msg;        
    }

    private void updateDealerAndPlayers(String state){
        String[] dealerAndPlayers = state.split("@");
        tablePane.updateDealerPane(dealerAndPlayers[0]);
        tablePane.updatePlayerPane(dealerAndPlayers[1]);
    }

    private void alertForFailedConnect(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Blackjack");
        alert.setContentText("Failed to connect to server. Please check the HOST and the PORT, then retry...");
        alert.showAndWait();
    }

    private void showMenu(){
        menuPane = new MenuPane();
        initMenuButtons(clientProperties);
        Scene scene = new Scene(menuPane, 500, 500);
        stage.setScene(scene);
    }

    private void initButtons(String state){
        tablePane.getCardButton().setOnAction(new EventHandler<ActionEvent>(){
            int odds = getOdds(state);
            @Override
            public void handle(ActionEvent event) {
                if(odds < 30){
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                if(tablePane.needHelp()){
                                    confirm = new Alert(AlertType.CONFIRMATION);
                                    confirm.setTitle("Blackjack");
                                    confirm.setHeaderText("You have " + Integer.toString(odds) + "% chance for good card");
                                    confirm.setContentText("Are you sure you want to Hit?");
                                    Timer confirmTimer = new Timer();
                                    confirmTimer.schedule(new TimerTask(){
                                        @Override
                                        public void run() {
                                            Platform.runLater(new Runnable(){
                                                @Override
                                                public void run() {
                                                    try{
                                                        confirm.close();
                                                    } catch(Exception e){
                                                        
                                                    }
                                                    return;
                                                }
                                            });                                    
                                        }
                                    }, 10*1000);
                                    Optional<ButtonType> result = confirm.showAndWait();
                                    if (result.get() == ButtonType.OK){
                                        confirmTimer.cancel();
                                        sendMSG("#card");
                                        tablePane.updateActionPane("", "", "");
                                    } else {
                                        confirmTimer.cancel();
                                    }
                                } else {
                                    sendMSG("#card");
                                    tablePane.updateActionPane("", "", "");
                                }
                            }
                        });
                } else {
                    turnTimer.cancel();
                    sendMSG("#card");
                    tablePane.updateActionPane("", "", "");
                    
                }                
            }
        });
        tablePane.getStopButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                turnTimer.cancel();
                sendMSG("#stop");
                tablePane.updateActionPane("", "", "");
                
            }
        });
    }

    private void initBetButtons(){
        tablePane.getBetButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                String s = tablePane.getBet();
                if(s.matches("[0-9]+")){
                    if(Integer.parseInt(s)>0){
                        sendMSG(s);
                        betTimer.cancel();
                        tablePane.updateActionPane("", "", "");
                    }
                }
            }
        });
        tablePane.getSkipBetButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                betTimer.cancel();
                sendMSG("#skip");
                tablePane.updateActionPane("", "", "");
            }
        });
    }

    private int getOdds(String state){
        String[] dealerAndPlayers = state.split("@");
        String[] players = dealerAndPlayers[1].split("#");
        int[] deck = new int[10];
        for(int i=0; i<9; i++){
            deck[i] = 4;
        }
        deck[9] = 16;
        int mySum = 0;
        for(int i=0; i<players.length; i++){
            String[] data = players[i].split(";");
            if(myId.equals(data[0])){
                String[] sumString = data[6].split("/");
                mySum = Integer.parseInt(sumString[sumString.length-1]);
            }
            if(data[5].length()>0){
                String[] cards = data[5].split(" ");
                for(int j = 0; j<cards.length; j++){
                    String[] card = cards[j].split(":");
                    int k = Integer.parseInt(card[1]);
                    if(k>=10){
                        deck[9]--;
                    } else {
                        deck[k-1]--;
                    }
                }
            }
        }
        int cardsCount = 0;
        int goodCard = 0;
        for(int i=0; i<deck.length; i++){
            cardsCount += deck[i];
            if(i+1<=21-mySum){
                goodCard += deck[i];
            }
        }
        return Math.round(goodCard*100/cardsCount);
    }

    private void closeAll(){
        running = false;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                stage.close();
                Platform.exit();
                System.exit(0);
                return;
            }
        });
    }
}