package Analysis.Evaluation;

import Analysis.EvaluationPipelineContext;

import java.util.List;

public class SensitivitySpecificityEvaluator implements Evaluator {
    @Override
    public void evaluateResult(EvaluationPipelineContext context) {
        int c = context.getClassSize();
        List<String> matches = context.getMatchedLabels();
        String label = matches.get(0);

        int tp = 0;
        for (int i = 0; i < c; i++) {
            if (matches.get(i).equals(label)) tp++;
        }

        float sensitivity = ((float) tp) / ((float) c);

        int tn = 0;
        int d = context.getDbSize();
        for (int i = c; i < d; i++) {
            if (!matches.get(i).equals(label)) tn++;
        }

        float specificity = ((float) tn) / ((float) (d - c));

        context.addMetric("sensitivity", sensitivity);
        context.addMetric("specificity", specificity);
    }
}
