/**
 * Panels for floor lamp cells
 */
package Main;

import javax.swing.*;
import java.awt.*;

public class CellPanel extends JPanel {

    JPanel upLamp;
    JPanel downLamp;
    JPanel floorNumber;
    JTextArea floorNumberText;

    public CellPanel() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        floorNumber = new JPanel();
        floorNumber.setOpaque(true);
        floorNumber.setBackground(Color.white);
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        this.add(floorNumber, gbc);
        floorNumberText = new JTextArea();
        floorNumber.add(floorNumberText);
        upLamp = new JPanel();
        upLamp.setOpaque(true);
        upLamp.setBackground(Color.white);
        gbc.gridheight = 1;
        gbc.weighty = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        this.add(upLamp, gbc);
        downLamp = new JPanel();
        downLamp.setOpaque(true);
        downLamp.setBackground(Color.white);
        this.add(downLamp, gbc);
    }

    /**
     * Turn on UP Lamp
     */
    public void upLampOn() {
        upLamp.setBackground(Color.yellow);
    }

    /**
     * Turn off UP lamp
     */
    public void upLampOff() {
        upLamp.setBackground(Color.white);
        upLamp.updateUI();
        //this.repaint();
    }

    /**
     * Turn on DOWN lamp
     */
    public void downLampOn() {
        downLamp.setBackground(Color.yellow);
        //downLamp.repaint();
        //this.repaint();
    }

    /**
     * Turn off DOWN lamp
     */
    public void downLampOff() {
        downLamp.setBackground(Color.white);
        //downLamp.repaint();
        //this.repaint();
    }

    /**
     * Sets floor number
     * @param text floor number
     */
    public void setFloorNumber(String text) {
        floorNumberText.setText(text);
    }
}