package Readers;

import com.jogamp.opengl.math.Vec3f;

public class ReadResult {
    private final String name;
    private final int[][] faces;
    private final Vec3f[] vertices;

    public ReadResult(String name, int[][] faces, Vec3f[] vertices) {
        this.name = name;
        this.faces = faces;
        this.vertices = vertices;
    }

    public String getName() {
        return name;
    }

    public int[][] getFaces() {
        return faces;
    }

    public Vec3f[] getVertices() {
        return vertices;
    }
}
