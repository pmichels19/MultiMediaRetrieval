package Analysis.Evaluation;

import Analysis.EvaluationPipelineContext;

import java.util.List;

public class LastRankEvaluator implements Evaluator {
    @Override
    public void evaluateResult(EvaluationPipelineContext context) {
        List<String> matches = context.getMatchedLabels();
        String label = matches.get(0);
        int lastRank = matches.lastIndexOf(label) + 1;

        context.addMetric("LR", lastRank);
    }
}
