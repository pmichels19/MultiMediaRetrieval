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
    private static final int K_BEST = 10;

    private static final int DISTANCE_FUNCTION = 0;

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

        FeatureStatistics statistics = new FeatureStatistics();
        statistics.loadJson();

        try {
            queryContext = pipeline.calculateMeshDescriptors(filePath);
            queryContext.normalizeElementaries(statistics);
        } catch (Exception e) {
            System.err.println("Failed to process query mesh " + filePath + ":");
            e.printStackTrace();
            return new FileQueryResult();
        }

        float[] distances;
        String[] meshFiles;
        FeaturePipelineContext[] contexts;
        String jsonQueryFile = filePath.substring(0, filePath.lastIndexOf('.')) + ".json";
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, this::isJson)) {
            System.out.println("===== Reading database shape statistics =====");
            String[] paths = pathStream.map(Path::toString).toArray(String[]::new);

            meshFiles = Arrays.stream(paths)
                    .map(p -> {
                        String meshFile = p.replace("_clean", "");
                        String offFile = meshFile.replace("json", "off");
                        String objFile = meshFile.replace("json", "obj");
                        String plyFile = meshFile.replace("json", "oply");

                        if (new File(offFile).isFile()) return offFile;
                        if (new File(objFile).isFile()) return objFile;
                        if (new File(plyFile).isFile()) return plyFile;
                        throw new IllegalStateException("Found json file with no corresponding mesh file.");
                    }).toArray(String[]::new);

            contexts = Arrays.stream(paths)
                    .map(FeaturePipelineContext::fromJson)
                    .toArray(FeaturePipelineContext[]::new);

            System.out.println("===== Computing distances to database =====");
            distances = new float[contexts.length];
            for (int i = 0; i < contexts.length; i++) distances[i] = getDistance(queryContext, contexts[i], statistics);
        } catch (IOException e) {
            System.err.println("Failed to find json files for database:");
            e.printStackTrace();
            return new FileQueryResult();
        }

        System.out.println("===== Preparing " + K_BEST + " matching shapes for rendering =====");
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();

        List<Float> bestDists = new ArrayList<>();
        List<Mesh> bestMeshes = new ArrayList<>();
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

        int elementaryCount = elementaryKeys.size();
        float[] dbElementaries = new float[elementaryCount];
        float[] queryElementaries = new float[elementaryCount];
        for (int i = 0; i < elementaryCount; i++) {
            dbElementaries[i] = dbContext.getElementary(elementaryKeys.get(i));
            queryElementaries[i] = queryContext.getElementary(elementaryKeys.get(i));
        }

        String function = Helpers.DISTANCE_FUNCTIONS[DISTANCE_FUNCTION];
        float distance = Helpers.getDistance(function, dbElementaries, queryElementaries);
        for (String key : globalKeys) {
            String dfKey = key + function;
            float mean = statistics.getMean(dfKey);
            float stdev = statistics.getStdev(dfKey);
            distance += weightedDistance(function, dbContext.getGlobal(key), queryContext.getGlobal(key), mean, stdev);
        }

        float div = ((float) globalKeys.size()) + 1.0f;
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
