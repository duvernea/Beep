package xyz.peast.beep.gles.objects;

import android.opengl.GLES20;
import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.peast.beep.gles.util.Geometry;

/**
 * Created by duvernea on 7/23/16.
 */
public class ObjectBuilder {

    private static final String TAG = ObjectBuilder.class.getSimpleName();

    private static final int FLOATS_PER_VERTEX = 3;
    private final float[] vertexData;
    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();
    private int offset = 0;

    static interface DrawCommand {
        void draw();
    }
    static class GeneratedData {
        final float[] vertexData;
        final List<DrawCommand> drawList;

        GeneratedData(float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }
    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }
    private static int sizeOfOpenCyclinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }

    private void appendBar(float width, float extention) {
        final int startVertex = offset/FLOATS_PER_VERTEX;
        final int numVertices = 18;
        // Vertical Bar
        // vertex 1
        vertexData[offset++] = 0;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;
        // vertex 2
        vertexData[offset++] = width;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;

        // vertex 3
        vertexData[offset++] = 0;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;

        // vertex 4
        vertexData[offset++] = 0;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;

        // vertex 5
        vertexData[offset++] = width;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;

        // vertex 6
        vertexData[offset++] = width;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;

        // Horizontal Marker Top
        // vertex 1
        vertexData[offset++] = width;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;
        // vertex 2
        vertexData[offset++] = width+extention;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;
        // vertex 3
        vertexData[offset++] = width;
        vertexData[offset++] = 1-width;
        vertexData[offset++] = 0;
        // vertex 4
        vertexData[offset++] = width;
        vertexData[offset++] = 1-width;
        vertexData[offset++] = 0;
        // vertex 5
        vertexData[offset++] = width+extention;
        vertexData[offset++] = 1-width;
        vertexData[offset++] = 0;
        // vertex 6
        vertexData[offset++] = width+extention;
        vertexData[offset++] = 1;
        vertexData[offset++] = 0;
        // Horizontal Marker Bottom
        // vertex 1
        vertexData[offset++] = width;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;
        // vertex 2
        vertexData[offset++] = width+extention;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;
        // vertex 3
        vertexData[offset++] = width;
        vertexData[offset++] = -1+width;
        vertexData[offset++] = 0;
        // vertex 4
        vertexData[offset++] = width;
        vertexData[offset++] = -1+width;
        vertexData[offset++] = 0;
        // vertex 5
        vertexData[offset++] = width+extention;
        vertexData[offset++] = -1+width;
        vertexData[offset++] = 0;
        // vertex 6
        vertexData[offset++] = width+extention;
        vertexData[offset++] = -1;
        vertexData[offset++] = 0;



        Log.d(TAG, "Bar start vertex: " + startVertex);
        Log.d(TAG, "Bar num vertex: " + numVertices);
        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, startVertex, numVertices);
            }
        });


    }

    static GeneratedData createBar(float width, float extention) {
        // 2 triangles to draw a rectangle = 6 vertices
        ObjectBuilder builder = new ObjectBuilder(18);
        builder.appendBar(width, extention);

        return builder.build();

    }

    static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) +
                sizeOfOpenCyclinderInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);
        Geometry.Circle puckTop = new Geometry.Circle(
                puck.center.translateY(puck.height / 2f),
                puck.radius);
        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);

        return builder.build();
    }
    static GeneratedData createMallet(Geometry.Point center, float radius, float height, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2
                + sizeOfOpenCyclinderInVertices(numPoints) * 2;

        Log.d(TAG, "Size of Mallet: " + size);
        ObjectBuilder builder = new ObjectBuilder(size);

        // generate the mallet base
        float baseHeight = height * .25f;
        Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight), radius);
        Geometry.Cylinder baseCylinder = new Geometry.Cylinder(baseCircle.center.translateY(-baseHeight/2f), radius, baseHeight);

        builder.appendCircle(baseCircle, numPoints);
        builder.appendOpenCylinder(baseCylinder, numPoints);

        float handleHeight = height * .75f;
        float handleRadius = radius / 3f;

        Geometry.Circle handleCircle = new Geometry.Circle(
                center.translateY(height * .5f), handleRadius);
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f), handleRadius, handleHeight);
        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);

        return builder.build();
    }
    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset/FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        // center point of fan
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;

        // Fan around center point
        for (int i=0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

            vertexData[offset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }
    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCyclinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f);
        final float yEnd = cylinder.center.y + (cylinder.height / 2f);

        for (int i=0; i<= numPoints; i++ ) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                    * ((float) Math.PI *2f);

            float xPosition = cylinder.center.x +
                    cylinder.radius * (float) Math.cos(angleInRadians);
            float zPosition = cylinder.center.z +
                    cylinder.radius * (float) Math.sin(angleInRadians);

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;
            //vertexData[offset++] = 0;

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
            //vertexData[offset++] = 0;


            drawList.add(new DrawCommand() {
                @Override
                public void draw() {
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices);
                }
            });

        }
    }

    private GeneratedData build() {
        return new GeneratedData(vertexData, drawList);
    }
}
