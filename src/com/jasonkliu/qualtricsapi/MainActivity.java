package com.jasonkliu.qualtricsapi;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MainActivity extends Activity {

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
            .authority("survey.qualtrics.com")
            .appendPath("WRAPI")
            .appendPath("ControlPanel")
            .appendPath("api.php")
            .appendQueryParameter("API_SELECT", "ControlPanel")
            .appendQueryParameter("Version", "2.4")
            .appendQueryParameter("Request", "getSurveys")
            .appendQueryParameter("Format", "JSON")
            .appendQueryParameter("JSONPrettyPrint", "1");
        String myUrl = builder.build().toString();
        Log.d("myURL", myUrl);

        // If you want to use XML Parameters instead, use the lines:
        // .appendQueryParameter("User", "user#group")
        // .appendQueryParameter("Token", "token")

        JsonObject json = new JsonObject();
        json.addProperty("User", new DeveloperKey().USER_ID);
        json.addProperty("Token", new DeveloperKey().DEVELOPER_KEY);

        if (isOnline()) {
            Ion.with(this)
                .load(myUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (result != null)
                            Log.d("JSONresult", result.toString());
                        else
                            Log.d("JSONexcept", e.toString());
                    }
                });
        }
    }

}
