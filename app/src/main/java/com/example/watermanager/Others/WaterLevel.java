package com.example.watermanager.Others;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.watermanager.R;
import com.example.watermanager.VolleyUtil.VolleyCallback;
import com.jjoe64.graphview.series.DataPoint;
import com.ramijemli.percentagechartview.PercentageChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class WaterLevel extends AppCompatActivity implements View.OnClickListener{

    private PercentageChartView percentageChartView;
    private RequestQueue requestQueue;
    private TextView waterLitre;
    private String field1 = "50",field2 = "50",field3 = "50";
    private String temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_level);
        percentageChartView = (PercentageChartView) findViewById(R.id.percentView);
        findViewById(R.id.showLevel).setOnClickListener(this);
        waterLitre = (TextView) findViewById(R.id.volume);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.showLevel :
                System.out.println("pressed");
                sendCommand("REFRESH");
                display();
                break;
        }
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String response);
    }

    private void display(){
        JsonParse("1", new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                System.out.println(message);
            }

            @Override
            public void onResponse(String response) {
                System.out.println(response + "1");
                field1 = response;
                JsonParse("2", new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        System.out.println(message);
                    }

                    @Override
                    public void onResponse(String response) {
                        System.out.println(response + "2");
                        field2 = response;
                        JsonParse("3", new VolleyResponseListener() {
                            @Override
                            public void onError(String message) {
                                System.out.println(message);
                            }

                            @Override
                            public void onResponse(String response) {
                                field3 = response;
                                addInArray();
                            }
                        });
                    }
                });
            }
        });
    }

    private void sendCommand(String req){
        try {
            requestQueue = Volley.newRequestQueue(this);
            String URL = "https://api.thingspeak.com/talkbacks/39798/"
                    + "commands.json?api_key=02J9IBWJOSUTFUHW&"
                    +"command_string="+req;
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Title", "Android Volley Demo");
            jsonBody.put("Author", "BNK");
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response.equalsIgnoreCase("200")){
                        Toast.makeText(getApplicationContext()
                                , "Current Water Level "
                                ,Toast.LENGTH_SHORT)
                                .show();
                    }
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void JsonParse(String value, VolleyResponseListener volleyResponseListener){
        String URL = "https://api.thingspeak.com/channels/1124811/fields/"+value+"/last.json";
        temp = "";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String res = response.getString("field" + value);
                            temp = res;
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        System.out.println(temp);
                        volleyResponseListener.onResponse(temp);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                volleyResponseListener.onError("Some Error Occurred");
            }
        });
        requestQueue.add(jsonObjectRequest);

    }
    private void addInArray(){

        Float currLevel = Math.abs(Float.parseFloat(field1));
        Float tankHeight = Math.abs(Float.parseFloat(field2));
        Float tankVolume = Math.abs(Float.parseFloat(field3));
        System.out.println(currLevel + " " + tankHeight + " " + tankVolume);
        Float waterLevel = (currLevel/tankHeight)*100;
        System.out.println(waterLevel);
        Float currVolume = (tankVolume*currLevel)/tankHeight;
//        if(waterLevel < 40) waterLevel+=30;
        waterLitre.setText(currVolume.toString().substring(0,4) + " L");

        if(waterLevel>40){
            percentageChartView.setProgress(waterLevel,true);
        } else if(waterLevel > 25){
            percentageChartView.setProgress(waterLevel,true);
            percentageChartView.progressColor(Color.YELLOW).apply();
        } else {
            percentageChartView.setProgress(waterLevel,true);
            percentageChartView.progressColor(Color.RED).apply();
        }

    }

}