package com.killerwhale.memary.Presenter;
import com.killerwhale.memary.DataModel.Locations;
import com.opencsv.CSVReader;

import java.io.FileReader;

public class LocationPresenter {
    private Locations[] mlocations = new Locations[1000];
    public LocationPresenter(){
//        double lat = 42.2654;
//        double long1 = -71.1606;
//        for( int i = 0 ; i < mlocations.length; i++){
//            mlocations[i] = new Locations(i,lat, long1);
//            lat-= 0.003;
//            long1 -= 0.003;
//        }
        readDataLineByLine("streetlight-locations.csv");
    }

    public Locations[] getLocations(){
        return mlocations;
    }
    public static void readDataLineByLine(String file)
    {

        try {

            // Create an object of file reader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(file);

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            // we are going to read data line by line
            while ((nextRecord = csvReader.readNext()) != null) {
                for (String cell : nextRecord) {
                    System.out.print(cell + "\t");
                }
                System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
