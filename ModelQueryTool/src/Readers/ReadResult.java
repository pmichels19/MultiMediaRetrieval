package Readers;

import com.jogamp.opengl.math.Vec3f;

public class ReadResult {
    private final String filePath;
    private final int[][] faces;
    private final Vec3f[] vertices;

    public ReadResult(String filePath, int[][] faces, Vec3f[] vertices) {
        this.filePath = filePath;
        this.faces = faces;
        this.vertices = vertices;
    }

    public String getFilePath() {
        return filePath;
    }

    public int[][] getFaces() {
        return faces;
    }

    public Vec3f[] getVertices() {
        return vertices;
    }
}
