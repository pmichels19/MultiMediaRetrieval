package Querying;

import Basics.Mesh;
import Preprocessing.PreperationPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileQueryResult {
    private final List<String> meshFiles;

    private final List<String> distances;

    FileQueryResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public FileQueryResult(List<String> meshFiles, List<Float> distances) {
        this.meshFiles = meshFiles;
        this.distances = distances.stream().map(f -> {
            if (f != null) return String.format("%.4f", f);
            return "input file";
        }).collect(Collectors.toList());
    }

    public List<String> getDistances() {
        return distances;
    }

    public List<String> getMeshFiles() {
        return meshFiles;
    }

    public List<Mesh> getMeshes() {
        System.out.println("Loading in result meshes...");
        PreperationPipeline preperationPipeline = PreperationPipeline.getInstance();
        return meshFiles.stream().map(meshFile -> {
            try {
                return preperationPipeline.getCleanMesh(meshFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).toList();
    }

    public void addInputFile(String inputFile) {
        distances.add(0, "input file");
        meshFiles.add(0, inputFile);
    }
}
