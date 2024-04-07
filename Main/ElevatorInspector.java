/**
 * Represent the GUI interface for the elevator subsystem
 * @author Marwan Zeid
 * @author Garrison Su
 * @author Eric Wang
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
import javax.swing.text.DefaultCaret;
import java.lang.Integer;
import java.lang.String;
import java.util.Map;

public class ElevatorInspector extends JFrame implements Runnable {
    private JTable elevatorTable;
    private SchedulerStoreInt store;
    GridBagLayout layout;
    GridBagConstraints gbc;
    JTextArea elev1TextArea;
    JTextArea elev2TextArea;
    JTextArea elev3TextArea;
    JTextArea elev4TextArea;
    JTextArea SchedulerTextArea;
    JTextField elev1Pass;
    JTextField elev2Pass;
    JTextField elev3Pass;
    JTextField elev4Pass;
    JTextField requestsDone;
    JTextField firstRequest;
    JTextField lastRequest;
    JTextField totalMoves;

    /**
     * Constructor for Elevator Inspector
     * @param store store where values being pulled are from
     */
    public ElevatorInspector(SchedulerStoreInt store)
    {
        super("Elevator Inspector");
        this.layout = new GridBagLayout();
        this.gbc = new GridBagConstraints();
        this.store = store;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1260, 900);
        setResizable(false);
        setLayout(layout);

        //constructors for textareas and textfields
        elev1TextArea = new JTextArea();
        elev2TextArea = new JTextArea();
        elev3TextArea = new JTextArea();
        elev4TextArea = new JTextArea();
        SchedulerTextArea = new JTextArea();
        elev1Pass = new JTextField("", 10);
        elev2Pass = new JTextField("", 10);
        elev3Pass = new JTextField("", 10);
        elev4Pass = new JTextField("", 10);
        requestsDone = new JTextField("", 10);
        firstRequest = new JTextField("", 10);
        lastRequest = new JTextField("", 10);
        totalMoves = new JTextField("", 10);

        //setting up borders for textfields
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
        elev4TextArea.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 1, 10, 10)));


        //setting up editability of components
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
        elev1Pass.setEditable(false);
        elev2Pass.setEditable(false);
        elev3Pass.setEditable(false);
        elev4Pass.setEditable(false);
        requestsDone.setEditable(false);
        firstRequest.setEditable(false);
        lastRequest.setEditable(false);
        totalMoves.setEditable(false);

        //setting up elevator table
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Floor", "Elevator 1", "Elevator 2", "Elevator 3", "Elevator 4"}, 0);
        for (int i = 22; i >= 1; i--) {
            model.addRow(new Object[]{null, "", "", "", ""});
        }

        JTable elevatorTable = new JTable(model);
        this.elevatorTable = elevatorTable;
        elevatorTable.setDefaultEditor(Object.class, null);
        elevatorTable.getColumnModel().getColumn(1).setCellRenderer(new CustomCellRenderer(1 , 2, Color.red));
        elevatorTable.getColumnModel().getColumn(2).setCellRenderer(new CustomCellRenderer(1 , 2, Color.red));
        elevatorTable.getColumnModel().getColumn(3).setCellRenderer(new CustomCellRenderer(1 , 2, Color.red));
        elevatorTable.getColumnModel().getColumn(4).setCellRenderer(new CustomCellRenderer(1 , 2, Color.red));

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

        elevatorTable.getColumnModel().getColumn(0).setCellRenderer(new SplitTableCellRenderer());
        elevatorTable.getTableHeader().setReorderingAllowed(false);

        // setting up text area auto scroll
        testPanel.setLayout(new GridLayout(4,1));
        DefaultCaret caret = (DefaultCaret)elev1TextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret2 = (DefaultCaret)elev2TextArea.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret3 = (DefaultCaret)elev3TextArea.getCaret();
        caret3.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret4 = (DefaultCaret)elev4TextArea.getCaret();
        caret4.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret5 = (DefaultCaret)SchedulerTextArea.getCaret();
        caret5.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollElev1 = new JScrollPane(elev1TextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scrollElev2 = new JScrollPane(elev2TextArea);
        JScrollPane scrollElev3 = new JScrollPane(elev3TextArea);
        JScrollPane scrollElev4 = new JScrollPane(elev4TextArea);
        JScrollPane scrollSched = new JScrollPane(SchedulerTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollElev1.setPreferredSize(new Dimension(40,100));
        scrollElev2.setPreferredSize(new Dimension(40,100));
        scrollElev3.setPreferredSize(new Dimension(40,100));
        scrollElev4.setPreferredSize(new Dimension(40,100));
        scrollSched.setPreferredSize(new Dimension(200,100));

        testPanel.add(scrollElev1);
        testPanel.add(scrollElev2);
        testPanel.add(scrollElev3);
        testPanel.add(scrollElev4);
        //(new JLabel("elev pass")).setLabelFor(elev1Pass);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Elevator Panel"));
        testPanel.setBorder(BorderFactory.createTitledBorder("Elevator Logs"));
        testPanel2.setBorder(BorderFactory.createTitledBorder("Control Panel"));
        //gbc.insets = new Insets(0, 150, 0, 150);
        addObject(scrollPane, this, 0,0,1,1, 0.11, 0.224);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        addObject(testPanel, this, 1,0,1,1, 0.32, 0.4);
        addObject(testPanel2, this, 2,0,1,1, 0.125, 0.4);
        testPanel2.setLayout(new GridLayout(2,1));
        testPanel2.add(scrollSched);


        JPanel numberFields = new JPanel(new GridLayout(4,2));

        //adding textfields for various statistics
        JPanel elev1Panel = new JPanel(new FlowLayout());
        elev1Panel.add(new JLabel("<html>Elevator 1 Passengers:</html>"));
        elev1Panel.add(elev1Pass);
        //elev1Pass.setPreferredSize(new Dimension(10,elev1Pass.getHeight()));
        numberFields.add(elev1Panel);
        JPanel elev2Panel = new JPanel(new FlowLayout());
        elev2Panel.add(new JLabel("<html>Elevator 2 Passengers:</html>"));
        elev2Panel.add(elev2Pass);
        numberFields.add(elev2Panel);
        JPanel elev3Panel = new JPanel(new FlowLayout());
        elev3Panel.add(new JLabel("<html>Elevator 3 Passengers:</html>"));
        elev3Panel.add(elev3Pass);
        numberFields.add(elev3Panel);
        JPanel elev4Panel = new JPanel(new FlowLayout());
        elev4Panel.add(new JLabel("<html>Elevator 4 Passengers:</html>"));
        elev4Panel.add(elev4Pass);
        numberFields.add(elev4Panel);
        JPanel requestsPanel = new JPanel(new FlowLayout());
        requestsPanel.add(new JLabel("<html>Requests Done:</html>"));
        requestsPanel.add(requestsDone);
        numberFields.add(requestsPanel);
        JPanel movesPanel = new JPanel(new FlowLayout());
        movesPanel.add(new JLabel("<html>Total Moves:</html>"));
        movesPanel.add(totalMoves);
        numberFields.add(movesPanel);
        JPanel firstReqPanel = new JPanel(new FlowLayout());
        firstReqPanel.add(new JLabel("<html>First Request:</html>"));
        firstReqPanel.add(firstRequest);
        numberFields.add(firstReqPanel);
        JPanel lastReqPanel = new JPanel(new FlowLayout());
        lastReqPanel.add(new JLabel("<html>Last Request:</html>"));
        lastReqPanel.add(lastRequest);
        numberFields.add(lastReqPanel);
        elev1Pass.setText("0");
        elev2Pass.setText("0");
        elev3Pass.setText("0");
        elev4Pass.setText("0");
        requestsDone.setText("0");
        totalMoves.setText("0");
        firstRequest.setText("0");
        lastRequest.setText("0");

        //numberFields.set
        testPanel2.add(numberFields);
        numberFields.setPreferredSize(new Dimension(testPanel2.getWidth(), testPanel2.getHeight()/2));

        //System.out.println(elev2TextArea.getWidth());
        setVisible(true);
        for (int i = 0; i < 22; i++) {
            elevatorTable.getColumnModel().getColumn(0).getCellRenderer().getTableCellRendererComponent(elevatorTable, "test", false, false, i, 0);
            CellPanel panel = (CellPanel) elevatorTable.getValueAt(i, 0);
            panel.setFloorNumber(Integer.toString(22-i));
            //System.out.println(panel.getFloorNumberText());
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

    private void setTableVal(int col, String val)
    {
            for (int row = 0; row < elevatorTable.getRowCount(); row++) {
                if (!elevatorTable.getValueAt(row, col).equals("*"))
                    elevatorTable.setValueAt(val, row, col);
            }
    }

    private void updateFloorLamps()
    {
        for (int row = 0; row < 22; row++)
        {
            elevatorTable.setValueAt(elevatorTable.getValueAt(row, 0), row, 0);
        }
    }

    private void getMessages()
    {
        try{
            String message = store.receiveLog();
            if (!message.isEmpty()) {
                if (message.contains("Scheduler-1")) { // Scheduler
                    updateSchedulerLog(message);
                }
                else if(message.contains("Elevator-")){ // Elevator

                    int elevatorId = Character.getNumericValue(message.charAt(message.indexOf("-")+1));
                    Map<Integer, ArrayList<Serializable>> allElevators = store.getElevators();
                    ArrayList<Serializable> currentElevatorInfo = allElevators.get(elevatorId);
                    if (currentElevatorInfo != null) {
                        if (!message.contains("opening") & !message.contains("closing")) setTableVal(elevatorId, "");
                        int currentFloor = (Integer) currentElevatorInfo.get(2);
                        int destination = (Integer) currentElevatorInfo.get(4);

                        if (destination == 0) ;
                        else if (destination < currentFloor) {
                            ((CellPanel) elevatorTable.getValueAt(22 - destination, 0)).downLampOn();
                            System.out.println("down lamp on " + destination);
                        } else if (destination > currentFloor) {
                            ((CellPanel) elevatorTable.getValueAt(22 - destination, 0)).upLampOn();
                            System.out.println("up lamp on " + destination);
                        } else {
                            if ((int) currentElevatorInfo.get(3) == 1)
                                ((CellPanel) elevatorTable.getValueAt(22 - destination, 0)).upLampOff();
                            else if ((int) currentElevatorInfo.get(3) == 2)
                                ((CellPanel) elevatorTable.getValueAt(22 - destination, 0)).downLampOff();
                        }

                        if ((int) currentElevatorInfo.get(3) != 0) destinationColor(elevatorId, destination);
                        updateElevatorLog(elevatorId, message);
                        if (message.contains("Error-1")) { // transient error
                            setTableVal(elevatorId, "-");
                        } else if (message.contains("Error-2")) { // hard fault error
                            setTableVal(elevatorId, "*");
                        } else {
                            moveElevatorGUI(elevatorId, currentFloor, 0);
                        }
                    }
                } else if (message.contains("all")){
                    printALlElevators(message);
                }
                firstRequest.setText(store.getFirstRequest());
                lastRequest.setText(store.getLastRequest());
                requestsDone.setText(String.valueOf(store.getPassengersServiced()));
                totalMoves.setText(String.valueOf(store.getMovesDone()));
                if (!store.getElevators().isEmpty())
                {
                    if (store.getElevators().get(1) != null) elev1Pass.setText(store.getElevators().get(1).get(5).toString());
                    if (store.getElevators().get(2) != null)elev2Pass.setText(store.getElevators().get(2).get(5).toString());
                    if (store.getElevators().get(3) != null)elev3Pass.setText(store.getElevators().get(3).get(5).toString());
                    if (store.getElevators().get(4) != null)elev4Pass.setText(store.getElevators().get(4).get(5).toString());
                }
                updateFloorLamps();

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

    public void printALlElevators(String message){
        elev1TextArea.append(message + "\n");
        elev2TextArea.append(message + "\n");
        elev3TextArea.append(message + "\n");
        elev4TextArea.append(message + "\n");
    }

    public void moveElevatorGUI(int elevatorId, int floor, int error) {
        String val = switch (error) {
            case 0 -> "A";
            case 1 -> "-";
            case 2 -> "*";
            default -> ""; //Nothing used for testing
        };

        //elevatorTable.getColumnModel().getColumn(elevatorId).setCellRenderer(new CustomCellRenderer(22 - floor, elevatorId, color));
        //elevatorTable.repaint();
        elevatorTable.setValueAt(val, 22 - floor, elevatorId);
    }

    //Highlight the destination color of elevater it wants to go to
    public void destinationColor(int elevatorId, int destination) {
        //Color color = Color.blue;
        //elevatorTable.getColumnModel().getColumn(elevatorId).setCellRenderer(new CustomCellRenderer(destination, elevatorId, color));
        //elevatorTable.repaint();
        elevatorTable.setValueAt("O", 22-destination, elevatorId);
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
            Thread InspectorThread = new Thread(new ElevatorInspector(store));
            InspectorThread.start();
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
        //upLamp.update();
        //this.repaint();
    }

    public void upLampOff() {
        upLamp.setBackground(Color.white);
        upLamp.updateUI();
        //this.repaint();
    }

    public void downLampOn() {
        downLamp.setBackground(Color.yellow);
        //downLamp.repaint();
        //this.repaint();
    }

    public void downLampOff() {
        downLamp.setBackground(Color.white);
        //downLamp.repaint();
        //this.repaint();
    }

    public void setFloorNumber(String text) {
        floorNumberText.setText(text);
    }

    public String getFloorNumberText() {
        return floorNumberText.getText();
    }
}
