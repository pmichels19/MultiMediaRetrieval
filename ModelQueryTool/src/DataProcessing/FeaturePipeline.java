package DataProcessing;

import Basics.Helpers;
import Basics.Mesh;
import Basics.Stitcher;
import DataProcessing.Descriptors.Descriptor;
import DataProcessing.Descriptors.Elementary.*;
import DataProcessing.Descriptors.Global.*;
import Readers.Reader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class FeaturePipeline {
    private static final String ERROR_FILE = "feature_errored_objects.txt";

    private static FeaturePipeline featurePipeline;

    private final Stitcher stitcher;

    private static final Descriptor[] descriptors = new Descriptor[] {
            new SurfaceAreaDescriptor(),
            new CompactnessDescriptor(),
            new RectangularityDescriptor(),
            new DiameterDescriptor(),
            new ConvexityDescriptor(),
            new EccentricityDescriptor(),

            new A3Descriptor(),
            new D1Descriptor(),
            new D2Descriptor(),
            new D3Descriptor(),
            new D4Descriptor(),
    };

    private FeaturePipeline() {
        this.stitcher = Stitcher.getInstance();
    }

    public static FeaturePipeline getInstance() {
        if (featurePipeline == null) featurePipeline = new FeaturePipeline();
        return featurePipeline;
    }

    private void clearExistingJson(String jsonFileLocation) {
        File json = new File(jsonFileLocation);
        if (json.isFile() && !json.delete()) throw new IllegalStateException("Failed to delete descriptor data file for " + jsonFileLocation);
    }

    public void calculateDatabaseDescriptors() {
        String[] jsonFiles;
        String[] meshFiles;
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, this::shouldProcess)) {
            meshFiles = pathStream.map(Path::toString).toArray(String[]::new);
            jsonFiles = Arrays.stream(meshFiles).map(this::toJsonFile).toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Clear existing json files
        Arrays.stream(jsonFiles).forEach(this::clearExistingJson);
        System.out.println("########## Cleared database json files ##########");

        // Calculate the descriptors for all meshes and report any failing ones
        StringBuilder builder = new StringBuilder();
        FeaturePipelineContext[] contexts = calculateDatabaseDescriptors(meshFiles, builder);
        if (!builder.isEmpty()) reportFailedMeshes(builder);
        System.out.println("########## Calculated database descriptors ##########");

        // Normalize the elementary data and get standardized weights of the global features
        FeatureStatistics statistics = new FeatureStatistics(contexts);
        System.out.println("########## Extracted database mins, maxs, means and standard deviations ##########");

        for (FeaturePipelineContext context : contexts) {
            if (context == null) continue;
            context.normalizeElementaries(statistics);
        }

        // Save the contexts to their json files and save the statistics
        saveContexts(contexts, jsonFiles);
        statistics.saveToJson();
        System.out.println("########## Saved database json files and statistics ##########");
    }

    private FeaturePipelineContext[] calculateDatabaseDescriptors(String[] meshFiles, StringBuilder erroredFiles) {
        FeaturePipelineContext[] contexts = new FeaturePipelineContext[meshFiles.length];
        for (int i = 0; i < meshFiles.length; i++) {
            String meshFile = meshFiles[i];

            try {
                contexts[i] = calculateMeshDescriptors(meshFile);
                contexts[i].unloadMesh();
            } catch (Exception e) {
                erroredFiles.append(meshFile).append("\n");
                e.printStackTrace();
            }
        }

        return contexts;
    }

    public FeaturePipelineContext calculateMeshDescriptors(String meshFile) throws IOException {
        Mesh mesh = stitcher.stitchHoles(Reader.read(meshFile));
        FeaturePipelineContext context = new FeaturePipelineContext(mesh);
        System.out.println("===== " + meshFile + " =====");
        for (int i = 0; i < descriptors.length; i++) {
            Descriptor descriptor = descriptors[i];
            descriptor.process(context);
            System.out.println((i + 1) + " - " + descriptor.getKey());
        }

        return context;
    }

    private void reportFailedMeshes(StringBuilder erroredFiles) {
        if (erroredFiles.isEmpty()) return;
        try (FileWriter errorWriter = new FileWriter(ERROR_FILE)) {
            errorWriter.write(erroredFiles.toString());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void saveContexts(FeaturePipelineContext[] contexts, String[] jsonFiles) {
        if (contexts.length != jsonFiles.length) throw new IllegalArgumentException("Array length mismatch: " + contexts.length + " != " + jsonFiles.length);

        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] == null) continue;

            ObjectMapper mapper = new ObjectMapper();
            try {
                File json = new File(jsonFiles[i]);

                if (!json.createNewFile()) throw new IOException("Failed to create file for " + jsonFiles[i]);

                ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
                writer.writeValue(json, contexts[i].toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean shouldProcess(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().matches(".*_clean\\.(obj|off|ply)");
    }

    private String toJsonFile(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf('.')) + ".json";
    }
}
