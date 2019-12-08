package com.killerwhale.memary.ARComponent.Renderer;

import android.content.Context;

import com.google.ar.core.Anchor;
import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.ARComponent.View.ARSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Author: Qili Zeng (qzeng@bu.edu)
 * For rendering multiple AR Object
 * */

public class LineShaderRendererGroup {

    private int QUEUE_MAX_NUM = 2;
    private boolean flushflag = false;
    private int mtoken = 1;
    private List<LineShaderRenderer> mRenderers  = new ArrayList<>();
    private List<Anchor> mAnchors = new ArrayList<>();
    private Map<String, Stroke> mSharedStrokes = new HashMap<>();


    public LineShaderRendererGroup() {

    }

    public void initialize(List<List<Stroke>> cloudStrokes, List<Anchor> cloudAnchors,
                           float distanceScale, float LineWidthMax){

        QUEUE_MAX_NUM = ARSettings.getMaxCloudStrokesNum() + 1;
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            LineShaderRenderer renderer = new LineShaderRenderer();
            renderer.setColor(ARSettings.getColor());
            renderer.mDrawDistance = ARSettings.getStrokeDrawDistance();
            renderer.setDistanceScale(distanceScale);
            renderer.setLineWidth(LineWidthMax);
            renderer.clear();
            mRenderers.add(renderer);
        }

        mAnchors = cloudAnchors;
        for (int i=0; i<mAnchors.size(); i++){
            mAnchors.get(i).getPose().toMatrix(mRenderers.get(i).mModelMatrix, 0);
        }

        for (int i=0; i<cloudStrokes.size(); i++){
            mRenderers.get(i).updateStrokes(cloudStrokes.get(i), mSharedStrokes);
            mRenderers.get(i).upload();
        }

        mtoken = cloudStrokes.size();
    }

    public void setNeedsUpdate(){
        for (int i=0; i<mRenderers.size(); i++){
            mRenderers.get(i).bNeedsUpdate.set(true);
        }
    }


    public void checkUpload(){
        for (int i=0; i<mRenderers.size(); i++){
            if (mRenderers.get(i).bNeedsUpdate.get()){
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

    public void update(Context context, List<Stroke> userStrokes, Anchor userAnchor){
        //mRenderers.get(mtoken).clearGL();
        mRenderers.get(mtoken).clear();
        //mAnchors.set(mtoken, userAnchor);
        userAnchor.getPose().toMatrix(mRenderers.get(mtoken).mModelMatrix, 0);
        mRenderers.get(mtoken).updateStrokes(userStrokes, mSharedStrokes);
        mRenderers.get(mtoken).upload();
//        try{
//            mRenderers.get(mtoken).createOnGlThread(context);
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
        mtoken = (mtoken + 1) % QUEUE_MAX_NUM;
    }

    public void anchorTransform(Anchor mAnchor){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mAnchor.getPose().toMatrix(mRenderers.get(i).mModelMatrix, 0);
        }
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

    public void clear(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clear();
        }
    }

    public void clearGL(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clearGL();
        }
    }


}
