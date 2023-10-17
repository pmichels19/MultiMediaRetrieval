package DataProcessing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureStatistics {
    private final Map<String, Float> mins;
    private final Map<String, Float> maxs;
    private final Map<String, Float> means;
    private final Map<String, Float> stdevs;

    FeatureStatistics(FeaturePipelineContext[] contexts) {
        mins = new HashMap<>();
        maxs = new HashMap<>();
        means = new HashMap<>();
        stdevs = new HashMap<>();

        float n = 0;
        for (FeaturePipelineContext context : contexts) {
            if (context == null) continue;

            n++;
            Set<String> keys = context.getElementaryKeys();
            for (String key : keys) {
                if (!mins.containsKey(key)) mins.put(key, Float.MAX_VALUE);
                if (!maxs.containsKey(key)) maxs.put(key, -Float.MAX_VALUE);
                if (!means.containsKey(key)) means.put(key, 0.0f);
                if (!stdevs.containsKey(key)) stdevs.put(key, 0.0f);

                float v = context.getElementary(key);
                if (v < mins.get(key)) mins.put(key, v);
                if (v < maxs.get(key)) maxs.put(key, v);

                float currMean = means.get(key);
                float delta = v - currMean;
                currMean += delta / ((float) n);
                means.put(key, currMean);

                float currStdev = stdevs.get(key);
                currStdev += delta * delta;
                stdevs.put(key, currStdev);
            }
        }

        for (String key : stdevs.keySet()) {
            float variance = stdevs.get(key) / (n - 1.0f);
            float stdev = (float) Math.sqrt(variance);
            stdevs.put(key, stdev);
        }
    }

    float getMin(String key) {
        return mins.get(key);
    }

    float getMax(String key) {
        return maxs.get(key);
    }

    float getMean(String key) {
        return means.get(key);
    }

    float getStdev(String key) {
        return stdevs.get(key);
    }
}
