/**
 * Represent the GUI interface for the elevator subsystem
 * @author Marwan Zeid
 * @author Garrison Su
 * @version 2024-04-07
 */


package Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.table.DefaultTableCellRenderer;
import java.lang.Integer;
import java.lang.String;
import java.util.Map;

public class ElevatorInspector extends JFrame implements Runnable {
    private static ElevatorInspector instance;
    private JTable elevatorTable;
    private SchedulerStoreInt store;
    GridBagLayout layout;
    GridBagConstraints gbc;
    JTextArea elev1TextArea;
    JTextArea elev2TextArea;
    JTextArea elev3TextArea;
    JTextArea elev4TextArea;
    JTextArea SchedulerTextArea;

//    public void moveElevatorGUI(int elevatorId, int floor) {
//        DefaultTableModel model = (DefaultTableModel) elevatorTable.getModel();
//        for (int i = 0; i < model.getRowCount(); i++) {
//            model.setValueAt("", i, elevatorId);
//
//        }
//        model.setValueAt("TESTING", 22 - floor, elevatorId); //floor 1 = index
//    }

    public ElevatorInspector(SchedulerStoreInt store)
    {
        super("Elevator Inspector");
        this.layout = new GridBagLayout();
        this.gbc = new GridBagConstraints();
        this.store = store;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 900);
        setResizable(false);
        setLayout(layout);

        elev1TextArea = new JTextArea();
        elev2TextArea = new JTextArea();
        elev3TextArea = new JTextArea();
        elev4TextArea = new JTextArea();
        SchedulerTextArea = new JTextArea();

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        SchedulerTextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 1)));

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
        SchedulerTextArea.setPreferredSize(new Dimension(200,400));


        elev1TextArea.setLineWrap(true);
        elev1TextArea.setWrapStyleWord(true);
        elev2TextArea.setLineWrap(true);
        elev2TextArea.setWrapStyleWord(true);
        elev3TextArea.setLineWrap(true);
        elev3TextArea.setWrapStyleWord(true);
        elev4TextArea.setLineWrap(true);
        elev4TextArea.setWrapStyleWord(true);
        SchedulerTextArea.setLineWrap(true);
        SchedulerTextArea.setWrapStyleWord(true);

        elev1TextArea.setEditable(false);
        elev2TextArea.setEditable(false);
        elev3TextArea.setEditable(false);
        elev4TextArea.setEditable(false);
        SchedulerTextArea.setEditable(false);
