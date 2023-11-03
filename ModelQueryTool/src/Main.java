import Analysis.EvaluationPipeline;
import DataProcessing.FeaturePipeline;
import Analysis.AnalysisPipeline;
import Preprocessing.PreperationPipeline;
import Querying.FileQueryProcessor;
import Querying.FileQueryResult;
import Rendering.MeshRenderer;
import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Objects;

public class Main {
    // Whether the analysis should run on clean or unclean files
    private static final boolean ANALYZE_CLEAN = true;

    // Whether the preparer should clean already cleaned files (<name>_clean.ext) or the unclean files (<name>.ext)
    private static final boolean PREPARE_CLEANED = true;

    public static void main(String[] args) {
        Main main = new Main();
        // Clean the shapes in the database
//        main.cleanDatabase();
        // Write mesh data to csv file
//        main.analyseMeshes();
        // Calculates all features
//        main.describeDatabase();
        // Calculates all metrics
        main.evaluateDatabase();
        // Save database distances for t-SNE
//        main.tSneDIstances();
        // Do a query
//        main.makeQuery();
    }

    private void makeQuery() {
        // Make a query and render the results
        MeshRenderer renderer = MeshRenderer.getInstance();
        FileQueryProcessor processor = FileQueryProcessor.getInstance();
//        processor.prepareTSNEDistances();

        Dotenv dotenv = Dotenv.configure().load();
        int k = Integer.parseInt(Objects.requireNonNull(dotenv.get("K_BEST")));

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
        renderer.addQueryResults(result);
        renderer.startRenderer();
    }

    private void tSneDIstances() {
        FileQueryProcessor processor = FileQueryProcessor.getInstance();
        processor.prepareTSNEDistances();
    }

    private void analyseMeshes() {
        AnalysisPipeline analysisPipeline = AnalysisPipeline.getInstance();
        analysisPipeline.run(ANALYZE_CLEAN);
    }

    private void cleanDatabase() {
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();
        preperationPipeline.prepareDatabase(PREPARE_CLEANED);
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
