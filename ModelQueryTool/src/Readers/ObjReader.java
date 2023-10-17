package Readers;

import Basics.Mesh;
import com.jogamp.opengl.math.Vec3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjReader implements Reader {
    @Override
    public ReadResult readFile(String fileName) throws IOException {
        List<Vec3f> points = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader( new FileReader(fileName) )) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                String[] parts = line.split("\\s+");
                if (parts[0].equals("v")) {
                    points.add(new Vec3f(
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[3])
                    ));
                } else if (parts[0].equals("f")) {
                    faces.add(new int[]{
                            Integer.parseInt(parts[1].split("/")[0]) - 1,
                            Integer.parseInt(parts[2].split("/")[0]) - 1,
                            Integer.parseInt(parts[3].split("/")[0]) - 1
                    });
                }
            }
        }

        return new ReadResult(fileName.substring(fileName.lastIndexOf("/") + 1), faces.toArray(new int[0][]), points.toArray(new Vec3f[0]));
    }
}
