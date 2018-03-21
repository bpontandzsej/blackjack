package blackjackserver;

import java.io.*;
import java.net.*;
import java.util.*;
import blackjackserver.Player;
import blackjackserver.Dealer;
import blackjackserver.Deck;

public class BlackjackTable extends Thread{
    //private final int id;
    private ArrayList<Player> players;
    private Dealer dealer;
    private final int startMoney;
    private Deck deck;

    public BlackjackTable(int id, ArrayList<Socket> sockets, Properties serverProperties/*Properties tableProperties*/){
        //this.id = id;
        players = new ArrayList<Player>();
        int playerId = 0;
        for(Socket socket : sockets){
            try{
                players.add(new Player(socket, playerId));
                playerId++;
            } catch(IOException e){
                System.out.print("Nem sikerult a kommunikacio megteremtese a klienssel");
            }
        }
        for(Player player : players){
            player.sendMSG("_id___" + Integer.toString(player.getId()));
            player.getMSG();
        }
        dealer = new Dealer();
        startMoney = Integer.parseInt(serverProperties.getProperty("startmoney"));
        System.out.println("letrejott a szal");
    }

    public void run(){
        System.out.println("elindult a szal");
        sendToAll("_strt_");
        serverMSG("WELCOME!");

        fillMoney();
        int roundCount = 0;
        while(checkEnd()) {
            roundCount++;
            serverMSG("Round " + Integer.toString(roundCount) + ": STARTED");
            newGame();
            sendStatusToAll(true);
            for(Player player : players){
                player.sendMSG(getStatus(true, "_bet__"));
                player.setBet(Integer.parseInt(player.getMSG()));
                sendStatusToAll(true);
            }
            for(Player player : players){
                player.addCard(deck.takeCard());
                player.addCard(deck.takeCard());
                player.setStatus(0);
                player.setRealSum(getSumInInt(player.getSum()));
            }
            dealer.addCard(deck.takeCard());
            dealer.addCard(deck.takeCard());
            sendStatusToAll(true);
            for(Player player : players){
                player.setStatus(1);
                sendStatusToAll(true);
                String s = "";
                while(!(s.equals("#stop")) && (getSumInInt(player.getSum())<21)){
                    player.sendMSG(getStatus(true, "_turn_"));
                    s = player.getMSG();
                    if(s.equals("#card")){
                        player.addCard(deck.takeCard());
                    } /*else {

                        if(s.substring(0, 6).equals("_chat_")){
                            sendToAll(s);
                        }
                    }*/
                    player.setRealSum(getSumInInt(player.getSum()));
                    sendStatusToAll(true);
                }
                
                player.setStatus(2);
                player.setRealSum(getSumInInt(player.getSum()));
                sendStatusToAll(true);
            }
            dealer.setRealSum(getSumInInt(dealer.getSum()));
            sendStatusToAll(false);
            System.out.println(checkAll());
            wait(2);
            while(getSumInInt(dealer.getSum())<17 && checkAll()<=0){
                System.out.println(checkAll());
                dealer.addCard(deck.takeCard());
                dealer.setRealSum(getSumInInt(dealer.getSum()));
                sendStatusToAll(false);
                wait(2);
            }
            System.out.println(checkAll());
            if(checkAll()>0){
                serverMSG("Round " + Integer.toString(roundCount) + ": the WINNER is the BANK");
            } else {
                if(checkAll()<0){
                    serverMSG("Round " + Integer.toString(roundCount) + ": the WINNERs are the PLAYERS");
                } else {
                    serverMSG("Round " + Integer.toString(roundCount) + ": DRAW");
                }
            }
            
            for(Player player : players){
                if(player.getRealSum()==21 && player.getCards().size()==2){
                    player.setMoney(player.getMoney() + (int)Math.round(player.getBet()*1.5));
                    dealer.setMoney(dealer.getMoney() - (int)(player.getBet()*1.5));
                } else {
                    if(player.getRealSum()>21){
                        player.setMoney(player.getMoney() - player.getBet());
                        dealer.setMoney(dealer.getMoney() + player.getBet());
                    } else {
                        if(dealer.getRealSum()>21){
                            player.setMoney(player.getMoney() + player.getBet());
                            dealer.setMoney(dealer.getMoney() - player.getBet());
                        } else {
                            if(player.getRealSum()>dealer.getRealSum()){
                                player.setMoney(player.getMoney() + player.getBet());
                                dealer.setMoney(dealer.getMoney() - player.getBet());
                            } else {
                                if(player.getRealSum()<dealer.getRealSum()){
                                    player.setMoney(player.getMoney() - player.getBet());
                                    dealer.setMoney(dealer.getMoney() + player.getBet());
                                }
                            }
                        }
                    }
                }
            }
            sendStatusToAll(false);
            wait(2);
            for(Player player : new ArrayList<Player>(players)){
                if(player.getMoney()<=0){
                    player.sayBye();
                    players.remove(player);
                    serverMSG(player.getName() + " left the game");
                }
            }
        }

        for(Player player : players){
            player.sendMSG("#byebye");
        }
        System.out.println("lelepett mindenki");
    }

