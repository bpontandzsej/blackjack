package blackjackserver;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import blackjackserver.Player;
import blackjackserver.Dealer;
import blackjackserver.Deck;

public class BlackjackTable extends Thread{
    private ArrayList<Player> players;
    private Dealer dealer;
    private final int startMoney;
    private Deck deck;
    private ArrayList<String> names;
    private ArrayList<Player> needToDelete;
    private ArrayList<ArrayList<Socket>> sockets;
    private final int multiplier;

    public BlackjackTable(int id, ArrayList<ArrayList<Socket>> sockets, Properties tableProperties){
        this.sockets = new ArrayList<ArrayList<Socket>>(sockets);
        players = new ArrayList<Player>();
        names = new ArrayList<String>(); 
        startMoney = Integer.parseInt(tableProperties.getProperty("startmoney"));
        multiplier = Integer.parseInt(tableProperties.getProperty("multiplier"));
        System.out.println("letrejott a szal");
    }

    private String namesToString(ArrayList<String> names){
        String s = "_nms__#";
        for(String name : names){
            s += name + "#";
        }
        return s;
    }

    public void run(){
        init();
        System.out.println("elindult a szal");
        sendToAll("_strt_");

        fillMoney();
        int roundCount = 0;
        while(checkEnd()) {
            roundCount++;
            newGame();
            sendStatusToAll(true);
            
            needToDelete = new ArrayList<Player>();
            for(Player player : players){
                if(player.getStatus()<4){
                    try{
                        player.setStatus(1);
                        sendStatusToAll(true);
                        player.sendMSG(getStatus(true, "_bet__"));
                        String msg = player.getMSG();
                        if(msg.equals("#skip")){
                            player.setStatus(3);
                        } else {
                            try{
                                player.setBet(Integer.parseInt(msg));
                            } catch(NumberFormatException e){
                                
                            }
                            
                            player.setStatus(2);
                        }                        
                    } catch(IllegalStateException e){
                        System.out.println("bet off");
                        playerLeft(player);
                    } catch(IOException e){
                        System.out.println("kommunikacios hiba");
                    }
                    sendStatusToAll(true);
                }
            }
            players.removeAll(needToDelete);
            if(notSkippers()>0){
                for(Player player : players){
                    if(player.getStatus()<3){
                        player.addCard(deck.takeCard());
                        player.addCard(deck.takeCard());
                        player.setStatus(0);
                        player.setRealSum(getSumInInt(player.getSum()));
                    }
                }
                dealer.addCard(deck.takeCard());
                dealer.addCard(deck.takeCard());
                sendStatusToAll(true);
                needToDelete = new ArrayList<Player>();
                for(Player player : players){
                    if(player.getStatus()<3){
                        player.setStatus(1);
                        sendStatusToAll(true);
                        String s = "";
                        try{
                            while(!(s.equals("#stop")) && (getSumInInt(player.getSum())<21) && !(s.equals("#skip"))){
                                player.sendMSG(getStatus(true, "_turn_"));
                                s = player.getMSG();
                                if(s.equals("#card")){
                                    player.addCard(deck.takeCard());
                                }
                                player.setRealSum(getSumInInt(player.getSum()));
                                sendStatusToAll(true);                    
                            }
                            player.setRealSum(getSumInInt(player.getSum()));
                        } catch(IllegalStateException e){
                            System.out.println("asd7");
                            playerLeft(player);
                        } catch(IOException e){
                            System.out.println("kommunikacios hiba");
                        }
                        player.setStatus(2);
                        sendStatusToAll(true);
                    }                
                }
                players.removeAll(needToDelete);
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
                /*if(checkAll()>0){
                    //serverMSG("Round " + Integer.toString(roundCount) + ": the WINNER is the BANK");
                } else {
                    if(checkAll()<0){
                        //serverMSG("Round " + Integer.toString(roundCount) + ": the WINNERs are the PLAYERS");
                    } else {
                        //serverMSG("Round " + Integer.toString(roundCount) + ": DRAW");
                    }
                }*/
                
                for(Player player : players){
                    if(player.getRealSum() != dealer.getRealSum()){
                        if(player.getStatus()<3){
                            if(player.getRealSum()==21 && player.getCards().size()==2){
                                deal(dealer, player, 1.5);
                                player.sendServerMSG("MEGNYERTED A KORT: BLACKJACK!");
                            } else {
                                if(player.getRealSum()>21){
                                    deal(dealer, player, -1);
                                    player.sendServerMSG("ELVESZTETTED A KORT: A lapjaid osszege: " + Integer.toString(player.getRealSum()));
                                } else {
                                    if(dealer.getRealSum()>21){
                                        deal(dealer, player, 1);
                                        player.sendServerMSG("MEGNYERTED A KORT: Az oszto lapjainak osszege: " + Integer.toString(dealer.getRealSum()));
                                    } else {
                                        if(player.getRealSum()>dealer.getRealSum()){
                                            deal(dealer, player, 1);
                                            player.sendServerMSG("MEGNYERTED A KORT: A lapjaid osszege: " + Integer.toString(player.getRealSum()) + ", az oszto lapjainak osszege: " + Integer.toString(dealer.getRealSum()));
                                        } else {
                                            if(player.getRealSum()<dealer.getRealSum()){
                                                deal(dealer, player, -1);
                                                player.sendServerMSG("ELVESZTETTED A KORT: A lapjaid osszege: " + Integer.toString(player.getRealSum()) + ", az oszto lapjainak osszege: " + Integer.toString(dealer.getRealSum()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        try{
                            player.sendServerMSG("DRAW: You have: " + Integer.toString(player.getRealSum()) + ", Dealer has: " + Integer.toString(dealer.getRealSum()));
                        } catch(IllegalStateException e){
                            System.out.println("mar lelepett");
                        }
                    }
                }
            }
            sendStatusToAll(false);
            wait(2);
            for(Player player : new ArrayList<Player>(players)){
                if(player.getStatus()<4){
                    if(player.getMoney()<=0){
                        try{
                            player.sendServerMSG("You are out of money! You lost the game");
                        } catch(IllegalStateException e){
                            System.out.println("mar lelepett");
                        }
                        player.setStatus(4);
                    }
                }
            }
            sendStatusToAll(false);
        }

        needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                if(player.getMoney()>0){
                    player.sendServerMSG("Congratulations! You WON the game!");
                } else {
                    if(dealer.getMoney()>0){
                        player.sendServerMSG("The Dealer WON the game!");
                    } else {
                        player.sendServerMSG("The playersWON the game!");
                    }
                }
                player.sayBye();            
            } catch(Exception e){
                playerLeft(player);
            } 
        }
        players.removeAll(needToDelete);
        System.out.println("lelepett mindenki");
        return;
    }

    private void deal(Dealer dealer, Player player, double toPlayer){
        player.setMoney(player.getMoney() + (int)Math.round(player.getBet()*toPlayer));
        dealer.setMoney(dealer.getMoney() - (int)(player.getBet()*toPlayer));
    }

    private void playerLeft(Player player){
        player.setStatus(4);
        needToDelete.add(player);
        player.flush();
        player.close();
    }

    private void init(){
        int playerId = 0;
        for(ArrayList<Socket> socket : sockets){
            try{
                players.add(new Player(socket, playerId, names));
                names.add(players.get(players.size()-1).getName());
                sendToAll(namesToString(names));
                playerId++;
            } catch(IOException e){
                System.out.print("Nem sikerult a kommunikacio megteremtese a klienssel");
            } catch(Exception e){

            }
        }
        needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                player.sendMSG("_id___" + Integer.toString(player.getId()));
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("asd3");
                playerLeft(player);
            } catch(IOException e){
                System.out.println("kommunikacios hiba");
            }
            Thread chatHandler = new Thread(){
                @Override
                public void run(){
                    System.out.println(player.getName() + " chat started");
                    String msg = "";
                    try{
                        msg = player.getChatMSG();
                        while(msg!="bye"){
                                sendChatToAll(msg);
                                msg = player.getChatMSG();
                        }
                    } catch(Exception e){
                        System.out.println("chat off");
                        player.flush();
                        return;
                    }
                    return;
                } 
            };
            chatHandler.start();
        }
        players.removeAll(needToDelete);

        dealer = new Dealer();        
    }

    private int activePlayers(){
        int n = 0;
        for(Player player : players){
            if(player.getStatus()<4){
                n++;
            }
        }
        return n;
    }

    private int notSkippers(){
        int n = 0;
        for(Player player : players){
            if(player.getStatus()<3){
                n++;
            }
        }
        return n;
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
        dealer.setMoney(startMoney*players.size()*multiplier);
    }

    /**
     * RETURNS with TRUE if the GAME is OVER
     */
    private boolean checkEnd(){
        return (activePlayers() > 0 && dealer.getMoney() > 0);
    }

    public void newGame(){
        deck = new Deck();
        for(Player player : players){
            if(player.getStatus()<4){
                player.setStatus(0);
            }
            player.setBet(0);            
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

    private String str(int s){
        return Integer.toString(s);
    }

    private void sendStatusToAll(boolean hideSecond){
        for(Player player : players){
            try{
                player.sendMSG(getStatus(hideSecond, "_stat_"));
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("status off");
            } catch(IOException e){
                System.out.println("kommunikacios hiba");
            }
        }
    }

    private void serverMSG(String msg){
        sendToAll("_svms_ " + msg);
    }

    private void sendToAll(String msg){
        needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                player.sendMSG(msg);
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("asd12");
                playerLeft(player);
            } catch(IOException e){
                System.out.println("kommunikacios hiba");
            }
        }
        players.removeAll(needToDelete);
    }

    private void sendChatToAll(String msg){
        for(Player player : players){
            try{
                player.sendChatMSG(msg);
            } catch(Exception e){
                System.out.println("asd11");
                players.remove(player);
                player.flush();
            }
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