package Preprocessing.Analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Analysis {
    protected String analysisFileName;

    private boolean clean;

    public void setCleanMode(boolean cleanMode) {
        clean = cleanMode;
    }

    public void clearCSV() {
        File f = new File(getFileName());
        if (f.delete()) System.out.println("Cleared the existing CSV file.");
        else System.out.println("Couldn't delete CSV file.");
    }

    protected boolean ignoreFile(String filePath) {
        if (!filePath.endsWith(".obj") && !filePath.endsWith(".off") && !filePath.endsWith(".ply")) return true;
        boolean isCleanFile = filePath.substring(0, filePath.lastIndexOf('.')).endsWith("_clean");
        return (clean && !isCleanFile) || (!clean && isCleanFile);
    }

    protected void createCSV(String[] columns) {
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

    protected String getFileName() {
        String extension = ".csv";
        if (clean) extension = "_clean" + extension;
        return "src/Preprocessing/Analysis/CSV/" + analysisFileName + extension;
    }

    public abstract void analyse(String filePath) throws IOException;
}
