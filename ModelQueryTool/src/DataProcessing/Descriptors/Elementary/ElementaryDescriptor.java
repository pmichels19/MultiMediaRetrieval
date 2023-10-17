package DataProcessing.Descriptors.Elementary;

import DataProcessing.Descriptors.Descriptor;
import DataProcessing.FeaturePipelineContext;

public abstract class ElementaryDescriptor implements Descriptor {
    @Override
    public void process(FeaturePipelineContext context) {
        context.putData(getKey(), calculateDescriptor(context));
    }

    protected abstract float calculateDescriptor(FeaturePipelineContext context);
}
