package com.example.watermanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.watermanager.Notification.FcmMessageService;
import com.example.watermanager.Others.Details;
import com.example.watermanager.Others.Display;
import com.example.watermanager.Auth.LoginActivity;
import com.example.watermanager.Others.Level;
import com.example.watermanager.Others.WaterLevel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser user;

    private RequestQueue requestQueue;

    private DatabaseReference mDatabase;

    private FcmMessageService fcmMessageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);

        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fcmMessageService = new FcmMessageService();
        addToken();

        findViewById(R.id.turnOn).setOnClickListener(this);
        findViewById(R.id.turnOff).setOnClickListener(this);
        findViewById(R.id.signOut).setOnClickListener(this);
        findViewById(R.id.toShow).setOnClickListener(this);
        findViewById(R.id.config).setOnClickListener(this);
        findViewById(R.id.crLevel).setOnClickListener(this);
        findViewById(R.id.details).setOnClickListener(this);

    }

    private void addToken(){
        if(user == null) return;
        String token = fcmMessageService.generateToken();
        String key = user.getEmail();
        for(int i = 0; i<key.length(); i++){
            if(key.charAt(i) == '.' || key.charAt(i) == '#' || key.charAt(i) == '@'){
                key = key.substring(0,i) + '_' + key.substring(i+1);
            }
        }
//        System.out.println(key);
//        System.out.println(token);
//        mDatabase.child(key).setValue(token);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.turnOn :
                sendCommand("ON");

                break;
            case R.id.turnOff :
                sendCommand("OFF");
                Toast.makeText(getApplicationContext()
                        , "Motor turned OFF"
                        ,Toast.LENGTH_SHORT)
                .show();
                break;
            case R.id.signOut :
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.toShow :
                startActivity(new Intent(this, Display.class));
                break;
            case R.id.config :
                String url = "http://192.168.4.1";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.crLevel :
                startActivity(new Intent(this, WaterLevel.class));
                break;
            case R.id.details :
                startActivity(new Intent(this, Details.class));
                break;

        }
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
                                , "Motor turned " + req
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