    private void wait(int seconds){
        try{
            Thread.sleep(seconds*1000);
        } catch(InterruptedException e){

        }
    }

    /**
     * FILL the players' and dealer's MONEY
     */
    private void fillMoney(){
        for(Player player : players){
            player.setMoney(startMoney);
        }
        dealer.setMoney(startMoney*players.size()*3);
    }

    /**
     * RETURNS with TRUE if the GAME is OVER
     */
    private boolean checkEnd(){
        return (players.size() > 0 && dealer.getMoney() > 0);
    }

    public void newGame(){
        deck = new Deck();
        for(Player player : players){
            player.setBet(0);
            player.setStatus(0);
            player.setRealSum(0);
            player.clearCards();
        }
        dealer.setRealSum(0);
        dealer.clearCards();
    }

    private String getStatus(boolean hideSecond, String pre){
        String status = pre;
        status += str(dealer.getMoney()) + ";";
        status += dealer.cardsToString(hideSecond) + ";";
        if(hideSecond){
            status += "0;";
            status += str(0);
        } else {
            status += dealer.getSum() + ";";
            status += str(dealer.getRealSum());
        }
        status += "@";

        for(Player player : players){
            status += str(player.getId()) + ";";
            status += player.getName()  + ";";
            status += str(player.getStatus()) + ";";
            status += str(player.getMoney()) + ";";
            status += str(player.getBet()) + ";";
            status += player.cardsToString() + ";";
            status += player.getSum() + ";";
            status += str(player.getRealSum()) + "#";
        }
        return status.substring(0, status.length()-1);
    }

    /*private String mergeInteger(String a, int b){
        return a + "-" + Integer.toString(b) + ";";
    }*/

    private String str(int s){
        return Integer.toString(s);
    }

    /*private String mergeString(String a, String b){
        return a + "-" + b + ";";
    }*/

    private void sendStatusToAll(boolean hideSecond){
        for(Player player : players){
            player.sendMSG(getStatus(hideSecond, "_stat_"));
            player.getMSG();
        }
    }

    private void serverMSG(String msg){
        sendToAll("_svms_[SERVER] " + msg);
    }

    private void sendToAll(String msg){
        for(Player player : players){
            player.sendMSG(msg);
            player.getMSG();
        }
    }

    private int getSumInInt(String s){
        if(s.length() <= 2){
            return Integer.parseInt(s);
        } else {
            String[] strArray = s.split("/");
            int[] intArray = new int[strArray.length];
            for(int i = 0; i < strArray.length; i++) {
                intArray[i] = Integer.parseInt(strArray[i]);
            }
            for(int i = 0; i < intArray.length / 2; i++){
                int temp = intArray[i];
                intArray[i] = intArray[intArray.length - i - 1];
                intArray[intArray.length - i - 1] = temp;
            }
            int max = intArray[0];
            for(int i=1; i<intArray.length; i++){
                if(intArray[i] > max && intArray[i] <= 21) max = intArray[i];
            }
            return max;
        }
    }

    private int checkAll(){
        int sumAll = 0;
        for(Player player : players){
            if(player.getRealSum()==21 && player.getCards().size()==2){
                sumAll -= player.getBet()*1.5;
            } else {
                if(player.getRealSum()>21){
                    sumAll += player.getBet();
                } else {
                    if(dealer.getRealSum()>21){
                        sumAll -= player.getBet();
                    } else {
                        if(player.getRealSum()>dealer.getRealSum()){
                            sumAll -= player.getBet();
                        } else {
                            if(player.getRealSum()<dealer.getRealSum()){
                                sumAll += player.getBet();
                            }
                        }
                    }
                }
            }
        }
        return sumAll;
    }
}