// Copyright 2018 Google LLC
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

package com.killerwhale.memary.Activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.ARComponent.Renderer.AnchorRenderer;
import com.killerwhale.memary.ARComponent.Renderer.BackgroundRenderer;
import com.killerwhale.memary.ARComponent.Renderer.LineShaderRenderer;
import com.killerwhale.memary.ARComponent.Renderer.LineUtils;
import com.killerwhale.memary.ARComponent.Renderer.PointCloudRenderer;
import com.killerwhale.memary.ARComponent.Utils.BrushSelector;
import com.killerwhale.memary.ARComponent.Utils.ClearDrawingDialog;
import com.killerwhale.memary.ARComponent.Utils.DebugView;
import com.killerwhale.memary.ARComponent.Utils.ErrorDialog;
import com.killerwhale.memary.ARComponent.Utils.TrackingIndicator;


import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;


/**
 * This is a basic implementation of Offline AR functions
 */

public class ARActivity extends ARBaseActivity
        implements RecordableSurfaceView.RendererCallbacks, View.OnClickListener,
        ErrorDialog.Listener,ClearDrawingDialog.Listener{

    private static final String TAG = "ARActivity";
    private static final boolean JOIN_GLOBAL_ROOM = BuildConfig.GLOBAL;
    private static final int TOUCH_QUEUE_SIZE = 10;

    enum Mode {
        DRAW, PAIR_PARTNER_DISCOVERY, PAIR_ANCHOR_RESOLVING, PAIR_ERROR, PAIR_SUCCESS
    }

    private Mode mMode = Mode.DRAW;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private RecordableSurfaceView mSurfaceView;
    private BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
    private LineShaderRenderer mLineShaderRenderer = new LineShaderRenderer();
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();
    private AnchorRenderer zeroAnchorRenderer;
    private AnchorRenderer cloudAnchorRenderer;
    private TrackingIndicator mTrackingIndicator;
    private Button btnSave;
    private ImageButton
    private Button btnLoad;
    private ImageButton btnClear;
    private View mUndoButton;
    private View mDrawUiContainer;
    private View mOverflowButton;
    private TextView mPairActiveView;
    private DebugView mDebugView;

    private Frame mFrame;
    private Session mSession;
    private Anchor mAnchor;
    private List<Stroke> mStrokes;
    private BrushSelector mBrushSelector;

    private float[] projmtx = new float[16];
    private float[] viewmtx = new float[16];
    private float[] mZeroMatrix = new float[16];
    private float mScreenWidth = 0;
    private float mScreenHeight = 0;
    private Vector2f mLastTouch;
    private AtomicInteger touchQueueSize;
    private AtomicReferenceArray<Vector2f> touchQueue;
    private float mLineWidthMax = 0.33f;
    private float[] mLastFramePosition;
    private Boolean isDrawing = false;
    private AtomicBoolean bHasTracked = new AtomicBoolean(false);
    private AtomicBoolean bTouchDown = new AtomicBoolean(false);
    private AtomicBoolean bClearDrawing = new AtomicBoolean(false);
    private AtomicBoolean bUndo = new AtomicBoolean(false);
    private AtomicBoolean bNewStroke = new AtomicBoolean(false);
    private static final int MAX_UNTRACKED_FRAMES = 5;
    private int mFramesNotTracked = 0;
    private Map<String, Stroke> mSharedStrokes = new HashMap<>();
    private boolean mDebugEnabled = false;
    private long mRenderDuration;

    /**
     * Setup the app when main activity is created
     */
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        // Debug view
        if (BuildConfig.DEBUG) {
            mDebugView = findViewById(R.id.debug_view);
            mDebugView.setVisibility(View.VISIBLE);
            mDebugEnabled = true;
        }

        mTrackingIndicator = findViewById(R.id.finding_surfaces_view);

        mSurfaceView = findViewById(R.id.surfaceview);
        mSurfaceView.setRendererCallbacks(this);

        //btnLoad = findViewById(R.id.btnLoad);
        btnClear = findViewById(R.id.btnClear);

        btnSave.setOnClickListener(this);
        btnLoad.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        mUndoButton = findViewById(R.id.btnUndo);
        
        // set up brush selector
        mBrushSelector = findViewById(R.id.brush_selector);

        // Reset the zero matrix
        Matrix.setIdentityM(mZeroMatrix, 0);

        mStrokes = new ArrayList<>();
        touchQueueSize = new AtomicInteger(0);
        touchQueue = new AtomicReferenceArray<>(TOUCH_QUEUE_SIZE);
}

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * onResume part of the Android Activity Lifecycle
     */
    @Override
    protected void onResume() {
        super.onResume();

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.

        // Check if ARCore is installed/up-to-date
        int message = -1;
        Exception exception = null;
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance()
                        .requestInstall(this, mUserRequestedARCoreInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);

                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedARCoreInstall = false;
                        // at this point, the activity is paused and user will go through
                        // installation process
                        return;
                }
            }
        } catch (Exception e) {
            exception = e;
            message = getARCoreInstallErrorMessage(e);
        }

        // display possible ARCore error to user
        if (message >= 0) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception creating session", exception);
            finish();
            return;
        }

        // Create default config and check if supported.
        Config config = new Config(mSession);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        if (!mSession.isSupported(config)) {
            Toast.makeText(getApplicationContext(), R.string.ar_not_supported,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mSession.configure(config);

        // Note that order of session/surface resume matters - session must be resumed
        // before the surface view is resumed or the surface may call back on a session that is
        // not ready.
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            ErrorDialog.newInstance(R.string.error_camera_not_available, true)
                    .show(this);
        } catch (Exception e) {
            ErrorDialog.newInstance(R.string.error_resuming_session, true).show(this);
        }

        mSurfaceView.resume();

        mSurfaceView.resume();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        if (!SessionHelper.shouldContinueSession(this)) {
            // if user has left activity for too long, clear the strokes from the previous session
            bClearDrawing.set(true);
            showStrokeDependentUI();
        }

        findViewById(R.id.draw_container).setVisibility(View.VISIBLE);
    }

    /**
     * onPause part of the Android Activity Lifecycle
     */
    @Override
    public void onPause() {

        mSurfaceView.pause();
        if (mSession != null) {
            mSession.pause();
        }

        mTrackingIndicator.resetTrackingTimeout();

        SessionHelper.setSessionEnd(this);

        super.onPause();
    }


    /**
     * addStroke adds a new stroke to the scene
     */
    private void addStroke() {
        mLineWidthMax = mBrushSelector.getSelectedLineWidth().getWidth();

        Stroke stroke = new Stroke();
        stroke.localLine = true;
        stroke.setLineWidth(mLineWidthMax);
        mStrokes.add(stroke);
        showStrokeDependentUI();

        mTrackingIndicator.setDrawnInSession();
    }

    /**
     * addPoint2f adds a point to the current stroke
     *
     * @param touchPoint a 2D point in screen space and is projected into 3D world space
     */
    private void addPoint2f(Vector2f... touchPoint) {
        Vector3f[] newPoints = new Vector3f[touchPoint.length];
        for (int i = 0; i < touchPoint.length; i++) {
            newPoints[i] = LineUtils
                    .GetWorldCoords(touchPoint[i], mScreenWidth, mScreenHeight, projmtx, viewmtx);
        }

        addPoint3f(newPoints);
    }

    /**
     * addPoint3f adds a point to the current stroke
     *
     * @param newPoint a 3D point in world space
     */
    private void addPoint3f(Vector3f... newPoint) {
        Vector3f point;
        int index = mStrokes.size() - 1;

        if (index < 0)
            return;

        for (int i = 0; i < newPoint.length; i++) {
            if (mAnchor != null && mAnchor.getTrackingState() == TrackingState.TRACKING) {
                point = LineUtils.TransformPointToPose(newPoint[i], mAnchor.getPose());
                mStrokes.get(index).add(point);
                Log.i("ADD 3D Point", "With Anchor");
            } else {
                mStrokes.get(index).add(newPoint[i]);
                Log.i("ADD 3D Point", "Without Anchor");
            }
        }
        isDrawing = true;
    }

    /**
     * update() is executed on the GL Thread.
     * The method handles all operations that need to take place before drawing to the screen.
     * The method :
     * extracts the current projection matrix and view matrix from the AR Pose
     * handles adding stroke and points to the data collections
     * updates the ZeroMatrix and performs the matrix multiplication needed to re-center the drawing
     * updates the Line Renderer with the current strokes, color, distance scale, line width etc
     */
    private void update() {

        try {
            final long updateStartTime = System.currentTimeMillis();

            // Update ARCore frame
            mFrame = mSession.update();

            if (mAnchor == null){
                mAnchor =  mSession.createAnchor(
                        mFrame.getCamera().getPose()
                                .compose(Pose.makeTranslation(0, 0, -1f))
                                .extractTranslation());
            }

            // Notify the hostManager of all the anchor updates.
            Collection<Anchor> updatedAnchors = mFrame.getUpdatedAnchors();

            // Update tracking states
            mTrackingIndicator.setTrackingStates(mFrame, mAnchor);
            if (mAnchor == null) {
                createAnchor();
            }

            if (mTrackingIndicator.trackingState == TrackingState.TRACKING && !bHasTracked.get()) {
                bHasTracked.set(true);
            }

            // Get projection matrix.
            mFrame.getCamera().getProjectionMatrix(projmtx, 0, ARSettings.getNearClip(),
                    ARSettings.getFarClip());
            mFrame.getCamera().getViewMatrix(viewmtx, 0);

            float[] position = new float[3];

            mFrame.getCamera().getPose().getTranslation(position, 0);

            // Multiply the zero matrix
            Matrix.multiplyMM(viewmtx, 0, viewmtx, 0, mZeroMatrix, 0);

            // Check if camera has moved much, if thats the case, stop touchDown events
            // (stop drawing lines abruptly through the air)
            if (mLastFramePosition != null) {
                Vector3f distance = new Vector3f(position[0], position[1], position[2]);
                distance.sub(new Vector3f(mLastFramePosition[0], mLastFramePosition[1],
                        mLastFramePosition[2]));

                if (distance.length() > 0.15) {
                    bTouchDown.set(false);
                }
            }

            mLastFramePosition = position;

            // Add points to strokes from touch queue
            int numPoints = touchQueueSize.get();
            if (numPoints > TOUCH_QUEUE_SIZE) {
                numPoints = TOUCH_QUEUE_SIZE;
            }

            if (numPoints > 0) {
                if (bNewStroke.get()) {
                    bNewStroke.set(false);
                    addStroke();
                }

                Vector2f[] points = new Vector2f[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    points[i] = touchQueue.get(i);
                    mLastTouch = new Vector2f(points[i].x, points[i].y);
                }
                addPoint2f(points);
            }

            // If no new points have been added, and touch is down, add last point again
            if (numPoints == 0 && bTouchDown.get()) {
                addPoint2f(mLastTouch);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (numPoints > 0) {
                touchQueueSize.set(0);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bClearDrawing.get()) {
                bClearDrawing.set(false);
                clearDrawing();
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            // Check if we are still drawing, otherwise finish line
            if (isDrawing && !bTouchDown.get()) {
                isDrawing = false;
                if (!mStrokes.isEmpty()) {
                    mStrokes.get(mStrokes.size() - 1).finishStroke();
                }
            }

            boolean renderNeedsUpdate = false;
            for (Stroke stroke : mSharedStrokes.values()) {
                if (stroke.update()) {
                    renderNeedsUpdate = true;
                }
            }
            if (renderNeedsUpdate) {
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bUndo.get()) {
                bUndo.set(false);
                if (mStrokes.size() > 0) {
                    int index = mStrokes.size() - 1;
//                    mPairSessionManager.undoStroke(mStrokes.get(index));
                    mStrokes.remove(index);
                    if (mStrokes.isEmpty()) {
                        showStrokeDependentUI();
                    }
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }
            }
            if (mLineShaderRenderer.bNeedsUpdate.get()) {
                mLineShaderRenderer.setColor(ARSettings.getColor());
                mLineShaderRenderer.mDrawDistance = ARSettings.getStrokeDrawDistance();
                float distanceScale = 0.0f;
                mLineShaderRenderer.setDistanceScale(distanceScale);
                mLineShaderRenderer.setLineWidth(mLineWidthMax);
                mLineShaderRenderer.clear();
                mLineShaderRenderer.updateStrokes(mStrokes, mSharedStrokes);
                mLineShaderRenderer.upload();
            }

            // Debug view
            if (mDebugEnabled) {
                final long deltaTime = System.currentTimeMillis() - updateStartTime;
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDebugView
                                .setRenderInfo(mLineShaderRenderer.mNumPoints, deltaTime,
                                        mRenderDuration);
                    }
                });

            }

        } catch (Exception e) {
            Log.e(TAG, "update: ", e);
        }
    }

    /**
     * renderScene() clears the Color Buffer and Depth Buffer, draws the current texture from the
     * camera
     * and draws the Line Renderer if ARCore is tracking the world around it
     */
    private void renderScene() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mFrame != null) {
            mBackgroundRenderer.draw(mFrame);
        }

        // Draw debug anchors
        if (BuildConfig.DEBUG) {
            if (mFrame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                zeroAnchorRenderer.draw(viewmtx, projmtx, false);
            }
        }

        // Draw background.
        if (mFrame != null) {

            // Draw Lines
            if (mTrackingIndicator.isTracking() || (
                    // keep painting through 5 frames where we're not tracking
                    (bHasTracked.get() && mFramesNotTracked < MAX_UNTRACKED_FRAMES))) {

                if (!mTrackingIndicator.isTracking()) {
                    mFramesNotTracked++;
                } else {
                    mFramesNotTracked = 0;
                }

                // If the anchor is set, set the modelMatrix of the line renderer to offset to the anchor
                if (mAnchor != null && mAnchor.getTrackingState() == TrackingState.TRACKING) {
                    mAnchor.getPose().toMatrix(mLineShaderRenderer.mModelMatrix, 0);
                    Log.i("Anchor", "set modelMatrix");

                    if (BuildConfig.DEBUG) {
                        mAnchor.getPose().toMatrix(cloudAnchorRenderer.mModelMatrix, 0);
                        cloudAnchorRenderer.draw(viewmtx, projmtx, true);
                    }
                }

                // Render the lines
                mLineShaderRenderer
                        .draw(viewmtx, projmtx, mScreenWidth, mScreenHeight,
                                ARSettings.getNearClip(),
                                ARSettings.getFarClip());
            }

            if (mDebugEnabled) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDebugView.setAnchorTracking(mAnchor);
                    }
                });
            }
        }

        if (mMode == Mode.PAIR_PARTNER_DISCOVERY || mMode == Mode.PAIR_ANCHOR_RESOLVING) {
            if (mFrame != null) {
                PointCloud pointCloud = mFrame.acquirePointCloud();
                this.pointCloud.update(pointCloud);
                this.pointCloud.draw(viewmtx, projmtx);

                // Application is responsible for releasing the point cloud resources after
                // using it.
                pointCloud.release();
            }
        }

    }

    /**
     * Clears the Datacollection of Strokes and sets the Line Renderer to clear and update itself
     * Designed to be executed on the GL Thread
     */
    private void clearDrawing() {
        mStrokes.clear();
        mLineShaderRenderer.clear();
        showStrokeDependentUI();
    }


    /**
     * onClickUndo handles the touch input on the GUI and sets the AtomicBoolean bUndo to be true
     * the actual undo functionality is executed in the GL Thread
     */
    public void onClickUndo(View button) {

        bUndo.set(true);
    }

    /**
     * onClickClear handle showing an AlertDialog to clear the drawing
     */
    private void onClickClear() {
        ClearDrawingDialog.newInstance(false).show(this);
    }

    // ------- Touch events

    /**
     * onTouchEvent handles saving the lastTouch screen position and setting bTouchDown and
     * bNewStroke
     * AtomicBooleans to trigger addPoint3f and addStroke on the GL Thread to be called
     */
    @Override
    public boolean onTouchEvent(MotionEvent tap) {
        int action = tap.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            closeViewsOutsideTapTarget(tap);
        }

        // do not accept touch events through the playback view
        // or when we are not tracking
        //if (mPlaybackView.isOpen() || !mTrackingIndicator.isTracking()) {
        if (!mTrackingIndicator.isTracking()) {
            if (bTouchDown.get()) {
                bTouchDown.set(false);
            }
            return false;
        }

        if (mMode == Mode.DRAW) {
            if (action == MotionEvent.ACTION_DOWN) {
                touchQueue.set(0, new Vector2f(tap.getX(), tap.getY()));
                bNewStroke.set(true);
                bTouchDown.set(true);
                touchQueueSize.set(1);

                bNewStroke.set(true);
                bTouchDown.set(true);

                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (bTouchDown.get()) {
                    int numTouches = touchQueueSize.addAndGet(1);
                    if (numTouches <= TOUCH_QUEUE_SIZE) {
                        touchQueue.set(numTouches - 1, new Vector2f(tap.getX(), tap.getY()));
                    }
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP
                    || tap.getAction() == MotionEvent.ACTION_CANCEL) {
                bTouchDown.set(false);
                return true;
            }
        }

        return false;
    }

    private void closeViewsOutsideTapTarget(MotionEvent tap) {
        if (isOutsideViewBounds(mBrushSelector, (int) tap.getRawX(), (int) tap.getRawY())
                && mBrushSelector.isOpen()) {
            mBrushSelector.close();
        }
    }

    private boolean isOutsideViewBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return !outRect.contains(x, y);
    }

    @Override
    public void onSurfaceDestroyed() {
        mBackgroundRenderer.clearGL();
        mLineShaderRenderer.clearGL();
    }

    @Override
    public void onSurfaceCreated() {

        zeroAnchorRenderer = new AnchorRenderer();
        cloudAnchorRenderer = new AnchorRenderer();
        pointCloud.createOnGlThread(/*context=*/ this);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        int rotation = Surface.ROTATION_0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rotation = Surface.ROTATION_90;
        }
        mSession.setDisplayGeometry(rotation, width, height);
    }


    @Override
    public void onContextCreated() {
        mBackgroundRenderer.createOnGlThread(this);
        mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
        try {
            mLineShaderRenderer.createOnGlThread(ARActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLineShaderRenderer.bNeedsUpdate.set(true);
    }

    @Override
    public void onPreDrawFrame() {
        update();
    }

    @Override
    public void onDrawFrame() {
        long renderStartTime = System.currentTimeMillis();

        renderScene();

        mRenderDuration = System.currentTimeMillis() - renderStartTime;
    }

    private void showStrokeDependentUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUndoButton.setVisibility(mStrokes.size() > 0 ? View.VISIBLE : View.GONE);
                btnClear.setVisibility(
                        (mStrokes.size() > 0 || mSharedStrokes.size() > 0) ? View.VISIBLE
                                : View.GONE);
                mTrackingIndicator.setHasStrokes(mStrokes.size() > 0);
            }
        });
    }

    @Override
    public void onClearDrawingConfirmed() {
        bClearDrawing.set(true);
        showStrokeDependentUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                onClickClear();
                break;
            case R.id.btnSave:
                saveStrokes();
                break;
        }
        mBrushSelector.close();
    }


    /**
     * Update views for the given mode
     */
    private void setMode(Mode mode) {
        if (mMode != mode) {
            mMode = mode;

            switch (mMode) {
                case DRAW:
                    showView(mDrawUiContainer);
                    showView(mTrackingIndicator);
                    mTrackingIndicator.setDrawPromptEnabled(true);
                    break;
                case PAIR_ANCHOR_RESOLVING:
                    hideView(mDrawUiContainer);
                    mTrackingIndicator.setDrawPromptEnabled(false);
                    showView(mTrackingIndicator);
                    break;
                case PAIR_PARTNER_DISCOVERY:
                case PAIR_ERROR:
                case PAIR_SUCCESS:
                    hideView(mDrawUiContainer);
                    hideView(mTrackingIndicator);
                    mTrackingIndicator.setDrawPromptEnabled(false);
                    break;
            }
        }
    }

    public void createAnchor() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Pose pose = mFrame.getCamera().getPose();

                try {
                    mAnchor = mSession.createAnchor(pose);
                } catch (NotTrackingException e) {
                    Log.e(TAG, "Cannot create anchor when not tracking", e);
                    mTrackingIndicator.addListener(new TrackingIndicator.DisplayListener() {
                        @Override
                        public void onErrorDisplaying() {
                            // Do nothing, can't set anchor
                        }

                        @Override
                        public void onErrorRemoved() {
                            mTrackingIndicator.removeListener(this);
                            createAnchor();
                        }
                    });
                    return;
                }

                //mPairSessionManager.onAnchorCreated();
                if (mStrokes.size() > 0) {
                    for (int i = 0; i < mStrokes.size(); i++) {
                        mStrokes.get(i).offsetToPose(pose);
                    }
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }
        });
    }


    @Override
    public void exitApp(){
        finish();
    }

    public void saveStrokes(){
        try {
            serializeStorkes(mStrokes);
            Toast.makeText(ARActivity.this,"Strokes saved",Toast.LENGTH_SHORT);
            Log.i("Checkpointer", "Saved!");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void serializeStorkes(List<Stroke> mStrokes) throws IOException {
        try {
            FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("strokeFile.ser", getApplicationContext().MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(mStrokes);
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStrokes(){
        try {
            List<Stroke> newmStrokes = fetchStrokes();
            mStrokes = newmStrokes;
            Log.i("Checkpointer", "Loaded!");
            update();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Stroke> fetchStrokes() {
        try{
            FileInputStream fileInputStream = getApplicationContext().openFileInput("strokeFile.ser");
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            List<Stroke> fetchStrokes = (ArrayList<Stroke>) in.readObject();
            in.close();
            return fetchStrokes;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
