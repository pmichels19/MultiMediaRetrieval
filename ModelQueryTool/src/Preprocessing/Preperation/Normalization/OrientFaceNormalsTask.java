package Preprocessing.Preperation.Normalization;

import Basics.PythonScriptExecutor;
import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.List;

public class OrientFaceNormalsTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        int[][] faces = context.getFaces();
        Vec3f[] vertices = context.getVertices();

        PythonScriptExecutor executor = new PythonScriptExecutor();
        List<String> output = executor.executeMeshScript(vertices, faces, PythonScriptExecutor.REORIENT_NORMALS);
        for (int i = 0; i < faces.length; i++) {
            String[] parts = output.get(i).split("\\s+");
            faces[i][0] = Integer.parseInt(parts[0]);
            faces[i][1] = Integer.parseInt(parts[1]);
            faces[i][2] = Integer.parseInt(parts[2]);
        }
    }

    @Override
    public String getDescription() {
        return "Orient normals into a consistent direction.";
    }
}
