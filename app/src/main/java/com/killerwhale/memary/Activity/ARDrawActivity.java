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
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.icu.util.Calendar;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.ARComponent.Rendering.AnchorRenderer;
import com.killerwhale.memary.ARComponent.Rendering.BackgroundRenderer;
import com.killerwhale.memary.ARComponent.Rendering.LineShaderRenderer;
import com.killerwhale.memary.ARComponent.Rendering.LineUtils;
import com.killerwhale.memary.ARComponent.Rendering.PointCloudRenderer;
import com.killerwhale.memary.ARComponent.View.BrushSelector;
import com.killerwhale.memary.ARComponent.View.ClearDrawingDialog;
import com.killerwhale.memary.ARComponent.View.DebugView;
import com.killerwhale.memary.ARComponent.View.ErrorDialog;
import com.killerwhale.memary.ARComponent.View.TrackingIndicator;
import com.killerwhale.memary.ARSettings;
import com.killerwhale.memary.BuildConfig;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotTrackingException;
import com.killerwhale.memary.PermissionHelper;
import com.killerwhale.memary.R;
import com.killerwhale.memary.SessionHelper;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * This is a complex example that shows how to create an augmented reality (AR) application using
 * the ARCore API.
 */

public class ARDrawActivity extends ARBaseActivity
        implements View.OnClickListener,RecordableSurfaceView.RendererCallbacks,
                   ClearDrawingDialog.Listener, ErrorDialog.Listener {

    private static final String TAG = "DrawARActivity";

    private static final boolean JOIN_GLOBAL_ROOM = BuildConfig.GLOBAL;

    private static final int TOUCH_QUEUE_SIZE = 10;


    enum Mode {
        DRAW, VIEW
    }

    private Mode mMode = Mode.DRAW;

    private View mDrawUiContainer;

    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedARCoreInstall = true;

    private RecordableSurfaceView mSurfaceView;

    private Session mSession;

    private BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();

    private LineShaderRenderer mLineShaderRenderer = new LineShaderRenderer();
//    private DebugMeshShaderRenderer mLineShaderRenderer = new DebugMeshShaderRenderer();

    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    private AnchorRenderer zeroAnchorRenderer;

    private AnchorRenderer cloudAnchorRenderer;

    private Frame mFrame;

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

    private List<Stroke> mStrokes;

    private File mOutputFile;

    private BrushSelector mBrushSelector;

    private Button mRecordButton;

    private View mUndoButton;

    private TrackingIndicator mTrackingIndicator;

    //private View mOverflowButton;

    private LinearLayout mOverflowLayout;

    private View mClearDrawingButton;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /*
     * Track number frames where we lose ARCore tracking. If we lose tracking for less than
     * a given number then continue painting.
     */
    private static final int MAX_UNTRACKED_FRAMES = 5;

    private int mFramesNotTracked = 0;

    private DebugView mDebugView;

    private boolean mDebugEnabled = false;

    private long mRenderDuration;

    /*
     * Session sharing
     */

    private Anchor mAnchor;

    private Map<String, Stroke> mSharedStrokes = new HashMap<>();


    // private PairSessionManager mPairSessionManager;

    /**
     * Setup the app when main activity is created
     */
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_draw);

        // Debug view
        if (BuildConfig.DEBUG) {
            mDebugView = findViewById(R.id.draw_debug_view);
            mDebugView.setVisibility(View.VISIBLE);
            mDebugEnabled = true;
        }

        mTrackingIndicator = findViewById(R.id.draw_finding_surfaces_view);

        mSurfaceView = findViewById(R.id.draw_surfaceview);
        mSurfaceView.setRendererCallbacks(this);


        //mOverflowButton.setOnClickListener(this);
        mClearDrawingButton = findViewById(R.id.draw_menu_item_clear);
        mClearDrawingButton.setOnClickListener(this);

