package Querying;

import Basics.Config;
import Basics.Helpers;
import Basics.Mesh;
import DataProcessing.FeaturePipeline;
import DataProcessing.FeaturePipelineContext;
import DataProcessing.FeatureStatistics;
import Preprocessing.PreperationPipeline;
import Querying.DistanceFunctions.WeightedCosine;
import Querying.DistanceFunctions.WeightedDistanceFunction;
import Querying.DistanceFunctions.WeightedEMD;
import Querying.DistanceFunctions.WeightedEuclidean;
import Querying.Matchers.BruteForceMatcher;
import Querying.Matchers.KDTreeMatcher;
import Querying.Matchers.Matcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileQueryProcessor {
    private final boolean STANDARDIZED;

    private final FeatureStatistics statistics;

    private final PreperationPipeline preperationPipeline;

    private final FeaturePipeline featurePipeline;

    private final List<FeaturePipelineContext> dbContexts;

    private static FileQueryProcessor processor;

    private FileQueryProcessor() {
        STANDARDIZED = Config.standardized();

        preperationPipeline = PreperationPipeline.getInstance();
        featurePipeline = FeaturePipeline.getInstance();
        statistics = new FeatureStatistics();
        statistics.loadJson();

        System.out.println("===== Reading database shape statistics =====");
        List<String> paths = Objects.requireNonNull(Helpers.getJsonFiles());
        dbContexts = paths.stream()
                .map(FeaturePipelineContext::fromJson)
                .collect(Collectors.toList());
    }

    public static FileQueryProcessor getInstance() {
        if (processor == null) processor = new FileQueryProcessor();
        return processor;
    }

    public FileQueryResult queryFile(String filePath, int k) {
        FeaturePipelineContext queryContext;

        String queryWithJsonExtension = filePath.replaceAll("\\.(off|obj|ply)", ".json");
        String jsonQueryFile = queryWithJsonExtension.replace("/", "\\");

        List<String> meshFiles;
        List<String> paths = Objects.requireNonNull(Helpers.getJsonFiles());

        int queryIndex = paths.indexOf(jsonQueryFile);
        if (queryIndex >= 0) {
            queryContext = dbContexts.remove(queryIndex);
            System.out.println("Read mesh data from " + jsonQueryFile);
        } else {
            try {
                Mesh toProcess = preperationPipeline.getCleanMesh(filePath);
                queryContext = featurePipeline.calculateMeshDescriptors(toProcess);
                queryContext.normalizeElementaries(statistics);
            } catch (Exception e) {
                System.err.println("Failed to process query mesh " + filePath + ":");
                e.printStackTrace();
                return new FileQueryResult();
            }
        }

        String inputMesh = filePath;
        String baseFilePath = filePath.replace("_clean", "");
        if (new File(baseFilePath).isFile()) inputMesh = baseFilePath;

        paths.remove(queryIndex);
        meshFiles = paths.stream()
                .filter(p -> !p.equals(jsonQueryFile))
                .map(p -> {
                    String meshFile = p.replace("_clean", "");
                    String offFile = meshFile.replace("json", "off");
                    String objFile = meshFile.replace("json", "obj");
                    String plyFile = meshFile.replace("json", "ply");

                    if (new File(offFile).isFile()) return offFile;
                    if (new File(objFile).isFile()) return objFile;
                    if (new File(plyFile).isFile()) return plyFile;
                    throw new IllegalStateException("Found json file with no corresponding mesh file.");
                }).toList();

        int elementaryKeys = (int) queryContext.getElementaryKeys().stream().filter(this::processElementary).count();
        List<String> globalKeys = queryContext.getGlobalKeys().stream().toList();

        WeightedDistanceFunction distanceFunction;
        String df = Config.getDistanceFunction();
        switch (df) {
            case "_cosine" -> distanceFunction = new WeightedCosine(elementaryKeys, globalKeys);
            case "_euclidean" -> distanceFunction = new WeightedEuclidean(elementaryKeys, globalKeys);
            case "_emd" -> distanceFunction = new WeightedEMD(elementaryKeys, globalKeys);
            default -> throw new IllegalStateException("Distance fucntion " + df + " does not exist.");
        }

        Matcher matcher;
        String dm = Config.getMatchingMethod();
        switch (dm) {
            case "brute_force" -> matcher = new BruteForceMatcher(distanceFunction);
            case "kd_tree" -> matcher = new KDTreeMatcher(distanceFunction);
            default -> throw new IllegalStateException("Matcher type " + dm + " does not exist.");
        }

        System.out.println("Starting query.");
        long pre = System.currentTimeMillis();

        FileQueryResult result = matcher.getBestMatches(meshFiles, dbContexts, queryContext.flattened(), k);
        result.addInputFile(inputMesh);

        long post = System.currentTimeMillis();
        System.out.println("Completed query in " + (post - pre) / 1000.0 + " s." );

        if (queryIndex >= 0) dbContexts.add(queryIndex, queryContext);
        return result;
    }

    public void prepareTSNEDistances() {
        List<FeaturePipelineContext> contexts;
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(".*_clean\\.json"))) {
            contexts = pathStream.map(Path::toString).map(FeaturePipelineContext::fromJson).toList();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String df = Config.getDistanceFunction();

        int elementaryKeys = (int) contexts.get(0).getElementaryKeys().stream().filter(this::processElementary).count();
        List<String> globalKeys = contexts.get(0).getGlobalKeys().stream().toList();

        WeightedDistanceFunction distanceFunction;
        switch (df) {
            case "_cosine" -> distanceFunction = new WeightedCosine(elementaryKeys, globalKeys);
            case "_euclidean" -> distanceFunction = new WeightedEuclidean(elementaryKeys, globalKeys);
            case "_emd" -> distanceFunction = new WeightedEMD(elementaryKeys, globalKeys);
            default -> throw new IllegalStateException("Distance fucntion " + df + " does not exist.");
        }

        int n = contexts.size();
        float[][] result = new float[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                System.out.print(i + ", " + j);
                float d = distanceFunction.distance(contexts.get(i).flattened(), contexts.get(j).flattened());
                result[i][j] = d;
                result[j][i] = d;
                System.out.print("\r");
            }
        }

        File distanceFile = new File("..\\DataPlots\\shared\\distances.csv");
        try (FileWriter writer = new FileWriter(distanceFile)) {
            for (float[] row : result) {
                String[] rowAsString = IntStream.range(0, row.length).mapToObj(i -> Float.toString(row[i])).toArray(String[]::new);
                writer.write(String.join(",", rowAsString));
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isJson(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().matches(".*_clean\\.json");
    }

    boolean processElementary(String key) {
        return (STANDARDIZED && key.endsWith("_standardized")) || (!STANDARDIZED && key.endsWith("_minmax"));
    }
}
