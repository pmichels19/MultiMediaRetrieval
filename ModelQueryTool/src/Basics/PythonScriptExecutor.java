package Basics;

import com.jogamp.opengl.math.Vec3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PythonScriptExecutor {
    public static final String CONVEX_HULL = "convex_hull";
    public static final String REORIENT_NORMALS = "reorient_normals";
    public static final String VOLUME = "volume";
    public static final String DETECT_LOOPS = "detect_loops";
    public static final String REMOVE_NON_MANIFOLD = "remove_non_manifold";

    public List<String> executeGraphScript(List<Edge> edges, String scriptName) {
        try {
            saveToTempFile(fileData(edges), "graph.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return runScript(scriptName);
    }

    public List<String> executeMeshScript(Vec3f[] vertices, int[][] faces, String scriptName) {
        try {
            saveToTempFile(fileData(vertices, faces), "temp.off");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return runScript(scriptName);
    }

    public List<String> executeMeshScript(Mesh mesh, String scriptName) {
        try {
            saveToTempFile(fileData(mesh.getVertices(), mesh.getFaces()), "temp.off");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return runScript(scriptName);
    }

    private String fileData(List<Edge> edges) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            builder.append(edge.vertex1).append(",").append(edge.vertex2);
            if (i != edges.size() - 1) builder.append(",");
        }

        return builder.toString();
    }

    private String fileData(Vec3f[] vertices, int[][] faces) {
        StringBuilder builder = new StringBuilder();
        builder.append("OFF\n");
        builder.append(vertices.length).append(" ").append(faces.length).append(" 0\n");
        for (Vec3f v : vertices) builder.append(v.x()).append(" ").append(v.y()).append(" ").append(v.z()).append("\n");
        for (int[] f : faces) builder.append("3 ").append(f[0]).append(" ").append(f[1]).append(" ").append(f[2]).append("\n");
        return builder.toString();
    }

    private void saveToTempFile(String data, String fileName) throws IOException {
        File tempFile = new File("../DataPlots/shared/" + fileName);
        if (tempFile.isFile() && !tempFile.delete()) {
            throw new IOException("Failed to delete existing temporary file.");
        }

        if (!tempFile.createNewFile()) {
            throw new IOException("Failed to create temporary off file.");
        }

        FileWriter writer = new FileWriter(tempFile);
        writer.write(data);
        writer.close();
    }

    private List<String> runScript(String scriptName) {
        List<String> error = new ArrayList<>();
        List<String> output = new ArrayList<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("py", "../DataPlots/tools/" + scriptName + ".py");
            Process process = processBuilder.start();

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = outputReader.readLine()) != null) output.add(line);

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) error.add(errorLine);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script " + scriptName + " failed with exit code " + exitCode);
                for (String err : error) System.err.println(err);
            }
        } catch (IOException | InterruptedException e) {
            for (String err : error) System.err.println(err);
            e.printStackTrace();
        }

        return output;
    }
}