//      findViewById(R.id.menu_item_crash).setOnClickListener(this);
//      findViewById(R.id.menu_item_hide_ui).setOnClickListener(this);

        mUndoButton = findViewById(R.id.draw_undo_button);

        // set up brush selector
        mBrushSelector = findViewById(R.id.draw_brush_selector);

        mRecordButton = findViewById(R.id.draw_record_button);
        mRecordButton.setEnabled(true);

        // Reset the zero matrix
        Matrix.setIdentityM(mZeroMatrix, 0);

        mStrokes = new ArrayList<>();
        touchQueueSize = new AtomicInteger(0);
        touchQueue = new AtomicReferenceArray<>(TOUCH_QUEUE_SIZE);

        mDrawUiContainer = findViewById(R.id.draw_draw_container);

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
        if (PermissionHelper.hasRequiredPermissions(this)) {

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
        } else {
            // take user to permissions activity
            startActivity(new Intent(this, PermissionsActivity.class));
            finish();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        //mRecordButton.reset();
        //mRecordButton.setListener(this);


        // TODO: Only used id hidden by "Hide UI menu"
        findViewById(R.id.draw_draw_container).setVisibility(View.VISIBLE);

        if (!BuildConfig.SHOW_NAVIGATION) {
            mRecordButton.setVisibility(View.GONE);
            //mOverflowButton.setVisibility(View.GONE);
        }
    }

    /**
     * onPause part of the Android Activity Lifecycle
     */
    @Override
    public void onPause() {

        // Note that the order matters - SurfaceView is paused first so that it does not try
        // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call mSession.update() and get a SessionPausedException.
        mSurfaceView.pause();
        if (mSession != null) {
            mSession.pause();
        }

        mTrackingIndicator.resetTrackingTimeout();
        SessionHelper.setSessionEnd(this);

        super.onPause();
    }

    public void saveStrokes(View v){
        try {
            serializeStorkes(mStrokes);
            Toast.makeText(ARDrawActivity.this,"Strokes saved",Toast.LENGTH_SHORT);
            Log.i("Checkpointer", "Saved!");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void serializeStorkes(List<Stroke> mStrokes) throws IOException {
        try{
            //File outFile = new File(Environment.getExternalStorageDirectory(), "appSaveStroke.data");
            //ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outFile));
            FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("strokeFile.ser", getApplicationContext().MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(mStrokes);
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
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

        // update firebase
        int index = mStrokes.size() - 1;
//        mPairSessionManager.updateStroke(index, mStrokes.get(index));

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
            } else {
                mStrokes.get(index).add(newPoint[i]);
            }
        }

        // update firebase database
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

            // Notify the hostManager of all the anchor updates.
            Collection<Anchor> updatedAnchors = mFrame.getUpdatedAnchors();

            // Update tracking states
            mTrackingIndicator.setTrackingStates(mFrame, mAnchor);
            if (mTrackingIndicator.trackingState == TrackingState.TRACKING && !bHasTracked.get()) {
                bHasTracked.set(true);
            }

            // Get projection matrix.
            mFrame.getCamera().getProjectionMatrix(projmtx, 0, ARSettings.getNearClip(),
                    ARSettings.getFarClip());
            mFrame.getCamera().getViewMatrix(viewmtx, 0);

            // obtain T matrix according to camera pose
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

            // Update line animation
//            for (int i = 0; i < mStrokes.size(); i++) {
//                mStrokes.get(i).update();
//            }
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
                mLineShaderRenderer.updateStrokes(mStrokes,mSharedStrokes);
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
            Log.i("BackgroundRender","Start");
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
                Log.i("LineRender","Start");
            }

            if (mDebugEnabled) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDebugView.setAnchorTracking(mAnchor);
                    }
                });
            }


            PointCloud pointCloud = mFrame.acquirePointCloud();
            this.pointCloud.update(pointCloud);
            this.pointCloud.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();
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

