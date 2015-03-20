package com.jasonkliu.qualtricsapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    TextView display;
    Button linksurvey;
    String lastsurveyid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = (TextView) findViewById(R.id.tvDisplay);
        linksurvey = (Button) findViewById(R.id.bLinkSurvey);

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

        /* Result is of type:
         * {
         *    "Meta": {
         *        "Status": "Success",
         *        "Debug": ""
         *    },
         *    "Result": {
         *        "Surveys": [
         *            {
         *                "responses": "1",
         *                "SurveyType": "SV",
         *                "SurveyID": "SV_eidnumber",
         *                "SurveyName": "SurveyName",
         *                "SurveyDescription": "SurveyDescritption",
         *                "SurveyOwnerID": "UR_aidnumber",
         *                "SurveyStatus": "Active",
         *                "SurveyStartDate": "0000-00-00 00:00:00",
         *                "SurveyExpirationDate": "0000-00-00 00:00:00",
         *                "SurveyCreationDate": "2015-03-19 12:06:13",
         *                "CreatorID": "UR_aidnumber",
         *                "LastModified": "2015-03-19 12:07:06",
         *                "LastActivated": "2015-03-19 12:07:06",
         *                "UserFirstName": "User",
         *                "UserLastName": "Name"
         *            }
         *            {
         *                ...
         *            }
         *        ]
         *    }
         *}
         */

        JsonObject json = new JsonObject();
        json.addProperty("User", new DeveloperKey().USER_ID);
        json.addProperty("Token", new DeveloperKey().DEVELOPER_KEY);

        if (!isOnline()) { Log.d("onCreate", "No internet"); return; }

        Ion.with(this)
            .load(myUrl)
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (result != null) {
                        Log.d("JSONresult", result.toString());

                        /* Ion uses the Gson Library, which makes pretty printing hard (it's
                         * normally json.toString(4) in org.json libraries.
                         *
                         * Below is a one-liner of the following:
                         * Gson gs = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                         * JsonParser parser = new JsonParser();
                         * JsonElement je = parser.parse(result.toString());
                         * display.setText(gs.toJson(je));
                         *
                         * https://stackoverflow.com/questions/4105795
                         * https://stackoverflow.com/questions/11020894
                         */

                        display.setText(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                                .toJson(new JsonParser().parse(result.toString())));

                        // Extract the Surveys JsonArray, and specifically each SurveyID
                        JsonArray ja = result.getAsJsonObject("Result").getAsJsonArray("Surveys");
                        // Log.d("JsonArray", ja.toString());

                        for (final JsonElement survey : ja) {
                            final JsonObject each = survey.getAsJsonObject();
                            lastsurveyid = each.getAsJsonObject().getAsJsonPrimitive("SurveyID")
                                    .toString().replaceAll("\"", "");  // remove double quotes
                            Log.d("new", lastsurveyid);
                        }

                        // Set clickable button to last survey loaded
                        linksurvey.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uriUrl1 = Uri.parse("https://yalesurvey.qualtrics.com/SE/?SID=" + lastsurveyid);
                                Intent launchBrowser1 = new Intent(Intent.ACTION_VIEW, uriUrl1);
                                startActivity(launchBrowser1);
                            }
                        });
                    } else {
                        Log.d("JSONexcept", e.toString());
                    }
                }
            });
    }

}
