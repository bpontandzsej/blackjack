package blackjackclient;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class MenuPane extends BorderPane{
    private Button newGameButton;
    private Button exitButton;
    public MenuPane(){
        newGameButton = new Button("NEW GAME");
        setSize(newGameButton, 500, 40);
        newGameButton.relocate(0, 0);

        Label description = new Label("some description");
        setSize(description, 500, 420);
        description.relocate(0, 40);

        exitButton = new Button("EXIT");
        setSize(exitButton, 500, 40);
        exitButton.relocate(0, 440);
        
        setAlignment(newGameButton, Pos.CENTER);
        setTop(newGameButton);
        setAlignment(description, Pos.CENTER);
        setCenter(description);
        setAlignment(exitButton, Pos.CENTER);
        setBottom(exitButton);
    }

    private void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    public Button getNewGameButton(){
        return newGameButton;
    }

    public Button getExitButton(){
        return exitButton;
    }
    
}