import Basics.Mesh;
import DataProcessing.FeaturePipeline;
import Preprocessing.Analysis.Analysis;
import Preprocessing.Analysis.AreaAnalysis;
import Preprocessing.Analysis.GeneralAnalysis;
import Preprocessing.PreperationPipeline;
import Querying.FileQueryProcessor;
import Querying.FileQueryResult;
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
//        analyseMeshes();
        // Calculates all features over the entire database and performs standardization and normalization
//        describeDatabase();
        MeshRenderer renderer = MeshRenderer.getInstance();

        FileQueryProcessor processor = FileQueryProcessor.getInstance();
        renderer.addQueryResults(processor.queryFile("Shapes/ShapeDatabase_INFOMR-master/Jet/m1216_clean.obj"));
        renderer.startRenderer();
    }

    private static void analyseMeshes() {
        Analysis generalAnalysis = GeneralAnalysis.getInstance();
        generalAnalysis.setCleanMode(ANALYZE_CLEAN);
        generalAnalysis.clearCSV();

        Analysis areaAnalysis = AreaAnalysis.getInstance();
        areaAnalysis.setCleanMode(ANALYZE_CLEAN);
        areaAnalysis.clearCSV();

        try {
            processShapeDB("Labeled_PSB");
            processShapeDB("ShapeDatabase_INFOMR-master");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processShapeDB(String shapeDB) throws IOException {
        Analysis generalAnalysis = GeneralAnalysis.getInstance();
        Analysis areaAnalysis = AreaAnalysis.getInstance();
        File dir = new File("Shapes/" + shapeDB);

        for (File type : Objects.requireNonNull(dir.listFiles())) {
            if (!type.isDirectory()) continue;

            for (String fileName : Objects.requireNonNull(type.list())) {
                String path = "Shapes/" + shapeDB + "/" + type.getName() + "/" + fileName;
                generalAnalysis.analyse(path);
                areaAnalysis.analyse(path);
            }

            System.out.println("Processed " + shapeDB + "/" + type.getName());
        }
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
