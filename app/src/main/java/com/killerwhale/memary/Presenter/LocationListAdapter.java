package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.LocationModel;
import com.killerwhale.memary.R;

import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class LocationListAdapter extends BaseAdapter {

    private
    Context context;
    ArrayList<String> location;
    ArrayList<String> address;
    ArrayList<Integer> numPosts;
    ArrayList<Integer> images;
    ArrayList<Float> distance;
    LocationModel[] items;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public LocationListAdapter (Context aContext) {
        address = new ArrayList<>();
        location = new ArrayList<>();
        context = aContext;
//        location = aContext.getResources().getStringArray((R.array.location));
//        address = aContext.getResources().getStringArray((R.array.address));

        images = new ArrayList<Integer>();
        numPosts = new ArrayList<Integer>();
        distance = new ArrayList<Float>();

        for (int i = 0; i < 7; i++){
            images.add(R.drawable.location_image);
            numPosts.add((int) (Math.random() * 10 + 1));
            distance.add((float) (Math.random() * 100));
        }
        db.collection("location")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("firebase", document.getId() + " => " + document.getData());
                                Map tmp = document.getData();
                                for (Object key: tmp.keySet()) Log.i("fetch", key + tmp.get(key).toString());
                                location.add((String) Objects.requireNonNull(tmp.get("name")));
                                address.add((String) Objects.requireNonNull(tmp.get("address")));
                            }
                            Log.d("address", address.toString());
                            Log.d("name", location.toString());
                            notifyDataSetChanged();
                            items = new LocationModel[location.size()];
                        } else {
                            Log.w("firebase", "Error getting documents.", task.getException());
                        }
                    }
                });
        Log.d("address", address.toString());
        Log.d("name", location.toString());
    }

    @Override
    public int getCount() {
        return location.size();
    }

    @Override
    public Object getItem(int position) {
        return location.get(position);
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
        tvLocation.setText(location.get(position));
        tvAddress.setText(address.get(position));
        tvNumPosts.setText(String.valueOf(numPosts.get(position)));
        String string = (distance.get(position)) + " miles";
        tvDistance.setText(string);
        ivImage.setImageResource(R.drawable.location_image);
        return row;
    }

    private void setItems() {
        for (int i = 0; i < this.getCount(); i++) {
            Log.i("menu", String.valueOf(this.getCount()) + items.length);
            LocationModel item = new LocationModel(location.get(i), address.get(i), distance.get(i), 0, numPosts.get(i));
            items[i] = item;
        }
    }

    private void setSortedItems() {
        for (int i = 0; i < items.length; i++) {
            distance.set(i, items[i].distance);
            images.set(i, items[i].img);
            numPosts.set(i, items[i].numPosts);
            location.set(i, items[i].location);
            address.set(i, items[i].address);

        }
    }

    public void sortByDistance() {
        setItems();
        Arrays.sort(items, new Comparator<LocationModel>() {
            @Override
            public int compare(LocationModel o1, LocationModel o2) {
                return Float.compare(o1.distance, o2.distance);
            }
        });
        setSortedItems();
    }

    public void sortByName() {
        setItems();
        Arrays.sort(items, new Comparator<LocationModel>() {
            @Override
            public int compare(LocationModel o1, LocationModel o2) {
                return o1.address.compareTo(o2.address);
            }
        });
        setSortedItems();
    }

    public void sortByPost() {
        setItems();
        Arrays.sort(items, new Comparator<LocationModel>() {
            @Override
            public int compare(LocationModel o1, LocationModel o2) {
                return Integer.compare(o2.numPosts, o1.numPosts);
            }
        });
        setSortedItems();
    }
}