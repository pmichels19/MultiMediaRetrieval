package Preprocessing.Preperation.Normalization;

import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

public class BarycenterToOriginTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        Vec3f[] vertices = context.getVertices();
        Vec3f baryCenter = getBarycenter(vertices);
        for (Vec3f vertex : vertices) vertex.sub(baryCenter);
    }

    @Override
    public String getDescription() {
        return "Set the barycenter to the world origin.";
    }

    private Vec3f getBarycenter(Vec3f[] vertices) {
        Vec3f result = new Vec3f();
        for (Vec3f vertex : vertices) result.add(vertex);
        return result.scale(1.0f / (float) vertices.length);
    }
}
