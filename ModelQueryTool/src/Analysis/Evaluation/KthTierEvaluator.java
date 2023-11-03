package Analysis.Evaluation;

import Analysis.EvaluationPipelineContext;

import java.util.List;

public class KthTierEvaluator implements Evaluator {
    @Override
    public void evaluateResult(EvaluationPipelineContext context) {
        List<String> matches = context.getMatchedLabels();
        String label = matches.get(0);
        int lastRank = matches.lastIndexOf(label) + 1;
        int k = Math.ceilDiv(lastRank, context.getClassSize());

        context.addMetric("kth", k);
    }
}
