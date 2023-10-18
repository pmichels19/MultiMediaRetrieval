package Readers;

import Basics.Mesh;
import com.jogamp.opengl.math.Vec3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OffReader implements Reader {
    @Override
    public ReadResult readFile(String fileName) throws IOException {
        Vec3f[] points = null;
        int[][] faces = null;
        int vIdx = 0;
        int vCount = 0;
        int fIdx = 0;
        int fCount = 0;
        try (BufferedReader reader = new BufferedReader( new FileReader(fileName) )) {
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
                    points = new Vec3f[vCount];
                    fCount = Integer.parseInt(parts[1]);
                    faces = new int[fCount][3];
                    foundMeshInfo = true;
                } else {
                    String[] parts = line.split("\\s+");

                    if (vIdx == vCount && fIdx == fCount) {
                        break;
                    } else if (vIdx < vCount) {
                        points[vIdx] = new Vec3f(
                                Float.parseFloat(parts[0]),
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2])
                        );
                        vIdx++;
                    } else if (fIdx < fCount) {
                        faces[fIdx][0] = Integer.parseInt(parts[1]);
                        faces[fIdx][1] = Integer.parseInt(parts[2]);
                        faces[fIdx][2] = Integer.parseInt(parts[3]);
                        fIdx++;
                    }
                }
            }
        }

        return new ReadResult(fileName, faces, points);
    }
}
