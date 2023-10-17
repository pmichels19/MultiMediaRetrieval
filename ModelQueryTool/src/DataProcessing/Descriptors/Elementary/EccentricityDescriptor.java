package DataProcessing.Descriptors.Elementary;

import Basics.Helpers;
import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Vec3f;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class EccentricityDescriptor extends ElementaryDescriptor {
    @Override
    protected float calculateDescriptor(FeaturePipelineContext context) {
        Vec3f[] vertices = context.getMesh().getVertices();

        RealMatrix covariance = Helpers.getCovarianceMatrix(vertices);
        EigenDecomposition decomposition = new EigenDecomposition(covariance);
        return (float) decomposition.getRealEigenvalue(0) / (float) decomposition.getRealEigenvalue(1);
    }

    @Override
    public String getKey() {
        return "eccentricity";
    }
}
