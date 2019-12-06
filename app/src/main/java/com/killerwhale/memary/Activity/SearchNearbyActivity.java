package com.killerwhale.memary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchNearbyActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private ListView nearbyList;
    private ArrayAdapter<String> nearbyAdapter;
    private ArrayList<LocationModel> nearbyArray = new ArrayList<>();
    private ArrayList<String> nearbyAddressArray = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_nearby);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_KEY_HERE");
        }
// Create a new Places client instance.
        placesClient = Places.createClient(this);
        startSearch();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d("TAG", "onCreate: " + nearbyAddressArray.size());
                nearbyList = (ListView) findViewById(R.id.NearbyList);
                nearbyAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, nearbyAddressArray);
                nearbyList.setAdapter(nearbyAdapter);
                nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String add = (String)parent.getItemAtPosition(position);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("address", add);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
            }
        }, 4000);



    }
    private void startSearch(){
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ID);
// Construct a request object, passing the place ID and fields array.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(fields).build();
        Log.d("TAG", "startSearch: ");
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
        task.addOnSuccessListener( new OnSuccessListener<FindCurrentPlaceResponse>() {
            @Override
            public void onSuccess(FindCurrentPlaceResponse response) {
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Log.i("TAG", String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
//                    LocationModel LM = new LocationModel(placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getPlace().getAddress(),0,0,0);
//                    nearbyArray.add(LM);
                    nearbyAddressArray.add(placeLikelihood.getPlace().getName());
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
