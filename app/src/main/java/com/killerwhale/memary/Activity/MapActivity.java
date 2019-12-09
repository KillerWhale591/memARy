package com.killerwhale.memary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import android.view.MenuItem;
import android.widget.TextView;

import com.killerwhale.memary.R;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
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

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
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
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import com.mapbox.mapboxsdk.annotations.BubbleLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener{

    private static final String CIRCLE_LAYER_ID = "circle";
    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private static final String HEATMAP_LAYER_ID = "Location_heat";
    private static final String HEATMAP_LAYER_SOURCE = "Heatmap-source";
    private static final int  ZOOM_THRESHOLD = 11;
    private static final String MARKER_SOURCE_LOCATION = "markers-source-location";
    private static final String CIRCLE_LAYER_ID_LOCATION ="circle-location" ;
    private static final String MARKER_STYLE_LAYER_LOCATION = "markers-style-layer-location";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 001;


    private static final String SEARCH_MARKER_SOURCE = "search-marker-source";
    private static final String SEARCH_IMAGE = "search-marker";
    private static final String SEARCH_MARKER_LAYER = "search-marker-layer";
    private static final String CAMERA_LOCATION_SOURCE ="camera-location-source" ;
    private static final String CAMERA_LOCATION_LAYER = "camera-location-layer";

    private static final String INFO_WINDOW_LAYER = "info-window-layer";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String TAG = "MAP";
    private FeatureCollection featureCollection;
    private GeoJsonSource source;
    private int formalSelectLocationIndex = -1;

    private FloatingActionButton fabCenterCamera;
    private FloatingActionButton fabTogglePostLocation;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private int displayMarkerType = 0;// 0 = post, 1 = location
    private FirebaseFirestore db;
    private CollectionReference  mLocRef;
    private CollectionReference mPostRef;
    private ArrayList<Location> mLocations = new ArrayList<>();
    private ArrayList<Location> mPostLocations = new ArrayList<>();
    private Double cameralat;
    private Double cameralong;
    private BottomNavigationView navBar;

    private LatLng passInPoint = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create instance of mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map_acitivity);
        db = FirebaseFirestore.getInstance();
        //Bottom navigation bar
        navBar = findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_map:
                        break;
                    case R.id.action_posts:
                        startActivity(new Intent(getBaseContext(), PostFeedActivity.class));
                        finish();
                        break;
                    case R.id.action_places:
                        startActivity(new Intent(getBaseContext(), LocationListActivity.class));
                        finish();
                        break;
                    case R.id.action_profile:
                        startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                        finish();
                        break;
                    default:
                        Log.i(TAG, "Unhandled nav click");

                }
                return true;
            }
        });
/**
 *         Step 1: Read Mapbox android apis
 */
        /* Map: This represents the map in the application. */
        /**
         * initiate firebase connections, get geo location informations
         */
        mLocRef = db.collection("location");
        mPostRef = db.collection("posts");
