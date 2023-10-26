package Rendering;

import Basics.Mesh;
import Querying.FileQueryResult;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;

public class MeshRenderer extends JFrame implements GLEventListener {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    // Fields used for shaders
    private int vaoId;
    private int viewMatrixLocation;
    private int projectionMatrixLocation;
    private int wireframeLocation;

    // Mesh data to be passed to shaders
    private int meshSize;
    private int[] indices;
    private float[] vertices;
    private float[] vertexNormals;

    // KeyListener used to have controls
    private final Controls controls;

    // Perspective matrix
    private final float[] PERSPECTIVE_MATRIX;

    private final GLCanvas rendererCanvas;

    // The renderer instance
    private static MeshRenderer renderer;

    public static MeshRenderer getInstance() {
        if (renderer == null) renderer = new MeshRenderer();
        return renderer;
    }

    private MeshRenderer() {
        super("ModelQueryTool Renderer");

        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
        rendererCanvas = new GLCanvas(capabilities);
        rendererCanvas.addGLEventListener(this);

        // Perspective matrix stuff, needed in shaders
        float fieldOfView = (float) Math.toRadians(45.0);
        float aspectRatio = (float) WIDTH / (float) HEIGHT;
        float near = 0.1f;
        float far = 100.0f;
        float f = 1.0f / (float) Math.tan(fieldOfView / 2.0);
        float zRange = near - far;
        PERSPECTIVE_MATRIX = new float[16];
        PERSPECTIVE_MATRIX[0] = f / aspectRatio;
        PERSPECTIVE_MATRIX[5] = f;
        PERSPECTIVE_MATRIX[10] = (far + near) / zRange;
        PERSPECTIVE_MATRIX[11] = -1.0f;
        PERSPECTIVE_MATRIX[14] = (2.0f * far * near) / zRange;

        // Add the controls to the canvas
        controls = new Controls();
        rendererCanvas.addKeyListener(controls);

        this.setTitle("ModelQueryTool Renderer");
        this.getContentPane().add(rendererCanvas);

        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
    }

    public void startRenderer() {
        if (!controls.canStart()) {
            System.err.println("Add meshes to the renderer before starting the renderer. Aborting start procedure.");
            return;
        }

        this.setVisible(true);
        rendererCanvas.requestFocusInWindow();

        FPSAnimator animator = new FPSAnimator(rendererCanvas, 60);
        animator.start();
    }

    public void add(Mesh mesh) {
        controls.addMesh(mesh);
    }

    public void addAll(Collection<Mesh> meshes) {
        controls.addMeshes(meshes);
    }

