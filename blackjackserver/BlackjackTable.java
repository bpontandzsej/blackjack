package blackjackserver;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class BlackjackTable extends Thread{
    private final int startMoney;
    private ArrayList<ArrayList<Socket>> sockets;
    private final int multiplier;

    public BlackjackTable(ArrayList<ArrayList<Socket>> sockets, Properties tableProperties){
        this.sockets = new ArrayList<ArrayList<Socket>>(sockets);
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
        ArrayList<Player> players = new ArrayList<Player>();
        ArrayList<String> names = new ArrayList<String>(); 
        Dealer dealer = new Dealer();
        init(players, names);
        System.out.println("elindult a szal");
        sendToAll(players, "_strt_");

        fillMoney(dealer, players);
        int roundCount = 0;
        ArrayList<Player> needToDelete = new ArrayList<Player>();
        while(checkEnd(dealer, players)) {
            roundCount++;
            Deck deck = new Deck();
            newGame(dealer, players);
            sendStatusToAll(dealer, players, true);
            
            needToDelete = new ArrayList<Player>();
            for(Player player : players){
                if(player.getStatus()<4){
                    try{
                        player.setStatus(1);
                        sendStatusToAll(dealer, players, true);
                        player.sendMSG(getStatus(dealer, players, true, "_bet__"));
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
                        needToDelete.add(player);
                    } catch(IOException e){
                        System.out.println("kommunikacios hiba1");
                    } catch(NoSuchElementException e){
                        System.out.println("kommunikacios hiba2");
                    }
                    sendStatusToAll(dealer, players, true);
                }
            }
            players.removeAll(needToDelete);
            if(notSkippers(players)>0){
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
                sendStatusToAll(dealer, players, true);
                needToDelete = new ArrayList<Player>();
                for(Player player : players){
                    if(player.getStatus()<3){
                        player.setStatus(1);
                        sendStatusToAll(dealer, players, true);
                        String s = "";
                        try{
                            while(!(s.equals("#stop")) && (getSumInInt(player.getSum())<21) && !(s.equals("#skip"))){
                                player.sendMSG(getStatus(dealer, players, true, "_turn_"));
                                s = player.getMSG();
                                if(s.equals("#card")){
                                    player.addCard(deck.takeCard());
                                }
                                player.setRealSum(getSumInInt(player.getSum()));
                                sendStatusToAll(dealer, players, true);                    
                            }
                            player.setRealSum(getSumInInt(player.getSum()));
                        } catch(IllegalStateException e){
                            System.out.println("asd7");
                            playerLeft(player);
                            needToDelete.add(player);
                        } catch(IOException e){
                            System.out.println("kommunikacios hiba3");
                        } catch(NoSuchElementException e){
                            System.out.println("kommunikacios hiba4");
                        }
                        player.setStatus(2);
                        sendStatusToAll(dealer, players, true);
                    }                
                }
                players.removeAll(needToDelete);
                dealer.setRealSum(getSumInInt(dealer.getSum()));
                sendStatusToAll(dealer, players, false);
                System.out.println(checkAll(dealer, players));
                wait(2);
                while(getSumInInt(dealer.getSum())<17 && checkAll(dealer, players)<=0){
                    System.out.println(checkAll(dealer, players));
                    dealer.addCard(deck.takeCard());
                    dealer.setRealSum(getSumInInt(dealer.getSum()));
                    sendStatusToAll(dealer, players, false);
                    wait(2);
                }
                System.out.println(checkAll(dealer, players));
                
                for(Player player : players){
                    if(player.getRealSum() != dealer.getRealSum()){
                        if(player.getStatus()<3){
                            if(player.getRealSum()==21 && player.getCards().size()==2){
                                deal(dealer, player, 1.5);
                                player.sendServerMSG("YOU WON THE ROUND: BLACKJACK!");
                            } else {
                                if(player.getRealSum()>21){
                                    deal(dealer, player, -1);
                                    player.sendServerMSG("YOU LOST THE ROUND: You have: " + Integer.toString(player.getRealSum()));
                                } else {
                                    if(dealer.getRealSum()>21){
                                        deal(dealer, player, 1);
                                        player.sendServerMSG("YOU WON THE ROUND: Dealer has: " + Integer.toString(dealer.getRealSum()));
                                    } else {
                                        if(player.getRealSum()>dealer.getRealSum()){
                                            deal(dealer, player, 1);
                                            player.sendServerMSG("YOU WON THE ROUND: You have: " + Integer.toString(player.getRealSum()) + ", Dealer has: " + Integer.toString(dealer.getRealSum()));
                                        } else {
                                            if(player.getRealSum()<dealer.getRealSum()){
                                                deal(dealer, player, -1);
                                                player.sendServerMSG("YOU LOST THE ROUND: You have: " + Integer.toString(player.getRealSum()) + ", Dealer has: " + Integer.toString(dealer.getRealSum()));
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
            sendStatusToAll(dealer, players, false);
            wait(2);
            for(Player player : new ArrayList<Player>(players)){
                if(player.getStatus()<4){
                    if(player.getMoney()<=0){
                        try{
                            player.sendServerMSG("You are out of money. You lost the game...");
                        } catch(IllegalStateException e){
                            System.out.println("mar lelepett");
                        }
                        player.setStatus(4);
                    }
                }
            }
            sendStatusToAll(dealer, players, false);
        }

        needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                if(player.getMoney()>0){
                    player.sendServerMSG("CONGRATULATIONS! You won the game!");
                } else {
                    if(dealer.getMoney()>0){
                        player.sendServerMSG("The Dealer won the game!");
                    } else {
                        player.sendServerMSG("The players won the game!");
                    }
                }
                player.sayBye();            
            } catch(Exception e){
                playerLeft(player);
                needToDelete.add(player);
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
        player.flush();
        player.close();
    }

    private void init(ArrayList<Player> players, ArrayList<String> names){
        int playerId = 0;
        for(ArrayList<Socket> socket : sockets){
            try{
                players.add(new Player(socket, playerId, names));
                names.add(players.get(players.size()-1).getName());
                sendToAll(players, namesToString(names));
                playerId++;
            } catch(IOException e){
                System.out.print("Nem sikerult a kommunikacio megteremtese a klienssel");
            } catch(Exception e){

            }
        }
        ArrayList<Player> needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                player.sendMSG("_id___" + Integer.toString(player.getId()));
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("asd3");
                playerLeft(player);
                needToDelete.add(player);
            } catch(IOException e){
                System.out.println("kommunikacios hiba5");
            }
            Thread chatHandler = new Thread(){
                @Override
                public void run(){
                    System.out.println(player.getName() + " chat started");
                    String msg = "";
                    try{
                        msg = player.getChatMSG();
                        while(msg!="bye"){
                                sendChatToAll(players, msg);
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
    }

    private int activePlayers(ArrayList<Player> players){
        int n = 0;
        for(Player player : players){
            if(player.getStatus()<4){
                n++;
            }
        }
        return n;
    }

    private int notSkippers(ArrayList<Player> players){
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

    private void fillMoney(Dealer dealer, ArrayList<Player> players){
        for(Player player : players){
            player.setMoney(startMoney);
        }
        dealer.setMoney(startMoney*players.size()*multiplier);
    }

    private boolean checkEnd(Dealer dealer, ArrayList<Player> players){
        return (activePlayers(players) > 0 && dealer.getMoney() > 0);
    }

    public void newGame(Dealer dealer, ArrayList<Player> players){
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

    private String getStatus(Dealer dealer, ArrayList<Player> players, boolean hideSecond, String pre){
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

    private void sendStatusToAll(Dealer dealer, ArrayList<Player> players, boolean hideSecond){
        for(Player player : players){
            try{
                player.sendMSG(getStatus(dealer, players, hideSecond, "_stat_"));
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("status off");
            } catch(IOException e){
                System.out.println("kommunikacios hiba6");
            } catch(NoSuchElementException e){
                System.out.println("kommunikacios hiba7");
            }
        }
    }

    private void serverMSG(ArrayList<Player> players, String msg){
        sendToAll(players, "_svms_ " + msg);
    }

    private void sendToAll(ArrayList<Player> players, String msg){
        ArrayList<Player> needToDelete = new ArrayList<Player>();
        for(Player player : players){
            try{
                player.sendMSG(msg);
                player.getMSG();
            } catch(IllegalStateException e){
                System.out.println("asd12");
                playerLeft(player);
                needToDelete.add(player);
            } catch(IOException e){
                System.out.println("kommunikacios hiba8");
            }
        }
        players.removeAll(needToDelete);
    }

    private void sendChatToAll(ArrayList<Player> players, String msg){
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

    private int checkAll(Dealer dealer, ArrayList<Player> players){
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