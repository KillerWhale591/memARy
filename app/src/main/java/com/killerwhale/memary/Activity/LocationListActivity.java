package com.killerwhale.memary.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.killerwhale.memary.Presenter.LocationListAdapter;
import com.killerwhale.memary.R;

public class LocationListActivity extends AppCompatActivity {

    private
    ListView locationList;
    ListAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        locationList = (ListView) findViewById(R.id.locationList);
        locationAdapter = new LocationListAdapter(this.getBaseContext());
        locationList.setAdapter(locationAdapter);

//        locationList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        LocationListAdapter adapter = (LocationListAdapter) locationAdapter;
        switch (id){
            case R.id.mnu_distance:
                adapter.sortByDistance();
                break;
            case R.id.mnu_name:
                adapter.sortByName();
                break;
            case R.id.mnu_posts:
                adapter.sortByPost();
                break;
        }
        locationList.setAdapter((ListAdapter) adapter);
        return true;
    }
}