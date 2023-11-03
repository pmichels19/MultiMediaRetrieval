package Querying.DistanceFunctions;

import DataProcessing.FeatureStatistics;
import com.github.jelmerk.knn.DistanceFunction;

import java.util.Arrays;
import java.util.List;

public abstract class WeightedDistanceFunction implements DistanceFunction<float[], Float> {
    protected int elementaryKeys;

    protected List<String> globalKeys;

    protected FeatureStatistics statistics;

    WeightedDistanceFunction() {
    }

    WeightedDistanceFunction(int elementaryKeys, List<String> globalKeys) {
        this.elementaryKeys = elementaryKeys;
        this.globalKeys = globalKeys;

        statistics = new FeatureStatistics();
        statistics.loadJson();
    }

    public static WeightedDistanceFunction getDistanceFuncion(String function) {
        WeightedDistanceFunction distanceFunction;
        switch (function) {
            case "_cosine" -> distanceFunction = new WeightedCosine();
            case "_euclidean" -> distanceFunction = new WeightedEuclidean();
            case "_emd" -> distanceFunction = new WeightedEMD();
            default -> throw new IllegalArgumentException("Function " + function + " does not exist.");
        }

        return distanceFunction;
    }

    public static String[] getDistanceFunctions() {
        return new String[] {
                "_cosine",
                "_euclidean",
                "_emd",
        };
    }

    public abstract Float descriptorDistance(float[] u, float[] v);

    @Override
    public Float distance(float[] u, float[] v) {
        float result = 0.0f;
        int idx = 0;
        while (idx < elementaryKeys) {
            float diff = u[idx] - v[idx];
            result += Math.sqrt(diff * diff);
            idx++;
        }

        int globalSize = (u.length - idx) / globalKeys.size();
        for (String globalKey : globalKeys) {
            float mean = statistics.getMean(globalKey + getName());
            float stdev = statistics.getStdev(globalKey + getName());
            int endIdx = idx + globalSize;
            float[] subU = Arrays.copyOfRange(u, idx, endIdx);
            float[] subV = Arrays.copyOfRange(v, idx, endIdx);

            result += (descriptorDistance(subU, subV) - mean) / stdev;
            idx = endIdx;
        }

        return result / (elementaryKeys + globalKeys.size());
    }

    abstract String getName();
}
