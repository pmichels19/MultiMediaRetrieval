package DataProcessing.Descriptors.Global;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.Random;

public class D2Descriptor extends GlobalDescriptor {
    @Override
    protected float[] calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();

        int n = (int) Math.pow(SAMPLE_COUNT, 1.0 / 2.0);
        int max = vertices.length;
        float[] result = new float[n * n];

        int idx = 0;
        int v0, v1;
        Random rng = new Random();
        for (int i = 0; i < n; i++) {
            v0 = rng.nextInt(max);

            for (int j = 0; j < n; j++) {
                do {
                    v1 = rng.nextInt(max);
                } while (v0 == v1);

                result[idx] = vertices[v0].dist(vertices[v1]);
                idx++;
            }
        }

        return result;
    }

    @Override
    public String getKey() {
        return "d2";
    }
}
