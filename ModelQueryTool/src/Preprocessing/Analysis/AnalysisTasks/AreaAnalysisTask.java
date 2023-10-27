package Preprocessing.Analysis.AnalysisTasks;

import Readers.ReadResult;
import Readers.Reader;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Vec3f;

import java.io.FileWriter;
import java.io.IOException;

public class AreaAnalysisTask extends AnalysisTask {
    public AreaAnalysisTask() {
        this.analysisFileName = "area_analysis";
    }

    @Override
    String analyse(String filePath) throws IOException {
        ReadResult readResult = Reader.read(filePath);

        Vec3f[] vertices = readResult.getVertices();
        int[][] faces = readResult.getFaces();
        float[] areas = new float[faces.length];
        for (int i = 0; i < faces.length; i++) {
            Vec3f v0 = vertices[faces[i][0]];
            Vec3f v1 = vertices[faces[i][1]];
            Vec3f v2 = vertices[faces[i][2]];

            Vec3f e1 = v1.minus(v0);
            Vec3f e2 = v2.minus(v0);
            Vec3f n = e1.cross(e2);
            float length = n.length();
            if (FloatUtil.isZero(length)) {
                areas[i] = 0.0f;
            } else {
                areas[i] = length * 0.5f;
            }
        }

        StringBuilder builder = new StringBuilder();
        for (float area : areas) builder.append(area).append("\n");
        return builder.toString();
    }

    @Override
    String[] getColumns() {
        return new String[] {"areas"};
    }
}
