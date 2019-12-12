// Copyright 2019 Team KillerWhale
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.killerwhale.memary.ARComponent.Renderer;

import android.content.Context;

import com.google.ar.core.Anchor;
import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.ARComponent.Utils.ARSettings;
import com.killerwhale.memary.Preference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private int mtoken = 1;
    private List<LineShaderRenderer> mRenderers;
    private List<Anchor> mAnchors;
    private List<Boolean> mRendererStatus;
    private List<Stroke> mPlaceholderStroke;

    /**
     * Constructior
     * @param distanceScale a float for scale of line width
     * @param LineWidthMax a float for maximum line width
     * */
    public LineShaderRendererGroup(float distanceScale, float LineWidthMax) {
        mPlaceholderStroke = new ArrayList<>();
        random = new Random(l);
        QUEUE_MAX_NUM = ((Long) Preference.arNumber).intValue() + 1;
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

    /**
     * Initialize Strokes data for all the Renderers.
     * Set the state of initialized Renderer to be valid.
     * Set the intial token for updating in the future.
     * @param cloudStrokes downloaded AR drawings.
     * */
    public void initStrokes(List<List<Stroke>> cloudStrokes){
        for (int i=0; i<cloudStrokes.size(); i++){
            mRenderers.get(i).updateStrokes(cloudStrokes.get(i));
            mRenderers.get(i).setColor(getRandomColor());
            mRendererStatus.set(i, true);
            mRenderers.get(i).upload();
        }

        mtoken = cloudStrokes.size();
    }

    /**
     * Initialize anchors for all the Renderers.
     * @param cloudAnchors downloaded anchors correspond to downloaded strokes.
     * */
    public void initAnchors(List<Anchor> cloudAnchors){
        System.out.println(cloudAnchors.size());
        mAnchors = cloudAnchors;
        for (int i=0; i<mAnchors.size(); i++){
            mAnchors.get(i).getPose().toMatrix(mRenderers.get(i).mModelMatrix, 0);
        }
    }

    /**
     * Set every Renderer to be updated.
     * */
    public void setNeedsUpdate(){
        for (int i=0; i<mRenderers.size(); i++){
            mRenderers.get(i).bNeedsUpdate.set(true);
        }
    }

    /**
     * Call upload function of every Renderer
     * */
    public void checkUpload(){
        for (int i=0; i<mRenderers.size(); i++){
            if ((mRendererStatus.get(i)) && (mRenderers.get(i).bNeedsUpdate.get())){
                mRenderers.get(i).upload();
            }
        }
    }


    /**
     * Call draw function of every Renderer
     * */
    public void draw(float[] cameraView, float[] cameraPerspective, float screenWidth,
                     float screenHeight, float nearClip, float farClip){
        for (int i=0; i<mRenderers.size(); i++){
            mRenderers.get(i).draw(cameraView, cameraPerspective, screenWidth, screenHeight,
                    nearClip, farClip);
        }
    }

    /**
     * Take in current AR Drawing and modify internal data of certain Renderer.
     * Updating follows the logic of Token Ring.
     * @param userStrokes current AR Drawing created by user.
     * @param userAnchor the anchor corresponds to userStrokes.
     * @return 3-dimensional Vector representing RGB color.
     * */
    public void update(List<Stroke> userStrokes, Anchor userAnchor){

        mRenderers.get(mtoken).clear();
        userAnchor.getPose().toMatrix(mRenderers.get(mtoken).mModelMatrix, 0);
        mRenderers.get(mtoken).updateStrokes(userStrokes);
        mRenderers.get(mtoken).setColor(ARSettings.getColor());
        mRenderers.get(mtoken).upload();
        mRendererStatus.set(mtoken, true);
        mtoken = (mtoken + 1) % QUEUE_MAX_NUM;
    }


    /**
     * Create Gl resources for every Renderer.
     * @param context context for access shader source
     * */
    public void createOnGlThread(Context context){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            try{
                mRenderers.get(i).createOnGlThread(context);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Generate random color for distinguishing cloud AR objects and current user's drawings.
     * @return 3-dimensional Vector representing RGB color.
     * */
    public Vector3f getRandomColor(){
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        Vector3f color = new Vector3f(r, g, b);
        return color;
    }

    /**
     * Clear internal data of all the Renderers. Reset states for all the Renderers.
     * */
    public void clear(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clear();
            mRenderers.get(i).updateStrokes(mPlaceholderStroke);
        }
        resetRendererStatus();
    }

    /**
     * Clear allocated GL resources for every renderer
     * */
    public void clearGL(){
        for (int i=0; i<QUEUE_MAX_NUM; i++){
            mRenderers.get(i).clearGL();
        }
    }

    /**
     * Set the state of every Renderer to be invalid (disable uploading)
     * */
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
