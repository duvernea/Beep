package xyz.peast.beep.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import xyz.peast.beep.R;
import xyz.peast.beep.gles.objects.Mallet;
import xyz.peast.beep.gles.objects.Table;
import xyz.peast.beep.gles.programs.ColorShaderProgram;
import xyz.peast.beep.gles.programs.TextureShaderProgram;
import xyz.peast.beep.gles.util.ShaderHelper;
import xyz.peast.beep.gles.util.TextResourceReader;

/**
 * Created by duvernea on 7/18/16.
 */
public class RendererWrapper implements GLSurfaceView.Renderer {

    private static final String TAG = RendererWrapper.class.getSimpleName();

    private Context mContext;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private Table mTable;
    private Mallet mMallet;

    private TextureShaderProgram textureShaderProgram;
    private ColorShaderProgram colorShaderProgram;

    private int texture;

    public RendererWrapper(Context context) {
        mContext = context;
    }

    int mWidth;
    int mHeight;
//
//    int mProgram = 0;
//    float _animation = 0.0f;
//    float a = 0f;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated run..");

        // change default background color R G B A
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTable = new Table();
        mMallet = new Mallet(0.08f, .15f, 32);

        textureShaderProgram = new TextureShaderProgram(mContext);
        colorShaderProgram = new ColorShaderProgram(mContext);

        texture = TextureHelper.loadTexture(mContext, R.drawable.air_hockey_surface);

        //GLES20.glUseProgram(mProgram);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        mWidth = width;
        mHeight = height;
        // where inside of glport are we rendering to - top left corner, and set height/width
        // setting coordinate system
        GLES20.glViewport(0, 0, width, height);

        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            // Landscape
            android.opengl.Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        }
        else {
            android.opengl.Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
        Log.d(TAG, "Width: " + width);
        Log.d(TAG, "Height: " + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //Log.d(TAG, "onDrawFrame");
        // sets background color - clear color buffer is the thing you see
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Draw the table
        positionTableInScene();
        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(projectionMatrix, texture);
        mTable.bindData(textureShaderProgram);
        mTable.draw();

        // Draw the mallets
        positionObjectInScene(0f, mMallet.height / 2f, 0.4f);
        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(projectionMatrix, 1f, 0f, 0f);
        mMallet.bindData(colorShaderProgram);
        mMallet.draw();

//        positionObjectInScene(0f, mMallet.height / 2f, .4f);
//        colorShaderProgram.setUniforms(projectionMatrix, 0f, 0f, 1f);
//        mMallet.draw();
    }
    private void positionTableInScene() {
    }
    private void positionObjectInScene(float x, float y, float z) {

    }
}
