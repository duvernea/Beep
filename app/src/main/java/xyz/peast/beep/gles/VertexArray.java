package xyz.peast.beep.gles;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by duverneay on 7/21/16.
 */
public class VertexArray {


    private final FloatBuffer floatBuffer;
    public final float[] vertexData;

    public VertexArray (float[] vertexData) {
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        this.vertexData = vertexData;
    }
    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        floatBuffer.position(dataOffset);
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT,
                false, stride, floatBuffer);
        GLES20.glEnableVertexAttribArray(attributeLocation);

        floatBuffer.position(0);
    }
    public String toString() {
        String vertices = "";
        for (int i = 0; i<vertexData.length; i++) {
            vertices += " " + vertexData[i];
        }
        return vertices;
    }
}
