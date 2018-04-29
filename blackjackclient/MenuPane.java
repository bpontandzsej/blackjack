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
        setStyle("-fx-background-image: url('/blackjackclient/media/currenttable.png'); -fx-font: 16px sans-serif;");
        newGameButton = new Button("UJ JATEK");
        setSize(newGameButton, 500, 40);
        newGameButton.relocate(0, 0);

        final String desc = "BLACKJACK jatek!" + System.lineSeparator() + System.lineSeparator() + "A Blackjack, masneven Huszonegy jatekot egy 52 lapos francia kartyapaklival jatszak. A legfobb szabaly: a lapjaid erteke legyen tobb, mint az oszto lapjainakt erteke, de ne lepje tul a 21-et." + System.lineSeparator() + System.lineSeparator() + "A jatekot korokbe bontva jatszak. Kor elejen minden jatekos, valamint az oszto kap 2 lapot. Eloszor a jatekosok lepnek: kerhetnek meg lapot vagy megallhatnak. Amint az osszes jatekos befejezte a lepest, kovetkezik az oszto, aki szinten huzhat meg lapot vagy megallhat." + System.lineSeparator() + System.lineSeparator() + "A jatek celja: fogyjon el az oszto osszes zsetonja!" + System.lineSeparator() + System.lineSeparator() + "Jatekra fel!";

        Label description = new Label(desc);
        description.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        description.setWrapText(true);
        setSize(description, 500, 420);
        description.relocate(0, 40);

        exitButton = new Button("KILEPES");
        setSize(exitButton, 500, 40);
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