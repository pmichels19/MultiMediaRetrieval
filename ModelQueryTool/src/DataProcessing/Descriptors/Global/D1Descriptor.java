package DataProcessing.Descriptors.Global;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.Random;

public class D1Descriptor extends GlobalDescriptor {
    @Override
    protected float[] calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();

        Random rng = new Random();
        int max = vertices.length;
        float[] result = new float[SAMPLE_COUNT];
        for (int i = 0; i < SAMPLE_COUNT; i++) result[i] = vertices[rng.nextInt(max)].length();

        return result;
    }

    @Override
    public String getKey() {
        return "d1";
    }
}
