package src.main.java.com.mainInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class menuPage extends JFrame {

    private JSplitPane splitPane1;
    private JSplitPane splitPane2;
    private JPanel optionPanel;
    private JPanel confirmPanel;
    private JPanel menuPanel;
    private registerPage register;
    private registeredPage registered;

    public menuPage() {
        createPanels();
        createButtons();
        createSplitPanes();
        addSplitPaneToFrame();
        setFrameProperties();
    }

    private void createPanels() {
        optionPanel = new JPanel();
        confirmPanel = new JPanel();
        menuPanel = new JPanel();
    }

    private void createButtons() {
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterButtonActionListener());
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.setMaximumSize(registerButton.getPreferredSize());

        JButton registeredButton = new JButton("Registered");
        registeredButton.addActionListener(new RegisteredButtonActionListener());
        registeredButton.setPreferredSize(new Dimension(100, 30));
        registeredButton.setMaximumSize(registeredButton.getPreferredSize());

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.add(registerButton);
        menuPanel.add(registeredButton);
        menuPanel.setBorder(BorderFactory.createTitledBorder("MENU"));
    }

    private void createSplitPanes() {
        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionPanel, confirmPanel);
        splitPane1.setResizeWeight(0.5);

        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, splitPane1);
    }

    private void addSplitPaneToFrame() {
        add(splitPane2);
    }

    private void setFrameProperties() {
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private class RegisterButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Initialize the register page and add it to the option panel
            register = new registerPage();
            optionPanel.removeAll();
            optionPanel.add(register);
            optionPanel.revalidate();
            optionPanel.repaint();
        }
    }

    private class RegisteredButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Initialize the registered page and add it to the option panel
            registered = new registeredPage();
            optionPanel.removeAll();
            optionPanel.add(registered);
            optionPanel.revalidate();
            optionPanel.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(menuPage::new);
    }
}