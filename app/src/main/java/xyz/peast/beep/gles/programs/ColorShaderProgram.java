package xyz.peast.beep.gles.programs;

import android.content.Context;
import android.opengl.GLES20;

import xyz.peast.beep.R;

/**
 * Created by duvernea on 7/23/16.
 */
public class ColorShaderProgram extends ShaderProgram {
    // Uniform locations
    private final int uMatrixLocation;

    // Attribute locations
    private final int aPositionLocation;
    private final int aColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);
        // Retrieve uniform locations for shader program
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        // Retrieve attribute locations for the shader program
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR);
    }
    public void setUniforms(float[] matrix) {
        // Pass the matrix into shader program
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }
    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
    public int getColorAttributeLocation() {
        return aColorLocation;
    }
}
