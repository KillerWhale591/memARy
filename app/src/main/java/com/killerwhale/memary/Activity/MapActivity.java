package com.killerwhale.memary.Activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.killerwhale.memary.R;

import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
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

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener{
    private static final String CIRCLE_LAYER_ID = "circle";
    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private static final String HEATMAP_LAYER_ID = "Location_heat";
    private static final String HEATMAP_LAYER_SOURCE = "Heatmap-source";
    private static final int  ZOOM_THRESHOLD = 14;
    private static final String SELECTED_MARKER = "selected-marker";
    private static final String SELECTED_MARKER_LAYER = "selected-marker-layer";
    private static final String MARKER_SOURCE_LOCATION = "markers-source-location";
    private static final String CIRCLE_LAYER_ID_LOCATION ="circle-location" ;
    private static final String MARKER_STYLE_LAYER_LOCATION = "markers-style-layer-location";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 001;

    private static final String SEARCH_MARKER_SOURCE = "search-marker-source";
    private static final String SEARCH_IMAGE = "search-marker";
    private static final String SEARCH_MARKER_LAYER = "search-marker-layer";

    private FloatingActionButton fabCenterCamera;
    private FloatingActionButton fabTogglePostLocation;
    private Location[] mlocations;
    private Location[] mLocationLocation;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private ValueAnimator markerAnimator;
    private boolean markerSelected = false;
    private int displayMarkerType = 0;// 0 = post, 1 = location
    private CarmenFeature home;
    private CarmenFeature work;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map_acitivity);
/**
 *         Step 1: Read Mapbox android apis
 */
        /* Map: This represents the map in the application. */
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("a.csv")));
            mlocations = new Location[473];
            String[] nextLine;
            int i = 0;
            while ((nextLine = reader.readNext()) != null && i < 473) {
                // nextLine[] is an array of values from the line
                NumberFormat f = NumberFormat.getInstance();
                String lat = nextLine[3].trim();
                System.out.println(lat);
                String long1 =  nextLine[4].trim();
                System.out.println(long1);

                float lat1 = Float.parseFloat(lat);
                float longd = Float.parseFloat(long1);
                mlocations[i] = new Location(Integer.toString(i));
                mlocations[i].setLatitude(Double.valueOf(lat1));
                mlocations[i].setLongitude(Double.valueOf(longd));
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mLocationLocation = new Location[9];
        mLocationLocation[0] = new Location(Integer.toString(0));
        mLocationLocation[0].setLatitude(42.355084);
        mLocationLocation[0].setLongitude(-71.061482);
        mLocationLocation[1] = new Location(Integer.toString(0));
        mLocationLocation[1].setLatitude(42.357621);
        mLocationLocation[1].setLongitude(-71.054787);
        mLocationLocation[2] = new Location(Integer.toString(0));
        mLocationLocation[2].setLatitude(42.363900);
        mLocationLocation[2].setLongitude(-71.067834);
        mLocationLocation[3] = new Location(Integer.toString(0));
        mLocationLocation[3].setLatitude(42.349068);
        mLocationLocation[3].setLongitude(-71.082115);
        mLocationLocation[4] = new Location(Integer.toString(0));
        mLocationLocation[4].setLatitude(42.346890);
        mLocationLocation[4].setLongitude(-71.091979);
        mLocationLocation[5] = new Location(Integer.toString(0));
        mLocationLocation[5].setLatitude(42.350681);
        mLocationLocation[5].setLongitude(-71.085702);
        mLocationLocation[6] = new Location(Integer.toString(0));
        mLocationLocation[6].setLatitude(42.352642);
        mLocationLocation[6].setLongitude(-71.078965);
        mLocationLocation[7] = new Location(Integer.toString(0));
        mLocationLocation[7].setLatitude(42.346833);
        mLocationLocation[7].setLongitude(-71.071259);
        mLocationLocation[8] = new Location(Integer.toString(0));
        mLocationLocation[8].setLatitude(42.360336);
        mLocationLocation[8].setLongitude(-71.087731);



        mapView = findViewById(R.id.mapView);
        fabCenterCamera = (FloatingActionButton)findViewById(R.id.fabCenterCam);
        fabTogglePostLocation = (FloatingActionButton)findViewById(R.id.fabTogglePostLocation);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        //Setting max/min zoom level for camera
        mapboxMap.setMaxZoomPreference(18);
        mapboxMap.setMinZoomPreference(7);
        //Step3: set up sytles, there are bunch of styles for us to choose
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.map_marker));

                initMarkerPosition(style);
                initSearchFab();
                enableLocationComponent(style);
                addHeatmapLayer(style);
                addCircleLayer(style);
                addCircleLayerLocation(style);
                addPostMarkers(style);
                addLocationMarkers(style);
                addUserLocations();
                style.addImage(SEARCH_IMAGE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.blue_marker_view));
                setupSearchSource(style);
                setupSearchLayer(style);
                mapboxMap.addOnMapClickListener(MapActivity.this);
                fabCenterCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        double lat1= mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
                        double long1 = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
                        if(mapboxMap.getLocationComponent()!= null){
                            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                                    .zoom(13)
                                    .target(new LatLng(lat1, long1))
                                    .bearing(0)
                                    .build());
                        }
                    }
                });
                fabTogglePostLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(displayMarkerType == 0){
                            displayMarkerType = 1;
                            toggleLayer(displayMarkerType);
                        }else if(displayMarkerType == 1){
                            displayMarkerType = 0;
                            toggleLayer(displayMarkerType);
                        }


                    }
                });
            }
        });
    }
    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("50 Beale St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-71.0338348, 41.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }
    private void setupSearchLayer(Style style) {
        style.addLayer(new SymbolLayer(SEARCH_MARKER_LAYER,SEARCH_MARKER_SOURCE).withProperties(
                iconImage(SEARCH_IMAGE),
                iconOffset(new Float[]{0f, -8f}),
                iconSize(0.3f))
        );
    }

    private void setupSearchSource(@NonNull Style style) {
        style.addSource(new GeoJsonSource(SEARCH_MARKER_SOURCE));
    }

    private void toggleLayer(final int displayMarkerType) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Layer PostLayer = style.getLayer(MARKER_STYLE_LAYER);
                Layer PostCirCleLayer = style.getLayer(CIRCLE_LAYER_ID);
                Layer heatmapLayer = style.getLayer(HEATMAP_LAYER_ID);
                Layer LocationLayer = style.getLayer(MARKER_STYLE_LAYER_LOCATION);
                if (PostLayer != null) {
                    if (displayMarkerType == 1) {
                        PostLayer.setProperties(visibility(NONE));
                        PostCirCleLayer.setProperties(visibility(NONE));
                        heatmapLayer.setProperties(visibility(NONE));
                        LocationLayer.setProperties(visibility(VISIBLE));

                    } else {
                        PostLayer.setProperties(visibility(VISIBLE));
                        PostCirCleLayer.setProperties(visibility(VISIBLE));
                        heatmapLayer.setProperties(visibility(VISIBLE));
                        LocationLayer.setProperties(visibility(NONE));

                    }
                }
            }
        });
    }

    //Step 4: step up location component, where u are basically
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
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
    }




