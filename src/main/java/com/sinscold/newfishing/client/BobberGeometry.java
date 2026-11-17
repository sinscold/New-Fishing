package com.sinscold.newfishing.client;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Геометрия поплавка из Blockbench (models-src/fishing_hook.bbmodel): кубик 3×3×3 и две плоские пластины.
 * Координаты в пикселях (1/16 блока), текстура 16×16. Грани и обход вершин — как в Java-моделях блоков.
 */
public final class BobberGeometry {
    private static final float TEX = 16.0F;

    private record Quad(float[][] pos, float[][] uv, float[] normal) {}

    private static final List<Quad> QUADS = new ArrayList<>();

    static {
        // кубик: (-1.5,0,-1.5)…(1.5,3,1.5)
        cube(-1.5F, 0, -1.5F, 1.5F, 3, 1.5F, 0, 0, 0, 0,
                new float[]{6, 3, 3, 6}, new float[]{9, 3, 6, 6}, new float[]{12, 3, 9, 6},
                new float[]{3, 3, 0, 6}, new float[]{3, 3, 6, 0}, new float[]{6, 0, 9, 3});
        // пластина-«парус»: x = 0, y от -3 до 4, z от -2.5 до 0.5
        cube(0, -3, -2.5F, 0, 4, 0.5F, 0, 0, 0, 0,
                null, new float[]{0, 6, 3, 13}, null, new float[]{3, 6, 6, 13}, null, null);
        // маленькая пластина, повёрнутая на 90° вокруг оси Y через точку (-0.5, 2, 0)
        cube(-0.5F, 2, 0, -0.5F, 4, 1, 90, -0.5F, 2, 0,
                null, new float[]{0, 0, 1, 2}, null, new float[]{1, 0, 2, 2}, null, null);
    }

    private BobberGeometry() {}

    private static void cube(float x1, float y1, float z1, float x2, float y2, float z2, float rotY, float ox, float oy, float oz,
                             float[] north, float[] east, float[] south, float[] west, float[] up, float[] down) {
        if (north != null) face(new float[][]{{x2, y2, z1}, {x2, y1, z1}, {x1, y1, z1}, {x1, y2, z1}}, north, 0, 0, -1, rotY, ox, oy, oz);
        if (south != null) face(new float[][]{{x1, y2, z2}, {x1, y1, z2}, {x2, y1, z2}, {x2, y2, z2}}, south, 0, 0, 1, rotY, ox, oy, oz);
        if (east != null) face(new float[][]{{x2, y2, z2}, {x2, y1, z2}, {x2, y1, z1}, {x2, y2, z1}}, east, 1, 0, 0, rotY, ox, oy, oz);
        if (west != null) face(new float[][]{{x1, y2, z1}, {x1, y1, z1}, {x1, y1, z2}, {x1, y2, z2}}, west, -1, 0, 0, rotY, ox, oy, oz);
        if (up != null) face(new float[][]{{x1, y2, z1}, {x1, y2, z2}, {x2, y2, z2}, {x2, y2, z1}}, up, 0, 1, 0, rotY, ox, oy, oz);
        if (down != null) face(new float[][]{{x1, y1, z2}, {x1, y1, z1}, {x2, y1, z1}, {x2, y1, z2}}, down, 0, -1, 0, rotY, ox, oy, oz);
    }

    /** Поворот вокруг оси Y на rotY градусов: x' = x·cos + z·sin, z' = −x·sin + z·cos (относительно точки поворота). */
    private static float[] rotate(float x, float y, float z, float rotY, float ox, float oy, float oz) {
        if (rotY == 0) return new float[]{x, y, z};
        double a = Math.toRadians(rotY);
        float rx = x - ox;
        float rz = z - oz;
        return new float[]{(float) (rx * Math.cos(a) + rz * Math.sin(a)) + ox, y, (float) (-rx * Math.sin(a) + rz * Math.cos(a)) + oz};
    }

    private static void face(float[][] corners, float[] uv, float nx, float ny, float nz, float rotY, float ox, float oy, float oz) {
        float[][] pos = new float[4][];
        for (int i = 0; i < 4; i++) {
            pos[i] = rotate(corners[i][0], corners[i][1], corners[i][2], rotY, ox, oy, oz);
        }
        float[] n = rotate(nx, ny, nz, rotY, 0, 0, 0);
        float[][] uvs = {{uv[0], uv[1]}, {uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]}};
        QUADS.add(new Quad(pos, uvs, n));
    }

    /** Рисует поплавок в начале координат poseStack (центр кубика по x/z). */
    public static void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        PoseStack.Pose pose = poseStack.last();
        for (Quad quad : QUADS) {
            for (int i = 0; i < 4; i++) {
                consumer.addVertex(pose, quad.pos()[i][0] / 16.0F, quad.pos()[i][1] / 16.0F, quad.pos()[i][2] / 16.0F)
                        .setColor(-1)
                        .setUv(quad.uv()[i][0] / TEX, quad.uv()[i][1] / TEX)
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, quad.normal()[0], quad.normal()[1], quad.normal()[2]);
            }
        }
    }
}
