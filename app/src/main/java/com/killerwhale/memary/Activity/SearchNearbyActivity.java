package com.killerwhale.memary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.killerwhale.memary.DataModel.LocationModel;
import com.killerwhale.memary.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchNearbyActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private ListView nearbyList;
    private ArrayAdapter<String> nearbyAdapter;
    private ArrayList<LocationModel> nearbyArray = new ArrayList<>();
    private ArrayList<String> nearbyAddressArray = new ArrayList<>();
    private HashMap<String, double[]> nameLatLng = new HashMap<>();
    private HashMap<String, String> nameAddress = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_nearby);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCcwwdxW14lLy_YpXScbJW0-TbaQYPJUDQ");
        }
// Create a new Places client instance.
        placesClient = Places.createClient(this);



        startSearch(nameLatLng, nameAddress);



        Handler handler = new Handler();
        Log.d("TAG", "onCreate: " + nearbyAddressArray.size());
        nearbyList = (ListView) findViewById(R.id.NearbyList);
        nearbyAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, nearbyAddressArray);
        nearbyList.setAdapter(nearbyAdapter);
        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String add = (String)parent.getItemAtPosition(position);
                double[] loc = nameLatLng.get(add);
                String address = nameAddress.get(add);
//                Log.i("gg", String.valueOf(loc[0]) + String.valueOf(loc[1]));
                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", add);
                resultIntent.putExtra("latlng", loc);
                resultIntent.putExtra("address", address);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });


    }
    private void startSearch(final HashMap<String, double[]> latlngMap, final HashMap<String, String> addressMap){
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        // Construct a request object, passing the place ID and fields array.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(fields).build();
//        Log.d("TAG", "startSearch: ");
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
        task.addOnSuccessListener( new OnSuccessListener<FindCurrentPlaceResponse>() {
            @Override
            public void onSuccess(FindCurrentPlaceResponse response) {
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    String name = placeLikelihood.getPlace().getName();
                    com.google.android.gms.maps.model.LatLng loc = placeLikelihood.getPlace().getLatLng();
                    String address = placeLikelihood.getPlace().getAddress();
                    double lat = loc.latitude;
                    double lng = loc.longitude;
                    double[] latlng = {lat, lng};
//                    Log.i("TAG", String.format("Place '%s' has likelihood: %f",
//                            name,
//                            placeLikelihood.getLikelihood()));
//                    Log.i("TAG", String.valueOf(latlng[0]) + String.valueOf(latlng[1]));
//                    LocationModel LM = new LocationModel(placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getPlace().getAddress(),0,0,0);
//                    nearbyArray.add(LM);
                    latlngMap.put(name, latlng);
                    addressMap.put(name, address);
                    nearbyAddressArray.add(placeLikelihood.getPlace().getName());
                    nearbyAdapter.notifyDataSetChanged();
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e("TAG", "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }

}
