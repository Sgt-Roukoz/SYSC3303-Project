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
        if (column == specialColumn && (color == Color.red || color == Color.yellow)){
            if (color == Color.red){
                cellBox.setBackground(Color.red);
            } else{
                cellBox.setBackground(Color.yellow);
            }
            return cellBox;
        }
        if (row == specialRow && column == specialColumn) { //Cell only
                cellBox.setBackground(color);
        } else {
            cellBox.setBackground(Color.WHITE);
            System.out.println("Something went wrong in GUI coloring");
        }
        return cellBox;
    }
}
