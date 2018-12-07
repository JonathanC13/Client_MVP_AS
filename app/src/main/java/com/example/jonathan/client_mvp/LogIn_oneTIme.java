package com.example.jonathan.client_mvp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogIn_oneTIme extends AppCompatActivity {

    private static String url_validate;
    private String in_username;
    private String in_password;

    private TextView txt_info;
    private EditText et_user;
    private EditText et_pass;

    private String em_card = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_one_time);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String s_http = this.getResources().getString(R.string.http);
        String s_serverIP = this.getResources().getString(R.string.serverIP);
        String s_serverPort = this.getResources().getString(R.string.serverPort);
        String s_phpFolder = this.getResources().getString(R.string.phpFolder);
        String s_serverDir = s_http + s_serverIP + s_serverPort;
        String s_scriptDir = s_serverDir + s_phpFolder;

        String s_validatePHP = this.getResources().getString(R.string.validate_scr);
        url_validate = s_scriptDir + s_validatePHP;

        // Log in information stored on company server or their preferred online service.
        // For demonstration, we use mySQL workbench

        txt_info = (TextView) findViewById(R.id.txt_hello);
        et_user = (EditText) findViewById(R.id.et_user);
        et_pass = (EditText) findViewById(R.id.et_pass);


        //txt_info.setText(url_validate);
        // Sign in button
        Button btn_signIn = (Button) findViewById(R.id.btn_signin);
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Prevent SQL injection by having the php not execute a custom query. Just doing compares.
                in_username = et_user.getText().toString();
                in_password = et_pass.getText().toString();

                try {
                    Object result = new validateLogin().execute().get();
                } catch (Exception e) {
                    Log.v("TASK: ", "flr " + e.toString());
                }

                txt_info.setText(em_card);

                if(em_card == null){
                    txt_info.setText("NOTHING");
                } else {
                    // if em_card is assigned a value, now check if the user has checked the box that indicates if they want to remain logged in.
                    //


                    Intent main_intent = new Intent(LogIn_oneTIme.this, MainActivity.class);
                    // Need to pass Card number to next activity, since it is used to open doors
                    main_intent.putExtra("CardID", em_card);
                    startActivity(main_intent);
                    // Remove activity from back stack
                    finish();
                }
            }
        });
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class validateLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {

            try {

                JSONArray products = null;
                JSONParser jParserLogin = new JSONParser();
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", in_username));
                params.add(new BasicNameValuePair("pass", in_password));

                //txt_info.setText(url_validate);
                // getting JSON string from URL
                JSONObject json = jParserLogin.makeHttpRequest(url_validate, "POST", params);
                //txt_info.setText("SSSS");
                try {

                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                    if (success == 1) {
                        //txt_info.setText("SSSS1");
                        products = json.getJSONArray("cardinfo");

                        if (products.length() == 1){
                            JSONObject c = products.getJSONObject(0);

                            // Storing each json item in variable
                            String cardNum = c.getString("card");
                            em_card = cardNum;
                            //txt_info.setText(cardNum);
                        } else {
                            //txt_info.setText("not found.");
                        }

                    } else {
                        // no products found
                        //txt_info.setText("not found.");

                    }
                } catch (JSONException e) {
                    //txt_info.setText("err1");
                    e.printStackTrace();
                }

            } catch (Exception e){
                //t1.setText(e.toString());
                Log.v("TASK: ", e.toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String result) {

        }

    }

}
