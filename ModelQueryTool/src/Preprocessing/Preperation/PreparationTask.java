package Preprocessing.Preperation;

import Preprocessing.PreperationPipelineContext;

public interface PreparationTask {
    void execute(PreperationPipelineContext context);

    String getDescription();
}
