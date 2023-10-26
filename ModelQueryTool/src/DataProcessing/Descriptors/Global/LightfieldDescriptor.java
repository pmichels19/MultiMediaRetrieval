package DataProcessing.Descriptors.Global;

import Basics.Mesh;
import DataProcessing.Descriptors.Descriptor;
import DataProcessing.FeaturePipelineContext;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.Vec2i;
import com.jogamp.opengl.math.Vec3f;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LightfieldDescriptor implements Descriptor {
    private static final float[][] rotations = new float[][] {
            new float[] {0.0f, 0.0f, 0.0f},

            new float[] {0.2f, 0.0f, 0.0f},
            new float[] {0.4f, 0.0f, 0.0f},
            new float[] {0.6f, 0.0f, 0.0f},
            new float[] {0.8f, 0.0f, 0.0f},

            new float[] {0.0f, 0.2f, 0.0f},
            new float[] {0.0f, 0.4f, 0.0f},
            new float[] {0.0f, 0.6f, 0.0f},
            new float[] {0.0f, 0.8f, 0.0f},

            new float[] {0.0f, 0.0f, 0.2f},
            new float[] {0.0f, 0.0f, 0.4f},
            new float[] {0.0f, 0.0f, 0.6f},
            new float[] {0.0f, 0.0f, 0.8f},
    };

    private static final int SIZE = 1000;

    private static final float SIZEF = (float) SIZE;

    private static final Color fillColor = Color.WHITE;

    @Override
    public void process(FeaturePipelineContext context) {
        Mesh mesh = context.getMesh();
        for (float[] rotation : rotations) {
            BufferedImage image = mapShapeTo2D(getAdjustedVertices(mesh.getVertices(), rotation), mesh.getFaces());

            float n = 0.0f;
            float step = ((float) BIN_COUNT) / SIZEF;
            float[] blackPixelsX = new float[BIN_COUNT];
            float[] blackPixelsY = new float[BIN_COUNT];
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    int pixel = image.getRGB(x, y);
                    if (pixel != fillColor.hashCode()) continue;

                    n++;
                    int binIdx = Math.min(BIN_COUNT - 1, (int) (((float) x) * step));
                    blackPixelsX[binIdx]++;
                    binIdx = Math.min(BIN_COUNT - 1, (int) (((float) y) * step));
                    blackPixelsY[binIdx]++;
                }
            }

            for (int i = 0; i < BIN_COUNT; i++) {
                blackPixelsX[i] = blackPixelsX[i] / n;
                blackPixelsY[i] = blackPixelsY[i] / n;
            }

            String keyExtension = "_" + rotation[0] + "_" + rotation[1] + "_" + rotation[2];
            context.putData(getKey() + keyExtension + "_x", blackPixelsX);
            context.putData(getKey() + keyExtension + "_y", blackPixelsY);
        }
    }

    @Override
    public String getKey() {
        return "lightfield";
    }

    private Vec2i[] getAdjustedVertices(Vec3f[] vertices, float[] rotation) {
        float maxx, minx, maxy, miny;
        maxx = maxy = -Float.MAX_VALUE;
        minx = miny = Float.MAX_VALUE;
        for (Vec3f vertex : vertices) {
            if (vertex.x() > maxx) maxx = vertex.x();
            if (vertex.x() < minx) minx = vertex.x();
            if (vertex.y() > maxy) maxy = vertex.y();
            if (vertex.y() < miny) miny = vertex.y();
        }

        float xScale = maxx - minx;
        float yScale = maxy - miny;
        float scale = Math.max(xScale, yScale);
        float xAdjust = (scale - xScale) * 0.5f;
        float yAdjust = (scale - yScale) * 0.5f;

        Quaternion quaternion = new Quaternion();
        quaternion.rotateByAngleX(rotation[0] * (float) Math.PI);
        quaternion.rotateByAngleY(rotation[1] * (float) Math.PI);
        quaternion.rotateByAngleZ(rotation[2] * (float) Math.PI);

        Vec2i[] scaledVertices = new Vec2i[vertices.length];
        for (int i = 0; i < scaledVertices.length; i++) {
            Vec3f vertex = new Vec3f();
            quaternion.rotateVector(vertices[i], vertex);

            int scaledX = (int) (SIZEF * ((vertex.x() - minx) / scale + xAdjust));
            int scaledY = (int) (SIZEF * ((vertex.y() - miny) / scale + yAdjust));
            scaledVertices[i] = new Vec2i(scaledX, scaledY);
        }

        return scaledVertices;
    }

    private BufferedImage mapShapeTo2D(Vec2i[] vertices, int[][] faces) {
        BufferedImage bufferedImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D image = bufferedImage.createGraphics();

        image.setPaint(fillColor);
        for (int[] face : faces) {
            int[] xs = new int[3];
            int[] ys = new int[3];
            for (int i = 0; i < 3; i++) {
                xs[i] = vertices[face[i]].x();
                ys[i] = vertices[face[i]].y();
            }

            image.fillPolygon(xs, ys, 3);
        }

        return bufferedImage;
    }
}
