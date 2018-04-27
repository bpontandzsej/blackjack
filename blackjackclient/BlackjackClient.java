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
    private int loop;

    private Alert confirm;

    private Timer timer = new Timer();

    private Properties clientProperties;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primarystage){
        clientProperties = getProperties();
        chatArray = new ArrayList<String>();
        stage = primarystage;
        stage.getIcons().add(new Image("/blackjackclient/media/sum.png"));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(running){
                    event.consume();
                    confirm = new Alert(AlertType.CONFIRMATION);
                    confirm.setTitle("Blackjack");
                    confirm.setContentText("Biztosan tavozni szeretnel ettol az asztaltol?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.get() == ButtonType.OK){
                        running = false;
                        sendMSG("#close");
                        menuPane = new MenuPane();
                        Scene scene = new Scene(menuPane, 500, 500);
                        initMenuButtons(clientProperties);
                        stage.setScene(scene);
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                    } else {
                        //confirmTimer.cancel();
                    }
                    
                } else {
                    closeAll();
                }
           }
        });
        stage.setTitle("Blackjack");
        stage.setResizable(false);
        stage.show();
        menuPane = new MenuPane();
        Scene scene = new Scene(menuPane, 500, 500);
        initMenuButtons(clientProperties);
        stage.setScene(scene);
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void initMenuButtons(Properties clientProperties){
        menuPane.getNewGameButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                connectPane = new ConnectPane(false, "");
                Scene scene = new Scene(connectPane, 500, 300);
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
                                } catch(NoSuchElementException e){
                                    System.out.println("Megszakadt a kapcsolat a szerverrel");
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            Alert alert = new Alert(AlertType.INFORMATION);
                                            alert.setTitle("Blackjack");
                                            alert.setHeaderText("Szerver uzenet");
                                            alert.setContentText("Megszakadt a kapcsolat a szerverrel...");
                                            alert.showAndWait();
                                            //closeAll();
                                        }
                                    });
                                    running = false;
                                }
                            }
                            return;
                        }
                    };
                    inputChecker.start();
                    
                } catch(UnknownHostException e){
                    System.out.println("Nem letezo host");
                    connectPane.setNamesLabelText("Nem sikerult csatlakozni a szerverhez...");
                } catch(IOException e){
                    System.out.println("Hiba tortent a socket letrehozasa soran");
                    connectPane.setNamesLabelText("Nem sikerult csatlakozni a szerverhez...");
                } catch(Exception e){
                    System.out.println("Varatlan hiba");
                    connectPane.setNamesLabelText("Nem sikerult csatlakozni a szerverhez...");
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
        final String propertiesFileName = "blackjackclient/default.properties";

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try {
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            inputForConfig.close();
        } catch (IOException e) {
            System.out.println("Nem talalhato default.properties fajl vagy nem nyithato meg");
            System.exit(0);
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
                try{
                    String msg = getChatMSG();
                    while(running){
                        inbox(msg);
                        msg = getChatMSG();
                    }
                } catch(NoSuchElementException e){
                    System.out.println("Megszakadt a kapcsolat a szerverrel");
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
                                alert.setHeaderText("Szerver uzenet");
                                alert.setContentText(msg.substring(6));
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask(){
                                    @Override
                                    public void run() {
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                alert.close();
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
                            timer = new Timer();
                            loop = 120;
                            timer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    loop--;
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            tablePane.setBetRemaining((loop*100/120));
                                            return;
                                        }
                                    });
                                    if(loop==0){
                                        sendMSG("#skip");
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                tablePane.updateActionPane("", "", "");
                                                return;
                                            }
                                        });                                    
                                        timer.cancel();                                    
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
                            /*if(tablePane.needHelp()){
                                tablePane.getCardButton().setText("Lapot kerek (" + Integer.toString(getOdds(msg.substring(6))) + "%)");
                            } else {
                                tablePane.getCardButton().setText("Lapot kerek");
                            }*/
                            updateDealerAndPlayers(msg.substring(6));
                            initButtons(msg.substring(6));
                            timer = new Timer();
                            loop = 120;
                            timer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    loop--;
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            tablePane.setCardRemaining((loop*100/120));
                                            return;
                                        }
                                    });
                                    if(loop==0){
                                        sendMSG("#stop");
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                tablePane.updateActionPane("", "", "");
                                                confirm.close();
                                                return;
                                            }
                                        });                                    
                                        timer.cancel();                                    
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
                            Scene scene = new Scene(connectPane, 500, 300);
                            stage.setScene(scene);
                            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
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
                            Scene scene = new Scene(connectPane, 500, 300);
                            stage.setScene(scene);
                            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
                            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                            return;
                        }
                    });
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

    private void initButtons(String state){
        tablePane.getCardButton().setOnAction(new EventHandler<ActionEvent>(){
            int odds = getOdds(state);
            @Override
            public void handle(ActionEvent event) {
                if(odds < 30){
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            confirm = new Alert(AlertType.CONFIRMATION);
                            confirm.setTitle("Blackjack");
                            confirm.setHeaderText(Integer.toString(odds) + "% eselyed van jo lapra");
                            confirm.setContentText("Mindenkeppen lapot kersz?");
                            Timer confirmTimer = new Timer();
                            confirmTimer.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            confirm.close();
                                            return;
                                        }
                                    });                                    
                                }
                            }, 10*1000);
                            Optional<ButtonType> result = confirm.showAndWait();
                            if (result.get() == ButtonType.OK){
                                sendMSG("#card");
                                tablePane.updateActionPane("", "", "");
                                confirmTimer.cancel();
                            } else {
                                confirmTimer.cancel();
                            }
                        }
                    });
                } else {
                    sendMSG("#card");
                    tablePane.updateActionPane("", "", "");
                    timer.cancel();
                }                
            }
        });
        tablePane.getStopButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                sendMSG("#stop");
                tablePane.updateActionPane("", "", "");
                timer.cancel();
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
                        timer.cancel();
                        tablePane.updateActionPane("", "", "");
                    }
                }
            }
        });
        tablePane.getSkipBetButton().setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
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