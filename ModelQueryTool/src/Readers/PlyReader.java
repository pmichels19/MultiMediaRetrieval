package Readers;

import com.jogamp.opengl.math.Vec3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PlyReader implements Reader {
    @Override
    public ReadResult readFile(String fileName) {
        int fIdx = 0;
        int vIdx = 0;
        int[][] faces = null;
        Vec3f[] points = null;
        boolean headerEndFound = false;
        try (BufferedReader reader = new BufferedReader( new FileReader(fileName) )) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("element")) {
                    switch (parts[1]) {
                        case "vertex" -> points = new Vec3f[Integer.parseInt(parts[2])];
                        case "face" -> faces = new int[Integer.parseInt(parts[2])][3];
                    }

                    continue;
                } else if (parts[0].equals("end_header")) {
                    if (points == null || faces == null) throw new IllegalStateException("Header end found before vertex or face count -> invalid PLY file.");
                    headerEndFound = true;
                    continue;
                }

                if (!headerEndFound) continue;

                if (vIdx == points.length && fIdx == faces.length) {
                    break;
                } else if (vIdx < points.length) {
                    points[vIdx] = new Vec3f(
                            Float.parseFloat(parts[0]),
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2])
                    );
                    vIdx++;
                } else if (fIdx < faces.length) {
                    faces[fIdx][0] = Integer.parseInt(parts[1]);
                    faces[fIdx][1] = Integer.parseInt(parts[2]);
                    faces[fIdx][2] = Integer.parseInt(parts[3]);
                    fIdx++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert faces != null;
        assert points != null;
        return new ReadResult(fileName.substring(fileName.lastIndexOf("/") + 1), faces, points);
    }
}
