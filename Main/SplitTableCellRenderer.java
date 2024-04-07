/**
 * Renderer for Floor lamp cells
 */

package Main;

import Main.CellPanel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


public class SplitTableCellRenderer implements TableCellRenderer {

    CellPanel rendererPanel;

    public SplitTableCellRenderer()
    {
        super();
        rendererPanel = new CellPanel();
    }

    //overriding builtin table cell renderer
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