package blackjackclient;

import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class BlackjackClient2{
    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;


    public BlackjackClient2(String name){
        try{
            clientSocket = new Socket("localhost", 1234);
            sender = new PrintWriter(clientSocket.getOutputStream(), true);
            receiver = new Scanner(clientSocket.getInputStream());
            run(name);
        } catch(UnknownHostException e){
            System.out.println("Nem letezo host");
        } catch(IOException e){
            System.out.println("Hiba tortent a socket letrehozasa soran");
        }


    }


    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }

    public static void main(String[] args){
        BlackjackClient2 blackjackClient = new BlackjackClient2(args[0]);
    }    

    private void run(String name){
        sendMSG(name);
        String s = "";
        while(!s.equals("#byebye")){
            s = getMSG();
            System.out.println(s);
            switch (s) {
                case "#bet":
                    sendMSG("250");
                    break;
                case "#mehetsz":
                    break;
                case "#turn":
                    //sendMSG("#howmany");
                    String in = System.console().readLine();
                    sendMSG(in);
                    //sendMSG("#stop");
                    break;
                default:
                    sendMSG("#gotit");
                    break;
            }
        }

        try{
            clientSocket.close();
        } catch(IOException e){
            System.out.println("Hiba tortente a socket zarasa soran");
        }        
    }
}