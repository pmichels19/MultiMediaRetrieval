package Preprocessing.Preperation.Normalization;

import Basics.Helpers;
import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

public class ScaleToUnitCubeTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        Vec3f[] vertices = context.getVertices();
        float[] ext = Helpers.getMinMaxCoordinates(vertices);

        float scale = 1.0f / Math.max(Math.max(ext[1] - ext[0], ext[3] - ext[2]), ext[5] - ext[4]);
        for (Vec3f vertex : vertices) vertex.scale(scale);
    }

    @Override
    public String getDescription() {
        return "Scale vertices into a unit cube.";
    }
}
