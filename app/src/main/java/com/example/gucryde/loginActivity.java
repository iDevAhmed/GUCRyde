package com.example.gucryde;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;

public class loginActivity extends AppCompatActivity {


    private CallbackManager callbackManager;
 //   private static final String EMAIL = "email";
    private LoginButton loginButton;

    public static int age(JSONObject response){
        int age = 0;
        String year = "";
        String birthdate = "";
        int counter = 0;
        try{
        birthdate = response.get("birthday").toString();}
        catch(JSONException e){
            e.printStackTrace();
        }
        for(int i = 0; i<birthdate.length()-1;i++){

            if(birthdate.charAt(i) == '/')
                counter++;
            if(counter == 2)
                year += birthdate.charAt(i+1);
        }
        age = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(year);
        return age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email","user_birthday"));


        LoginManager.getInstance().registerCallback(callbackManager,
          new FacebookCallback<LoginResult>() {
          @Override
              public void onSuccess(LoginResult loginResult) {
              Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
              finish();
              AccessToken accessToken = AccessToken.getCurrentAccessToken();
              final boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
              // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_birthday"));
              GraphRequest request = GraphRequest.newMeRequest(
                      accessToken,
                      new GraphRequest.GraphJSONObjectCallback() {
                          @Override
                          public void onCompleted(JSONObject object, GraphResponse response) {
                              final String userName;
                              final String email;
                              final int age;
                              try {
                                  if (isLoggedIn) {
                                      userName = (String) object.get("first_name");
                                      age = age(object);
                                      email = (String) object.get("email");
                                      Intent i = new Intent(getApplicationContext(), choice.class);
                                      i.putExtra("email", email);
                                      i.putExtra("name", userName);
                                      startActivity(i);
                                      SaveSharedPreference.setUserName(getApplicationContext(), userName);
                                      new GetUrlContentTask().execute("http://156.192.0.48/index.php?user_name=" + userName + "&age=" + age + "&email=" + email);
                                  }
                              } catch (JSONException e) {
                                  e.printStackTrace();
                              }
                          }
                      });
              Bundle parameters = new Bundle();
              parameters.putString("fields", "id,name,birthday, first_name, email");
              request.setParameters(parameters);
              request.executeAsync();
          }
          @Override
          public void onCancel() {
              Toast.makeText(getApplicationContext(),"Login cancelled!",Toast.LENGTH_SHORT).show();
              LoginManager.getInstance().logOut();
          }
          @Override
          public void onError(FacebookException exception) {
              Toast.makeText(getApplicationContext(),"Login not successful!",Toast.LENGTH_SHORT).show();
              LoginManager.getInstance().logOut();
          }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
        LoginManager.getInstance().logOut();
    }
}


