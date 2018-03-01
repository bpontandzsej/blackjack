package blackjackserver;

import java.io.*;
import java.net.*;
import java.util.*;
import blackjackserver.BlackjackTable;

public class BlackjackServer {
    /**
     * BlackjackServer is the main class of the server.
     * This class collects the connections, then starts a thread with these connections.
     * The thread represents a table.
     */
    private int tableId;
    private ServerSocket serverSocket;
    private int PLAYERS_PER_TABLE;
    private ArrayList<BlackjackTable> tables;
    //private int START_MONEY;
    private Properties tableProperties;

    /**
     * The constructor starts  a serverSocket and creates an arraylist for the tables.
     * @param serverProperties contains the settings of the server andthe table
     */
    public BlackjackServer(Properties serverProperties){
        tableId = 0;
        tableProperties = new Properties();
        try{
            serverSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("port")));
            PLAYERS_PER_TABLE = Integer.parseInt(serverProperties.getProperty("maxplayer"));
            tableProperties.setProperty("startmoney", serverProperties.getProperty("startmoney"));
            //START_MONEY = Integer.parseInt(serverProperties.getProperty("startmoney"));
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
        blackjackServer.run();
    }

    /**
     * The run method creates an arraylist for the sockets and waitings for them.
     * When the count of sockets is enough the method adds a table to tables
     * which extends a thread, then starts the thread
     */
    private void run(){
        ArrayList<Socket> socketQueue = new ArrayList<Socket>();
        while(true){
            try{
                socketQueue.add(serverSocket.accept());
                System.out.println("socket csatlakozott");
            } catch(IOException e){
                System.out.println("Problema tortent egy socket fogadasa soran");
            }
            if(PLAYERS_PER_TABLE == socketQueue.size()){
                tables.add(new BlackjackTable(tableId, socketQueue, tableProperties));
                tableId++;
                tables.get(tables.size()-1).start();
                socketQueue.clear();
            }  
        }
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
        } catch(Exception e){
            System.out.println("Hianyzo parameter.");
            propertiesFileName = defaultPropertiesFileName;
        }        

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try {
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            System.out.println("Settings:" + properties.getProperty("name"));
        } catch (IOException e) {

        } 
        if (inputForConfig != null) {
            try {
                inputForConfig.close();
            } catch (IOException e) {
                e.printStackTrace();
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