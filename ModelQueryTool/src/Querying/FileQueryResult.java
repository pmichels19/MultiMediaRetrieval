package Querying;

import Basics.Mesh;

import java.util.ArrayList;
import java.util.List;

public class FileQueryResult {
    private final List<Mesh> meshes;

    private final List<String> distances;

    FileQueryResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    FileQueryResult(List<Mesh> meshes, List<Float> distances) {
        this.meshes = meshes;
        this.distances = distances.stream().map(f -> {
            if (f != null) return String.format("%.4f", f);
            return "input file";
        }).toList();
    }

    public List<String> getDistances() {
        return distances;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }
}