//        Log.d("Tag1",mLocRef.getPath());

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
        mapboxMap.setMinZoomPreference(6);
        //Step3: set up sytles, there are bunch of styles for us to choose
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.map_marker));

                //init* functions: retrieve data from firebase or other class, put them in features
                enableLocationComponent(style);
                initPost(style);
                initLocation(style);
                initCameraPosition(style);
                initSearchFab();
                Intent intent = getIntent();
                String id = intent.getStringExtra("uid");
                String name = intent.getStringExtra("name");
                //get a intent from location list, default is 0
                cameralat = intent.getDoubleExtra("lat", 0);
                cameralong = intent.getDoubleExtra("long", 0);

                passInPoint = new LatLng(cameralat, cameralong);

                //add* functions: based on the init functions data, paint the map with different colors
                addHeatmapLayer(style);
                addCircleLayer(style);
                addCircleLayerLocation(style);
                addPostMarkers(style);
                addLocationMarkers(style);
                setUpInfoWindowLayer(style);
                style.addImage(SEARCH_IMAGE, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.blue_marker_view));
                setupSearchSource(style);
                setupSearchLayer(style);
                //toggle between location map and post map
                toggleLayer(displayMarkerType);
                mapboxMap.addOnMapClickListener(MapActivity.this);
                //if lat and long = 0 start map activity to current location
                if(cameralat != 0 || cameralong != 0) {
                    displayMarkerType  = 1;
                    updateMarkerPosition(passInPoint);
                }

                fabCenterCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCameratoCurrentLocation();
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

    private void initCameraPosition(Style style) {
        style.addSource(new GeoJsonSource(CAMERA_LOCATION_SOURCE));
        style.addLayer(new SymbolLayer(CAMERA_LOCATION_LAYER,CAMERA_LOCATION_SOURCE).withProperties(
                iconImage(MARKER_IMAGE),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(0.07f)
        ));

    }

    private void setCameratoCurrentLocation(){
        double lat1= mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
        double long1 = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
        if(mapboxMap.getLocationComponent()!= null){
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .zoom(ZOOM_THRESHOLD)
                    .target(new LatLng(lat1, long1))
                    .bearing(0)
                    .build());
        }
    }
    /**
     * Get documents from database, set these location(lat and longs) to featurecollections(think it as dots, but transparent, its there but you cant see it)
     */
        private void initLocation(@NonNull final Style loadedMapStyle) {
        mLocRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                List<Feature> featureLocation = new ArrayList<>();
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

                if (documents.size() > 0) {
                    for (DocumentSnapshot document : documents) {
                        Location l = new Location(document.getId());
                        GeoPoint geoPoint = document.getGeoPoint("geopoint");
                        if(geoPoint != null) {
                            l.setLatitude(geoPoint.getLatitude());
                            l.setLongitude(geoPoint.getLongitude());
                            mPostLocations.add(l);
                            Feature currFeature = Feature.fromGeometry(Point.fromLngLat(geoPoint.getLongitude(),geoPoint.getLatitude()));
                            String name = document.getString("name");
                            currFeature.addStringProperty("name", name);
                            currFeature.addBooleanProperty(PROPERTY_SELECTED, false);
                            featureLocation.add(currFeature);
                            //set on click to a location, if you click, a bubble layout with name and address will show
                            BubbleLayout bubbleLayout = (BubbleLayout) inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);
                            String address = document.getString("address");
                            TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                            titleTextView.setText(name);
                            TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
                            descriptionTextView.setText(address);
                            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                            bubbleLayout.measure(measureSpec, measureSpec);
                            bubbleLayout.setScaleY(0.5f);
                            float measuredWidth = bubbleLayout.getMeasuredWidth();
                            bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);
                            Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                            imagesMap.put(name, bitmap);
                        }
                    }
                }
                source = new GeoJsonSource(MARKER_SOURCE_LOCATION, FeatureCollection.fromFeatures(featureLocation));
                featureCollection = FeatureCollection.fromFeatures(featureLocation);
                loadedMapStyle.addSource(source);
