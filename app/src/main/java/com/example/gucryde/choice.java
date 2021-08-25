package com.example.gucryde;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class choice extends AppCompatActivity {

    private Button riderButton;
    private Button pickUpButton;
    private Button logoutButton;
    private String email;
    private TextView selectRider;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        logoutButton = findViewById(R.id.logOutButton);
        riderButton = findViewById(R.id.riderButton);
        pickUpButton = findViewById(R.id.pickUpButton);


            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                email = extras.getString("email");
                name = extras.getString("name");
            }

            riderButton.setVisibility(View.VISIBLE);
            pickUpButton.setVisibility(View.VISIBLE);
            logoutButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    FacebookSdk.sdkInitialize(getApplicationContext());
                    if(AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                    finish();
                    Intent i = new Intent(getApplicationContext(),loginActivity.class);
                    startActivity(i);
                }
            });
            riderButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View V){
                    new GetUrlContentTask().execute("http://156.192.0.48/rider.php?rider=1&email=" + email);
                    finish();
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    i.putExtra("name",name);
                    i.putExtra("email", email);
                    startActivity(i);
                }
            });

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
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();

    }
}
