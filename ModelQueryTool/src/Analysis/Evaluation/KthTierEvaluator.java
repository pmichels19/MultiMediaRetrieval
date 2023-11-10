package Analysis.Evaluation;

import Analysis.EvaluationPipelineContext;

import java.util.List;

public class KthTierEvaluator implements Evaluator {
    @Override
    public void evaluateResult(EvaluationPipelineContext context) {
        List<String> matches = context.getMatchedLabels();
        String label = matches.get(0);
        float k = (matches.lastIndexOf(label) + 1.0f) / (float) context.getClassSize();

        context.addMetric("kth", k);
    }
}
