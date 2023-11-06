package Basics;

import Analysis.AnalysisPipeline;
import Analysis.EvaluationPipeline;
import DataProcessing.FeaturePipeline;
import Preprocessing.PreperationPipeline;
import Querying.DistanceFunctions.WeightedDistanceFunction;
import Querying.FileQueryProcessor;
import Querying.FileQueryResult;
import Rendering.MeshRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;

public class ModelQueryTool {

    public void startupGUI() {
        // Set the system default look for the UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set UI theme, using default.");
        }

        JFrame frame = new JFrame("ModelQueryTool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // <editor-fold desc="=== ACTION PANEL ===">
        JPanel actionPanel = new JPanel(new GridLayout(1, 2));
        // <editor-fold desc="Left panel">
        JPanel leftPanel = new JPanel(new GridLayout(0, 1));
        JCheckBox describeCheckBox = new JCheckBox("Calculate database features");
        JCheckBox cleanCheckBox = new JCheckBox("Clean database");
        JCheckBox analyzeCheckBox = new JCheckBox("Analyze database");
        leftPanel.add(panelWithMargin(describeCheckBox));
        leftPanel.add(panelWithMargin(cleanCheckBox));
        leftPanel.add(panelWithMargin(analyzeCheckBox));
        // </editor-fold>
        // <editor-fold desc="Right panel">
        JPanel rightPanel = new JPanel(new GridLayout(0, 1));
        JCheckBox evaluateCheckBox = new JCheckBox("Evaluate performance");
        JCheckBox matrixCheckBox = new JCheckBox("Save distance matrix");
        JCheckBox queryCheckBox = new JCheckBox("Make a query");
        rightPanel.add(panelWithMargin(evaluateCheckBox));
        rightPanel.add(panelWithMargin(matrixCheckBox));
        rightPanel.add(panelWithMargin(queryCheckBox));
        queryCheckBox.setSelected(true);
        // </editor-fold>
        actionPanel.add(leftPanel);
        actionPanel.add(rightPanel);
        GridBagConstraints upperConstraints = new GridBagConstraints();
        upperConstraints.gridx = 0;
        upperConstraints.gridy = 0;
        frame.add(panelWithMargin(actionPanel, 16, 0), upperConstraints);
        // </editor-fold>

        // <editor-fold desc="=== PARAMETER PANEL ===">
        JPanel parameterPanel = new JPanel(new GridLayout(1, 2));
        // <editor-fold desc="preprocessing parameters">
        JPanel preprocessingParamPanel = new JPanel(new GridLayout(4, 1));

        JComboBox<String> cleaned = new JComboBox<>(new String[]{"Original files", "Cleaned files"});
        cleaned.setEnabled(false);
        if (Config.workOnCleaned()) cleaned.setSelectedIndex(1);
        String[] pythonHandles = new String[] {"py", "python"};
        JComboBox<String> pythonCommands = new JComboBox<>(pythonHandles);
        pythonCommands.setSelectedItem(Config.getPythonHandle());

        preprocessingParamPanel.add(panelWithMargin(cleaned, "Target files"));
        preprocessingParamPanel.add(new JPanel());
        preprocessingParamPanel.add(panelWithMargin(pythonCommands, "Python command"));
        parameterPanel.add(preprocessingParamPanel);
        // </editor-fold>

        // <editor-fold desc="query parameters">
        JPanel queryParamPanel = new JPanel(new GridLayout(4, 1));

        String[] distanceFunctions = WeightedDistanceFunction.getDistanceFunctions();
        JComboBox<String> dfs = new JComboBox<>(distanceFunctions);
        dfs.setSelectedItem(Config.getDistanceFunction());
        String[] normalizationMethods = new String[] {"standardization", "min-max normalization"};
        JComboBox<String> normalizationMethod = new JComboBox<>(normalizationMethods);
        if (!Config.standardized()) normalizationMethod.setSelectedIndex(1);
        String[] matchingMethods = new String[] {"brute_force", "kd_tree"};
        JComboBox<String> matchers = new JComboBox<>(matchingMethods);
        matchers.setSelectedItem(Config.getMatchingMethod());
        JTextField kBestField = new JTextField(String.valueOf(Config.kBest()));


        queryParamPanel.add(panelWithMargin(dfs, "Distance function"));
        queryParamPanel.add(panelWithMargin(normalizationMethod, "Normalization method"));
        queryParamPanel.add(panelWithMargin(matchers, "Matching method"));
        queryParamPanel.add(panelWithMargin(kBestField, "Number of best matches"));
        parameterPanel.add(queryParamPanel);
        // </editor-fold>

        GridBagConstraints middleConstraints = new GridBagConstraints();
        middleConstraints.gridx = 0;
        middleConstraints.gridy = 1;
        frame.add(parameterPanel, middleConstraints);
        // </editor-fold>

        // <editor-fold desc="=== GO BUTTON PANEL ===">
        JButton confirmButton = new JButton("Confirm choices");
        GridBagConstraints lowerConstraints = new GridBagConstraints();
        lowerConstraints.gridx = 0;
        lowerConstraints.gridy = 2;
        frame.add(panelWithMargin(confirmButton, 64, 32), lowerConstraints);
        // </editor-fold>

        // <editor-fold desc="=== TOGGLES ===">
        // === CLEANED TOGGLE ===
        ActionListener cleanSelectionListener = e -> cleaned.setEnabled(cleanCheckBox.isSelected() || analyzeCheckBox.isSelected());
        cleanCheckBox.addActionListener(cleanSelectionListener);
        analyzeCheckBox.addActionListener(cleanSelectionListener);

        // === DISTANCE FUNCTION TOGGLE ===
        ActionListener dfSelectionListener = e -> dfs.setEnabled(evaluateCheckBox.isSelected() || matrixCheckBox.isSelected() || queryCheckBox.isSelected());
        evaluateCheckBox.addActionListener(dfSelectionListener);
        matrixCheckBox.addActionListener(dfSelectionListener);
        queryCheckBox.addActionListener(dfSelectionListener);

        // === NORMALIZATION METHOD TOGGLE ===
        ActionListener normalizationListener = e -> normalizationMethod.setEnabled(evaluateCheckBox.isSelected() || matrixCheckBox.isSelected() || queryCheckBox.isSelected());
        evaluateCheckBox.addActionListener(normalizationListener);
        matrixCheckBox.addActionListener(normalizationListener);
        queryCheckBox.addActionListener(normalizationListener);

        // === MATCHING METHOD TOGGLE ===
        ActionListener matchingListener = e -> matchers.setEnabled(evaluateCheckBox.isSelected() || queryCheckBox.isSelected());
        evaluateCheckBox.addActionListener(matchingListener);
        queryCheckBox.addActionListener(matchingListener);

        // === K BEST TOGGLE ===
        ActionListener kBestListener = e -> kBestField.setEnabled(queryCheckBox.isSelected());
        queryCheckBox.addActionListener(kBestListener);
        // </editor-fold>

        ActionListener fireListener = e -> {
            confirmButton.setEnabled(false);
            if (cleaned.isEnabled()) Config.setWorkOnCleaned(cleaned.getSelectedIndex() == 1);
            if (pythonCommands.isEnabled()) Config.setPythonHandle(pythonHandles[pythonCommands.getSelectedIndex()]);
            if (dfs.isEnabled()) Config.setDistanceFunction(distanceFunctions[dfs.getSelectedIndex()]);
            if (normalizationMethod.isEnabled()) Config.setStandardized(normalizationMethod.getSelectedIndex() == 0);
            if (matchers.isEnabled()) Config.setMatchingMethod(matchingMethods[matchers.getSelectedIndex()]);
            if (kBestField.isEnabled()) Config.setkBest(Integer.parseInt(kBestField.getText()));

            if (cleanCheckBox.isSelected()) cleanDatabase();
            if (analyzeCheckBox.isSelected()) analyseDatabase();
            if (describeCheckBox.isSelected()) describeDatabase();
            if (evaluateCheckBox.isSelected()) evaluateDatabase();
            if (matrixCheckBox.isSelected()) tSneDIstances();
            if (queryCheckBox.isSelected()) makeQuery();
            confirmButton.setEnabled(true);
        };
        confirmButton.addActionListener(fireListener);

        // Make the UI visible
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel panelWithMargin(Component component) {
        return panelWithMargin(component, "");
    }

    private JPanel panelWithMargin(Component component, int v, int h) {
        return panelWithMargin(component, "", v, h);
    }

    private JPanel panelWithMargin(Component component, String label) {
        return panelWithMargin(component, label, 8, 32);
    }

    private JPanel panelWithMargin(Component component, String label, int v, int h) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(v, h, v, h));
        if (label.equals("")) {
            panel.add(component);
        } else {
            JPanel subpanel = new JPanel(new GridLayout(2, 1));
            JLabel labelPane = new JLabel(label);
            Font bold = new Font(labelPane.getFont().getName(), Font.BOLD, labelPane.getFont().getSize());
            labelPane.setFont(bold);
            subpanel.add(labelPane);
            subpanel.add(component);
            panel.add(subpanel);
        }

