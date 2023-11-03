package Querying.DistanceFunctions;

import java.util.List;

public class WeightedEuclidean extends WeightedDistanceFunction {
    public WeightedEuclidean() {
        super();
    }

    public WeightedEuclidean(int elementaryKeys, List<String> globalKeys) {
        super(elementaryKeys, globalKeys);
    }

    @Override
    public Float descriptorDistance(float[] u, float[] v) {
        float distance = 0.0f;
        for (int i = 0; i < u.length; i++) {
            float diff = u[i] - v[i];
            distance += diff * diff;
        }

        return (float) Math.sqrt(distance);
    }

    @Override
    String getName() {
        return "_euclidean";
    }
}
