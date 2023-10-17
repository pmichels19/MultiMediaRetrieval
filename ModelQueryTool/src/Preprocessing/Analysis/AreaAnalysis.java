package Preprocessing.Analysis;

import Basics.Mesh;
import Readers.Reader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AreaAnalysis extends Analysis {
    private static AreaAnalysis areaAnalysis;

    private AreaAnalysis() {
        this.analysisFileName = "area_analysis";
    }

    public static AreaAnalysis getInstance() {
        if (areaAnalysis == null) {
            areaAnalysis = new AreaAnalysis();
        }

        return areaAnalysis;
    }

    @Override
    public void analyse(String filePath) throws IOException {
        if (ignoreFile(filePath)) return;
        createCSV(new String[]{"areas"});

        Mesh mesh = new Mesh(Reader.read(filePath));
        float[] areas = mesh.getAreas();
        File f = new File(getFileName());
        FileWriter writer = new FileWriter(f, true);

        for (float area : areas) writer.write(area + "\n");
        writer.close();
    }
}