//Step 5; enable clickable symbol( marker)
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
//add Symbol layer
    private void addLocationMarkers(@NonNull Style loadedMapStyle) {

        /**
         * add the features and assigned an ID to him. Example: MARKER_SOURCE
         */
        loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER_LOCATION, MARKER_SOURCE_LOCATION)
                .withProperties(
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconImage((step(zoom(), literal(CIRCLE_LAYER_ID_LOCATION),
                                stop(ZOOM_THRESHOLD, MARKER_IMAGE)))),
//                        PropertyFactory.iconImage(MARKER_IMAGE),
                        iconOffset(new Float[]{0f, -9f}),
                        visibility(NONE),
                        iconSize(0.07f)
                        ));

// Adjust the second number of the Float array based on the height of your marker image.
// This is because the bottom of the marker should be ancon
        loadedMapStyle.addLayer(new SymbolLayer(SELECTED_MARKER_LAYER, SELECTED_MARKER)
                .withProperties(PropertyFactory.iconImage(MARKER_IMAGE),
                        iconOffset(new Float[]{0f, -70f}),
                        iconSize(0.10f),
                        visibility(NONE),
                        iconAllowOverlap(true)));

    }
    private void initMarkerPosition(@NonNull Style loadedMapStyle){
        List<Feature> features = new ArrayList<>();
        /**
         *         Step 6:get geo information, add to features
         */
        for (int i = 0; i < mlocations.length; i++) {
            features.add(Feature.fromGeometry(Point.fromLngLat(mlocations[i].getLongitude(),
                    mlocations[i].getLatitude())));
        }
        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(features)));
        loadedMapStyle.addSource(new GeoJsonSource(SELECTED_MARKER));
        List<Feature> featureLocation = new ArrayList<>();
        for (int i = 0; i < mLocationLocation.length; i++) {
            featureLocation.add(Feature.fromGeometry(Point.fromLngLat(mlocations[i].getLongitude(),
                    mLocationLocation[i].getLatitude())));
        }
        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE_LOCATION, FeatureCollection.fromFeatures(featureLocation)));

    }

    private void addPostMarkers(@NonNull Style loadedMapStyle){

        /**
         * add the features and assigned an ID to him. Example: MARKER_SOURCE
         */
        //should use if statemtn to check if already exist or not

        loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE)
                .withProperties(
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconImage(CIRCLE_LAYER_ID),
                        iconOffset(new Float[]{0f, -9f}),
                        iconSize(0.7f)
                ));

// Adjust the second number of the Float array based on the height of your marker image.
// This is because the bottom of the marker should be ancon
    }

//Step 7: add heatmap layer
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
                                stop(7, 1),
                                stop(12, 0)
                        )
                )
        );
        loadedMapStyle.addLayerAbove(heatmapLayer, "waterway-label");
    }
    /*
     * add circle layer
     */
    private void addCircleLayer(@NonNull Style loadedMapStyle) {

        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, MARKER_SOURCE);
        circleLayer.setProperties(
                circleColor(rgba(123, 239, 178, 1)),
// Transition from heatmap to circle layer by zoom level
                circleStrokeColor("white"),
                circleStrokeWidth(0.2f),
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop( 12, 0),
                                stop(16, 1)
                        )
                ),
                circleRadius(
                        interpolate(
                                linear(), zoom(),

                                stop(16, 1)
                                ,stop(12, 4)
                ))
        );
        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }
    private void addCircleLayerLocation(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID_LOCATION, MARKER_SOURCE_LOCATION);
        circleLayer.setProperties(
                circleColor(rgba(123, 239, 178, 1)),
// Transition from heatmap to circle layer by zoom level
                circleStrokeColor("white"),
                circleStrokeWidth(0.2f),
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop( 12, 0),
                                stop(16, 1)
                        )
                ),
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(12, 4),
                                stop(16, 1)
                        ))
        );
        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }

    private void selectMarker(final SymbolLayer iconLayer) {
        markerSelected = true;
    }

    private void deselectMarker(final SymbolLayer iconLayer) {

        markerSelected = false;
    }
    private void initSearchFab() {
        findViewById(R.id.fabSearchGlobal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

// Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

// Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
// Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(SEARCH_MARKER_SOURCE);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

// Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }
            }
        }
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

