package Rendering;

import Basics.Mesh;
import Querying.FileQueryResult;
import com.jogamp.opengl.math.Matrix4f;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.Vec3f;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Controls implements KeyListener {
    // Camera matrix
    private Matrix4f camera;
    private Vec3f position;
    private Quaternion rotation;
    private int drawingMode;
    private boolean exit;
    private int shadingMode;
    private boolean shadingModeChanged;
    private int meshIdx;
    private boolean meshChanged;
    private final List<Mesh> meshes;
    private final List<Float> meshDistances;
    private boolean takeScreenshot;
    private boolean screenshotPressed;

    // Translation and rotation that is applied when a key is pressed for a single frame
    private final float dt = 0.0125f;
    private final float dr = dt * (float) Math.PI;

    private float dx, dy, dz;
    private float rx, ry, rz;
    private final boolean[] pressed;

    Controls() {
        resetCamera();

        drawingMode = 0;

        shadingMode = 0;
        shadingModeChanged = true;

        exit = false;

        pressed = new boolean[12];

        meshIdx = 0;
        meshes = new ArrayList<>();
        meshDistances = new ArrayList<>();
        meshChanged = true;

        takeScreenshot = false;
        screenshotPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> {
                if (!pressed[0]) {
                    dx += dt;
                    pressed[0] = true;
                }
            }
            case KeyEvent.VK_D -> {
                if (!pressed[1]) {
                    dx -= dt;
                    pressed[1] = true;
                }
            }
            case KeyEvent.VK_CONTROL -> {
                if (!pressed[2]) {
                    dy += dt;
                    pressed[2] = true;
                }
            }
            case KeyEvent.VK_SPACE -> {
                if (!pressed[3]) {
                    dy -= dt;
                    pressed[3] = true;
                }
            }
            case KeyEvent.VK_W -> {
                if (!pressed[4]) {
                    dz += dt;
                    pressed[4] = true;
                }
            }
            case KeyEvent.VK_S -> {
                if (!pressed[5]) {
                    dz -= dt;
                    pressed[5] = true;
                }
            }
            case KeyEvent.VK_UP -> {
                if (!pressed[6]) {
                    rx += dr;
                    pressed[6] = true;
                }
            }
            case KeyEvent.VK_DOWN -> {
                if (!pressed[7]) {
                    rx -= dr;
                    pressed[7] = true;
                }
            }
            case KeyEvent.VK_RIGHT -> {
                if (!pressed[8]) {
                    ry += dr;
                    pressed[8] = true;
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (!pressed[9]) {
                    ry -= dr;
                    pressed[9] = true;
                }
            }
            case KeyEvent.VK_E -> {
                if (!pressed[10]) {
                    rz += dr;
                    pressed[10] = true;
                }
            }
            case KeyEvent.VK_Q -> {
                if (!pressed[11]) {
                    rz -= dr;
                    pressed[11] = true;
                }
            }
            case KeyEvent.VK_N -> {
                meshIdx = (meshIdx + 1) % meshes.size();
                meshChanged = true;
                if (meshes.size() > 1) resetCamera();
            }
            case KeyEvent.VK_B -> {
                meshIdx = (meshIdx - 1 + meshes.size()) % meshes.size();
                meshChanged = true;
                if (meshes.size() > 1) resetCamera();
            }
            case KeyEvent.VK_P -> {
                if (!screenshotPressed) {
                    takeScreenshot = true;
                    screenshotPressed = true;
                }
            }
            case KeyEvent.VK_Z -> drawingMode = (drawingMode + 1) % DrawingMode.values().length;
            case KeyEvent.VK_X -> {
                shadingMode = (shadingMode + 1) % ShadingMode.values().length;
                shadingModeChanged = true;
            }
            case KeyEvent.VK_ESCAPE -> exit = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> {
                if (pressed[0]) {
                    dx -= dt;
                    pressed[0] = false;
                }
            }
            case KeyEvent.VK_D -> {
                if (pressed[1]) {
                    dx += dt;
                    pressed[1] = false;
                }
            }
            case KeyEvent.VK_CONTROL -> {
                if (pressed[2]) {
                    dy -= dt;
                    pressed[2] = false;
                }
            }
            case KeyEvent.VK_SPACE -> {
                if (pressed[3]) {
                    dy += dt;
                    pressed[3] = false;
                }
            }
            case KeyEvent.VK_W -> {
                if (pressed[4]) {
                    dz -= dt;
                    pressed[4] = false;
                }
            }
            case KeyEvent.VK_S -> {
                if (pressed[5]) {
                    dz += dt;
                    pressed[5] = false;
                }
            }
            case KeyEvent.VK_UP -> {
                if (pressed[6]) {
                    rx -= dr;
                    pressed[6] = false;
                }
            }
            case KeyEvent.VK_DOWN -> {
                if (pressed[7]) {
                    rx += dr;
                    pressed[7] = false;
                }
            }
            case KeyEvent.VK_RIGHT -> {
                if (pressed[8]) {
                    ry -= dr;
                    pressed[8] = false;
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (pressed[9]) {
                    ry += dr;
                    pressed[9] = false;
                }
            }
            case KeyEvent.VK_E -> {
                if (pressed[10]) {
                    rz -= dr;
                    pressed[10] = false;
                }
            }
            case KeyEvent.VK_Q -> {
                if (pressed[11]) {
                    rz += dr;
                    pressed[11] = false;
                }
            }
            case KeyEvent.VK_P -> {
                if (screenshotPressed) screenshotPressed = false;
            }
        }
    }

    private void resetCamera() {
        position = new Vec3f(0, 0, 1.5f);
        rotation = new Quaternion();
        camera = new Matrix4f().setToTranslation(position);
    }

    private void updateCamera() {
        Quaternion dR = new Quaternion();
        dR.rotateByAngleX(rx);
        dR.rotateByAngleY(ry);
        dR.rotateByAngleZ(rz);

        Vec3f dP = new Vec3f(dx, dy, dz);

        rotation.mult(dR);
        position.add(dP);

        Matrix4f tMatrix = new Matrix4f().setToTranslation(position);
        Matrix4f rMatrix = rotation.toMatrix(new Matrix4f());
        camera = rMatrix.mul(tMatrix);
    }

    Matrix4f getCameraMatrix() {
        updateCamera();
        Matrix4f copy = new Matrix4f(camera);
        if (!copy.invert()) {
            throw new ArithmeticException("Failed to invert camera matrix.");
        }

        return copy;
    }

    DrawingMode getDrawingMode() {
        return DrawingMode.values()[drawingMode];
    }

    ShadingMode getShadingMode() {
        return ShadingMode.values()[shadingMode];
    }

    Mesh getMesh() {
        return meshes.get(meshIdx);
    }

    int getMeshIdx() {
        return meshIdx + 1;
    }

    int getMeshCount() {
        return meshes.size();
    }

    String getDistance() {
        return String.format("%.4f", meshDistances.get(meshIdx));
    }

    boolean meshChanged() {
        return meshChanged;
    }

    boolean shadingModeChanged() {
        return shadingModeChanged;
    }

    boolean shouldExit() {
        return exit;
    }

    boolean canStart() {
        return meshes.size() > 0;
    }

    public boolean takeScreenshot() {
        return takeScreenshot;
    }

    public void tookScreenshot() {
        takeScreenshot = false;
    }

    void updatedMesh() {
        meshChanged = false;
    }

    void updatedShadingMode() {
        shadingModeChanged = false;
    }

    void addMesh(Mesh mesh) {
        if (meshDistances.size() > 0) return;
        meshes.add(mesh);
    }

    void addMeshes(Collection<Mesh> meshCollection) {
        if (meshDistances.size() > 0) return;
        meshes.addAll(meshCollection);
    }

    void addQueryResult(FileQueryResult result) {
        List<Mesh> resultMeshes = result.getMeshes();
        List<Float> resultDistances = result.getDistances();
        if (resultMeshes.size() != resultDistances.size()) throw new IllegalStateException("Mismatch between distances and meshes.");
        meshes.clear();
        meshDistances.clear();
        meshes.addAll(result.getMeshes());
        meshDistances.addAll(result.getDistances());
    }
}
