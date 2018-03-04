package blackjackclient;

import blackjackclient.BlackjackClientConnectView;
import blackjackclient.BlackjackClientTableView;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class BlackjackClient/* implements ActionListener*/{
    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;

    public BlackjackClient(){
        try {
            BlackjackClientTableView table = new BlackjackClientTableView();
        } catch(Exception e){
            e.printStackTrace();
        }
        
        
        /*try{
            connect();
        } catch(UnknownHostException e){
            System.out.println("Nem letezo host");
        } catch(IOException e){
            System.out.println("Hiba tortent a socket letrehozasa soran");
        }*/
    }

    public static void main(String[] args){
        BlackjackClient client = new BlackjackClient();
    }

   /* private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }

    private void connect() throws IOException, UnknownHostException{
        //BlackjackClientTableView table = new BlackjackClientTableView();
    
        
        //BlackjackClientConnectView connectView = new BlackjackClientConnectView();
        clientSocket = new Socket("localhost", 1234);
        sender = new PrintWriter(clientSocket.getOutputStream(), true);
        receiver = new Scanner(clientSocket.getInputStream());
        //sendMSG("sasffasf");
        /*connectView.getConnectButton().addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                sendMSG(connectView.getInputName());
                connectView.setVisible(false);
                letsplay();
            }
        });*//*
        letsplay();
    }

    private void letsplay(){
        BlackjackClientTableView table = new BlackjackClientTableView();
    
        String s = "";
        s = getMSG();
        while(!s.equals("#byebye")){
            table.reCreate(s); 
            table.getSendButton().addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    sendMSG(table.getChatMSG());                
                }
            });


            s = getMSG();
            
        
        }
        try{
            clientSocket.close();
        } catch(IOException e){
            System.out.println("Hiba tortente a socket zarasa soran");
        }
    }

    private void end(){
        try{
            clientSocket.close();
        } catch(IOException e){
            System.out.println("Hiba tortente a socket zarasa soran");
        }
    }*/

    



}