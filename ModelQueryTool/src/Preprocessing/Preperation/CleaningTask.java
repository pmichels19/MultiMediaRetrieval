package Preprocessing.Preperation;

import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.*;

public class CleaningTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        Vec3f[] vertices = context.getVertices();
        int[][] faces = context.getFaces();

        // Clear out any duplicate vertices
        int[] deduplicated_indices = new int[vertices.length];
        Vec3f[] newVertices = getUniqueVertices(vertices, deduplicated_indices);
        context.setVertices(newVertices);

        // Now filter out any faces
        int[][] newFaces = getValidFaces(newVertices, faces, deduplicated_indices);
        context.setFaces(newFaces);
    }

    @Override
    public String getDescription() {
        return "Removing duplicate vertices and zero-area faces.";
    }

    private Vec3f[] getUniqueVertices(Vec3f[] vertices, int[] deduplicated_indices) {
        List<Vec3f> newVertices = new ArrayList<>();
        Map<String, Integer> vertexMap = new HashMap<>();
        for (int vidx = 0; vidx < vertices.length; vidx++) {
            String vs = vertices[vidx].toString();

            if (vertexMap.containsKey(vs)) {
                // If v is a duplicate, don't save the vertex and simply store the deduplicated index
                deduplicated_indices[vidx] = vertexMap.get(vs);
            } else {
                // If v is new, save the vertex
                // Make sure to take possible discrepancy of indices from previous duplicates into account
                int idx = newVertices.size();
                deduplicated_indices[vidx] = idx;
                newVertices.add(vertices[vidx]);
                vertexMap.put(vs, idx);
            }
        }

        return newVertices.toArray(new Vec3f[0]);
    }

    private int[][] getValidFaces(Vec3f[] vertices, int[][] faces, int[] deduplicated_indices) {
        Set<String> uniqueFaces = new HashSet<>();
        List<int[]> newFaces = new ArrayList<>();
        for (int[] face : faces) {
            int v0 = deduplicated_indices[face[0]];
            int v1 = deduplicated_indices[face[1]];
            int v2 = deduplicated_indices[face[2]];
            // Check if all vertices are unique
            if (v0 == v1 || v0 == v2 || v1 == v2) continue;
            // Check if the vertices are not collinear
            if (collinear(vertices[v0], vertices[v1], vertices[v2])) continue;

            String faceToString = v0 + "-" + v1 + "-" + v2;
            if (uniqueFaces.contains(faceToString)) continue;

            newFaces.add(new int[]{v0, v1, v2});
            uniqueFaces.add(faceToString);
        }

        return newFaces.toArray(new int[0][]);
    }

    private boolean collinear(Vec3f v0, Vec3f v1, Vec3f v2) {
        // Check if v0, v1 and v2 are collinear by checking if v0v1 x v0v2 is equal to zero, doubles for extra precision
        double v0v1x = ((double) v1.x()) - ((double) v0.x());
        double v0v1y = ((double) v1.y()) - ((double) v0.y());
        double v0v1z = ((double) v1.z()) - ((double) v0.z());

        double v0v2x = ((double) v2.x()) - ((double) v0.x());
        double v0v2y = ((double) v2.y()) - ((double) v0.y());
        double v0v2z = ((double) v2.z()) - ((double) v0.z());

        double cx = v0v1y * v0v2z - v0v1z * v0v2y;
        double cy = v0v1z * v0v2x - v0v1x * v0v2z;
        double cz = v0v1x * v0v2y - v0v1y * v0v2x;
        return cx == 0 && cy == 0 && cz == 0;
    }
}
