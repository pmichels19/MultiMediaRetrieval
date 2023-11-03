package Analysis.Evaluation;

import Analysis.EvaluationPipelineContext;
import Querying.FileQueryResult;

import java.util.List;

public interface Evaluator {
    void evaluateResult(EvaluationPipelineContext context);
}
