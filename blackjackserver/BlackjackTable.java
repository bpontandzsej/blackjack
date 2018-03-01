package blackjackserver;

import java.io.*;
import java.net.*;
import java.util.*;
import blackjackserver.Player;
import blackjackserver.Dealer;
import blackjackserver.Deck;

public class BlackjackTable extends Thread{
    private final int id;
    private ArrayList<Player> players;
    private Dealer dealer;
    private final int startMoney;
    private Deck deck;

    public BlackjackTable(int id, ArrayList<Socket> sockets, Properties serverProperties/*Properties tableProperties*/){
        this.id = id;
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
        dealer = new Dealer();
        startMoney = Integer.parseInt(serverProperties.getProperty("startmoney"));
        System.out.println("letrejott a szal");
    }

    public void run(){
        System.out.println("elindult a szal");

        fillMoney();

        while(checkEnd()) {

            newGame();
            sendStatusToAll(true);

            for(Player player : players){
                player.sendMSG("#bet");
                player.setBet(Integer.parseInt(player.getMSG()));
                //sendStatusToAll(true);
            }
            for(Player player : players){
                player.addCard(deck.takeCard());
                player.addCard(deck.takeCard());
                player.setStatus(0);
            }
            dealer.addCard(deck.takeCard());
            dealer.addCard(deck.takeCard());
            sendStatusToAll(true);

            for(Player player : players){
                String s = "";
                while(!s.equals("#stop")){
                    if(getSum(player.getSum())>21){
                        player.sendMSG("#lose");
                        player.getMSG();
                        break;
                    } else {
                        if(getSum(player.getSum())==21) {
                            player.sendMSG("#blackjack");
                            player.getMSG();
                            break;
                        }
                    }
                    player.sendMSG("#turn");
                    s = player.getMSG();
                    if(s.equals("#card")){
                        player.addCard(deck.takeCard());
                    }

                    sendStatusToAll(true);

                }
                player.setRealSum(getSum(player.getSum()));
                //sendStatusToAll(true);
            }
            dealer.setRealSum(getSum(dealer.getSum()));
            //sendStatusToAll(false);
            System.out.println(checkAll());
            while(getSum(dealer.getSum())<17 && checkAll()<=0){
                System.out.println(checkAll());
                dealer.addCard(deck.takeCard());
                dealer.setRealSum(getSum(dealer.getSum()));
                //sendStatusToAll(false);
            }
            System.out.println(checkAll());
            for(Player player : players){
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
            sendStatusToAll(false);
            for(Player player : new ArrayList<Player>(players)){
                if(player.getMoney()<=0){
                    player.sayBye();
                    players.remove(player);
                }
            }
            
        }


        for(Player player : players){
            player.sendMSG("#byebye");
        }
        System.out.println("lelepett mindenki");
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

    private String getStatus(boolean hideSecond){
        String status = "";
        status += mergeInteger("money", dealer.getMoney());
        status += mergeString("cards", dealer.cardsToString(hideSecond));
        if(hideSecond){
            status += mergeString("sum", "0");
            status += mergeInteger("realsum", 0);
        } else {
            status += mergeString("sum", dealer.getSum());
            status += mergeInteger("realsum", dealer.getRealSum());
        }



        for(Player player : players){
            status += "$$$$$" + mergeInteger("id", player.getId());
            status += mergeString("name", player.getName());
            status += mergeInteger("status", player.getStatus());
            status += mergeInteger("money", player.getMoney());
            status += mergeInteger("bet", player.getBet());
            status += mergeString("cards", player.cardsToString());
            status += mergeString("sum", player.getSum());
            status += mergeInteger("realsum", player.getRealSum());
        }
        return status;
    }

    private String mergeInteger(String a, int b){
        return a + "-" + Integer.toString(b) + ";";

    }

    private String mergeString(String a, String b){
        return a + "-" + b + ";";

    }

    private void sendStatusToAll(boolean hideSecond){
        for(Player player : players){
            player.sendMSG(getStatus(hideSecond));
            player.getMSG();
        }
    }

    private int getSum(String s){
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
            if(player.getRealSum()>21){
                sumAll += player.getBet();
            } else {
                if(dealer.getRealSum()>21){
                    sumAll -= player.getBet();
                } else {
                    if(player.getRealSum()==21 && player.getCards().size()==2){
                        sumAll -= player.getBet()*1.5;
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


/**
 * 
 * zsetonok kiosztása+
 * kör kezdete+
 * tétek megtétele+
 * 2 lap osztása mindenkinek+
 * még/elég kérdezése mindent játékostól+
 * új lap, amíg még kér+
 * osztó helyzetkiértékelés
 * új lap az osztónak, amíg vesztes helyzet és <17
 * ha van még játékos és az osztónak van pénze: goto kör kezdete
 * 
 */