package Querying.Matchers;

import DataProcessing.FeaturePipelineContext;
import Querying.DistanceFunctions.WeightedDistanceFunction;
import Querying.FileQueryResult;
import com.github.jelmerk.knn.Item;
import com.github.jelmerk.knn.SearchResult;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KDTreeMatcher extends Matcher {
    public KDTreeMatcher(WeightedDistanceFunction weightedDistanceFunction) {
        super(weightedDistanceFunction);
    }

    @Override
    public FileQueryResult getBestMatches(List<String> meshFiles, List<FeaturePipelineContext> contexts, float[] queryData, int k) {
        List<ContextItem> contextItems = IntStream.range(0, contexts.size())
                .boxed()
                .map(i -> new ContextItem(meshFiles.get(i), contexts.get(i).flattened()))
                .collect(Collectors.toList());
        HnswIndex<String, float[], ContextItem, Float> index = HnswIndex
                .newBuilder(contextItems.get(0).dimensions(), weightedDistanceFunction, contextItems.size())
                .withM(32)
                .build();

        try {
            index.addAll(contextItems);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        List<SearchResult<ContextItem, Float>> knearest = index.findNearest(queryData, k);

        List<String> meshes = new ArrayList<>();
        List<Float> distances = new ArrayList<>();
        for (SearchResult<ContextItem, Float> result : knearest) {
            distances.add(result.distance());
            meshes.add(result.item().id());
        }
        return new FileQueryResult(meshes, distances);
    }

    private static class ContextItem implements Item<String, float[]> {
        private final String id;
        private final float[] vector;

        ContextItem(String id, float[] vector) {
            this.id = id;
            this.vector = vector;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public float[] vector() {
            return vector;
        }

        @Override
        public int dimensions() {
            return vector.length;
        }
    }
}
