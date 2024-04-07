/**
 * Represent the GUI interface for changing colors
 * @author Garrison Su
 * @version 2024-04-07
 */

package Main;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
public class CustomCellRenderer extends DefaultTableCellRenderer {
    private int specialRow;
    private int specialColumn;
    private Color color;
    public CustomCellRenderer(int specialRow, int specialColumn, Color color) {
        this.specialRow = specialRow;
        this.specialColumn = specialColumn;
        this.color = color;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellBox = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        //COMMENT THIS IF STATEMENT IF IT DOESNT WORK
        //if (column == specialColumn && (color == Color.red || color == Color.yellow)){
        if (((String) value).isEmpty()) {
            cellBox.setBackground(Color.white);
            cellBox.setForeground(Color.white);
        }
        else if (((String) value).equals("-")){
            cellBox.setBackground(Color.yellow);
            cellBox.setForeground(Color.yellow);
        }
        else if (((String) value).equals("*")){
            cellBox.setBackground(Color.red);
            cellBox.setForeground(Color.red);
        }
        else if (((String) value).equals("O")){
            cellBox.setBackground(Color.blue);
            cellBox.setForeground(Color.blue);
        }
        else if (((String) value).equals("A")){
            cellBox.setBackground(Color.green);
            cellBox.setForeground(Color.green);
        }
        return cellBox;
    }
}
