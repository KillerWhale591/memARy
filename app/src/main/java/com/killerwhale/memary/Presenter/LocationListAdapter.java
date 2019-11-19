package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.killerwhale.memary.DataModel.LocationModel;
import com.killerwhale.memary.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LocationListAdapter extends BaseAdapter {

    private
    Context context;
    String location[];
    String address[];
    ArrayList<Integer> numPosts;
    ArrayList<Integer> images;
    ArrayList<Float> distance;
    LocationModel[] items;


    public LocationListAdapter (Context aContext) {
        context = aContext;
        location = aContext.getResources().getStringArray((R.array.location));
        address = aContext.getResources().getStringArray((R.array.address));
        images = new ArrayList<Integer>();
        numPosts = new ArrayList<Integer>();
        distance = new ArrayList<Float>();
        items = new LocationModel[this.getCount()];
        for (int i = 0; i < 7; i++){
            images.add(R.drawable.location_image);
            numPosts.add((int) (Math.random() * 100000));
            distance.add((float) (Math.random() * 100));
        }
    }

    @Override
    public int getCount() {
        return location.length;
    }

    @Override
    public Object getItem(int position) {
        return location[position];
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
        tvLocation.setText(location[position]);
        tvAddress.setText(address[position]);
        tvNumPosts.setText(String.valueOf(numPosts.get(position)));
        String string = (distance.get(position)) + " miles";
        tvDistance.setText(string);
        ivImage.setImageResource(R.drawable.location_image);
        return row;
    }

    private void setItems() {
        for (int i = 0; i < this.getCount(); i++) {
            LocationModel item = new LocationModel(location[i], address[i], distance.get(i), 0, numPosts.get(i));
            items[i] = item;
        }
    }

    private void setSortedItems() {
        for (int i = 0; i < items.length; i++) {
            distance.set(i, items[i].distance);
            images.set(i, items[i].img);
            numPosts.set(i, items[i].numPosts);
            location[i] = items[i].location;
            address[i] = items[i].address;

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
                return Integer.compare(o1.numPosts, o2.numPosts);
            }
        });
        setSortedItems();
    }
}