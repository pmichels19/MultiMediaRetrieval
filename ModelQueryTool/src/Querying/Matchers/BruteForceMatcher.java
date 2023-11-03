package Querying.Matchers;

import DataProcessing.FeaturePipelineContext;
import Querying.DistanceFunctions.WeightedDistanceFunction;
import Querying.FileQueryResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class BruteForceMatcher extends Matcher {
    public BruteForceMatcher(WeightedDistanceFunction weightedDistanceFunction) {
        super(weightedDistanceFunction);
    }

    @Override
    public FileQueryResult getBestMatches(List<String> meshFiles, List<FeaturePipelineContext> contexts, float[] queryData, int k) {
        List<Float> distances = new ArrayList<>();
        for (FeaturePipelineContext context : contexts) distances.add(weightedDistanceFunction.distance(queryData, context.flattened()));

        List<Float> bestDists = new ArrayList<>();
        List<String> bestMeshes = new ArrayList<>();

        IntStream.range(0, contexts.size()).boxed()
                .sorted(Comparator.comparingDouble(distances::get))
                .limit(k)
                .forEach(i -> {
                    try {
                        bestDists.add(distances.get(i));
                        bestMeshes.add(meshFiles.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        return new FileQueryResult(bestMeshes, bestDists);
    }
}
