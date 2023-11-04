import Basics.ModelQueryTool;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ModelQueryTool modelQueryTool = new ModelQueryTool();
        SwingUtilities.invokeLater(modelQueryTool::startupGUI);
    }
}
