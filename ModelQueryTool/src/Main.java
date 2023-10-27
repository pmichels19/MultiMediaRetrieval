import Basics.Mesh;
import Basics.Stitcher;
import DataProcessing.FeaturePipeline;
import Preprocessing.Analysis.AnalysisPipeline;
import Preprocessing.PreperationPipeline;
import Querying.FileQueryProcessor;
import Querying.FileQueryResult;
import Readers.ReadResult;
import Readers.Reader;
import Rendering.MeshRenderer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    // Whether the analysis should run on clean or unclean files
    private static final boolean ANALYZE_CLEAN = true;

    // Whether the preparer should clean already cleaned files (<name>_clean.ext) or the unclean files (<name>.ext)
    private static final boolean PREPARE_CLEANED = true;

    public static void main(String[] args) {
        // Cleaning the shapes in the database. Removes zero area faces and duplicate vertices
//        cleanDatabase();
        // Generating mesh-based data in csv files (v-count, f-count, AABB coordinates, areas)
        analyseMeshes();
        // Calculates all features over the entire database and performs standardization and normalization
//        describeDatabase();

        // Make a query and render the results
//        MeshRenderer renderer = MeshRenderer.getInstance();
//        FileQueryProcessor processor = FileQueryProcessor.getInstance();
//        FileQueryResult result = processor.queryFile("Shapes\\ShapeDatabase_INFOMR-master\\Jet\\m1216_clean.obj"); // Jet query
//        FileQueryResult result = processor.queryFile("Shapes\\ShapeDatabase_INFOMR-master\\Quadruped\\D00380_clean.obj"); // Quadruped query
//        FileQueryResult result = processor.queryFile("Shapes\\ShapeDatabase_INFOMR-master\\Biplane\\m1123_clean.obj"); // Biplane query
//        FileQueryResult result = processor.queryFile("Shapes\\ShapeDatabase_INFOMR-master\\Humanoid\\m262_clean.obj"); // Humanoid query
//        FileQueryResult result = processor.queryFile("Shapes\\Labeled_PSB\\Octopus\\126_clean.off"); // Octopus query
//        renderer.addQueryResults(result);
//        renderer.startRenderer();
    }

    private static void analyseMeshes() {
        AnalysisPipeline analysisPipeline = AnalysisPipeline.getInstance();
        analysisPipeline.run(ANALYZE_CLEAN);
    }

    private static void cleanDatabase() {
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();
        preperationPipeline.prepareDatabase(PREPARE_CLEANED);
    }

    private static void describeDatabase() {
        FeaturePipeline featurePipeline = FeaturePipeline.getInstance();
        featurePipeline.calculateDatabaseDescriptors();
    }
}
