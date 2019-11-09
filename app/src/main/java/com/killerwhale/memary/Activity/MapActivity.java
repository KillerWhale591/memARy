package com.killerwhale.memary.Activity;

import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.killerwhale.memary.DataModel.Locations;
import com.killerwhale.memary.Presenter.LocationPresenter;
import com.killerwhale.memary.R;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;


import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.step;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener{
    private static final String CIRCLE_LAYER_ID = "earthquakes-circle";

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private static final String MARKER_IMAGE_CLOSE = "custom-marker-close";
    private static final String MARKER_SOURCE_CLOSE = "markers-source-close";
    private static final String HEATMAP_LAYER_ID = "Location_heat";
    private static final String HEATMAP_LAYER_SOURCE = "Heatmap-source";
    private static final int  ZOOM_THRESHOLD = 11;
    private static final String SELECTED_MARKER = "selected-marker";
    private static final String SELECTED_MARKER_LAYER = "selected-marker-layer";
    private PermissionsManager permissionsManager;
    private Locations[] mlocations;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private ValueAnimator markerAnimator;
    private boolean markerSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map_acitivity);

        /* Map: This represents the map in the application. */
        LocationPresenter LP = new LocationPresenter();
        mlocations = LP.getLocations();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.green_marker));
                style.addImage(MARKER_IMAGE_CLOSE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(),R.drawable.green_dot));
                addMarkers(style);
                enableLocationComponent(style);
                addHeatmapLayer(style);
                addCircleLayer(style);
                mapboxMap.addOnMapClickListener(MapActivity.this);
            }
        });
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getApplicationContext(),"Explaination invalid for now ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                    addHeatmapLayer(style);

                }
            });
        } else {
            Toast.makeText(getApplicationContext(),"Permission not Granted yet", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Style style = mapboxMap.getStyle();
        if (style != null) {
            final SymbolLayer selectedMarkerSymbolLayer =
                    (SymbolLayer) style.getLayer(SELECTED_MARKER_LAYER);
            final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, MARKER_STYLE_LAYER);
            List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
                    pixel, SELECTED_MARKER_LAYER);
            if (selectedFeature.size() > 0 && markerSelected) {
                return false;
            }

            if (features.isEmpty()) {
                if (markerSelected) {
                    deselectMarker(selectedMarkerSymbolLayer);
                }
                return false;
            }
            GeoJsonSource source = style.getSourceAs(SELECTED_MARKER);
            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(
                        new Feature[]{Feature.fromGeometry(features.get(0).geometry())}));
            }

            if (markerSelected) {
                deselectMarker(selectedMarkerSymbolLayer);
            }
            if (features.size() > 0) {
                selectMarker(selectedMarkerSymbolLayer);
            }
        }
        return true;
    }

    private void addMarkers(@NonNull Style loadedMapStyle) {
        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < mlocations.length; i++) {
            features.add(Feature.fromGeometry(Point.fromLngLat(mlocations[i].getmLongtitude(),
                    mlocations[i].getmLatitude())));
            Log.d("Tag",mlocations[i].getmLatitude() + " " + mlocations[i].getmLongtitude());
        }
        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(features)));
        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE_CLOSE,FeatureCollection.fromFeatures(features)));
        loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE)
                .withProperties(
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconImage((step(zoom(),literal(MARKER_IMAGE),
                                stop(ZOOM_THRESHOLD,MARKER_IMAGE_CLOSE)))),
                        iconSize(0.1f),

// Adjust the second number of the Float array based on the height of your marker image.
// This is because the bottom of the marker should be anchored to the coordinate point, rather
// than the middle of the marker being the anchor point on the map.
                        PropertyFactory.iconOffset(new Float[] {0f, -1f})
                ));

        loadedMapStyle.addSource(new GeoJsonSource(SELECTED_MARKER));
        loadedMapStyle.addLayer(new SymbolLayer(SELECTED_MARKER_LAYER, SELECTED_MARKER)
                .withProperties(PropertyFactory.iconImage(MARKER_IMAGE),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{0f, -9f})));
    }

    private  void addHeatmapLayer(@NonNull Style loadedMapStyle){
        HeatmapLayer heatmapLayer = new HeatmapLayer(HEATMAP_LAYER_ID, MARKER_SOURCE);
        heatmapLayer.setMaxZoom(12);
        heatmapLayer.setSourceLayer(HEATMAP_LAYER_SOURCE);
        heatmapLayer.setProperties(
                heatmapColor(interpolate(
                        linear(), heatmapDensity(),
                        literal(0), rgba(33, 102, 172, 0),
                        literal(0.2), rgb(103, 169, 207),
                        literal(0.4), rgb(209, 229, 240),
                        literal(0.6), rgb(253, 219, 199),
                        literal(0.8), rgb(239, 138, 98),
                        literal(1), rgb(178, 24, 43)
                )),
                heatmapWeight(interpolate(
                        linear(), get("mag"),
                        stop(0, 0),
                        stop(6, 1)
                        )),
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 10)
                        )),
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 10)
                        )),
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(9, 1),
                                stop(7, 0)
                        )
                )
        );
        loadedMapStyle.addLayerAbove(heatmapLayer, "waterway-label");
    }
    private void addCircleLayer(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, MARKER_SOURCE);
        circleLayer.setProperties(

// Size circle radius by earthquake magnitude and zoom level
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                literal(100), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 1),
                                        stop(9, 4)
                                ),
                                literal(100), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 5),
                                        stop(9, 50)
                                )
                        )
                ),

// Color circle by earthquake magnitude
                circleColor(
                        interpolate(
                                linear(), get("mag"),
                                literal(1), rgba(33, 102, 172, 0),
                                literal(2), rgb(103, 169, 207),
                                literal(3), rgb(209, 229, 240),
                                literal(4), rgb(253, 219, 199),
                                literal(5), rgb(239, 138, 98),
                                literal(6), rgb(178, 24, 43)
                        )
                ),

// Transition from heatmap to circle layer by zoom level
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(10, 0),
                                stop(11, 1)
                        )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }

    private void selectMarker(final SymbolLayer iconLayer) {
        markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(0.1f, 0.2f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) animator.getAnimatedValue())
                );
            }
        });
        markerAnimator.start();
        markerSelected = true;
    }

    private void deselectMarker(final SymbolLayer iconLayer) {
        markerAnimator.setObjectValues(0.2f, 0.1f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) animator.getAnimatedValue())
                );
            }
        });
        markerAnimator.start();
        markerSelected = false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        if (markerAnimator != null) {
            markerAnimator.cancel();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



}

