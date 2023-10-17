package DataProcessing.Descriptors.Elementary;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

public class DiameterDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();
        float maxDist = -Float.MAX_VALUE;
        for (int i = 0; i < vertices.length; i++) {
            for (int j = i + 1; j < vertices.length; j++) {
                float dist = vertices[i].dist(vertices[j]);
                if (dist > maxDist) maxDist = dist;
            }
        }

        return maxDist;
    }

    @Override
    public String getKey() {
        return "diameter";
    }
}
