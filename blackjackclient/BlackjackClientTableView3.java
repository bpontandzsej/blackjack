package blackjackclient;

import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.shape.Circle;

public class BlackjackClientTableView extends Application{
    public BlackjackClientTableView() throws Exception{
        launch();

    }

    @Override
    public void start(Stage stage) {
        Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("My JavaFX Application");
        stage.setScene(scene);
        stage.show();
    }




    /*private JButton chatSend;
    private JTextField chatInput;

    public BlackjackClientTableView(){
        this.setTitle("BlackJack - table");
        this.setSize(800, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        
        reCreate("fasf");
        this.setVisible(true);
    }

    public void reCreate(String s){
        this.getContentPane().removeAll(); 
        
        GridBagConstraints mainConstraints = new GridBagConstraints();

        JPanel chatPanel = new JPanel(new GridBagLayout());       
        JTextArea chatText = new JTextArea(s, 5, 20);
        chatInput = new JTextField(10);
        chatSend = new JButton("Send");
                
        GridBagConstraints chatConstraints = new GridBagConstraints();

        chatConstraints.gridx = 0;
        chatConstraints.gridy = 0;
        chatConstraints.gridwidth = 2;
        chatPanel.add(chatText, chatConstraints);

        chatConstraints.gridy = 1;
        chatConstraints.gridwidth = 1;
        chatPanel.add(chatInput, chatConstraints);

        chatConstraints.gridx = 1;
        chatPanel.add(chatSend, chatConstraints);
        
        mainConstraints.gridx = 0;
        mainConstraints.gridy = 0;
        this.add(chatPanel, mainConstraints);

        JPanel dealerPanel = new JPanel();
        dealerPanel.setLayout(new BoxLayout(dealerPanel, BoxLayout.PAGE_AXIS));

        JPanel dealersCardsPanel = new JPanel();
        dealersCardsPanel.setLayout(new BoxLayout(dealersCardsPanel, BoxLayout.LINE_AXIS));

        JLabel dealerSum = new JLabel("dealersum");

        dealerPanel.add(dealersCardsPanel);
        dealerPanel.add(dealerSum);

        mainConstraints.gridx = 1;
        mainConstraints.gridy = 0;
        this.add(dealerPanel, mainConstraints);


        this.validate();
        this.repaint();
    }

    public JButton getSendButton(){
        return chatSend;
    }

    public String getChatMSG(){
        return chatInput.getText();
    }*/

}