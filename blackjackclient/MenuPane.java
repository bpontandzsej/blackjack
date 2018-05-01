package blackjackclient;

import java.nio.charset.Charset;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class MenuPane extends EditablePane{
    private Button newGameButton;
    private Button exitButton;
    public MenuPane(){
        setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-font: 16px sans-serif; -fx-font-weight: bold;");
        newGameButton = new Button("NEW GAME");
        setSize(newGameButton, 500, 40);
        format(newGameButton, "#7c7", "black", 1, 5);
        newGameButton.relocate(0, 0);

        final String desc = "BLACKJACK game!" + System.lineSeparator() + System.lineSeparator() +
        "Blackjack, also known as twenty-one, is a card game played with a deck of French cards. The objective: reach higher score than the dealer without exceeding 21." + System.lineSeparator() + System.lineSeparator() + 
        "The game is played in rounds. All participants receive two cards and then are asked to Hit or Stand." + System.lineSeparator() + System.lineSeparator() + 
        "The main objective: loot the dealer!" + System.lineSeparator() + System.lineSeparator() + 
        "Let's play!";

        Label description = new Label(desc);
        description.setStyle("-fx-text-alignment: center; -fx-padding: 20;");
        description.setWrapText(true);
        setSize(description, 500, 420);
        description.relocate(0, 40);

        exitButton = new Button("EXIT");
        setSize(exitButton, 500, 40);
        format(exitButton, "#c77", "black", 1, 5);
        exitButton.relocate(0, 460);
        
        getChildren().addAll(newGameButton, description, exitButton);
    }

    public Button getNewGameButton(){
        return newGameButton;
    }

    public Button getExitButton(){
        return exitButton;
    }
    
}