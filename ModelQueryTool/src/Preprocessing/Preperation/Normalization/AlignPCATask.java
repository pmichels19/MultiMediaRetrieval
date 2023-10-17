package Preprocessing.Preperation.Normalization;

import Basics.Helpers;
import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class AlignPCATask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        Vec3f[] vertices = context.getVertices();
        RealMatrix covarianceMatrix = Helpers.getCovarianceMatrix(vertices);

        EigenDecomposition decomposition = new EigenDecomposition(covarianceMatrix);
        RealMatrix d = decomposition.getV();
        Vec3f eigx = new Vec3f(
                (float) d.getEntry(0, 0),
                (float) d.getEntry(1, 0),
                (float) d.getEntry(2, 0)
        ).normalize();
        Vec3f eigy = new Vec3f(
                (float) d.getEntry(0, 1),
                (float) d.getEntry(1, 1),
                (float) d.getEntry(2, 1)
        ).normalize();
        Vec3f eigz = new Vec3f().cross(eigx, eigy).normalize();

        // Using projection, update the vertex coordinates to align with the eigen vectors
        for (Vec3f vertex : vertices) {
            float newX = vertex.dot(eigx);
            float newY = vertex.dot(eigy);
            float newZ = vertex.dot(eigz);
            vertex.setX(newX);
            vertex.setY(newY);
            vertex.setZ(newZ);
        }
    }

    @Override
    public String getDescription() {
        return "Aligned PCA with world axis.";
    }
}
