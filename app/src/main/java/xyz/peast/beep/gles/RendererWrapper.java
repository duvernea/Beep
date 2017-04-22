package xyz.peast.beep.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;
import android.util.Log;

import xyz.peast.beep.R;
import xyz.peast.beep.gles.objects.Bar;
import xyz.peast.beep.gles.objects.Mallet;
import xyz.peast.beep.gles.objects.Table;
import xyz.peast.beep.gles.programs.ColorShaderProgram;
import xyz.peast.beep.gles.programs.TextureShaderProgram;
import xyz.peast.beep.gles.util.Geometry;

/**
 * Created by duvernea on 7/18/16.
 */
public class RendererWrapper implements GLSurfaceView.Renderer {

    private static final String TAG = RendererWrapper.class.getSimpleName();

    private final float leftBound = -1f;
    private final float rightBound = 1f;

    private Context mContext;

    private boolean malletPressed = false;
    private Geometry.Point blueMalletPosition;

    private boolean leftBarPressed = false;
    private boolean rightBarPressed = false;
    private Geometry.Point leftBarPosition;
    private Geometry.Point rightBarPosition;

    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private Table mTable;
    private Mallet mMallet;

    private Bar mBarLeft;
    private Bar mBarRight;

    private TextureShaderProgram textureShaderProgram;
    private ColorShaderProgram colorShaderProgram;

    private int texture;

    public RendererWrapper(Context context) {
        mContext = context;
    }

    int mWidth;
    int mHeight;

    private float aspectRatio;
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
        blueMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, .4f);

        mBarLeft = new Bar(Bar.BAR_LEFT,.04f, .1f);
        mBarRight = new Bar(Bar.BAR_RIGHT, .04f, .1f);
        leftBarPosition = new Geometry.Point(0f, 0f, 0f);
        rightBarPosition = new Geometry.Point(0f, 0f, 0f);


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

        aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            // Landscape
            android.opengl.Matrix.orthoM(viewProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
            Log.d(TAG, "aspectRatio: " + aspectRatio);
        }
        else {
            android.opengl.Matrix.orthoM(viewProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
        Log.d(TAG, "Width: " + width);
        Log.d(TAG, "Height: " + height);
        leftBarPosition = new Geometry.Point(-aspectRatio, 0f, 0f);
        rightBarPosition = new Geometry.Point(aspectRatio, 0f, 0f);

        Matrix.setIdentityM(modelMatrix, 0);
        //Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Log.d(TAG, "onDrawFrame");
        // sets background color - clear color buffer is the thing you see
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0 );
//        String temp = "";
//        for (int i=0; i<viewProjectionMatrix.length; i++) {
//            temp += " " + viewProjectionMatrix[i];
//        }
//        Log.d(TAG, "viewProjectionMatrix: " + temp);
//        temp = "";
//        for (int i=0; i<modelViewProjectionMatrix.length; i++) {
//            temp += " " + modelViewProjectionMatrix[i];
//        }
//        Log.d(TAG, "modelviewProjectionMatrix: " + temp);


        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Draw the table
        positionTableInScene();
        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, texture);
        mTable.bindData(textureShaderProgram);
        mTable.draw();

        // Draw the mallets
        //positionObjectInScene(0f, mMallet.height / 2f, 0.4f);
        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mMallet.bindData(colorShaderProgram);
        mMallet.draw();

        // Draw left bar slider
        //colorShaderProgram.setUniforms(viewProjectionMatrix, 1f, 1f, 1f);

        positionBarInScene(leftBarPosition.x);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 1f, 0f);
        mBarLeft.bindData(colorShaderProgram);
        mBarLeft.draw();

        positionBarInScene(rightBarPosition.x);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 1f, 0f);
        mBarRight.bindData(colorShaderProgram);
        mBarRight.draw();

//        positionObjectInScene(0f, mMallet.height / 2f, .4f);
//        colorShaderProgram.setUniforms(viewProjectionMatrix, 0f, 0f, 1f);
//        mMallet.draw();
    }
    private void positionTableInScene() {
    }
    private void positionObjectInScene(float x, float y, float z) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

    }
    private void positionBarInScene(float x) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, 0, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);


    }
    public void handleTouchPress(float normalizedX, float normalizedY) {
        Log.d(TAG, "Touch Press event X = " + normalizedX );
        Log.d(TAG, "Touch Press event Y = " + normalizedY );
        //Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
        rightBarPressed = false;
        leftBarPressed = false;
        // Create a method that checks for intersection of left bar

        Log.d(TAG, "Leftbarposition: " + leftBarPosition.x);
        float adjustedX = normalizedX * aspectRatio;
        Log.d(TAG, "Adjusted X = " + adjustedX);
        if (Math.abs(leftBarPosition.x - adjustedX) < .2) {
            Log.d(TAG, "Leftbarpressed");
            leftBarPressed = true;

        }
        if (Math.abs(rightBarPosition.x - adjustedX) < .2) {
            Log.d(TAG, "Rightbarpressed");
            rightBarPressed = true;

        }

        //Sphere malletBoundingSphere
    }
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        // Minimum left to right bar separation
        float minBarSeparation = .3f;
        Log.d(TAG, "Touch Drag event X = " + normalizedX );
        Log.d(TAG, "Touch Drag event Y = " + normalizedY );
        if (leftBarPressed) {
            //leftBarPosition =
            //Log.d(TAG, leftBarPressed and dragged)
            leftBarPosition = new Geometry.Point(
                    clamp(normalizedX*aspectRatio, leftBound*aspectRatio, rightBarPosition.x-minBarSeparation),
                    normalizedY,
                    0);
            //leftBarPosition = new Geometry.Point(aspectRatio*normalizedX, normalizedY, 0);
        }
        if (rightBarPressed) {
            //leftBarPosition =
            //Log.d(TAG, leftBarPressed and dragged)
            rightBarPosition = new Geometry.Point(
                    clamp(normalizedX*aspectRatio, leftBarPosition.x+minBarSeparation, rightBound*aspectRatio),
                    normalizedY,
                    0);
            //rightBarPosition = new Geometry.Point(aspectRatio*normalizedX, normalizedY, 0);
        }
    }
    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }
}