    public void addQueryResults(FileQueryResult result) {
        controls.addQueryResult(result);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL4 gl4 = glAutoDrawable.getGL().getGL4();
        gl4.glPointSize(2.0f);
        gl4.glLineWidth(2.0f);
        gl4.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        gl4.glEnable(GL4.GL_DEPTH_TEST);
        gl4.glEnable(GL4.GL_POLYGON_OFFSET_FILL);
        gl4.glPolygonOffset(1.0f, 2.0f);

        gl4.glDisable(GL4.GL_CULL_FACE);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl4 = glAutoDrawable.getGL().getGL4();
        if (controls.shouldExit()) {
            this.dispose();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }

        if (controls.meshChanged()) {
            loadMesh();
            useShader(gl4, controls.getShadingMode().getDirectory());
            controls.updatedMesh();
        }

        if (controls.shadingModeChanged()) {
            useShader(gl4, controls.getShadingMode().getDirectory());
            controls.updatedShadingMode();
        }

        gl4.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        float[] matrixBuffer = controls.getCameraMatrix().get(new float[16]);
        gl4.glUniformMatrix4fv(viewMatrixLocation, 1, false, matrixBuffer, 0);
        gl4.glUniformMatrix4fv(projectionMatrixLocation, 1, false, PERSPECTIVE_MATRIX, 0);

        int count = 3 * meshSize;
        DrawingMode drawingMode = controls.getDrawingMode();
        gl4.glBindVertexArray(vaoId);
        switch (drawingMode) {
            case TRIANGLES -> {
                gl4.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);
                gl4.glDrawElements(GL.GL_TRIANGLES, count, GL4.GL_UNSIGNED_INT, 0);
            }
            case VERTICES -> {
                gl4.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);
                gl4.glDrawElements(GL.GL_POINTS, count, GL4.GL_UNSIGNED_INT, 0);
            }
            case WIREFRAME -> {
                gl4.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);
                gl4.glDrawElements(GL.GL_TRIANGLES, count, GL4.GL_UNSIGNED_INT, 0);
                gl4.glUniform1i(wireframeLocation, 1);
                gl4.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);
                gl4.glDrawElements(GL.GL_TRIANGLES, count, GL4.GL_UNSIGNED_INT, 0);
                gl4.glUniform1i(wireframeLocation, 0);
            }
        }

        if (controls.takeScreenshot()) {
            saveScreen();
            controls.tookScreenshot();
        }

        gl4.glBindVertexArray(0);
        gl4.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
    }

    private void loadMesh() {
        Mesh mesh = controls.getMesh();
        meshSize = mesh.size();
        indices = mesh.getFacesBuffer();
        vertices = mesh.getVertexBuffer();
        vertexNormals = mesh.getVertexNormalBuffer();

        this.setTitle("ModelQueryTool Renderer - " + mesh.getName() + " (" + controls.getMeshIdx() + "/" + controls.getMeshCount() + ")\t\t" + controls.getDistance());
    }

    private void useShader(GL4 gl4, String type) {
        // Load shaders
        int vShader;
        int fShader;
        try {
            vShader = loadShader(gl4, GL4.GL_VERTEX_SHADER, type + "/vertexShader.glsl");
            fShader = loadShader(gl4, GL4.GL_FRAGMENT_SHADER, type + "/fragmentShader.glsl");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Create shader program
        int program = gl4.glCreateProgram();
        gl4.glAttachShader(program, vShader);
        gl4.glAttachShader(program, fShader);
        gl4.glLinkProgram(program);
        gl4.glUseProgram(program);

        // Get the location of the camera matrix uniform variable
        viewMatrixLocation = gl4.glGetUniformLocation(program, "viewMatrix");
        projectionMatrixLocation = gl4.glGetUniformLocation(program, "projectionMatrix");
        wireframeLocation = gl4.glGetUniformLocation(program, "wireframe");

        // Clean up the shaders after linking them
        gl4.glDeleteShader(vShader);
        gl4.glDeleteShader(fShader);

        // Create and bind Vertex Array Object (VAO)
        int[] vaoIds = new int[1];
        gl4.glGenVertexArrays(1, vaoIds, 0);
        vaoId = vaoIds[0];
        gl4.glBindVertexArray(vaoId);

        // Create and bind Vertex Buffer Object (VBO)
        bindFloatBuffer(gl4, vertices, "inPosition", program);
        bindFloatBuffer(gl4, vertexNormals, "inNormal", program);

        // Create and bind Element Buffer Object (EBO)
        int[] eboIds = new int[1];
        gl4.glGenBuffers(1, eboIds, 0);
        int eboId = eboIds[0];
        gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, eboId);

        IntBuffer indexBuffer = IntBuffer.wrap(indices);
        gl4.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, (long) indices.length * Integer.BYTES, indexBuffer, GL4.GL_STATIC_DRAW);
    }

    void bindFloatBuffer(GL4 gl4, float[] items, String location, int program) {
        int[] vboIds = new int[1];
        gl4.glGenBuffers(1, vboIds, 0);
        int vboId = vboIds[0];
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboId);

        FloatBuffer vertexBuffer = FloatBuffer.wrap(items);
        gl4.glBufferData(GL4.GL_ARRAY_BUFFER, (long) items.length * Float.BYTES, vertexBuffer, GL4.GL_STATIC_DRAW);

        int attribLocation = gl4.glGetAttribLocation(program, location);;
        gl4.glEnableVertexAttribArray(attribLocation);
        gl4.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId);
        gl4.glVertexAttribPointer(attribLocation, 3, GL4.GL_FLOAT, false, 0, 0);
    }

    private int loadShader(GL4 gl4, int shaderType, String fileName) throws IOException {
        int shader = gl4.glCreateShader(shaderType);

        // Read the text from a shader source
        BufferedReader shaderReader = new BufferedReader( new FileReader("src/Rendering/Shaders/" + fileName) );
        StringBuilder shaderBuilder = new StringBuilder();
        String line;
        while (true) {
            line = shaderReader.readLine();
            if (line == null) break;

            shaderBuilder.append(line).append("\n");
        }

        // Load and compile the shader
        String shaderText = shaderBuilder.toString();
        gl4.glShaderSource(shader, 1, new String[] {shaderText}, null, 0);
        gl4.glCompileShader(shader);

        // Check for compilation errors
        int[] success = new int[1];
        gl4.glGetShaderiv(shader, GL4.GL_COMPILE_STATUS, success, 0);
        if (success[0] == GL4.GL_FALSE) {
            System.err.println("Shader compilation failed: " + fileName);
            String infoLog = getShaderInfoLog(gl4, shader);
            System.err.println(infoLog);
            gl4.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private String getShaderInfoLog(GL4 gl4, int shader) {
        int[] logLength = new int[1];
        gl4.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, logLength, 0);
        byte[] logBytes = new byte[logLength[0]];
        gl4.glGetShaderInfoLog(shader, logLength[0], (int[])null, 0, logBytes, 0);
        return new String(logBytes);
    }

    private void saveScreen() {
        try {
            Robot robot = new Robot();

            Rectangle bounds = new Rectangle(rendererCanvas.getLocationOnScreen(), rendererCanvas.getSize());
            BufferedImage screenshot = robot.createScreenCapture(bounds);

            String meshName = controls.getMesh().getName();
            String fileName = "screenshot_" + meshName.substring(0, meshName.lastIndexOf('.'));
            ImageIO.write(screenshot, "PNG", new File("figures\\" + fileName + ".png"));
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }
}