//                    Log.i("gg", String.valueOf(featureLocation.size()));
//                    Log.i("gg", String.valueOf(imagesMap.keySet().size()));
                loadedMapStyle.addImages(imagesMap);
            }
        });
    }

    /**
     * Similar to initLocation, but this is position of posts, instead of tagged locations
     * @param loadedMapStyle: the style passed in, if you want to know what is Style, recommand read mapbox documentation, think like its a container that contain layers
     */
    private void initPost(@NonNull final Style loadedMapStyle) {
        mPostRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                List<Feature> featureLocation = new ArrayList<>();
                if (documents.size() > 0) {
                    for (DocumentSnapshot document : documents) {
                        Location l = new Location(document.getId());
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        if(geoPoint != null) {
                            l.setLatitude(geoPoint.getLatitude());
                            l.setLongitude(geoPoint.getLongitude());
                            mLocations.add(l);
                            Feature currFeature = Feature.fromGeometry(Point.fromLngLat(geoPoint.getLongitude(),geoPoint.getLatitude()));
                            featureLocation.add(currFeature);
                        }
                    }
                    Log.d("dd", "onSucces" + featureLocation.size());
                    loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(featureLocation)));
                }
            }
        });
    }

    /**
     * Utility class to generate Bitmaps for Symbol.
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);
            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();
            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }



    private void updateMarkerPosition(LatLng position) {
// This method is were we update the marker position once we have new coordinates. First we
// check if this is the first time we are executing this handler, the best way to do this is
// check if marker is null;
        if (mapboxMap.getStyle() != null) {
            GeoJsonSource cameraSource = mapboxMap.getStyle().getSourceAs(CAMERA_LOCATION_SOURCE);
            if (cameraSource != null) {
                cameraSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }

// Lastly, animate the camera to the new position so the user
// wont have to search for the marker and then return.
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(position))
                        .zoom(ZOOM_THRESHOLD)
                        .build()), 4000);

    }



    /**
     * Setup a layer with Android SDK call-outs
     * <p>
     * name of the feature is used as key for the iconImage
     * </p>
     */
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(INFO_WINDOW_LAYER, MARKER_SOURCE_LOCATION)
                .withProperties(
                        /* show image with id title based on the value of the name feature property */
                        iconImage("{name}"),

                        /* set anchor of icon to bottom-left */
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        /* all info window and marker image to appear at the same time*/
                        iconAllowOverlap(true),

                        /* offset the info window to be above the marker */
                        iconOffset(new Float[] {-2f, -28f})
                )
                /* add a filter to show only when selected feature property is true */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
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
                Layer InfoWindowLayer = style.getLayer(INFO_WINDOW_LAYER);
                try {
                    if (PostLayer != null) {
                        if (displayMarkerType == 1) {
                            PostLayer.setProperties(visibility(NONE));
                            PostCirCleLayer.setProperties(visibility(NONE));
                            heatmapLayer.setProperties(visibility(NONE));
                            InfoWindowLayer.setProperties(visibility(VISIBLE));
                            LocationLayer.setProperties(visibility(VISIBLE));

                        } else {
                            PostLayer.setProperties(visibility(VISIBLE));
                            PostCirCleLayer.setProperties(visibility(VISIBLE));
                            heatmapLayer.setProperties(visibility(VISIBLE));
                            InfoWindowLayer.setProperties(visibility(NONE));
                            LocationLayer.setProperties(visibility(NONE));

                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
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
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }

    /**
     * This method handles click events for SymbolLayer symbols.
     * <p>
     * When a SymbolLayer icon is clicked, we move that feature to the selected state.
     * </p>
     *
     * @param screenPoint the point on screen clicked
     */
    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_STYLE_LAYER_LOCATION);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty("name");
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty("name").equals(name)) {
                        if (featureSelectStatus(i)) {
                            setFeatureSelectState(featureList.get(i), false);
//                            formalSelectLocationIndex = -1;
                        } else {
//                            Log.i("gg", String.valueOf(formalSelectLocationIndex));
//                            Log.i("gg", String.valueOf(i));
//                            if(formalSelectLocationIndex != -1 && formalSelectLocationIndex != i) {
//                                setFeatureSelectState(featureList.get(formalSelectLocationIndex), false);
//                                formalSelectLocationIndex = i;
//                            }
                            setSelected(i);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private void setSelected(int index) {
        if (featureCollection.features() != null) {
            Feature feature = featureCollection.features().get(index);
            setFeatureSelectState(feature, true);
            refreshSource();
        }
    }


    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource();
        }
    }


    /**
     * Updates the display of data on the map after the FeatureCollection has been modified
     */
    private void refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }


    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
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

    }


    private void addPostMarkers(@NonNull Style loadedMapStyle){

        /**
         * add the features and assigned an ID to him. Example: MARKER_SOURCE
         */

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
        heatmapLayer.setMaxZoom(ZOOM_THRESHOLD);
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
                circleColor(rgba(245, 130, 140, 1)),
// Transition from heatmap to circle layer by zoom level
                circleStrokeColor("white"),
                circleStrokeWidth(0.4f),
                circleOpacity(
                        interpolate(
                                linear(),zoom(),
                                stop(10, 0),
                                stop(13, 1)
                        )
                ),
                circleRadius(5.5f)

        );
        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }
    private void addCircleLayerLocation(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID_LOCATION, MARKER_SOURCE_LOCATION);
        circleLayer.setProperties(
                circleColor(rgba(248, 200, 218, 1)),
// Transition from heatmap to circle layer by zoom level
                circleStrokeColor("white"),
                circleStrokeWidth(0.2f),
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 1),
                                stop(14, 0)
                        ))
        );
        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
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
    // mapbox method from life cycles
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        navBar.setSelectedItemId(R.id.action_map);

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
        mapView.onDestroy();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

