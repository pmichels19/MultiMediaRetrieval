package Analysis;

import Basics.Config;
import Basics.Helpers;
import Querying.FileQueryProcessor;
import Querying.FileQueryResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EvaluationPipelineContext {
    private final Map<String, List<Float>> metricValues;

    private final List<String> fileLabels;

    private final List<String> fileNames;

    private final Map<String, Integer> classSizes;

    private final FileQueryProcessor fileQueryProcessor;

    private final int dbSize;

    private List<String> matchedLabels;

    EvaluationPipelineContext() {
        fileLabels = new ArrayList<>();
        fileNames = new ArrayList<>();
        metricValues = new HashMap<>();

        fileQueryProcessor = FileQueryProcessor.getInstance();

        classSizes = new HashMap<>();
        Objects.requireNonNull(Helpers.getJsonFiles()).forEach(path -> {
            String[] parts = path.split("[\\\\/]");
            String label = parts[parts.length - 2];

            if (!classSizes.containsKey(label)) classSizes.put(label, 0);
            classSizes.put(label, classSizes.get(label) + 1);
        });

        dbSize = classSizes.values().stream().mapToInt(i -> i).sum();
    }

    public void saveToCsv() {
        String df = Config.getDistanceFunction();
        String csvFile = "src\\Analysis\\CSV\\evaluation_metrics" + df + ".csv";
        File file = new File(csvFile);
        if (file.isFile() && !file.delete()) throw new IllegalStateException("Failed to delete existing evaluation file.");

        try {
            if (!file.createNewFile()) throw new IllegalStateException("Failed to create new evaluation file.");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            Set<String> metrics = metricValues.keySet();
            writer.write("label,name," + String.join(",", metrics) + "\n");

            for (int i = 0; i < dbSize; i++) {
                StringBuilder line = new StringBuilder();
                line.append(fileLabels.get(i)).append(",").append(fileNames.get(i));

                int idx = i;
                metrics.forEach(metric -> line.append(",").append(metricValues.get(metric).get(idx)));
                line.append("\n");

                writer.write(line.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareForFile(String filePath) {
        FileQueryResult result = fileQueryProcessor.queryFile(filePath, dbSize);
        matchedLabels = result.getMeshFiles().stream().map(s -> {
            String[] parts = s.split("[\\\\/]");
            return parts[parts.length - 2];
        }).toList();

        String[] parts = filePath.split("[\\\\/]");
        fileLabels.add(parts[parts.length - 2]);
        fileNames.add(parts[parts.length - 1]);
    }

    public void addMetric(String metric, float value) {
        if (!metricValues.containsKey(metric)) metricValues.put(metric, new ArrayList<>());
        metricValues.get(metric).add(value);
    }

    public int getClassSize() {
        return classSizes.get(matchedLabels.get(0));
    }

    public int getDbSize() {
        return dbSize;
    }

    public List<String> getMatchedLabels() {
        return matchedLabels;
    }
}