//        JTable elevatorTable = new JTable(new DefaultTableModel(new Object[]{"Floor", "Elevator 1", "Elevator 2", "Elevator 3", "Elevator 4"}, 22));
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Floor", "Elevator 1", "Elevator 2", "Elevator 3", "Elevator 4"}, 0);
        for (int i = 22; i >= 1; i--) {
            model.addRow(new Object[]{null, "", "", "", ""});


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


        /*DefaultTableCellRenderer render1 = new DefaultTableCellRenderer();
        render1.setBackground(Color.lightGray);
        elevatorTable.getColumnModel().getColumn(0).setCellRenderer(render1);*/

        elevatorTable.getColumnModel().getColumn(0).setCellRenderer(new SplitTableCellRenderer());
        elevatorTable.getTableHeader().setReorderingAllowed(false);

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
        testPanel2.setLayout(new BorderLayout());
        testPanel2.add(new JScrollPane(SchedulerTextArea), BorderLayout.CENTER);
        //System.out.println(elev2TextArea.getWidth());
        setVisible(true);
        for (int i = 0; i < 22; i++) {
            elevatorTable.getColumnModel().getColumn(0).getCellRenderer().getTableCellRendererComponent(elevatorTable, "test", false, false, i, 0);
            CellPanel panel = (CellPanel) elevatorTable.getValueAt(i, 0);
            panel.setFloorNumber(Integer.toString(22-i));
            System.out.println(panel.getFloorNumberText());
        }
        this.repaint();
    }

    @Override
    public void run() {
        while (!Thread.interrupted())
        {
            getMessages();
        }
    }

    private void getMessages()
    {
        try{
            String message = store.receiveLog();
            if (!message.isEmpty()) {
                if (message.contains("Scheduler")) { // Scheduler
                    updateSchedulerLog(message);
//                    int semiIndex = message.indexOf(":");
//                    String messageText = message.substring(semiIndex + 1);
                }
                else if(message.contains("Elevator-")){ // Elevator
                    int elevatorId = Integer.parseInt(message.substring(message.lastIndexOf("Elevator-") + 1));
                    Map<Integer, ArrayList<Serializable>> allElevators = store.getElevators();
                    ArrayList<Serializable> currentElevatorInfo = allElevators.get(elevatorId);
                    int currentFloor = (Integer) currentElevatorInfo.get(2);

                    int destination = (Integer) currentElevatorInfo.get(3);
                    destinationColor(elevatorId, destination);

                    updateElevatorLog(elevatorId, message);
                    if (message.contains("Error-1")) { // traunset error
                        moveElevatorGUI(elevatorId, currentFloor, 1);
                    } else if (message.contains("Error-2")) { // hard fault
                        moveElevatorGUI(elevatorId, currentFloor, 2);
                    } else{
                        moveElevatorGUI(elevatorId, currentFloor, 0);
                    }
                } else if (message.contains("all")){
                    printALlElevators(message);
                }
            }
        } catch (RemoteException e) {
            System.out.println("Error in getMessages");
        }
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

    public void updateSchedulerLog(String message){
        SchedulerTextArea.append(message + "\n");
    }
    public static ElevatorInspector getInstance(SchedulerStoreInt store) {
        if (instance == null) {
            instance = new ElevatorInspector(store);
        }
        return instance;
    }
    public void printALlElevators(String message){
        elev1TextArea.append(message + "\n");
        elev2TextArea.append(message + "\n");
        elev3TextArea.append(message + "\n");
        elev4TextArea.append(message + "\n");
    }

    public void moveElevatorGUI(int elevatorId, int floor, int error) {
        Color color = switch (error) {
            case 0 -> Color.GREEN;
            case 1 -> Color.YELLOW;
            case 2 -> Color.RED;
            default -> Color.CYAN; //Nothing used for testing
        };
        elevatorTable.getColumnModel().getColumn(elevatorId).setCellRenderer(new CustomCellRenderer(22 - floor, elevatorId, color));
        elevatorTable.repaint();
    }

    //Highlight the destination color of elevater it wants to go to
    public void destinationColor(int elevatorId, int destination) {
        Color color = Color.blue;
        elevatorTable.getColumnModel().getColumn(elevatorId).setCellRenderer(new CustomCellRenderer(destination, elevatorId, color));
        elevatorTable.repaint();
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

    public static void main(String[] args) {
        try {
            SchedulerStoreInt store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");

            ElevatorInspector.getInstance(store).setVisible(true);
            Scheduler scheduler = new Scheduler(store);

            Thread schedulerThread = new Thread(scheduler);
            schedulerThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }

}

class SplitTableCellRenderer implements TableCellRenderer {

    CellPanel rendererPanel;

    public SplitTableCellRenderer()
    {
        super();
        rendererPanel = new CellPanel();
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(table.getValueAt(row, column) == null) {
            CellPanel panel = new CellPanel();
            panel.setOpaque(true);
            panel.requestFocusInWindow();
            table.setValueAt(panel, row, column);
            return panel;
        }
        else return (CellPanel) table.getValueAt(row, column);
    }
}

class CellPanel extends JPanel {

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

    public void upLampOn() {
        upLamp.setBackground(Color.yellow);
    }

    public void upLampOff() {
        upLamp.setBackground(Color.white);
    }

    public void downLampOn() {
        upLamp.setBackground(Color.yellow);
    }

    public void downLampOff() {
        upLamp.setBackground(Color.white);
    }

    public void setFloorNumber(String text) {
        floorNumberText.setText(text);
    }

    public String getFloorNumberText() {
        return floorNumberText.getText();
    }
}
