package blackjackserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import blackjackserver.BlackjackTable;

public class BlackjackServer {
    /**
     * BlackjackServer is the main class of the server.
     * This class collects the connections, then starts a thread with these connections.
     * The thread represents a table.
     */
    private int tableId;
    private ServerSocket serverSocket;
    private ServerSocket chatServerSocket;
    private int PLAYERS_PER_TABLE;
    private ArrayList<BlackjackTable> tables;
    private Properties tableProperties;
    private ArrayList<ArrayList<Socket>> socketQueue;

    /**
     * The constructor starts  a serverSocket and creates an arraylist for the tables.
     * @param serverProperties contains the settings of the server andthe table
     */
    public BlackjackServer(Properties serverProperties){
        tableId = 0;
        tableProperties = new Properties();
        try{
            serverSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("port")));
            chatServerSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("chatport")));
            PLAYERS_PER_TABLE = Integer.parseInt(serverProperties.getProperty("maxplayer"));
            tableProperties.setProperty("startmoney", serverProperties.getProperty("startmoney"));
            tableProperties.setProperty("multiplier", serverProperties.getProperty("multiplier"));
            tables = new ArrayList<BlackjackTable>();
        } catch(IOException e){
            System.out.println("Nem sikerult letrehozni a szervert");
        } catch(IllegalArgumentException e){
            System.out.println("Nem megfelelo port");
        }
    }

    /**
     * The main method collects the settings of the server from a .properties file,
     * then creates a server with the settings, finnaly calls then run method
     * @param args includes the location of the settings file
     */
    public static void main(String[] args){
        Properties serverProperties = getProperties(args);
        BlackjackServer blackjackServer = new BlackjackServer(serverProperties);
        blackjackServer.run(serverProperties);
    }

    /**
     * The run method creates an arraylist for the sockets and waitings for them.
     * When the count of sockets is enough the method adds a table to tables
     * which extends a thread, then starts the thread
     */
    private void run(Properties serverProperties){
        socketQueue = new ArrayList<ArrayList<Socket>>();
        Timer timer = new Timer();
        while(true){
            try{
                ArrayList<Socket> temp = new ArrayList<Socket>();
                temp.add(serverSocket.accept());
                temp.add(chatServerSocket.accept());
                socketQueue.add(temp);
                System.out.println("socket csatlakozott");
            } catch(IOException e){
                System.out.println("Problema tortent egy socket fogadasa soran");
            }            
            
            if(PLAYERS_PER_TABLE == socketQueue.size()){
                timer.cancel();
                startTable();
            } else {
                if(socketQueue.size() == 1){
                    timer = new Timer();
                    timer.schedule(new TimerTask(){
                        @Override
                        public void run() {
                            startTable();
                        }
                    }, Integer.parseInt(serverProperties.getProperty("chatport"))*1000);
                }
            }
        }
    }

    private void startTable(){
        tables.add(new BlackjackTable(tableId, socketQueue, tableProperties));
        tableId++;
        tables.get(tables.size()-1).start();
        socketQueue.clear();
    }

    /**
     * The getProperties reads the settings of the server from a .properties file.
     * If the argument is missing the method uses the default settings from default.properties file
     * @param args includes the location of the settings file (0th element of the args)
     * @return this method returns with a Properties object
     */
    static Properties getProperties(String[] args){
        final String defaultPropertiesFileName = "blackjackServer/default.properties";

        String propertiesFileName = null;
        try {
            propertiesFileName = args[0];
        } catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Nem talalhato parameter");
            propertiesFileName = defaultPropertiesFileName;
        }       

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try {
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            System.out.println("Settings:" + properties.getProperty("name"));
        } catch (IOException e) {
            System.out.println("Nem letezik a " + propertiesFileName + " vagy nem olvashato");
        } 
        if (inputForConfig != null) {
            try {
                inputForConfig.close();
            } catch (IOException e) {
                System.out.println("Nem sikerult a " + propertiesFileName + " lezarasa soran");
            }
        } else {
            try {
                inputForConfig = new FileInputStream(defaultPropertiesFileName);
                properties.load(inputForConfig);
                System.out.println("Settings:" + properties.getProperty("name"));
                inputForConfig.close();
            } catch (IOException e) {
                System.out.println("Hiba a fajl olvasasa soran");  
            }
        }
        return properties;
    }
}