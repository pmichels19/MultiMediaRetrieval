package DataProcessing.Descriptors.Elementary;

import DataProcessing.FeaturePipelineContext;

public class SurfaceAreaDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        float[] areas = context.getMesh().getAreas();
        float totalArea = 0.0f;
        for (float area : areas) totalArea += area;
        return totalArea;
    }

    @Override
    public String getKey() {
        return "surface_area";
    }
}
