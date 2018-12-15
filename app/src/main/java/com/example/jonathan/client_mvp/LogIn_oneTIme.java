package com.example.jonathan.client_mvp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Switch;
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
    private String in_serverIP;
    private String in_username;
    private String in_password;

    private TextView txt_info;
    private EditText et_serverIP;
    private EditText et_user;
    private EditText et_pass;

    private String em_card = null;

    private int autologged_flag = 0; // if 1, means auto log failed and manual must be done

    private Context context;

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

        String s_fail = getString(R.string.failtag);
        txt_info = (TextView) findViewById(R.id.txt_hello);
        context = LogIn_oneTIme.this;

        // Create or get shared preference file
        final SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // auto log check
        String sp_ServerIP = sharedPref.getString(getString(R.string.IPlabel), s_fail);
        String sp_username = sharedPref.getString(getString(R.string.userlabel), s_fail);
        String sp_password = sharedPref.getString(getString(R.string.passlabel), s_fail);
        // if it has a user name, confirm the password matches and then log them in.
        if (!(sp_ServerIP.equals(s_fail)) && !(sp_username.equals(s_fail)) && !(sp_username.equals(s_fail))) {
            in_serverIP = sp_ServerIP;
            in_username = sp_username;
            in_password = sp_password;

            // username and password seem valid, attempt login script. will replace card number, so use editor.
            try {
                // TODO - script uses IP provided by user.
                Object result = new validateLogin().execute().get();
                if (result == null && em_card != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.card), em_card);
                    editor.apply();

                    Intent main_intent = new Intent(LogIn_oneTIme.this, FloorActivity.class);
                    // Need to pass Card number to next activity, since it is used to open doors
                    //main_intent.putExtra("CardID", em_card); // don't need if save card number in a shared preference.
                    startActivity(main_intent);
                    // Remove activity from back stack
                    finish();

                } else {
                    // meaning script failed or card number was not set to any value.
                    // Check what kind of error and display a message to the user.
                    if(result.equals("error")){
                        txt_info.setText("Error: Stored IP was incorrect or problem with web server. Contact your security administrator.");
                    } else if(result.equals("wrongCred")) {
                        txt_info.setText("Error: Stored credentials were incorrect, manually sign in.");
                    } else if(em_card == null || result.equals("noCard")){
                        txt_info.setText("Error: Card ID could not be retrieved. Contact your security administrator.");
                    } else if(result != null){
                        txt_info.setText("Error: Unhandled error.");
                    }
                    autologged_flag = 1;
                }
            } catch (Exception e) {
                Log.v("TASK: ", "flr " + e.toString());
            }
        } else {
            autologged_flag = 1;
        }

        // else it means that we need to do the login process
        if(autologged_flag == 1){

            manualLogin();
        }
    }

    void manualLogin(){

        // Log in information stored on company server or their preferred online service.
        // For demonstration, we use mySQL workbench
        et_serverIP = (EditText) findViewById(R.id.et_webIP);
        et_user = (EditText) findViewById(R.id.et_user);
        et_pass = (EditText) findViewById(R.id.et_pass);
        // Switch for auto login feature
        final Switch sw_autolog = (Switch) findViewById(R.id.sw_auto);

        //txt_info.setText(url_validate);
        // Sign in button
        Button btn_signIn = (Button) findViewById(R.id.btn_signin);
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Prevent SQL injection by having the php not execute a custom query. Just doing compares.
                in_serverIP = et_serverIP.getText().toString();
                in_username = et_user.getText().toString();
                in_password = et_pass.getText().toString();

                try {
                    // TODO - POST THE Server IP to use.
                    Object result = new validateLogin().execute().get();
                    if(result == null || em_card != null){
                        // success
                        final SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        // Using saved preferences feature in android to have autologin and save the user's card information.
                        SharedPreferences.Editor editor = sharedPref.edit();
                        // if em_card is assigned a value, now check if the user has checked the box that indicates if they want to have auto log in for next time.
                        if (sw_autolog.isChecked() == true) {
                            // write user name and password to file
                            editor.putString(getString(R.string.IPlabel), in_serverIP);
                            editor.putString(getString(R.string.userlabel), in_username);
                            editor.putString(getString(R.string.passlabel), in_password);
                            // On logout, delete whole file or clear it. Done in main activity if user decides to log out.
                        }
                        // Now need to handle where to store card number, since only one user is on the shared preference at one time, can save all relevant information on it.
                        // Save card number in the shared preferences too
                        // Card as key and card number as value
                        editor.putString(getString(R.string.card), em_card);
                        editor.apply();

                        Intent main_intent = new Intent(LogIn_oneTIme.this, FloorActivity.class);
                        // Need to pass Card number to next activity, since it is used to open doors
                        //main_intent.putExtra("CardID", em_card); // don't need if save card number in a shared preference.
                        startActivity(main_intent);
                        // Remove activity from back stack
                        finish();

                    } else {
                        // error in script or card value was not set.
                        // Check what kind of error and display a message to the user.
                        if(result.equals("error")){
                            txt_info.setText("Error: Provided IP is incorrect or problem with web server. Contact your security administrator.");
                        } else if(result.equals("wrongCred")){
                            txt_info.setText("Error: Current credentials incorrect, try again.");
                        } else if(em_card == null || result.equals("noCard")){
                            txt_info.setText("Error: Card ID could not be retrieved. Contact your security administrator.");
                        } else if(result != null){
                            txt_info.setText("Error: Unhandled.");
                        }
                    }
                } catch (Exception e) {
                    Log.v("TASK: ", "flr " + e.toString());
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

                try {

                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                    if (success == 1) {

                        products = json.getJSONArray("cardinfo");

                        if (products.length() == 1){
                            JSONObject c = products.getJSONObject(0);

                            // Storing each json item in variable
                            String cardNum = c.getString("card");
                            em_card = cardNum;
                            //txt_info.setText(cardNum);
                        } else {
                            //txt_info.setText("not found.");
                            return "noCard";
                        }

                    } else {
                        // no products found
                        //txt_info.setText("not found.");
                        // em_card stays = null
                        return "wrongCred";
                    }
                } catch (JSONException e) {
                    //txt_info.setText("err1");
                    e.printStackTrace();
                    return "error";
                }

            } catch (Exception e){
                //t1.setText(e.toString());
                Log.v("TASK: ", e.toString());
                return "error";
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
