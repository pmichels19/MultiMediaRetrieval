package DataProcessing.Descriptors.Elementary;

import DataProcessing.FeaturePipelineContext;

public class ConvexityDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        // Get the shape volume
        float shapeVolume = context.getMesh().getVolume();
        // Get the hull volume
        float hullVolume = context.getMesh().getHull().getVolume();

        return shapeVolume / hullVolume;
    }

    @Override
    public String getKey() {
        return "convexity";
    }
}
