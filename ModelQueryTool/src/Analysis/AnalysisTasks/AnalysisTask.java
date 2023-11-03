package Analysis.AnalysisTasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class AnalysisTask {
    private static boolean clean;

    protected String analysisFileName;

    abstract String analyse(String filePath) throws IOException;

    abstract String[] getColumns();

    public void process(String filePath) throws IOException {
        if (ignoreFile(filePath)) return;
        createCSV(getColumns());

        try (FileWriter writer = new FileWriter(getFileName(), true)) {
            String toWrite = analyse(filePath);
            if (toWrite != null) writer.write(toWrite);
        }
    }

    public void clearCSV() {
        String fileName = getFileName();
        File f = new File(fileName);
        if (!f.isFile()) return;

        if (f.delete()) System.out.println("Cleared " + fileName);
        else System.out.println("Couldn't delete " + fileName);
    }

    public static void setCleanMode(boolean cleanMode) {
        clean = cleanMode;
    }

    private String getFileName() {
        String extension = ".csv";
        if (clean) extension = "_clean" + extension;
        return "src\\Analysis\\CSV\\" + analysisFileName + extension;
    }

    private boolean ignoreFile(String filePath) {
        if (!filePath.endsWith(".obj") && !filePath.endsWith(".off") && !filePath.endsWith(".ply")) return true;
        boolean isCleanFile = filePath.substring(0, filePath.lastIndexOf('.')).endsWith("_clean");
        return (clean && !isCleanFile) || (!clean && isCleanFile);
    }

    private void createCSV(String[] columns) {
        try {
            File f = new File(getFileName());
            if (f.createNewFile()) {
                FileWriter writer = new FileWriter(f);
                writer.write(String.join(",", columns) + "\n");
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to create analysis file for " + this.getClass().getName() + ":");
            e.printStackTrace();
        }
    }
}
