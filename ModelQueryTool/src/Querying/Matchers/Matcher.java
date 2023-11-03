package Querying.Matchers;

import DataProcessing.FeaturePipelineContext;
import DataProcessing.FeatureStatistics;
import Querying.DistanceFunctions.WeightedDistanceFunction;
import Querying.FileQueryResult;

import java.util.List;

public abstract class Matcher {
    final FeatureStatistics statistics;

    final WeightedDistanceFunction weightedDistanceFunction;

    protected Matcher(WeightedDistanceFunction weightedDistanceFunction) {
        this.weightedDistanceFunction = weightedDistanceFunction;

        statistics = new FeatureStatistics();
        statistics.loadJson();
    }

    public abstract FileQueryResult getBestMatches(List<String> meshFiles, List<FeaturePipelineContext> contexts, float[] queryData, int k);
}
