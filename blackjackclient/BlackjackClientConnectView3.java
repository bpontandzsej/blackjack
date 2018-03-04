package blackjackclient;

import javax.swing.*;

public class BlackjackClientConnectView extends JFrame{
    private JLabel label;
    private JTextField inputForName;
    private JButton connectButton;
    
    public BlackjackClientConnectView(){
        this.setTitle("BlackJack - connect");
        this.setSize(320, 240);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        label = new JLabel("Nickname: ");
        label.setBounds(10, 10, 200, 20);
        add(label);

        inputForName = new JTextField();
        inputForName.setBounds(10, 40, 200, 20);
        add(inputForName);

        connectButton = new JButton("Connect");
        connectButton.setBounds(10, 70, 200, 20);
        add(connectButton);

        this.setVisible(true);
    }

    public JButton getConnectButton(){
        return connectButton;
    }

    public String getInputName(){
        return inputForName.getText();
    }
}