package Querying.DistanceFunctions;

import java.util.List;

public class WeightedCosine extends WeightedDistanceFunction {
    public WeightedCosine() {
        super();
    }

    public WeightedCosine(int elementaryKeys, List<String> globalKeys) {
        super(elementaryKeys, globalKeys);
    }

    @Override
    public Float descriptorDistance(float[] u, float[] v) {
        float dot = 0.0f;
        float v1l = 0.0f;
        float v2l = 0.0f;
        for (int i = 0; i < u.length; i++) {
            dot += u[i] * v[i];
            v1l += u[i] * u[i];
            v2l += v[i] * v[i];
        }

        return 1.0f - ( dot / (float) (Math.sqrt(v1l) * Math.sqrt(v2l)) );
    }

    @Override
    String getName() {
        return "_cosine";
    }
}
