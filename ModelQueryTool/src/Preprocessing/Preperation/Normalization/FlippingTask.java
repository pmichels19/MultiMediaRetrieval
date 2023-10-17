package Preprocessing.Preperation.Normalization;

import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

public class FlippingTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        int[][] faces = context.getFaces();
        Vec3f[] vertices = context.getVertices();
        // Compute the barycenters of all faces
        Vec3f[] baryCenters = new Vec3f[faces.length];
        for (int i = 0; i < faces.length; i++) {
            baryCenters[i] = new Vec3f();
            baryCenters[i].add(vertices[faces[i][0]]);
            baryCenters[i].add(vertices[faces[i][1]]);
            baryCenters[i].add(vertices[faces[i][2]]);
            baryCenters[i].scale(1.0f / 3.0f);
        }

        float fx, fy, fz;
        fx = fy = fz = 0.0f;
        for (Vec3f center : baryCenters) {
            float x = center.x();
            fx += getSign(x) * (x * x);
            float y = center.y();
            fy += getSign(y) * (y * y);
            float z = center.z();
            fz += getSign(z) * (z * z);
        }

        Vec3f fs = new Vec3f(getSign(fx), getSign(fy), getSign(fz));
        for (Vec3f vertex : vertices) {
            vertex.mul(vertex, fs);
        }
    }

    @Override
    public String getDescription() {
        return "Performed flipping test.";
    }

    private float getSign(float in) {
        return in > 0 ? 1.0f : -1.0f;
    }
}
