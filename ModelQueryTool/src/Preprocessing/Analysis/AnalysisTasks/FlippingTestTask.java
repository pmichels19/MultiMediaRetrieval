package Preprocessing.Analysis.AnalysisTasks;

import Readers.ReadResult;
import Readers.Reader;
import com.jogamp.opengl.math.Vec3f;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class FlippingTestTask extends AnalysisTask {
    public FlippingTestTask() {
        this.analysisFileName = "flipping_analysis";
    }

    @Override
    String analyse(String filePath) throws IOException {
        ReadResult readResult = Reader.read(filePath);

        Vec3f[] vertices = readResult.getVertices();
        int[][] faces = readResult.getFaces();
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

        return String.format(Locale.ROOT, "%.1f,%.1f,%.1f\n", getSign(fx), getSign(fy), getSign(fz));
    }

    @Override
    String[] getColumns() {
        return new String[] {"sign_x", "sign_y", "sign_z"};
    }

    private float getSign(float in) {
        return in > 0 ? 1.0f : -1.0f;
    }
}
