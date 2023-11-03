package Querying.DistanceFunctions;

import org.apache.commons.math3.ml.distance.EarthMoversDistance;

import java.util.List;
import java.util.stream.IntStream;

public class WeightedEMD extends WeightedDistanceFunction {
    public WeightedEMD() {
        super();
    }

    public WeightedEMD(int elementaryKeys, List<String> globalKeys) {
        super(elementaryKeys, globalKeys);
    }

    @Override
    public Float descriptorDistance(float[] u, float[] v) {
        double[] ud = IntStream.range(0, u.length).boxed().mapToDouble(i -> (double) u[i]).toArray();
        double[] vd = IntStream.range(0, v.length).boxed().mapToDouble(i -> (double) v[i]).toArray();
        return (float) new EarthMoversDistance().compute(ud, vd);
    }

    @Override
    String getName() {
        return "_emd";
    }
}
