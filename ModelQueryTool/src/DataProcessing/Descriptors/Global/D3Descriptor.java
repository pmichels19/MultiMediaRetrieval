package DataProcessing.Descriptors.Global;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.Random;

public class D3Descriptor extends GlobalDescriptor {
    @Override
    protected float[] calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();

        int n = (int) Math.pow(SAMPLE_COUNT, 1.0 / 3.0);
        int max = vertices.length;
        float[] result = new float[n * n * n];

        int idx = 0;
        int v0, v1, v2;
        Random rng = new Random();
        for (int i = 0; i < n; i++) {
            v0 = rng.nextInt(max);

            for (int j = 0; j < n; j++) {
                do {
                    v1 = rng.nextInt(max);
                } while (v0 == v1);

                for (int k = 0; k < n; k++) {
                    do {
                        v2 = rng.nextInt(max);
                    } while (v0 == v2 || v1 == v2);

                    Vec3f a = vertices[v0];
                    Vec3f b = vertices[v1];
                    Vec3f c = vertices[v2];

                    Vec3f ab = b.minus(a);
                    Vec3f ac = c.minus(a);
                    Vec3f cross = new Vec3f().cross(ab, ac);

                    result[idx] = (float) Math.sqrt(0.5 * ((double) cross.length()));
                    idx++;
                }
            }
        }

        return result;
    }

    @Override
    public String getKey() {
        return "d3";
    }
}
