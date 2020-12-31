package com.example.watermanager.Others;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.watermanager.Data.User;
import com.example.watermanager.MainActivity;
import com.example.watermanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Details extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private EditText uniqueId;
    private EditText level1;
    private EditText level2;
    private EditText level3;
    private EditText level4;
    private EditText level5;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uniqueId = (EditText) findViewById(R.id.id_text);
        level1 = (EditText) findViewById(R.id.level1);
        level2 = (EditText) findViewById(R.id.level2);
        level3 = (EditText) findViewById(R.id.level3);
        level4 = (EditText) findViewById(R.id.level4);
        level5 = (EditText) findViewById(R.id.level5);


        findViewById(R.id.submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit :
                submitResponse();
                break;
        }
    }

    private void submitResponse(){
        String id = uniqueId.getText().toString();
        String key = user.getEmail();

        String l1 = level1.getText().toString();
        String l2 = level2.getText().toString();
        String l3 = level3.getText().toString();
        String l4 = level4.getText().toString();
        String l5 = level5.getText().toString();

        if(l1.isEmpty()) l1 = "-1";
        if(l2.isEmpty()) l2 = "-1";
        if(l3.isEmpty()) l3 = "-1";
        if(l4.isEmpty()) l4 = "-1";
        if(l5.isEmpty()) l5 = "-1";

        for(int i = 0; i<key.length(); i++){
            if(key.charAt(i) == '.' || key.charAt(i) == '#' || key.charAt(i) == '@'){
                key = key.substring(0,i) + '_' + key.substring(i+1);
            }
        }
        sendCommand(l1,l2,l3,l4,l5);
        mDatabase.child(id).setValue(key);
        startActivity(new Intent(this, MainActivity.class));
    }

    private void sendCommand(String l1, String l2, String l3, String l4, String l5){
        try {
            requestQueue = Volley.newRequestQueue(this);
            String URL = "https://api.thingspeak.com/update"
                    + "?api_key=BU1Q04VWWBUD19LL"
                    +"&field1=" + l1
                    +"&field2=" + l2
                    +"&field3=" + l3
                    +"&field4=" + l4
                    +"&field5=" + l5;
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
                                , "Updated!"
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
}