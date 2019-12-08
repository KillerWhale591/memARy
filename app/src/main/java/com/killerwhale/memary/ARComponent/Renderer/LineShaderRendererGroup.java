package com.killerwhale.memary.ARComponent.Renderer;

import android.content.Context;

import com.google.ar.core.Anchor;
import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.ARComponent.Utils.ARSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.vecmath.Vector3f;

/**
 * Author: Qili Zeng (qzeng@bu.edu)
 * For rendering multiple AR Object
 * */

public class LineShaderRendererGroup {

    private int QUEUE_MAX_NUM = 2;
    private long l = System.currentTimeMillis();
    private Random random;
    private boolean flushflag = false;
    private int mtoken = 1;
    private List<LineShaderRenderer> mRenderers;
    private List<Anchor> mAnchors;
    private Map<String, Stroke> mSharedStrokes = new HashMap<>();
    private List<Boolean> mRendererStatus;
    private List<Stroke> mPlaceholderStroke;


    public LineShaderRendererGroup(float distanceScale, float LineWidthMax) {
        mPlaceholderStroke = new ArrayList<>();
        random = new Random(l);
        QUEUE_MAX_NUM = ARSettings.getMaxCloudStrokesNum() + 1;
        mRendererStatus = new ArrayList<>(QUEUE_MAX_NUM);
        mRenderers  = new ArrayList<>(QUEUE_MAX_NUM);
        mAnchors = new ArrayList<>(QUEUE_MAX_NUM);
        resetRendererStatus();
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            LineShaderRenderer renderer = new LineShaderRenderer();
            renderer.setColor(ARSettings.getColor());
            renderer.mDrawDistance = ARSettings.getStrokeDrawDistance();
            renderer.setDistanceScale(distanceScale);
            renderer.setLineWidth(LineWidthMax);
            renderer.clear();
            mRenderers.add(renderer);
        }

    }


    public void initStrokes(List<List<Stroke>> cloudStrokes){
        for (int i=0; i<cloudStrokes.size(); i++){
            mRenderers.get(i).updateStrokes(cloudStrokes.get(i));
            mRenderers.get(i).setColor(getRandomColor());
            mRendererStatus.set(i, true);
            mRenderers.get(i).upload();
        }

        mtoken = cloudStrokes.size();

    }

    public void initAnchors(List<Anchor> cloudAnchors){
        mAnchors = cloudAnchors;
        for (int i=0; i<mAnchors.size(); i++){
            mAnchors.get(i).getPose().toMatrix(mRenderers.get(i).mModelMatrix, 0);
        }
    }


    public void setNeedsUpdate(){
        for (int i=0; i<mRenderers.size(); i++){
            mRenderers.get(i).bNeedsUpdate.set(true);
        }
    }


    public void checkUpload(){
        for (int i=0; i<mRenderers.size(); i++){
            if ((mRendererStatus.get(i)) && (mRenderers.get(i).bNeedsUpdate.get())){
                mRenderers.get(i).upload();
            }
        }
    }

    public void draw(float[] cameraView, float[] cameraPerspective, float screenWidth,
                     float screenHeight, float nearClip, float farClip){
        for (int i=0; i<mRenderers.size(); i++){
            mRenderers.get(i).draw(cameraView, cameraPerspective, screenWidth, screenHeight,
                    nearClip, farClip);
        }
    }

    public void update(List<Stroke> userStrokes, Anchor userAnchor){

        mRenderers.get(mtoken).clear();
        userAnchor.getPose().toMatrix(mRenderers.get(mtoken).mModelMatrix, 0);
        mRenderers.get(mtoken).updateStrokes(userStrokes);
        mRenderers.get(mtoken).setColor(ARSettings.getColor());
        mRenderers.get(mtoken).upload();
        mRendererStatus.set(mtoken, true);
        mtoken = (mtoken + 1) % QUEUE_MAX_NUM;
    }

    public void createOnGlThread(Context context){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            try{
                mRenderers.get(i).createOnGlThread(context);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRandomOffset(Anchor anchor){
        float x = anchor.getPose().tx();
        //float y = anchor.getPose().ty();
        float z = anchor.getPose().tz();

        long l = System.currentTimeMillis();
        Random random = new Random(l);
        float ox = random.nextFloat();
        //float oy = random.nextFloat();
        float oz = random.nextFloat();

    }

    public Vector3f getRandomColor(){

        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        Vector3f color = new Vector3f(r, g, b);
        return color;
    }

    public void clear(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clear();
            mRenderers.get(i).updateStrokes(mPlaceholderStroke);
        }
        resetRendererStatus();
    }

    public void clearGL(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clearGL();
        }
    }

    private void resetRendererStatus(){
        for (int i=0; i < QUEUE_MAX_NUM; i++){
            if (i < mRendererStatus.size()){
                mRendererStatus.set(i, false);
            }
            else{
                mRendererStatus.add(false);
            }

        }
    }

}
