package xyz.peast.beep.gles.objects;

import android.util.Log;

import java.util.List;

import xyz.peast.beep.gles.VertexArray;
import xyz.peast.beep.gles.programs.ColorShaderProgram;
import xyz.peast.beep.gles.util.Geometry;

/**
 * Created by duvernea on 7/23/16.
 */
public class Bar {
    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final String TAG = Bar.class.getSimpleName();

    public final float width;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Bar(float width, float extention) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createBar(width, extention);
        this.width = width;

        vertexArray = new VertexArray(generatedData.vertexData);
        Log.d(TAG, "Bar vertex: " + vertexArray.toString());
        drawList = generatedData.drawList;

    }
    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                0);
    }
    public void draw() {
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
