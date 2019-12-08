package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.LocationModel;
import com.killerwhale.memary.R;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocationListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> mLocation;
    private ArrayList<String> mAddress;
    private ArrayList<Integer> mNumPosts;
    private ArrayList<Integer> mImages;
    private ArrayList<Float> mDistance;
    private LocationModel[] locationItems;
    private ArrayList<LocationModel> mLocationModelList = new ArrayList<>();
    private Location currLocation;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mLocRef;
    private GeoFirestore geoFirestore;
    private ArrayList<Query> geoQueries;


    public LocationListAdapter (Context aContext) {
        context = aContext;
        mAddress = new ArrayList<>();
        mLocation = new ArrayList<>();
        mImages = new ArrayList<Integer>();
        mNumPosts = new ArrayList<Integer>();
        mDistance = new ArrayList<Float>();
        mLocRef = db.collection("location");
        geoFirestore = new GeoFirestore(mLocRef);

    }

    public void init(){
        mLocationModelList.clear();
//        db.collection("location")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("firebase", document.getId() + " => " + document.getData());
//                                Map tmp = document.getData();
//                                for (Object key: tmp.keySet()) Log.i("fetch", key + tmp.get(key).toString());
//                                mLocation.add((String) Objects.requireNonNull(tmp.get("name")));
//                                mAddress.add((String) Objects.requireNonNull(tmp.get("address")));
//                                mImages.add(R.drawable.location_image);
//                                mNumPosts.add((int) (Math.random() * 10 + 1));
//                                mDistance.add((float) (Math.random() * 100));
//                            }
////                            Log.d("address", address.toString());
////                            Log.d("name", location.toString());
//                            notifyDataSetChanged();
//                            locationItems = new LocationModel[mLocation.size()];
//                        } else {
//                            Log.w("firebase", "Error getting documents.", task.getException());
//                        }
//                    }
//                });
    }

    public void getLocation(Location location){
        currLocation = location;
    }

    @Override
    public int getCount() {
        return mLocationModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLocationModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_location_item, parent, false);
        }
        else row = convertView;

        ImageView ivImage = (ImageView) row.findViewById(R.id.imageView);
        TextView tvLocation = (TextView) row.findViewById(R.id.tvLocation);
        TextView tvAddress = (TextView) row.findViewById(R.id.tvAddress);
        TextView tvNumPosts = (TextView) row.findViewById(R.id.tvNumPosts);
        TextView tvDistance = (TextView) row.findViewById(R.id.tvDistance);
//        tvLocation.setText(mLocation.get(position));
//        tvAddress.setText(mAddress.get(position));
//        String string = (mDistance.get(position)) + " miles";
//        tvDistance.setText(string);
        ivImage.setImageResource(R.drawable.location_image);
//        tvNumPosts.setText(String.valueOf(mNumPosts.get(position)));

        try{
            tvLocation.setText(mLocationModelList.get(position).getLocation());
            tvAddress.setText(mLocationModelList.get(position).getAddress());
            tvDistance.setText(mLocationModelList.get(position).getDistance(currLocation));
            tvNumPosts.setText(mLocationModelList.get(position).getPosts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }



    private void setSortedItems() {
        for (int i = 0; i < locationItems.length; i++) {
            mDistance.set(i, locationItems[i].distance);
            mNumPosts.set(i, locationItems[i].numPosts);
            mLocation.set(i, locationItems[i].location);
            mAddress.set(i, locationItems[i].address);

        }
    }

    /**
     * Get documents from database, query by nearby radius area
     * @param adapter adapter
     * @param radius nearby searching radius
     */
    public void queryByDistance(final LocationListAdapter adapter, double radius) {
        mLocationModelList.clear();
        Log.i("location", String.valueOf(currLocation.getLatitude()) + String.valueOf(currLocation.getLongitude()));
        GeoQuery geoQuery = geoFirestore.queryAtLocation(
                new GeoPoint(currLocation.getLatitude(), currLocation.getLongitude()), 30000);
        geoQueries = geoQuery.getQueries();
        for (final Query query : geoQueries) {
            if (query != null) {
                query.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                Log.i("documentsize", String.valueOf(documents.size()));
                                if (documents.size() > 0) {
                                    for (DocumentSnapshot document : documents) {
                                        if (document != null) {
                                            mLocationModelList.add(new LocationModel(document.getData()));
                                        }
                                    }
                                    notifyDataSetChanged();
                                }
                            }
                        });
            }
        }
    }

    /**
     * Get documents from database, query by name of location
     * @param adapter adapter
     */
    public void queryByName(final LocationListAdapter adapter) {
        mLocationModelList.clear();
        Query nameQuery = mLocRef.orderBy("name", Query.Direction.ASCENDING);
            nameQuery.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            if (documents.size() > 0) {
                                for (DocumentSnapshot document : documents) {
                                    if (document != null) {
                                        mLocationModelList.add(new LocationModel(document.getData()));
                                    }
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
    }

    /**
     * Get documents from database, query by number of posts
     * @param adapter adapter
     */
    public void queryByPosts(final LocationListAdapter adapter) {
        mLocationModelList.clear();
        Query nameQuery = mLocRef.orderBy("posts", Query.Direction.DESCENDING);
        nameQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        if (documents.size() > 0) {
                            for (DocumentSnapshot document : documents) {
                                if (document != null) {
                                    mLocationModelList.add(new LocationModel(document.getData()));
                                }
                            }
                            notifyDataSetChanged();
                        }
                    }
                });
    }
}