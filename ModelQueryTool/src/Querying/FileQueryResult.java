package Querying;

import Basics.Mesh;

import java.util.List;

public class FileQueryResult {
    private final List<Mesh> meshes;

    private final List<Float> distances;

    FileQueryResult() {
        this(null, null);
    }

    FileQueryResult(List<Mesh> meshes, List<Float> distances) {
        this.meshes = meshes;
        this.distances = distances;
    }

    public List<Float> getDistances() {
        return distances;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }
}
