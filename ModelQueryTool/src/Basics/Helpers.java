package Basics;

import com.jogamp.opengl.math.Vec3f;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helpers {
    public static List<String> getJsonFiles() {
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(".*_clean\\.json"))) {
            return pathStream.map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float[] getMinMaxCoordinates(Vec3f[] vertices) {
        float minx, miny, minz;
        float maxx, maxy, maxz;
        minx = miny = minz =  Float.MAX_VALUE;
        maxx = maxy = maxz = -Float.MAX_VALUE;
        for (Vec3f vertex : vertices) {
            if (vertex.x() < minx) minx = vertex.x();
            if (vertex.x() > maxx) maxx = vertex.x();
            if (vertex.y() < miny) miny = vertex.y();
            if (vertex.y() > maxy) maxy = vertex.y();
            if (vertex.z() < minz) minz = vertex.z();
            if (vertex.z() > maxz) maxz = vertex.z();
        }

        return new float[] {minx, maxx, miny, maxy, minz, maxz};
    }

    public static RealMatrix getCovarianceMatrix(Vec3f[] vertices) {
        double[][] vertices2D = new double[vertices.length][3];
        // Sums of (sample.<a> - mean.<a>)(sample.<b> - mean.<b>)
        for (int i = 0; i < vertices.length; i++) {
            Vec3f vertex = vertices[i];
            vertices2D[i][0] = vertex.x();
            vertices2D[i][1] = vertex.y();
            vertices2D[i][2] = vertex.z();
        }

        return new Covariance(vertices2D).getCovarianceMatrix();
    }

    public static void covarianceToEigenVectors(RealMatrix covariance, Vec3f x, Vec3f y, Vec3f z) {
        EigenDecomposition decomposition = new EigenDecomposition(covariance);
        RealMatrix d = decomposition.getV();
        x.setX((float) d.getEntry(0, 0));
        x.setY((float) d.getEntry(1, 0));
        x.setZ((float) d.getEntry(2, 0));
        x.normalize();

        y.setX((float) d.getEntry(0, 1));
        y.setY((float) d.getEntry(1, 1));
        y.setZ((float) d.getEntry(2, 1));
        y.normalize();

        z.set(new Vec3f().cross(x, y).normalize());
    }
}