        return panel;
    }

    private void makeQuery() {
        // Make a query and render the results
        MeshRenderer renderer = MeshRenderer.getInstance();
        FileQueryProcessor processor = FileQueryProcessor.getInstance();
//        processor.prepareTSNEDistances();

        int k = Config.kBest();

        FileFilter meshFilter = new FileNameExtensionFilter("3D object files", "obj", "off", "ply");
        JFileChooser fileChooser = new JFileChooser("Shapes");
        fileChooser.setFileFilter(meshFilter);

        int returnValue = fileChooser.showOpenDialog(null);
        String chosenFile;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            chosenFile = fileChooser.getSelectedFile().getPath();
        } else {
            return;
        }

        FileQueryResult result = processor.queryFile(chosenFile, k);
        if (result == null) return;

        renderer.addQueryResults(result);
        try {
            renderer.startRenderer();
        } catch (Exception ignored) {
        }
    }

    private void tSneDIstances() {
        FileQueryProcessor processor = FileQueryProcessor.getInstance();
        processor.prepareTSNEDistances();
    }

    private void analyseDatabase() {
        AnalysisPipeline analysisPipeline = AnalysisPipeline.getInstance();
        analysisPipeline.run(Config.workOnCleaned());
    }

    private void cleanDatabase() {
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();
        preperationPipeline.prepareDatabase(Config.workOnCleaned());
    }

    private void describeDatabase() {
        FeaturePipeline featurePipeline = FeaturePipeline.getInstance();
        featurePipeline.calculateDatabaseDescriptors();
    }

    private void evaluateDatabase() {
        EvaluationPipeline evaluationPipeline = EvaluationPipeline.getInstance();
        evaluationPipeline.run();
    }
}
