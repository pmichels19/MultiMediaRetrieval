package DataProcessing.Descriptors.Global;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.Random;

public class D4Descriptor extends GlobalDescriptor {
    @Override
    protected float[] calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();

        int n = (int) Math.pow(SAMPLE_COUNT, 1.0 / 4.0);
        int max = vertices.length;
        float[] result = new float[n * n * n * n];

        int idx = 0;
        int v0, v1, v2, v3;
        Vec3f a, b, c, d;
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

                    for (int l = 0; l < n; l++) {
                        do {
                            v3 = rng.nextInt(max);
                        } while (v0 == v3 || v1 == v3 || v2 == v3);

                        a = vertices[v0];
                        b = vertices[v1];
                        c = vertices[v2];
                        d = vertices[v3];

                        Vec3f ad = a.minus(d);
                        Vec3f bd = b.minus(d);
                        Vec3f cd = c.minus(d);
                        Vec3f bdxcd = (new Vec3f()).cross(bd, cd);
                        result[idx] = (1.0f / 6.0f) * (ad.dot(bdxcd));
                        idx++;
                    }
                }
            }
        }

        return toHistogram(result);
    }

    @Override
    public String getKey() {
        return "d4";
    }
}
