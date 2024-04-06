/**
 * Represent the GUI interface for the elevator subsystem
 * @author Marwan Zeid
 * @author Garrison Su
 * @version 2024-04-06
 */


package Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;

public class ElevatorInspector extends JFrame {
    private static ElevatorInspector instance;
    private JTable elevatorTable;
    GridBagLayout layout;
    GridBagConstraints gbc;
    JTextArea elev1TextArea;
    JTextArea elev2TextArea;
    JTextArea elev3TextArea;
    JTextArea elev4TextArea;
//    public void moveElevatorGUI(int elevatorId, int floor) {
//        DefaultTableModel model = (DefaultTableModel) elevatorTable.getModel();
//        for (int i = 0; i < model.getRowCount(); i++) {
//            model.setValueAt("", i, elevatorId);
//
//        }
//        model.setValueAt("TESTING", 22 - floor, elevatorId); //floor 1 = index
//    }

    public void moveElevatorGUI(int elevatorId, int floor, int error) {
        Color color = switch (error) {
            case 0 -> Color.GREEN;
            case 1 -> Color.YELLOW;
            case 2 -> Color.RED;
            default -> Color.CYAN; //Nothing used for testing
        }; // Nothing for error checking
        elevatorTable.getColumnModel().getColumn(elevatorId).setCellRenderer(new CustomCellRenderer(22 - floor, elevatorId, color));
        elevatorTable.repaint();
    }

    public static ElevatorInspector getInstance() {
        if (instance == null) {
            instance = new ElevatorInspector();
        }
        return instance;
    }
    public void updateElevatorLog(int elevatorId, String message) {
        switch (elevatorId) {
            case 1-> elev1TextArea.append(message + "\n");
            case 2-> elev2TextArea.append(message + "\n");
            case 3-> elev3TextArea.append(message + "\n");
            case 4-> elev4TextArea.append(message + "\n");
            default-> System.out.println("NOT PRINTING TO GUI");
        }
    }

    public ElevatorInspector()
    {
        super("Elevator Inspector");
        this.layout = new GridBagLayout();
        this.gbc = new GridBagConstraints();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 900);
        setResizable(false);
        setLayout(layout);

        elev1TextArea = new JTextArea();
        elev2TextArea = new JTextArea();
        elev3TextArea = new JTextArea();
        elev4TextArea = new JTextArea();
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        elev1TextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 1)));
        elev2TextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 1, 10, 1)));
        elev3TextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 1, 1, 10)));
        elev4TextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 1, 10, 10)));
        elev1TextArea.setPreferredSize(new Dimension(50,100));
        elev2TextArea.setPreferredSize(new Dimension(50,100));
        elev3TextArea.setPreferredSize(new Dimension(50,100));
        elev4TextArea.setPreferredSize(new Dimension(50,100));


        elev1TextArea.setLineWrap(true);
        elev1TextArea.setWrapStyleWord(true);
        elev2TextArea.setLineWrap(true);
        elev2TextArea.setWrapStyleWord(true);
        elev3TextArea.setLineWrap(true);
        elev3TextArea.setWrapStyleWord(true);
        elev4TextArea.setLineWrap(true);
        elev4TextArea.setWrapStyleWord(true);


//        JTable elevatorTable = new JTable(new DefaultTableModel(new Object[]{"Floor", "Elevator 1", "Elevator 2", "Elevator 3", "Elevator 4"}, 22));

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Floor", "Elevator 1", "Elevator 2", "Elevator 3", "Elevator 4"}, 0);
        for (int i = 22; i >= 1; i--) {
            model.addRow(new Object[]{i, "", "", "", ""});
        }
        JTable elevatorTable = new JTable(model);
        this.elevatorTable = elevatorTable;


        elevatorTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(elevatorTable);
        elevatorTable.setRowHeight(37);
        TableColumnModel mod = elevatorTable.getColumnModel();
        mod.getColumn(0).setPreferredWidth(50);
        mod.getColumn(1).setPreferredWidth(123);
        mod.getColumn(2).setPreferredWidth(123);
        mod.getColumn(3).setPreferredWidth(123);
        mod.getColumn(4).setPreferredWidth(123);


        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel testPanel = new JPanel();
        JPanel testPanel2 = new JPanel();


        DefaultTableCellRenderer render1 = new DefaultTableCellRenderer();
        render1.setBackground(Color.lightGray);
        elevatorTable.getColumnModel().getColumn(0).setCellRenderer(render1);


        testPanel.setLayout(new GridLayout(4,1));
        testPanel.add(elev1TextArea);
        testPanel.add(elev2TextArea);
        testPanel.add(elev3TextArea);
        testPanel.add(elev4TextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Elevator Panel"));
        testPanel.setBorder(BorderFactory.createTitledBorder("Elevator Logs"));
        testPanel2.setBorder(BorderFactory.createTitledBorder("Control Panel"));
        //gbc.insets = new Insets(0, 150, 0, 150);
        addObject(scrollPane, this, 0,0,1,1, 0.1, 0.224);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        addObject(testPanel, this, 1,0,1,1, 0.4, 0.4);
        addObject(testPanel2, this, 2,0,1,1, 0.3, 0.4);

        System.out.println(elev2TextArea.getWidth());
        setVisible(true);
    }

    public void addObject(Component component, Container parentContainer, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty){

        gbc.gridx = gridx;
        gbc.gridy = gridy;

        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;

        gbc.weightx = weightx;
        gbc.weighty = weighty;

        parentContainer.add(component, gbc);
    }

    public static void main(String[] args)
    {
//        new ElevatorInspector();
            ElevatorInspector.getInstance().setVisible(true);

    }

}
