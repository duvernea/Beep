package xyz.peast.beep.gles.programs;

import android.content.Context;
import android.opengl.GLES20;

import xyz.peast.beep.gles.util.ShaderHelper;
import xyz.peast.beep.gles.util.TextResourceReader;

/**
 * Created by duvernea on 7/23/16.
 */
public class ShaderProgram {
    // Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    //Shader program
    protected final int program;
    protected ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        // Compile the shaders and link the pgoram
        program = ShaderHelper.buildProgram(TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));

    }
    public void useProgram() {
        // Set current OpenGL shader program to this
        GLES20.glUseProgram(program);
    }

}
