package Preprocessing;

import Readers.ReadResult;
import com.jogamp.opengl.math.Vec3f;

public class PreperationPipelineContext {
    private Vec3f[] vertices;
    private int[][] faces;

    public PreperationPipelineContext(ReadResult readResult) {
        int[][] ogFaces = readResult.getFaces();
        faces = new int[ogFaces.length][3];
        for (int i = 0; i < ogFaces.length; i++) faces[i] = new int[] { ogFaces[i][0], ogFaces[i][1], ogFaces[i][2] };

        Vec3f[] ogVertices = readResult.getVertices();
        vertices = new Vec3f[ogVertices.length];
        for (int i = 0; i < ogVertices.length; i++) vertices[i] = new Vec3f(ogVertices[i]);
    }

    public Vec3f[] getVertices() {
        return vertices;
    }

    public void setVertices(Vec3f[] vertices) {
//        System.out.println("v:" + this.vertices.length + " -> " + vertices.length);
        this.vertices = vertices;
    }

    public int[][] getFaces() {
        return faces;
    }

    public void setFaces(int[][] faces) {
//        System.out.println("f:" + this.faces.length + " -> " + faces.length);
        this.faces = faces;
    }
}
