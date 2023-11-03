package Analysis;

import Analysis.AnalysisTasks.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

public class AnalysisPipeline {
    private static final AnalysisTask[] analysisTasks = new AnalysisTask[] {
            new GeneralAnalysisTask(),
            new AreaAnalysisTask(),
            new AlignmentAnalysisTask(),
            new FlippingTestTask(),
    };

    private static AnalysisPipeline analysisPipeline;

    private AnalysisPipeline() {
    }

    public static AnalysisPipeline getInstance() {
        if (analysisPipeline == null) analysisPipeline = new AnalysisPipeline();
        return analysisPipeline;
    }

    public void run(boolean clean) {
        AnalysisTask.setCleanMode(clean);

        List<String> meshFiles = null;
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, ((path, attributes) -> shouldProcess(path, attributes, clean)))) {
            meshFiles = pathStream.map(Path::toString).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert meshFiles != null;
        for (AnalysisTask analysisTask : analysisTasks) {
            System.out.println("===== " + analysisTask.getClass().getName() + " =====");
            analysisTask.clearCSV();

            for (int i = 0; i < meshFiles.size(); i++) {
                System.out.print((i + 1) + "/" + meshFiles.size());
                try {
                    analysisTask.process(meshFiles.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.print("\r");
            }

            System.out.println();
        }
    }

    private boolean shouldProcess(Path path, BasicFileAttributes attributes, boolean clean) {
        String regex = "^(?!.*_clean).*\\.(obj|off|ply)";
        if (clean) regex = ".*_clean\\.(obj|off|ply)";
        return attributes.isRegularFile() && path.getFileName().toString().matches(regex);
    }
}
