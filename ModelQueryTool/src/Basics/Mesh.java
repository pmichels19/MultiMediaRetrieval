package Basics;

import Readers.ReadResult;
import com.jogamp.opengl.math.Vec3f;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class Mesh {
    private final Vec3f[] vertices;
    private final float[] vertexBuffer;
    private final int[][] faces;
    private final int[] facesBuffer;
    private final float[] areas;
    private final Vec3f[] faceNormals;
    private final float[] faceNormalBuffer;
    private final Vec3f[] vertexNormals;
    private final float[] vertexNormalBuffer;
    private Mesh convexHull;
    private Float volume;
    private final String filePath;

    public Mesh(ReadResult readResult) {
        this(readResult.getVertices(), readResult.getFaces(), readResult.getFilePath());
    }

    public Mesh(Vec3f[] vertices, int[][] faces, String filePath) {
        this.vertices = vertices;
        this.faces = faces;
        this.filePath = filePath;

        // Buffer building
        vertexBuffer = new float[3 * vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            int idx = i * 3;
            vertexBuffer[idx] = vertices[i].x();
            vertexBuffer[idx + 1] = vertices[i].y();
            vertexBuffer[idx + 2] = vertices[i].z();
        }

        areas = new float[faces.length];
        faceNormals = new Vec3f[faces.length];
        calculateFaceNormals(vertices, faces, faceNormals, areas);
        facesBuffer = new int[3 * faces.length];
        faceNormalBuffer = new float[3 * faces.length];
        for (int i = 0; i < faces.length; i++) {
            int idx = i * 3;
            facesBuffer[idx] = faces[i][0];
            facesBuffer[idx + 1] = faces[i][1];
            facesBuffer[idx + 2] = faces[i][2];

            faceNormalBuffer[idx] = faceNormals[i].x();
            faceNormalBuffer[idx + 1] = faceNormals[i].y();
            faceNormalBuffer[idx + 2] = faceNormals[i].z();
        }

        vertexNormals = new Vec3f[vertices.length];
        faceNormalsToVertexNormals(faceNormals, faces, areas, vertexNormals);
        vertexNormalBuffer = new float[3 * vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            int idx = i * 3;
            vertexNormalBuffer[idx] = vertexNormals[i].x();
            vertexNormalBuffer[idx + 1] = vertexNormals[i].y();
            vertexNormalBuffer[idx + 2] = vertexNormals[i].z();
        }
    }

    public Vec3f[] getVertices() {
        return vertices;
    }

    public int[][] getFaces() {
        return faces;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getName() {
        return filePath.substring(filePath.lastIndexOf("\\") + 1);
    }

    public float[] getVertexBuffer() {
        return vertexBuffer;
    }

    public int[] getFacesBuffer() {
        return facesBuffer;
    }

    public Vec3f[] getFaceNormals() {
        return faceNormals;
    }

    public float[] getFaceNormalBuffer() {
        return faceNormalBuffer;
    }

    public Vec3f[] getVertexNormals() {
        return vertexNormals;
    }

    public float[] getVertexNormalBuffer() {
        return vertexNormalBuffer;
    }

    public float[] getAreas() {
        return areas;
    }

    public int size() {
        return faces.length + vertices.length;
    }

    public float getVolume() {
        if (volume == null) {
            PythonScriptExecutor executor = new PythonScriptExecutor();
            List<String> output = executor.executeMeshScript(this, PythonScriptExecutor.VOLUME);
            volume = Math.abs(Float.parseFloat(output.get(0)));
        }

        return volume;
    }

    public Mesh getHull() {
        if (convexHull == null) {
            PythonScriptExecutor executor = new PythonScriptExecutor();
            List<String> output = executor.executeMeshScript(this, PythonScriptExecutor.CONVEX_HULL);

            int idx = 0;
            int numV = Integer.parseInt(output.get(idx));
            idx++;
            Vec3f[] hullVertices = new Vec3f[numV];
            for (int i = 0; i < numV; i++, idx++) {
                String[] parts = output.get(idx).split("\\s+");
                float x, y, z;
                x = Float.parseFloat(parts[0]);
                y = Float.parseFloat(parts[1]);
                z = Float.parseFloat(parts[2]);
                hullVertices[i] = new Vec3f(x, y, z);
            }

            int numF = Integer.parseInt(output.get(idx));
            idx++;
            int[][] hullFaces = new int[numF][3];
            for (int i = 0; i < numF; i++, idx++) {
                String[] parts = output.get(idx).split("\\s+");
                int v0, v1, v2;
                v0 = Integer.parseInt(parts[0]);
                v1 = Integer.parseInt(parts[1]);
                v2 = Integer.parseInt(parts[2]);
                hullFaces[i] = new int[] { v0, v1, v2 };
            }

            convexHull = new Mesh(hullVertices, hullFaces, "convex_hull_" + getName());
        }

        return convexHull;
    }

    private void calculateFaceNormals(Vec3f[] vs, int[][] fs, Vec3f[] ns, float[] as) {
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

    private void faceNormalsToVertexNormals(Vec3f[] fns, int[][] fs, float[] as, Vec3f[] vns) {
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
