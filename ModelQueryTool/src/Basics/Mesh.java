package Basics;

import Readers.ReadResult;
import com.jogamp.opengl.math.Vec3f;

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
            vertexBuffer[idx + 0] = vertices[i].x();
            vertexBuffer[idx + 1] = vertices[i].y();
            vertexBuffer[idx + 2] = vertices[i].z();
        }

        areas = new float[faces.length];
        faceNormals = new Vec3f[faces.length];
        Helpers.calculateFaceNormals(vertices, faces, faceNormals, areas);
        facesBuffer = new int[3 * faces.length];
        faceNormalBuffer = new float[3 * faces.length];
        for (int i = 0; i < faces.length; i++) {
            int idx = i * 3;
            facesBuffer[idx + 0] = faces[i][0];
            facesBuffer[idx + 1] = faces[i][1];
            facesBuffer[idx + 2] = faces[i][2];

            faceNormalBuffer[idx + 0] = faceNormals[i].x();
            faceNormalBuffer[idx + 1] = faceNormals[i].y();
            faceNormalBuffer[idx + 2] = faceNormals[i].z();
        }

        vertexNormals = new Vec3f[vertices.length];
        Helpers.faceNormalsToVertexNormals(faceNormals, faces, areas, vertexNormals);
        vertexNormalBuffer = new float[3 * vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            int idx = i * 3;
            vertexNormalBuffer[idx + 0] = vertexNormals[i].x();
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
}
