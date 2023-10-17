package Basics;

import Readers.ReadResult;
import com.jogamp.opengl.math.Vec3f;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Stitcher {
    private static Stitcher stitcher;

    private Stitcher() {}

    public static Stitcher getInstance() {
        if (stitcher == null) {
            stitcher = new Stitcher();
        }

        return stitcher;
    }

    public Mesh stitchHoles(ReadResult readResult) {
        int[][] originalFaces = readResult.getFaces();

        // First we stitch any holes
        List<Edge> singleUseEdges = getSingleUseEdges(originalFaces);
        if (singleUseEdges.isEmpty()) return new Mesh(readResult);

        List<int[]> loopFreeBoundaries = extractLoops(singleUseEdges);
        return fixHoles(readResult, loopFreeBoundaries);
    }

    private List<Edge> getSingleUseEdges(int[][] faces) {
        Map<Edge, Integer> edgeMap = new HashMap<>();
        for (int[] face : faces) {
            Edge e0 = new Edge(face[0], face[1]);
            Edge e1 = new Edge(face[0], face[2]);
            Edge e2 = new Edge(face[1], face[2]);
            if (!edgeMap.containsKey(e0)) edgeMap.put(e0, 0);
            if (!edgeMap.containsKey(e1)) edgeMap.put(e1, 0);
            if (!edgeMap.containsKey(e2)) edgeMap.put(e2, 0);
            edgeMap.put(e0, edgeMap.get(e0) + 1);
            edgeMap.put(e1, edgeMap.get(e1) + 1);
            edgeMap.put(e2, edgeMap.get(e2) + 1);
        }

        List<Edge> edges = new ArrayList<>();
        for (Edge edge : edgeMap.keySet()) {
            if (edgeMap.get(edge) == 1) edges.add(edge);
        }

        return edges;
    }

    private List<int[]> extractLoops(List<Edge> boundaries) {
        List<int[]> result = new ArrayList<>();
        PythonScriptExecutor executor = new PythonScriptExecutor();
        List<String> unloopedString = executor.executeGraphScript(boundaries, PythonScriptExecutor.DETECT_LOOPS);

        Iterator<String> iterator = unloopedString.listIterator();
        int numBounds = Integer.parseInt(iterator.next());
        for (int i = 0; i < numBounds; i++) {
            int[] path = new int[Integer.parseInt(iterator.next())];
            for (int j = 0; j < path.length; j++) path[j] = Integer.parseInt(iterator.next());

            result.add(path);
        }

        return result;
    }

    private Mesh fixHoles(ReadResult readResult, List<int[]> boundaries) {
        Vec3f[] originalVertices = readResult.getVertices();
        int[][] originalFaces = readResult.getFaces();

        // We need one new vertex for every hole
        Vec3f[] newVertices = new Vec3f[originalVertices.length + boundaries.size()];
        System.arraycopy(originalVertices, 0, newVertices, 0, originalVertices.length);
        for (int i = 0; i < boundaries.size(); i++) {
            Vec3f newVertex = new Vec3f();
            for (int vIdx : boundaries.get(i)) {
                newVertex.add(newVertices[vIdx]);
            }

            newVertex.scale(1.0f / (float) boundaries.get(i).length);
            newVertices[i + originalVertices.length] = newVertex;
        }

        // We need one face for every vertex in a hole
        int numNewFaces = 0;
        for (int[] face : boundaries) numNewFaces += face.length;
        int[][] newFaces = new int[originalFaces.length + numNewFaces][3];
        System.arraycopy(originalFaces, 0, newFaces, 0, originalFaces.length);
        int faceIdx = originalFaces.length;
        for (int i = 0; i < boundaries.size(); i++) {
            int newVertexIdx = i + originalVertices.length;
            int[] face = boundaries.get(i);
            for (int v = 0; v < face.length - 1; v++) {
                newFaces[faceIdx] = new int[] { newVertexIdx, face[v], face[v + 1] };
                faceIdx++;
            }

            newFaces[faceIdx] = new int[] { newVertexIdx, face[0], face[face.length - 1] };
            faceIdx++;
        }

//        System.out.println("Stitched " + boundaries.size() + " holes. Added " + boundaries.size() + " vertices and " + numNewFaces + " faces.");
        return new Mesh(newVertices, newFaces, readResult.getName());
    }
}
