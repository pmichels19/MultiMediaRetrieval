package Preprocessing;

import Basics.Mesh;
import Preprocessing.Preperation.CleaningTask;
import Preprocessing.Preperation.Normalization.*;
import Preprocessing.Preperation.PreparationTask;
import Readers.ReadResult;
import Readers.Reader;
import com.jogamp.opengl.math.Vec3f;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

public class PreperationPipeline {
    private static final String ERROR_FILE = "prepare_errored_objects.txt";

    private static final PreparationTask[] tasks = new PreparationTask[] {
            new RemoveNonManifoldFacesTask(),
            new CleaningTask(),
            new BarycenterToOriginTask(),
            new AlignPCATask(),
            new FlippingTask(),
            new ScaleToUnitCubeTask(),
            new OrientFaceNormalsTask(),
    };

    private static PreperationPipeline preperationPipeline;

    private PreperationPipeline() {
    }

    public static PreperationPipeline getInstance() {
        if (preperationPipeline == null) preperationPipeline = new PreperationPipeline();
        return preperationPipeline;
    }

    public void prepareDatabase(boolean clean) {
        List<String> toProcess = null;
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, ((path, attributes) -> shouldProcess(path, attributes, clean)))) {
            toProcess = pathStream.map(Path::toString).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (toProcess == null) return;
        for (String path : toProcess) {
            try {
                prepareFile(path);
            } catch (Exception e) {
                e.printStackTrace();
                builder.append(path).append("\n");
            }
        }

        if (builder.isEmpty()) return;
        try (FileWriter errorWriter = new FileWriter(ERROR_FILE)) {
            errorWriter.write(builder.toString());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prepareFile(String filePath) throws IOException {
        ReadResult data = Reader.read(filePath);
        PreperationPipelineContext context = runTasks(data);
        savePreparedMesh(filePath, context);
    }

    public Mesh getCleanMesh(String filePath) throws IOException {
        ReadResult data = Reader.read(filePath);
        PreperationPipelineContext result = runTasks(data);
        return new Mesh(result.getVertices(), result.getFaces(), data.getFilePath());
    }

    private PreperationPipelineContext runTasks(ReadResult data) {
        PreperationPipelineContext context = new PreperationPipelineContext(data);
        for (int i = 0; i < tasks.length; i++) {
            PreparationTask task = tasks[i];
            System.out.println(i + " - " + task.getDescription());
            task.execute(context);
        }

        return context;
    }

    private String getPreparedFileName(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        String pathToFile = filePath.substring(0, lastDotIndex);
        if (!pathToFile.endsWith("_clean")) pathToFile += "_clean";

        String extension = filePath.substring(lastDotIndex);
        return pathToFile + extension;
    }

    private boolean shouldProcess(Path path, BasicFileAttributes attributes, boolean clean) {
        String regex = ".*\\.(obj|off|ply)";
        if (clean) regex = ".*_clean\\.(obj|off|ply)";

        return attributes.isRegularFile() && path.getFileName().toString().matches(regex);
    }

    private void savePreparedMesh(String filePath, PreperationPipelineContext context) {
        String trimmedPath = filePath.trim();
        String fileName = getPreparedFileName(trimmedPath);

        int[][] faces = context.getFaces();
        Vec3f[] vertices = context.getVertices();
        try (FileWriter writer = new FileWriter(fileName)) {
            if (trimmedPath.endsWith(".obj")) {
                saveObj(writer, vertices, faces);
            } else if (trimmedPath.endsWith(".off")) {
                saveOff(writer, vertices, faces);
            } else if (trimmedPath.endsWith(".ply")) {
                savePly(writer, vertices, faces);
            } else {
                writer.close();
                throw new IllegalStateException("Tried to save to a file that isn't a 3D object. Aborting.");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void saveObj(FileWriter writer, Vec3f[] vertices, int[][] faces) throws IOException {
        for (Vec3f v : vertices) writer.write("v " + v.x() + " " + v.y() + " " + v.z() + "\n");
        for (int[] f : faces) writer.write("f " + (f[0] + 1)  + " " + (f[1] + 1) + " " + (f[2] + 1) + "\n");
    }

    private void saveOff(FileWriter writer, Vec3f[] vertices, int[][] faces) throws IOException {
        writer.write("OFF\n");
        writer.write(vertices.length + " " + faces.length + " 0\n");
        for (Vec3f v : vertices) writer.write(v.x() + " " + v.y() + " " + v.z() + "\n");
        for (int[] f : faces) writer.write("3 " + f[0] + " " + f[1] + " " + f[2] + "\n");
    }

    private void savePly(FileWriter writer, Vec3f[] vertices, int[][] faces) throws IOException {
        writer.write("ply\n");
        writer.write("format ascii 1.0\n");
        writer.write("element vertex " + vertices.length + "\n");
        writer.write("property float x\n");
        writer.write("property float y\n");
        writer.write("property float z\n");
        writer.write("element face " + faces.length + "\n");
        writer.write("property list uchar int vertex_indices\n");
        writer.write("end_header\n");
        for (Vec3f v : vertices) writer.write(v.x() + " " + v.y() + " " + v.z() + "\n");
        for (int[] f : faces) writer.write("3 " + f[0] + " " + f[1] + " " + f[2] + "\n");
    }
}
