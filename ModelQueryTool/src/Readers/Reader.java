package Readers;

import Basics.Mesh;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Reader {
    ReadResult readFile(String fileName) throws IOException;

    static ReadResult read(String fileName) throws IOException {
        Reader reader = null;
        String trimmedPath = fileName.trim();
        if (trimmedPath.endsWith(".obj")) {
            reader = new ObjReader();
        } else if (trimmedPath.endsWith(".off")) {
            reader = new OffReader();
        } else if (trimmedPath.endsWith(".ply")) {
            reader = new PlyReader();
        }

        if (reader == null) {
            throw new IllegalArgumentException("Input file " + fileName + " does not direct to a .obj, .off or .ply file.");
        }

        return reader.readFile(fileName);
    }

    static Mesh readToMesh(String fileName) throws IOException {
        return new Mesh(read(fileName));
    }
}
