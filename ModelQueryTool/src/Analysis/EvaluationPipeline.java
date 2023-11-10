package Analysis;

import Analysis.Evaluation.Evaluator;
import Analysis.Evaluation.KthTierEvaluator;
import Analysis.Evaluation.SensitivitySpecificityEvaluator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class EvaluationPipeline {
    private static final Evaluator[] evaluators = new Evaluator[] {
            new KthTierEvaluator(),
            new SensitivitySpecificityEvaluator(),
    };

    private static EvaluationPipeline evaluationPipeline;

    private EvaluationPipeline() {
    }

    public static EvaluationPipeline getInstance() {
        if (evaluationPipeline == null) evaluationPipeline = new EvaluationPipeline();
        return evaluationPipeline;
    }

    public void run() {
        List<String> meshFiles = null;
        try (Stream<Path> pathStream = Files.find(Paths.get("Shapes"), 3, (this::shouldProcess))) {
            meshFiles = pathStream.map(Path::toAbsolutePath).map(Path::toString).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert meshFiles != null;
        EvaluationPipelineContext context = new EvaluationPipelineContext();
        for (String filePath : meshFiles) {
            context.prepareForFile(filePath);
            for (Evaluator evaluator : evaluators) evaluator.evaluateResult(context);
        }

        context.saveToCsv();
    }

    private boolean shouldProcess(Path path, BasicFileAttributes attributes) {
        String regex = ".*_clean\\.(obj|off|ply)";
        String fileName = path.getFileName().toString();
        String jsonFile = path.toString().replaceAll(".(obj|off|ply)", ".json");
        return attributes.isRegularFile() && fileName.matches(regex) && (new File(jsonFile)).isFile();
    }
}
