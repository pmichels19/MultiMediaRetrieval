package DataProcessing.Descriptors.Elementary;

import Basics.Helpers;
import DataProcessing.FeaturePipelineContext;

public class RectangularityDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        // Get the shape volume
        float shapeVolume = context.getMesh().getVolume();
        // Get the AABB volume
        float[] ext = Helpers.getMinMaxCoordinates(context.getMesh().getVertices());

        return shapeVolume / (ext[1] - ext[0]) * (ext[3] - ext[2]) * (ext[5] - ext[4]);
    }

    @Override
    public String getKey() {
        return "rectangularity ";
    }
}
