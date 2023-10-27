package Preprocessing.Analysis.AnalysisTasks;

import Basics.Helpers;
import Readers.ReadResult;
import Readers.Reader;
import com.jogamp.opengl.math.Vec3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GeneralAnalysisTask extends AnalysisTask {
    public GeneralAnalysisTask() {
        this.analysisFileName = "analysis";
    }

    @Override
    String analyse(String filePath) throws IOException {
        filePath = filePath.trim();

        String[] splitPath = filePath.split("[\\\\/]");
        String mName = splitPath[splitPath.length - 1];
        String mClass = splitPath[splitPath.length - 2];
        String mGroup = splitPath[splitPath.length - 3];

        String types = "";
        if (filePath.endsWith(".obj")) types = checkObjFaces(filePath);
        else if (filePath.endsWith(".off")) types = checkOffFaces(filePath);
        else if (filePath.endsWith(".ply")) types = checkPlyFaces(filePath);

        ReadResult readResult = Reader.read(filePath);

        Vec3f[] vertices = readResult.getVertices();
        float[] ext = Helpers.getMinMaxCoordinates(vertices);

        return mGroup + "," + mClass + "," + mName + "," + readResult.getFaces().length + "," + vertices.length + "," + types + "," + ext[0] + "," + ext[1] + "," + ext[2] + "," + ext[3] + "," + ext[4] + "," + ext[5] + "\n";
    }

    @Override
    String[] getColumns() {
        return new String[] {"group", "class", "name", "faces", "vertices", "facetype", "xmin", "xmax", "ymin", "ymax", "zmin", "zmax"};
    }

    private String checkObjFaces(String object) {
        boolean foundTriangles = false;
        boolean foundQuads = false;
        try (BufferedReader reader = new BufferedReader( new FileReader(object) )) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("f")) {
                    if (parts.length == 4) foundTriangles = true;
                    else if (parts.length == 5) foundQuads = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getFaceType(foundTriangles, foundQuads);
    }

    private String checkOffFaces(String object) {
        int vIdx = 0;
        int vCount = 0;
        int fIdx = 0;
        int fCount = 0;

        boolean foundTriangles = false;
        boolean foundQuads = false;
        try (BufferedReader reader = new BufferedReader( new FileReader(object) )) {
            String line;
            boolean foundMeshInfo = false;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                // Safe to skip empty or comment lines
                if (line.isEmpty()) continue;
                if (line.trim().startsWith("#")) continue;
                // We first need to find the mesh info
                if (!foundMeshInfo) {
                    String[] parts = line.split("\\s+");
                    if (parts.length != 3) continue;

                    vCount = Integer.parseInt(parts[0]);
                    fCount = Integer.parseInt(parts[1]);
                    foundMeshInfo = true;
                } else {
                    String[] parts = line.split("\\s+");

                    if (vIdx == vCount && fIdx == fCount) {
                        break;
                    } else if (vIdx < vCount) {
                        vIdx++;
                    } else if (fIdx < fCount) {
                        fIdx++;
                        if (Integer.parseInt(parts[0]) == 3) foundTriangles = true;
                        else if (Integer.parseInt(parts[0]) == 4) foundQuads = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getFaceType(foundTriangles, foundQuads);
    }

    private String checkPlyFaces(String object) {
        int vIdx = 0;
        int vCount = -1;
        int fIdx = 0;
        int fCount = -1;
        boolean foundTriangles = false;
        boolean foundQuads = false;
        boolean headerEndFound = false;
        try (BufferedReader reader = new BufferedReader( new FileReader(object) )) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("element")) {
                    switch (parts[1]) {
                        case "vertex" -> vCount = Integer.parseInt(parts[2]);
                        case "face" -> fCount = Integer.parseInt(parts[2]);
                    }

                    continue;
                } else if (parts[0].equals("end_header")) {
                    if (vCount == -1 || fCount == -1) throw new IllegalStateException("Header end found before vertex or face count -> invalid PLY file.");
                    headerEndFound = true;
                    continue;
                }

                if (!headerEndFound) continue;

                if (vIdx == vCount && fIdx == fCount) {
                    break;
                } else if (vIdx < vCount) {
                    vIdx++;
                } else if (fIdx < fCount) {
                    fIdx++;
                    if (Integer.parseInt(parts[0]) == 3) foundTriangles = true;
                    else if (Integer.parseInt(parts[0]) == 4) foundQuads = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getFaceType(foundTriangles, foundQuads);
    }

    private String getFaceType(boolean t, boolean q) throws RuntimeException {
        if (t && q) {
            return  "mixed";
        } else if (t) {
            return "triangle";
        } else if (q) {
            return  "quads";
        }

        throw new RuntimeException("Neither types of faces found. Please check your mesh file for any oddities.");
    }
}
