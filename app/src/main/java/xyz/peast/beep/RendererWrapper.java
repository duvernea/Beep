package xyz.peast.beep;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by duvernea on 7/18/16.
 */
public class RendererWrapper implements GLSurfaceView.Renderer {

    private static final String TAG = RendererWrapper.class.getSimpleName();

    //private static final String U_COLOR = "u_Color";
    private static final String A_COLOR = "a_Color";
    private static final String A_POSITION = "a_Position";

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int uColorLocation;


    private int aColorLocation;
    private int aPositionLocation;
//    float[] mGeometry =
//            {
//                    -0.5f, -0.5f, 0.0f, 1.0f,
//                    0.5f, -0.5f, 0.0f, 1.0f,
//                    0.0f, 0.5f, 0.0f, 1.0f
//            };
    float[] mGeometryOld =
        {
                // Triangle 1
                -.5f, -.5f, 0f, 1f,
                .5f, .5f, 0f, 1f,
                -.5f, .5f, 0f, 1f,
                // Triangle 2
                -.5f, -.5f, 0f, 1f,
                .5f, -.5f, 0f, 1f,
                .5f, .5f, 0f, 1f,
                // Line 1
                -.5f, 0f, 0f, 1f,
                .5f, 0f, 0f, 1f,
                // Points
                0f, -.25f, 0f, 1f,
                0f, .25f, 0f, 1f
        };
    float[] mGeometry =
            {
                    // Triangle Fan
                    0f, 0f, 1f, 1f, 1f,
                    -.5f, -.5f, .7f, .7f, .7f,
                    .5f, -.5f, .7f, .7f, .7f,
                    .5f, .5f, .7f, .7f, .7f,
                    -.5f, .5f, .7f, .7f, .7f,
                    -.5f, -.5f, .7f, .7f, .7f,
                    // Line 1
                    -.5f, 0f, 1f, 0f, 0f,
                    .5f, 0f, 0f, 1f, 0f,
                    // Points
                    0f, -.25f, 0f, 0f, 1f,
                    0f, .25f, 1f, 0f, 0f
            };
    private FloatBuffer vertexData;

    int mWidth;
    int mHeight;

    int mProgram = 0;
    float _animation = 0.0f;
    float a = 0f;



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated run..");

        // write some programs that opengl will compile at hardware - executed on GPU

        // set gl_Position (defined in the OpenGL language)
        // pass through program
        String vertexShaderSource = "" +
                "uniform vec2 translate;" +
                "attribute vec4 a_Position;" +
                "attribute vec4 a_Color;" +
                "varying vec4 v_Color;" +
                "" +
                "void main()" +
                "{" +
                "    v_Color = a_Color;" +
                "    gl_Position = a_Position + vec4(translate.x, translate.y, 0.0, 0.0);" +
                "    gl_PointSize = 10.0;" +
                "}";
        String fragmentShaderSource = "" +
                "" +
                "varying vec4 v_Color;" +
                "void main()" +
                "{" +
                "    gl_FragColor = v_Color;" +
                "}";

        // create vertex shader, compile code, log to output to see if errors
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderSource);
        GLES20.glCompileShader(vertexShader);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);
        Log.d(TAG, "VertexShaderCompileLog: " + vertexShaderCompileLog);

        // create fragment shader, compile code, log to output to see if errors
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);
        Log.d(TAG, "FragmentShaderCompileLog: " + fragmentShaderCompileLog);


        // create the program and attach shaders
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        //GLES20.glBindAttribLocation(mProgram, 0, "position");
        GLES20.glLinkProgram(mProgram);
        String programLinkLog = GLES20.glGetProgramInfoLog(mProgram);
        Log.d(TAG, "ProgramLinkLog: " + programLinkLog);

        GLES20.glUseProgram(mProgram);


        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        //uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        aColorLocation = GLES20.glGetAttribLocation(mProgram, A_COLOR);

        Log.d(TAG, "mGeometry.length = " + mGeometry.length);

        vertexData = ByteBuffer.allocateDirect(mGeometry.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(mGeometry);
        vertexData.position(0);

        GLES20.glVertexAttribPointer(aPositionLocation, // attribute location
                POSITION_COMPONENT_COUNT, // number of floats per vertex
                GLES20.GL_FLOAT, // data type
                false, // normalized? - only used for integer data
                STRIDE, // applies only when more than 1 attribute per array
                vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation,
                COLOR_COMPONENT_COUNT,
                GLES20.GL_FLOAT,
                false,
                STRIDE,
                vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);



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

        _animation += 0.01;
//        float translateX = (float) Math.sin(_animation);
//        float translateY = 0.5f;
        float translateX = 0f;
        float translateY = 0f;

        // update u_Color in the shader
        //GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);


        vertexData = ByteBuffer.allocateDirect(mGeometry.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(mGeometry);
        vertexData.position(0);
        GLES20.glUniform2f(GLES20.glGetUniformLocation(mProgram, "translate"), translateX, translateY);

//        ByteBuffer geometryByteBuffer = ByteBuffer.allocateDirect(mGeometry.length* BYTES_PER_FLOAT);
//        geometryByteBuffer.order(ByteOrder.nativeOrder());
//        FloatBuffer geometryBuffer = geometryByteBuffer.asFloatBuffer();
//        geometryBuffer.put(mGeometry);
//        geometryBuffer.position(0);

//        GLES20.glVertexAttribPointer(0,
//                4,
//                GLES20.GL_FLOAT, // type
//                false,  //normalized?
//                4 * 4,  // stride = 16 bytes from one float to the next
//                geometryBuffer); // the info
        // lines, points, or triangles
        GLES20.glEnableVertexAttribArray(0);


//        // Draw 2 triangles (a rectangle)
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,
//                0, //read in vertices starting at the beginning of our array
//                6); // number of vertices to read in
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,
                0,
                6);

        // Draw a line
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES,
                6, // start 6 vertices after first
                2); // number of vertices to read in

        // Draw 2 points
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS,
                8,
                1);
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS,
                9,
                1);

    }
}
