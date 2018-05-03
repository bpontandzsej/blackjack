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
    private ServerSocket serverSocket;
    private ServerSocket chatServerSocket;

    public BlackjackServer(Properties serverProperties) throws Exception{
        try{
            serverSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("port")));
            chatServerSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("chatport")));
        } catch(IOException e){
            System.err.println("Nem sikerult letrehozni a szervert");
        } catch(IllegalArgumentException e){
            System.err.println("Nem megfelelo port");
        }
    }

    public static void main(String[] args){
        try{
            Properties serverProperties = getProperties(args);
            BlackjackServer blackjackServer = new BlackjackServer(serverProperties);
            blackjackServer.run(serverProperties);
        } catch(Exception e){
            System.err.println("Varatlan hiba");
        }
    }

    private void run(Properties serverProperties) throws Exception{
        Properties tableProperties = new Properties();
        tableProperties.setProperty("startmoney", serverProperties.getProperty("startmoney"));
        tableProperties.setProperty("multiplier", serverProperties.getProperty("multiplier"));
        final int PLAYERS_PER_TABLE = Integer.parseInt(serverProperties.getProperty("maxplayer"));
        ArrayList<BlackjackTable> tables = new ArrayList<BlackjackTable>();
        ArrayList<ArrayList<Socket>> socketQueue = new ArrayList<ArrayList<Socket>>();
        Timer timer = new Timer();
        while(true){
            try{
                ArrayList<Socket> temp = new ArrayList<Socket>();
                temp.add(serverSocket.accept());
                temp.add(chatServerSocket.accept());
                socketQueue.add(temp);
                System.err.println("socket csatlakozott");
            } catch(IOException e){
                System.err.println("Problema tortent egy socket fogadasa soran");
            }            
            
            if(PLAYERS_PER_TABLE == socketQueue.size()){
                timer.cancel();
                startTable(socketQueue, tables, tableProperties);
            } else {
                if(socketQueue.size() == 1){
                    timer = new Timer();
                    timer.schedule(new TimerTask(){
                        @Override
                        public void run() {
                            startTable(socketQueue, tables, tableProperties);
                        }
                    }, Integer.parseInt(serverProperties.getProperty("starttimeout"))*1000);
                }
            }
        }
    }

    private void startTable(ArrayList<ArrayList<Socket>> socketQueue, ArrayList<BlackjackTable> tables, Properties tableProperties){
        tables.add(new BlackjackTable(socketQueue, tableProperties));
        tables.get(tables.size()-1).start();
        socketQueue.clear();
    }

    static Properties getProperties(String[] args) throws Exception{
        final String defaultPropertiesFileName = "default_server.properties";
        String propertiesFileName = null;
        try{
            propertiesFileName = args[0];
        } catch(ArrayIndexOutOfBoundsException e){
            System.err.println("Nem talalhato parameter");
            propertiesFileName = defaultPropertiesFileName;
        }       

        Properties properties = new Properties();
        InputStream inputForConfig = null;
        try{
            inputForConfig = new FileInputStream(propertiesFileName);
            properties.load(inputForConfig);
            System.out.println("Settings:" + properties.getProperty("name"));
        } catch (IOException e) {
            System.err.println("Nem letezik a " + propertiesFileName + " vagy nem olvashato");
        } 
        if (inputForConfig != null){
            try {
                inputForConfig.close();
            } catch (IOException e) {
                System.err.println("Problema a " + propertiesFileName + " lezarasa soran");
                
            }
        } else {
            try{
                inputForConfig = new FileInputStream(defaultPropertiesFileName);
                properties.load(inputForConfig);
                System.out.println("Settings:" + properties.getProperty("name"));
                inputForConfig.close();
            } catch (IOException e){
                System.err.println("Hiba a fajl olvasasa soran");  
            }
        }
        return properties;
    }
}