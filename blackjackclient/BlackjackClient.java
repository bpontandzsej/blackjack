package blackjackclient;

import blackjackclient.BlackjackClientConnectView;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class BlackjackClient/* implements ActionListener*/{
    private BlackjackClientConnectView connectView;
    private Socket clientSocket;
    private PrintWriter sender;
    private Scanner receiver;

    public BlackjackClient(){        
        try{
            connectView = new BlackjackClientConnectView();
            clientSocket = new Socket("localhost", 1234);
            sender = new PrintWriter(clientSocket.getOutputStream(), true);
            receiver = new Scanner(clientSocket.getInputStream());
            connectView.getConnectButton().addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    sendMSG(connectView.getInputName());
                    connectView.setVisible(false);
                }
            });
        } catch(UnknownHostException e){
            System.out.println("Nem letezo host");
        } catch(IOException e){
            System.out.println("Hiba tortent a socket letrehozasa soran");
        }
    }

    public static void main(String[] args){
        BlackjackClient client = new BlackjackClient();

    }

    private void sendMSG(String msg){
        sender.println(msg);
    }

    private String getMSG(){
        return receiver.nextLine();
    }



}