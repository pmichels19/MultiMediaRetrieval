package Querying;

import Basics.Helpers;
import Basics.Mesh;
import DataProcessing.FeaturePipeline;
import DataProcessing.FeaturePipelineContext;
import DataProcessing.FeatureStatistics;
import Preprocessing.PreperationPipeline;
import com.jogamp.opengl.math.FloatUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileQueryProcessor {
    private static final int K_BEST = 5;

    private static final int DISTANCE_FUNCTION = 1;

    private static final boolean STANDARDIZED = true;

    private static FileQueryProcessor processor;

    private FileQueryProcessor() {
    }

    public static FileQueryProcessor getInstance() {
        if (processor == null) processor = new FileQueryProcessor();
        return processor;
    }

    public FileQueryResult queryFile(String filePath) {
        FeaturePipelineContext queryContext;
        FeaturePipeline pipeline = FeaturePipeline.getInstance();
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();

        FeatureStatistics statistics = new FeatureStatistics();
        statistics.loadJson();

        Mesh cleanMesh;
        try {
            Mesh toProcess = preperationPipeline.getCleanMesh(filePath);
            queryContext = pipeline.calculateMeshDescriptors(toProcess);
            queryContext.normalizeElementaries(statistics);

            String baseFilePath = filePath.replace("_clean", "");
            cleanMesh = toProcess;
            if (new File(baseFilePath).isFile()) {
                try {
                    cleanMesh = preperationPipeline.getCleanMesh(baseFilePath);
                } catch (Exception e) {
                    System.err.println("Failed to read base file, using existing clean file.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process query mesh " + filePath + ":");
            e.printStackTrace();
            return new FileQueryResult();
        }

        float[] distances;
        String[] meshFiles;
        FeaturePipelineContext[] contexts;

        String queryWithJsonExtension = filePath.replaceAll("\\.(off|obj|ply)", ".json");
        String jsonQueryFile = queryWithJsonExtension.replace("/", "\\");
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, this::isJson)) {
            System.out.println("===== Reading database shape statistics =====");
            String[] paths = pathStream.map(Path::toString).filter(path -> !path.equals(jsonQueryFile)).toArray(String[]::new);

            meshFiles = Arrays.stream(paths)
                    .map(p -> {
                        String meshFile = p.replace("_clean", "");
                        String offFile = meshFile.replace("json", "off");
                        String objFile = meshFile.replace("json", "obj");
                        String plyFile = meshFile.replace("json", "ply");

                        if (new File(offFile).isFile()) return offFile;
                        if (new File(objFile).isFile()) return objFile;
                        if (new File(plyFile).isFile()) return plyFile;
                        throw new IllegalStateException("Found json file with no corresponding mesh file.");
                    }).toArray(String[]::new);

            contexts = Arrays.stream(paths)
                    .map(FeaturePipelineContext::fromJson)
                    .toArray(FeaturePipelineContext[]::new);
        } catch (IOException e) {
            System.err.println("Failed to find json files for database:");
            e.printStackTrace();
            return new FileQueryResult();
        }

        System.out.println("===== Computing distances to database =====");
        distances = new float[contexts.length];
        for (int i = 0; i < contexts.length; i++) distances[i] = getDistance(queryContext, contexts[i], statistics);

        System.out.println("===== Preparing " + K_BEST + " matching shapes for rendering =====");
        List<Float> bestDists = new ArrayList<>();
        List<Mesh> bestMeshes = new ArrayList<>();

        bestDists.add(null);
        bestMeshes.add(cleanMesh);

        IntStream.range(0, contexts.length).boxed()
                .sorted(Comparator.comparingDouble(i -> distances[i]))
                .limit(K_BEST)
                .forEach(i -> {
                    try {
                        System.out.printf("===== %s: %.4f =====\n", meshFiles[i], distances[i]);
                        bestDists.add(distances[i]);
                        bestMeshes.add(preperationPipeline.getCleanMesh(meshFiles[i]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        return new FileQueryResult(bestMeshes, bestDists);
    }

    private float getDistance(FeaturePipelineContext queryContext, FeaturePipelineContext dbContext, FeatureStatistics statistics) {
        List<String> globalKeys = dbContext.getGlobalKeys().stream().toList();
        List<String> elementaryKeys = dbContext.getElementaryKeys().stream().filter(this::processKey).toList();

        float distance = 0.0f;
        for (String key : elementaryKeys) {
            float diff = queryContext.getElementary(key) - dbContext.getElementary(key);
            distance += Math.sqrt(diff * diff);
        }

        String function = Helpers.DISTANCE_FUNCTIONS[DISTANCE_FUNCTION];
        for (String key : globalKeys) {
            String dfKey = key + function;
            float mean = statistics.getMean(dfKey);
            float stdev = statistics.getStdev(dfKey);
            distance += weightedDistance(function, dbContext.getGlobal(key), queryContext.getGlobal(key), mean, stdev);
        }

        float div = (float) (elementaryKeys.size() + globalKeys.size());
        return distance / div;
    }

    private float weightedDistance(String function, float[] v1, float[] v2, float mean, float stdev) {
        assert !FloatUtil.isZero(stdev);
        return (Helpers.getDistance(function, v1, v2) - mean) / stdev;
    }

    private boolean isJson(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().matches(".*_clean\\.json");
    }

    private boolean processKey(String key) {
        return (STANDARDIZED && key.endsWith("_standardized")) || (!STANDARDIZED && key.endsWith("_minmax"));
    }
}
