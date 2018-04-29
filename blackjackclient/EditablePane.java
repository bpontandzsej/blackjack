package blackjackclient;

import javafx.scene.layout.Pane;

public abstract class EditablePane extends Pane{
    public EditablePane(){
    }

    public void setSize(javafx.scene.layout.Region obj, int width, int height){
        obj.setPrefSize(width, height);
        obj.setMinSize(width, height);
        obj.setMaxSize(width, height);
    }

    public void format(javafx.scene.layout.Region obj, String bgcolor, String brcolor, int brwidth, int round){
        obj.setStyle("-fx-background-color: " + bgcolor + "; -fx-background-radius: " + Integer.toString(round) + "; -fx-border-width: " + Integer.toString(brwidth) + "; -fx-border-color: " + brcolor + "; -fx-border-radius: " + Integer.toString(round) + ";");
    }
}