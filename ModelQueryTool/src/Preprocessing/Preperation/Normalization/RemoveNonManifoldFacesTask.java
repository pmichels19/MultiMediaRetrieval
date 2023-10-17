package Preprocessing.Preperation.Normalization;

import Basics.PythonScriptExecutor;
import Preprocessing.Preperation.PreparationTask;
import Preprocessing.PreperationPipelineContext;
import com.jogamp.opengl.math.Vec3f;

import java.util.List;

public class RemoveNonManifoldFacesTask implements PreparationTask {
    @Override
    public void execute(PreperationPipelineContext context) {
        int[][] faces = context.getFaces();
        Vec3f[] vertices = context.getVertices();

        PythonScriptExecutor executor = new PythonScriptExecutor();
        List<String> output = executor.executeMeshScript(vertices, faces, PythonScriptExecutor.REMOVE_NON_MANIFOLD);

        int idx = 0;
        int numV = Integer.parseInt(output.get(idx));
        idx++;
        Vec3f[] newVertices = new Vec3f[numV];
        for (int i = 0; i < numV; i++, idx++) {
            String[] parts = output.get(idx).split("\\s+");
            float x, y, z;
            x = Float.parseFloat(parts[0]);
            y = Float.parseFloat(parts[1]);
            z = Float.parseFloat(parts[2]);
            newVertices[i] = new Vec3f(x, y, z);
        }

        int numF = Integer.parseInt(output.get(idx));
        idx++;
        int[][] newFaces = new int[numF][3];
        for (int i = 0; i < numF; i++, idx++) {
            String[] parts = output.get(idx).split("\\s+");
            int v0, v1, v2;
            v0 = Integer.parseInt(parts[0]);
            v1 = Integer.parseInt(parts[1]);
            v2 = Integer.parseInt(parts[2]);
            newFaces[i] = new int[] { v0, v1, v2 };
        }

        context.setFaces(newFaces);
        context.setVertices(newVertices);
    }

    @Override
    public String getDescription() {
        return "Removes any non-manifold faces.";
    }
}
