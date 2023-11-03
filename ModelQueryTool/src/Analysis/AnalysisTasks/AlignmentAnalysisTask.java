package Analysis.AnalysisTasks;

import Basics.Helpers;
import Readers.ReadResult;
import Readers.Reader;
import com.jogamp.opengl.math.Vec3f;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.util.Locale;

public class AlignmentAnalysisTask extends AnalysisTask {
    public AlignmentAnalysisTask() {
        this.analysisFileName = "alignment_analysis";
    }

    @Override
    String analyse(String filePath) throws IOException {
        ReadResult readResult = Reader.read(filePath);

        Vec3f eigx = new Vec3f();
        Vec3f eigy = new Vec3f();
        Vec3f eigz = new Vec3f();
        RealMatrix covariance = Helpers.getCovarianceMatrix(readResult.getVertices());
        Helpers.covarianceToEigenVectors(covariance, eigx, eigy, eigz);

        float dotx = eigx.dot(new Vec3f(1, 0, 0));
        float doty = eigy.dot(new Vec3f(0, 1, 0));
        float dotz = eigz.dot(new Vec3f(0, 0, 1));

        return String.format(Locale.ROOT, "%.5f,%.5f,%.5f\n", dotx, doty, dotz);
    }

    @Override
    String[] getColumns() {
        return new String[] {"dot_x", "dot_y", "dot_z"};
    }
}
