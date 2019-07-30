package com.example.whtna.usingnaver;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String[] title;
    double[] mapx;
    double[] mapy;
    Intent intent;

    float map[][];
    int start;
    int end;
    boolean[] check;
    float max;
    ArrayList<LatLng> latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        intent = getIntent();
        title = intent.getStringArrayExtra("title");
        mapx = intent.getDoubleArrayExtra("mapx");
        mapy = intent.getDoubleArrayExtra("mapy");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        latLng = new ArrayList<>();
        // Add a marker in Sydney and move the camera
        LatLng[] latLngs=new LatLng[title.length];
        for(int i=0;title[i]!=null;i++) {
            latLngs[i] = new LatLng(mapx[i],mapy[i]);
            latLng.add(latLngs[i]);
            mMap.addMarker(new MarkerOptions().position(latLng.get(i)).title(title[i]));
        }
        check = new boolean[latLng.size()-1];
        map = new float[latLng.size()-1][latLng.size()-1];

        calDist();

        String url;
        int count = 0;

        if (latLng.size() == 3) {
            url = getUrl(title[1], title[2]);
            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);
            //move map camera
        } else if(latLng.size() >3){
            for(int i=0;i<check.length-1;i++){
                int next = calNum(start);
                url = getUrl(title[start+1],title[next+1]);
                Log.d("onMapClick", url.toString());
                FetchUrl FetchUrl = new FetchUrl();
                FetchUrl.execute(url);
                start = next;
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng.get(0),12));
    }

    public void calDist(){
        max=0;

        Location[] lo= new Location[latLng.size()-1];

        for(int i=0;i<lo.length; i++){
            lo[i] = new Location(title[i+1]);
            lo[i].setLatitude(mapx[i+1]);
            lo[i].setLongitude(mapy[i+1]);
        }

        for(int i =0;i<lo.length;i++){
            map[i][i] = 0;
            for(int j=i+1;j<lo.length;j++){
                map[i][j] = lo[i].distanceTo(lo[j]);
                map[j][i] = map[i][j];
                if(max<map[i][j]) {
                    max = map[i][j];
                    start = i;
                }
            }
        }
        check[start] = true;
    }

    private String getUrl(String ori,String des) {

        // Origin of route
        //String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_origin = "origin=" + ori ;

        // Destination of route
        String str_dest = "destination=" + des ;

        ///String waypoint = "waypoints=optimize:true|";
        // Sensor enabled
        String sensor = "sensor=false";

        String mode = "mode=transit";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor +"&"+mode;

        String key = "&key=AIzaSyB2cfaSAgx8Rf8MPcIWUPz9L7pXojKekSk";
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters+key;


        return url;
    }

    public int calNum(int origin){
        int next;
        float min=max;

        next = 0;
        for (int i = 0;i<map[origin].length; i++) {
            if (check[i] == true)
                continue;
            else if (i == origin)
                continue;
            else if (min >= map[origin][i]) {
                min = map[origin][i];
                next = i;
            }
        }
        check[next] = true;
        return next;
    }
    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
