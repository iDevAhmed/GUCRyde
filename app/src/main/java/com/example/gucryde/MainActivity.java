package com.example.gucryde;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import bolts.Task;

public class MainActivity extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Location location;
    protected boolean gps_enabled, network_enabled;

    private Button logoutButton;
    private Button pickUpButton;
    private String email;
    private String name;
    private TextView selectRider;
    private TextView userName;
    String area;
    Location gps_loc;
    Location network_loc;
    Location final_loc;
    double longitude;
    double latitude;
    String userCountry, address;
    int PERMISSION_ID = 44;
    ListView listView;
    private static final int REQUEST_PERMISSION_LOCATION = 255;

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                email = extras.getString("email");
                name = extras.getString("name");
            }

            logoutButton = findViewById(R.id.logOutButton);
            pickUpButton = findViewById(R.id.pickUpButton);
            selectRider = findViewById(R.id.nowRider);
            userName = findViewById(R.id.userNameDisplay);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            try {

                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (gps_loc != null) {
                final_loc = gps_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            } else if (network_loc != null) {
                final_loc = network_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            } else {
                latitude = 0.0;
                longitude = 0.0;
            }


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);

            try {

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {


                    userCountry = addresses.get(0).getCountryName();
                    address = addresses.get(0).getAddressLine(0);
                    area = addresses.get(0).getPostalCode();

                    new GetUrlContentTask().execute("http://156.192.0.48/addArea.php?area=" + area + "&address="+ address + "&email=" + email);
                    System.out.println("Address: " + address + " " + area);
                } else {
                    userCountry = "Unknown";
                    System.out.println(userCountry);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (SaveSharedPreference.getUserName(MainActivity.this).length() == 0) {
            selectRider.setVisibility(View.GONE);
            System.out.println(SaveSharedPreference.getUserName(getApplicationContext()) + " halaa ");
            Intent i = new Intent(getApplicationContext(), loginActivity.class);
            startActivity(i);
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                name = extras.getString("name");
                userName.setText(name);
            }

            logoutButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FacebookSdk.sdkInitialize(getApplicationContext());
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                    Intent i = new Intent(getApplicationContext(), loginActivity.class);
                    startActivity(i);
                }
            });

            pickUpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                    Intent i = new Intent(getApplicationContext(), pickUpActivity.class);
                    startActivity(i);
                }
            });

        }
        new GetUrlContentTask().execute("http://156.192.0.48/riderOptions.php?area=" + area);
       // getJSON("http://192.168.31.203/webcheerz/api.php");
        listView = (ListView) findViewById(R.id.riderOptions);
       getJSON("http://156.192.0.48/riderOptions.php?area=" + area);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mockEmail = "moe@hotmail.com";
                String o = (String )listView.getItemAtPosition(position);
                new GetUrlContentTask().execute("http://156.192.0.48/connectRider.php?email=" + mockEmail);
                getJSON("http://156.192.0.48/riderOptions.php?area=" + area);
                Toast.makeText(getBaseContext(),"You are now connected to " + toName(o) ,Toast.LENGTH_SHORT).show();
            }
        });


    }

    public static String toName(String option){
        String name = "";
        for (int i = 0; i<option.length();i++){

            if (option.charAt(i) != '-'){
                name += option.charAt(i);
            }
            else {
                return name;
            }
        }

return name;
    }
    private class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String content = "", line;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {

        }
    }
   /* @Override
    public void onStart(){
        super.onStart();
        if(SaveSharedPreference.getUserName(MainActivity.this).length() == 0)
        {
            selectRider.setVisibility(View.GONE);
            System.out.println(SaveSharedPreference.getUserName(getApplicationContext()));
            Intent i = new Intent(getApplicationContext(),loginActivity.class);
            startActivity(i);
        } else{

        }
    }*/

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }




    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().logOut();
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
            }
        }
    }

    private void getJSON(final String urlWebService) {
        /*
         * As fetching the json string is a network operation
         * And we cannot perform a network operation in main thread
         * so we need an AsyncTask
         * The constrains defined here are
         * Void -> We are not passing anything
         * Void -> Nothing at progress update as well
         * String -> After completion it should return a string and it will be the json string
         * */
        class GetJSON extends AsyncTask<Void, Void, String> {

            //this method will be called before execution
            //you can display a progress bar or something
            //so that user can understand that he should wait
            //as network operation may take some time
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            //this method will be called after execution
            //so here we are displaying a toast with the json string
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {

                        //appending it to string builder
                        sb.append(json + "\n");
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }




    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);

        String[] heroes = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            heroes[i] = obj.getString("name") + " - "+  obj.getString("address");
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, heroes);

        listView.setAdapter(arrayAdapter);


    }



}

