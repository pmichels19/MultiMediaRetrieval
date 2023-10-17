package DataProcessing.Descriptors.Elementary;

import DataProcessing.FeaturePipelineContext;

public class CompactnessDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        float[] areas = context.getMesh().getAreas();
        float s = 0.0f;
        for (float area : areas) s += area;

        float v = context.getMesh().getVolume();

        return (s * s * s) / (36.0f * ((float) Math.PI) * v * v);
    }

    @Override
    public String getKey() {
        return "compactness";
    }
}
