package blackjackserver;

public class Card{
    private int number;
    private char color;

    public Card(int number, char color){
        this.number = number;
        this.color = color;
    }

    public int getNumber(){
        return this.number;
    }

    public char getColor(){
        return this.color;
    }

    public String cardToString(){
        return (this.color + ":" + Integer.toString(this.number));
    }
}