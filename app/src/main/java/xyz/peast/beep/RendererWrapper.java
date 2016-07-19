package xyz.peast.beep;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by duvernea on 7/18/16.
 */
public class RendererWrapper implements GLSurfaceView.Renderer {

    private static final String TAG = RendererWrapper.class.getSimpleName();

    int mWidth;
    int mHeight;

    int _program = 0;
    float a = 0f;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // write some programs that opengl will compile at hardware - executed on GPU

        // set gl_Position (defined in the OpenGL language)
        // pass through program
        String vertexShaderSource = "" +
                "" +
                "attribute vec4 position;" +
                "" +
                "void main()" +
                "{" +
                "    gl_Position = position;" +
                "}";
        String fragmentShaderSource = "" +
                "" +
                "" +
                "void main()" +
                "{" +
                "    gl_FragColor = vec4(0.8, 0.7, 0.6, 1.0);" +
                "}";

        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderSource);
        GLES20.glCompileShader(vertexShader);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);
        Log.d(TAG, "VertexShaderCompileLog: " + vertexShaderCompileLog);


        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);
        Log.d(TAG, "FragmentShaderCompileLog: " + fragmentShaderCompileLog);


        _program = GLES20.glCreateProgram();
        GLES20.glAttachShader(_program, vertexShader);
        GLES20.glAttachShader(_program, fragmentShader);
        GLES20.glBindAttribLocation(_program, 0, "position");
        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        Log.d(TAG, "ProgramLinkLog: " + programLinkLog);

        GLES20.glUseProgram(_program);


        // change default background color R G B A
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        // where inside of glport are we rendering to - top left corner, and set height/width
        // setting coordinate system
        GLES20.glViewport(0, 0, width, height);
        Log.d(TAG, "Width: " + width);
        Log.d(TAG, "Height: " + height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //Log.d(TAG, "onDrawFrame");
        // sets background color - clear color buffer is the thing you see
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 9 floats, if ignore 4th item
        float[] geometry =
                {
                        -0.5f, -0.5f, 0.0f, 1.0f,
                        0.5f, -0.5f, 0.0f, 1.0f,
                        0.0f, 0.5f, 0.0f, 1.0f
                };
//        float[] geometry =
//                {
//                        -0.5f, -0.5f, 0.0f,
//                        0.5f, -0.5f, 0.0f,
//                        0.0f, 0.5f, 0.0f,
//                };
//        float[] geometry =
//                {
//                        mWidth/2, mHeight/2, 0.0f, 1.0f,
//                        mWidth, mHeight, 0.0f, 1.0f,
//                        mWidth, 0f, 0.0f, 1.0f
//                };
        ByteBuffer geometryByteBuffer = ByteBuffer.allocateDirect(geometry.length* 4); // 4 bytes per float
        geometryByteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer geometryBuffer = geometryByteBuffer.asFloatBuffer();
        geometryBuffer.put(geometry);
        geometryBuffer.position(0);

        // definie in moment, size number of elements per unit = 4
        GLES20.glVertexAttribPointer(0,
                4,
                GLES20.GL_FLOAT, // type
                false,  //normalized?
                4 * 4,  // stride = 16 bytes from one float to the next
                geometryBuffer); // the info
        // lines, points, or triangles
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
