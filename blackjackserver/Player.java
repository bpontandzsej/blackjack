package blackjackserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class Player extends Person{
    private PrintWriter sender;
    private Scanner receiver;
    private PrintWriter chatSender;
    private Scanner chatReceiver;
    private int id;
    private int bet;
    private int status;

    public Player(ArrayList<Socket> sockets, int id, ArrayList<String> names) throws IOException{
        /**
         * SET default VELUES
         */
        this.id = id;
        this.status = 0;
        this.bet = 0;

        /**
         * SET the SENDER and de RECEIVER
         */
        this.sender = new PrintWriter(sockets.get(0).getOutputStream(), true);
        this.receiver = new Scanner(sockets.get(0).getInputStream());
        this.chatSender = new PrintWriter(sockets.get(1).getOutputStream(), true);
        this.chatReceiver = new Scanner(sockets.get(1).getInputStream());
        /**
         * GET NAME from the CLIENT except of the DEALER
         */
        getMSG();
        sendMSG(namesToString(names));
        this.name = getMSG();
        
        System.out.println("en csatlakoztam" + name);        
    }

    private String namesToString(ArrayList<String> names){
        String s = "#";
        for(String name : names){
            s += name + "#";
        }
        return s;
    }


    public String getName(){
        return this.name;
    }

    public String getMSG() throws NoSuchElementException{
        return receiver.nextLine();
    }

    public void sendMSG(String msg){
        sender.println(msg);
    }

    public String getChatMSG(){
        return chatReceiver.nextLine();
    }

    public void sendChatMSG(String msg){
        chatSender.println(msg);
    }

    public int getId(){
        return this.id;
    }

    public int getBet(){
        return this.bet;
    }

    public void setBet(int bet){
        this.bet = bet;
    }

    public void setStatus(int i){
        this.status = i;
    }

    public int getStatus(){
        return this.status;
    }

    public void sayBye(){
        sendMSG("_bye__");
    }
}