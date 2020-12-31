package com.example.watermanager.Others;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.watermanager.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Display extends AppCompatActivity implements View.OnClickListener {
    private GraphView graphView;
    private RequestQueue requestQueue;
    private Button btn;
    private LineGraphSeries<DataPoint> series;
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        graphView = (GraphView) findViewById(R.id.graph);
        requestQueue = Volley.newRequestQueue(this);
//        JsonParse();
        findViewById(R.id.showId).setOnClickListener(this);


        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        
    }

    private void JsonParse(){
        String URL = "https://api.thingspeak.com/channels/1124811/fields/1.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            double prev = 0.0;
                            for(int i = 0; i<jsonArray.length(); i++){
                                JSONObject feed = jsonArray.getJSONObject(i);
                                if(feed.length() == 0) continue;
                                String createdAt = feed.getString("created_at");
                                String date = createdAt.substring(0,10)
                                        + " "
                                        + createdAt.substring(11,19);
                                Long entryId = feed.getLong("entry_id");
                                String field1 = feed.getString("field1");
                                if(field1.equals("null")) continue;
                                double data;
                                if(field1.equals("null")){
                                    data = prev;
                                } else {
                                    data = Double.parseDouble(field1);
                                    prev = data;
                                }
//                                System.out.println(date);
                                Date d = simpleDateFormat.parse(date);
                                series.appendData(new DataPoint(entryId, data),false,jsonArray.length());

                            }

                        } catch (JSONException | ParseException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.showId :
                JsonParse();
                series = new LineGraphSeries<>();
                // set manual X bounds
                graphView.getViewport().setYAxisBoundsManual(true);
                graphView.getViewport().setMinY(0);
                graphView.getViewport().setMaxY(100);

                // set manual Y bounds

                graphView.getViewport().setXAxisBoundsManual(true);
                graphView.getViewport().setMinX(0);
                graphView.getViewport().setMaxX(1000);

                graphView.getViewport().setScrollable(true);
                graphView.getViewport().setScalable(true);
                graphView.getViewport().setScalableY(true);
                graphView.getViewport().setScrollableY(true);
                graphView.addSeries(series);
                break;
        }
    }



}