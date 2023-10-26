package DataProcessing.Descriptors.Global;

import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.Random;

public class A3Descriptor extends GlobalDescriptor {
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

                    double dot = ab.dot(ac);
                    double cosAngle = dot / (ab.length() * ac.length());
                    cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
                    double angle = Math.acos(cosAngle);
                    if (dot < 0) angle = 2.0 * Math.PI - angle;

                    result[idx] = (float) angle;
                    idx++;
                }
            }
        }

        return toHistogram(result);
    }

    @Override
    public String getKey() {
        return "a3";
    }
}
