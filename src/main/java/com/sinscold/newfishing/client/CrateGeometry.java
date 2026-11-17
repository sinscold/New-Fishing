package com.sinscold.newfishing.client;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Геометрия ящика из Blockbench (models-src/crate.bbmodel): корпус 14×12×14 и крышка 14×2×14.
 * Модель в проекте повёрнута на 180° вокруг оси Z (из-за режима flip_y), поворот здесь учтён при расчёте вершин.
 * Крышка анимируется (animation.open): отъезжает влево на 8 пикселей, поворачивается на 90° и падает вниз на 6.
 * Координаты в пикселях (1/16 блока), начало в центре основания блока; текстура 64×64.
 */
public final class CrateGeometry {
    private static final float TEX = 64.0F;
    /** Длина анимации открытия в секундах (последний ключевой кадр). */
    private static final float ANIM_LENGTH = 0.54167F;
    private static final float PIVOT_Y = 13.0F;

    private record Quad(float[][] pos, float[][] uv, float[] normal, boolean lid) {}

    private static final List<Quad> QUADS = new ArrayList<>();

    static {
        // крышка: from (-7,12,-7) to (7,14,7), поворот 180° вокруг (0,13,0)
        addCube(-7, 12, -7, 7, 14, 7, 13.0F, true,
                new float[]{14, 40, 28, 42},  // north
                new float[]{0, 40, 14, 42},   // east
                new float[]{42, 40, 56, 42},  // south
                new float[]{28, 40, 42, 42},  // west
                new float[]{28, 40, 14, 26},  // up
                new float[]{42, 26, 28, 40}); // down
        // корпус: from (-7,2,-7) to (7,14,7), поворот 180° вокруг (0,7,0)
        addCube(-7, 2, -7, 7, 14, 7, 7.0F, false,
                new float[]{14, 14, 28, 26},
                new float[]{0, 14, 14, 26},
                new float[]{42, 14, 56, 26},
                new float[]{28, 14, 42, 26},
                new float[]{28, 14, 14, 0},
                new float[]{42, 0, 28, 14});
    }

    private CrateGeometry() {}

    /** Добавляет куб; повёрнут на 180° вокруг оси Z через точку (0, originY, 0): x → −x, y → 2·originY − y. */
    private static void addCube(float x1, float y1, float z1, float x2, float y2, float z2, float originY, boolean lid,
                                float[] north, float[] east, float[] south, float[] west, float[] up, float[] down) {
        // вершины граней в порядке обхода (по соглашению Java-моделей блоков) + углы UV
        face(lid, originY, new float[][]{{x2, y2, z1}, {x2, y1, z1}, {x1, y1, z1}, {x1, y2, z1}}, north, 0, 0, -1);
        face(lid, originY, new float[][]{{x1, y2, z2}, {x1, y1, z2}, {x2, y1, z2}, {x2, y2, z2}}, south, 0, 0, 1);
        face(lid, originY, new float[][]{{x2, y2, z2}, {x2, y1, z2}, {x2, y1, z1}, {x2, y2, z1}}, east, 1, 0, 0);
        face(lid, originY, new float[][]{{x1, y2, z1}, {x1, y1, z1}, {x1, y1, z2}, {x1, y2, z2}}, west, -1, 0, 0);
        face(lid, originY, new float[][]{{x1, y2, z1}, {x1, y2, z2}, {x2, y2, z2}, {x2, y2, z1}}, up, 0, 1, 0);
        face(lid, originY, new float[][]{{x1, y1, z2}, {x1, y1, z1}, {x2, y1, z1}, {x2, y1, z2}}, down, 0, -1, 0);
    }

    private static void face(boolean lid, float originY, float[][] corners, float[] uv, float nx, float ny, float nz) {
        float[][] pos = new float[4][3];
        for (int i = 0; i < 4; i++) {
            pos[i][0] = -corners[i][0];
            pos[i][1] = 2 * originY - corners[i][1];
            pos[i][2] = corners[i][2];
        }
        // углы UV: (u1,v1), (u1,v2), (u2,v2), (u2,v1)
        float[][] uvs = {{uv[0], uv[1]}, {uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]}};
        QUADS.add(new Quad(pos, uvs, new float[]{-nx, -ny, nz}, lid));
    }

    private static float lerp(float[] times, float[] values, float t) {
        if (t <= times[0]) return values[0];
        for (int i = 1; i < times.length; i++) {
            if (t <= times[i]) {
                float f = (t - times[i - 1]) / (times[i] - times[i - 1]);
                return values[i - 1] + (values[i] - values[i - 1]) * f;
            }
        }
        return values[values.length - 1];
    }

    private static Matrix4f lidMatrix(float openness) {
        float t = Math.max(0.0F, Math.min(1.0F, openness)) * ANIM_LENGTH;
        float px = lerp(new float[]{0, 0.08333F, 0.16667F, 0.54167F}, new float[]{0, -6, -8, -8.25F}, t);
        float py = lerp(new float[]{0, 0.08333F, 0.16667F, 0.54167F}, new float[]{0, 0, 0, -6}, t);
        float rz = lerp(new float[]{0, 0.125F, 0.20833F, 0.29167F}, new float[]{0, 0, 45, 90}, t);
        return new Matrix4f().translate(px, py, 0).translate(0, PIVOT_Y, 0).rotateZ((float) Math.toRadians(rz)).translate(0, -PIVOT_Y, 0);
    }

    /** Рисует ящик; начало координат poseStack — центр основания блока (низ, середина). */
    public static void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float openness) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f lid = lidMatrix(openness);
        Vector3f tmp = new Vector3f();
        for (Quad quad : QUADS) {
            for (int i = 0; i < 4; i++) {
                tmp.set(quad.pos()[i][0], quad.pos()[i][1], quad.pos()[i][2]);
                if (quad.lid()) {
                    lid.transformPosition(tmp);
                }
                consumer.addVertex(pose, tmp.x / 16.0F, tmp.y / 16.0F, tmp.z / 16.0F)
                        .setColor(-1)
                        .setUv(quad.uv()[i][0] / TEX, quad.uv()[i][1] / TEX)
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, quad.normal()[0], quad.normal()[1], quad.normal()[2]);
            }
        }
    }
}
