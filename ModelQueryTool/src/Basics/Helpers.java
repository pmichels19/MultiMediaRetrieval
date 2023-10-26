package Basics;

import com.jogamp.opengl.math.Vec3f;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Helpers {
    private static final String DISTANCE_EUCLIDEAN = "_euclidean";

    private static final String DISTANCE_COSINE = "_cosine";

    private static final String DISTANCE_EMD = "_emd";

    public static final String[] DISTANCE_FUNCTIONS = new String[] { DISTANCE_EUCLIDEAN, DISTANCE_COSINE };

    public static float getDistance(String function, float[] v1, float[] v2) {
        if (v1.length != v2.length) throw new IllegalArgumentException("Array length mismatch: " + v1.length + " != " + v2.length);

        float distance;
        switch (function) {
            case DISTANCE_EUCLIDEAN -> distance = getEuclidean(v1, v2);
            case DISTANCE_COSINE -> distance = getCosine(v1,v2);
            case DISTANCE_EMD -> distance = getEarthMovers(v1, v2);
            default -> throw new IllegalArgumentException("Distance function " + function + " does not exist.");
        }

        return distance;
    }

    private static float getEuclidean(float[] v1, float[] v2) {
        float distance = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            float diff = v1[i] - v2[i];
            distance += diff * diff;
        }

        return (float) Math.sqrt(distance);
    }

    private static float getCosine(float[] v1, float[] v2) {
        float dot = 0.0f;
        float v1l = 0.0f;
        float v2l = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            v1l += v1[i] * v1[i];
            v2l += v2[i] * v2[i];
        }

        return 1.0f - ( dot / (float) (Math.sqrt(v1l) * Math.sqrt(v2l)) );
    }

    private static float getEarthMovers(float[] v1, float[] v2) {
        throw new UnsupportedOperationException("EMD not (yet) implemented.");
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

    protected static void calculateFaceNormals(Vec3f[] vs, int[][] fs, Vec3f[] ns, float[] as) {
        for (int i = 0; i < fs.length; i++) {
            // Compute face normals, needed for flat shading
            Vec3f v0 = vs[fs[i][0]];
            Vec3f v1 = vs[fs[i][1]];
            Vec3f v2 = vs[fs[i][2]];

            Vec3f e1 = v1.minus(v0);
            Vec3f e2 = v2.minus(v0);
            Vec3f n = e1.cross(e2);
            double length = n.length();
            if (length < Double.MIN_VALUE) {
                // Sometimes we need extra accuracy -> BigDecimal
                BigDecimal e1x = BigDecimal.valueOf(e1.x());
                BigDecimal e1y = BigDecimal.valueOf(e1.y());
                BigDecimal e1z = BigDecimal.valueOf(e1.z());
                BigDecimal e2x = BigDecimal.valueOf(e2.x());
                BigDecimal e2y = BigDecimal.valueOf(e2.y());
                BigDecimal e2z = BigDecimal.valueOf(e2.z());
                // Calculate cross product
                BigDecimal nxb = e1y.multiply(e2z).min(e1z.multiply(e2y));
                BigDecimal nyb = e1z.multiply(e2x).min(e1x.multiply(e2z));
                BigDecimal nzb = e1x.multiply(e2y).min(e1y.multiply(e2x));
                // Calculate length of cross product
                BigDecimal nxb2 = nxb.multiply(nxb);
                BigDecimal nyb2 = nyb.multiply(nyb);
                BigDecimal nzb2 = nzb.multiply(nzb);
                BigDecimal nl = nxb2.add(nyb2).add(nzb2).sqrt(new MathContext(100));
                length = nl.doubleValue();
                // Back to a normal with 1 / nl
                BigDecimal scale = BigDecimal.ONE.divide(nl, RoundingMode.HALF_UP);
                nxb = nxb.multiply(scale);
                nyb = nyb.multiply(scale);
                nzb = nzb.multiply(scale);
                ns[i] = new Vec3f(nxb.floatValue(), nyb.floatValue(), nzb.floatValue());
                if (ns[i].isZero()) {
                    throw new ArithmeticException("Found a normal of length 0");
                }
            } else {
                float scale = 1.0f / (float) Math.sqrt(length);
                ns[i] = n.scale(scale);
            }

            as[i] = ((float) length) * 0.5f;
        }
    }

    protected static void faceNormalsToVertexNormals(Vec3f[] fns, int[][] fs, float[] as, Vec3f[] vns) {
        float[] weights = new float[vns.length];

        for (int i = 0; i < vns.length; i++) vns[i] = new Vec3f();

        for (int i = 0; i < fns.length; i++) {
            float a = as[i];
            int v1 = fs[i][0];
            int v2 = fs[i][1];
            int v3 = fs[i][2];
            weights[v1] += a;
            weights[v2] += a;
            weights[v3] += a;

            Vec3f scaledFaceNormal = (new Vec3f(fns[i])).scale(a);
            vns[v1].add( scaledFaceNormal );
            vns[v2].add( scaledFaceNormal );
            vns[v3].add( scaledFaceNormal );
        }

        for (int i = 0; i < vns.length; i++) vns[i].scale( 1.0f / weights[i] ).normalize();
    }
}
