package blackjackserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class Player extends Person{
    private PrintWriter sender;
    private Scanner receiver;
    private int id;
    private int bet;
    private int status;

    public Player(Socket socket, int id) throws IOException{
        /**
         * SET default VELUES
         */
        this.id = id;
        this.status = 0;
        this.bet = 0;

        /**
         * SET the SENDER and de RECEIVER
         */
        this.sender = new PrintWriter(socket.getOutputStream(), true);
        this.receiver = new Scanner(socket.getInputStream());
        /**
         * GET NAME from the CLIENT except of the DEALER
         */
        this.name = getMSG();
        //sendMSG(Integer.toString(id));
        System.out.println(name);        
    }


    public String getName(){
        return this.name;
    }

    public String getMSG(){
        return receiver.nextLine();
    }

    public void sendMSG(String msg){
        sender.println(msg);
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