//    private void toggleOverflowMenu() {
//        if (mOverflowLayout.getVisibility() == View.VISIBLE) {
//            hideOverflowMenu();
//        } else {
//            showOverflowMenu();
//        }
//    }
//
//    private void showOverflowMenu() {
//
//    }
//
//    private void hideOverflowMenu() {
//        mOverflowLayout.setVisibility(View.GONE);
//    }


    /**
     * onClickClear handle showing an AlertDialog to clear the drawing
     */
    private void onClickClear() {
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
//        if (isOutsideViewBounds(mOverflowLayout, (int) tap.getRawX(), (int) tap.getRawY())
//                && mOverflowLayout.getVisibility() == View.VISIBLE) {
//            //hideOverflowMenu();
//        }
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

    private File createVideoOutputFile() {

        File tempFile;

        File dir = new File(getCacheDir(), "captures");

        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        Calendar c = Calendar.getInstance();

        String filename = "JustALine_" +
                c.get(Calendar.YEAR) + "-" +
                (c.get(Calendar.MONTH) + 1) + "-" +
                c.get(Calendar.DAY_OF_MONTH)
                + "_" +
                c.get(Calendar.HOUR_OF_DAY) +
                c.get(Calendar.MINUTE) +
                c.get(Calendar.SECOND);

        tempFile = new File(dir, filename + ".mp4");

        return tempFile;

    }

    private void showStrokeDependentUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUndoButton.setVisibility(mStrokes.size() > 0 ? View.VISIBLE : View.GONE);
                mClearDrawingButton.setVisibility(
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
        boolean hideOverflow = true;
        boolean hidePairToolTip = true;
        switch (v.getId()) {
//            case R.id.button_overflow_menu:
//                toggleOverflowMenu();
//                hideOverflow = false;
//                break;
            case R.id.menu_item_clear:
                onClickClear();
                break;
//            case R.id.menu_item_crash:
//                throw new RuntimeException("Intentional crash from overflow menu option");
//            case R.id.menu_item_hide_ui:
//                findViewById(R.id.draw_container).setVisibility(View.INVISIBLE);
//                break;
        }
        mBrushSelector.close();
//        if (hideOverflow) {
//            hideOverflowMenu();
//        }

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
            mLineShaderRenderer.createOnGlThread(ARDrawActivity.this);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    public void requestStoragePermission() {
        PermissionHelper.requestStoragePermission(this, false);
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
            }
        }
    }

    public void setAnchor(Anchor anchor) {
        mAnchor = anchor;

        for (Stroke stroke : mStrokes) {
            Log.d(TAG, "setAnchor: pushing line");
            stroke.offsetToPose(mAnchor.getPose());
        }

        mLineShaderRenderer.bNeedsUpdate.set(true);
    }

    public void onModeChanged(Mode mode) {
        setMode(mode);
    }

    private void showView(View toShow) {
        toShow.setVisibility(View.VISIBLE);
        toShow.animate().alpha(1).start();
    }

    private void hideView(final View toHide) {
        toHide.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                toHide.setVisibility(View.GONE);
            }
        }).start();
    }

    public void enableView(View toEnable) {
        toEnable.setEnabled(true);
        toEnable.animate().alpha(1f);
    }

    public void disableView(View toDisable) {
        toDisable.setEnabled(false);
        toDisable.animate().alpha(.5f);
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


                if (mStrokes.size() > 0) {
                    for (int i = 0; i < mStrokes.size(); i++) {
                        mStrokes.get(i).offsetToPose(pose);
                    }
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }

            }
        });
    }

    public void clearLines() {
        mSharedStrokes.clear();
        mStrokes.clear();
        mLineShaderRenderer.bNeedsUpdate.set(true);
    }


    public void clearAnchor(Anchor anchor) {
        if (anchor != null && anchor.equals(mAnchor)) {
            for (Stroke stroke : mStrokes) {
                stroke.offsetFromPose(mAnchor.getPose());
            }
            mAnchor = null;
            Matrix.setIdentityM(mLineShaderRenderer.mModelMatrix, 0);
        }
    }


    public void onLineAdded(String uid, Stroke value) {
        value.localLine = false;
        value.calculateTotalLength();
        mSharedStrokes.put(uid, value);
        showStrokeDependentUI();
        mLineShaderRenderer.bNeedsUpdate.set(true);
    }

    public void onLineRemoved(String uid) {
        if (mSharedStrokes.containsKey(uid)) {
            mSharedStrokes.remove(uid);
            mLineShaderRenderer.bNeedsUpdate.set(true);
        } else {
            for (Stroke stroke : mStrokes) {
                mStrokes.remove(stroke);
                if (!stroke.finished) {
                    bTouchDown.set(false);
                }
                mLineShaderRenderer.bNeedsUpdate.set(true);
                break;

            }
        }

        showStrokeDependentUI();
    }

    public void onLineUpdated(String uid, Stroke value) {
        Stroke stroke = mSharedStrokes.get(uid);
        if (stroke == null) {
            return;
        }
        stroke.updateStrokeData(value);
        mLineShaderRenderer.bNeedsUpdate.set(true);
    }

    @Override
    public void exitApp() {
        finish();
    }



}
