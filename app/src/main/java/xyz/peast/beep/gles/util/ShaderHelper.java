package xyz.peast.beep.gles.util;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by duvernea on 7/23/16.
 */
public class ShaderHelper {

    public static final String TAG = ShaderHelper.class.getSimpleName();

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }
    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = GLES20.glCreateShader(type);

        if (shaderObjectId == 0) {
            Log.d(TAG, "Could not create new shader");
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.d(TAG, "Compilation of shader failed");
            GLES20.glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;

    }
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {

            Log.d(TAG, "Unable to create new program");
            return 0;
        }
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        GLES20.glLinkProgram(programObjectId);

        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        Log.d(TAG, "Results of linking program:" + GLES20.glGetProgramInfoLog(programObjectId));
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);
            Log.d(TAG, "Linking of program failed");
            return 0;
        }
        return programObjectId;

    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {

        int program;

        // Compile the shaders
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program
        program = linkProgram(vertexShader, fragmentShader);

        return program;
    }
}
