package blackjackserver;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Player extends Person{
    private PrintWriter sender;
    private Scanner receiver;
    private PrintWriter chatSender;
    private Scanner chatReceiver;
    private int id;
    private int bet;
    private int status;

    public Player(ArrayList<Socket> sockets, int id, ArrayList<String> names) throws Exception{
        super();
        
        this.id = id;
        this.status = 0;
        this.bet = 0;

        this.sender = new PrintWriter(sockets.get(0).getOutputStream(), true);
        this.receiver = new Scanner(sockets.get(0).getInputStream());
        this.chatSender = new PrintWriter(sockets.get(1).getOutputStream(), true);
        this.chatReceiver = new Scanner(sockets.get(1).getInputStream());

        getMSG();
        sendMSG(namesToString(names));
        String tempName = getMSG();
        if(tempName.equals("#player")){
            this.name = "Player" + Integer.toString(id+1);
        } else {
            this.name = tempName;
        }
        sendMSG("_ynm__" + this.name);
        getMSG();
         
        System.out.println("en csatlakoztam " + name);
    }

    private String namesToString(ArrayList<String> names){
        String s = "_gtnm_#";
        for(String name : names){
            s += name + "#";
        }
        return s;
    }

    public String getName(){
        return this.name;
    }

    public String getMSG() throws NoSuchElementException, IllegalStateException{
        String s = receiver.nextLine();
        if(s.equals("#close")){
            close();
            return "";
        } else {
            return s;
        }     
    }

    public void sendMSG(String msg) throws IOException, IllegalStateException{
        sender.println(msg);
    }

    public void sendServerMSG(String msg){
        try{
            sendMSG("_svms_" + msg);
            getMSG();
        } catch(IOException e){
            System.out.println("Megszakadt a kapcsolat a klienssel");
        } catch(NoSuchElementException e){
            System.out.println("Megszakadt a kapcsolat a klienssel");
        } catch(IllegalStateException e){
            System.out.println("Megszakadt a kapcsolat a klienssel");
        }
    }

    public String getChatMSG() throws NoSuchElementException{
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

    public void sayBye() throws IOException{
        sendMSG("_bye__");
    }

    public void close(){
        try{
            sender.close();
            receiver.close();
            chatSender.close();
            chatReceiver.close();
        } catch(Exception e){
            System.out.println("Megszakadt a kapcsolat a klienssel");
        }
    }

    public void flush(){
        sender.flush();
        chatSender.flush();
    }
}