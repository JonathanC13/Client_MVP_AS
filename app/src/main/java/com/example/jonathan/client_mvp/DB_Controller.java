package com.example.jonathan.client_mvp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;


public class DB_Controller {

    // Error pop up message if needed
    private String popUpError;

    // Creating JSON Parser object
    JSONParser jParserFlr;
    JSONParser jParserDr;
    JSONParser jParserImg;

    // Store parsed JSON to lists
    ArrayList<String> iconList = new ArrayList<String>();
    ArrayList<HashMap<String, String>> floorList; // needed since we sort the floors
    ArrayList<HashMap<String, String>> doorList; // hold all the existing doors

    // url to get all products list
    // 10.0.2.2 for localhost, since using "localhost" is refused and Android emulator is running on a virtual machine
    private static String url_all_icons;
    private static String url_all_floors; //= "http://10.0.2.2:8080/android_connect/get_all_floors.php"
    private static String url_all_doors;
    private static String url_all_images;

    private static String url_server_img;

    // <JSON Node names>
    private static String TAG_SUCCESS;
    // <image columns>
    private static String TAG_IMAGES;
    private static String TAG_IMG_PID;
    private static String TAG_IMG_NM;
    private static String TAG_IMG_PATH;
    // </image columns>
    // <floor columns>
    private static String TAG_FLOORS;
    private static String TAG_PID;
    private static String TAG_NAME;
    private static String TAG_ORDER;
    private static String TAG_IMAGE;
    private static String TAG_DOORID;
    // </floor columns>
    // <door columns>
    private static String TAG_DOORS; // JSON obj name
    private static String TAG_DR_PID;// ID, primary key from DB
    private static String TAG_DR_NM; // door name, can be anything
    private static String TAG_DR_ID; // match with TAG_DOORID
    private static String TAG_DR_ML; // Margin left
    private static String TAG_DR_MT; // Margin Top
    private static String TAG_DR_MR; // Margin right
    private static String TAG_DR_MB; // Margin bottom
    private static String TAG_DR_IP; // IP for the device that has control of that door system. For UDP transport
    private static String TAG_DR_PORT; // Port for the device. For UDP transport
    private static String TAG_DR_DEV_NAME; // Remote device name, put in UDP packet for verification purposes.
    private static String TAG_DR_DEV_MAC; // Remote device MAC, for bluetooth device pairing and transport

    // </door columns>

    String save_folder;
    private String webserverName;

    // <async return values>
    private int at_success = 0;
    private int at_error = 1;
    private int at_noFloors = 2;
    private int at_portError = 3;
    // </async return values>

    Data_Controller Data_shr;
    private Context cont;

    public DB_Controller(){}


    public DB_Controller(Context ct, Data_Controller Data_c) {

        cont = ct;
        popUpError = "";

        Data_shr = Data_c;
        save_folder = Data_c.getFullImgPath() + "/images/";

        // <Info from shared preference file>
        String s_fail = cont.getString(R.string.failtag);
        // Get IP from shared preference and have the scripts use it.
        final SharedPreferences sharedPref = cont.getSharedPreferences(cont.getString(R.string.preference_server_key), Context.MODE_PRIVATE);
        webserverName = sharedPref.getString(cont.getString(R.string.IPlabel), s_fail);
        String sharedp_serverPort = sharedPref.getString(cont.getString(R.string.Portlabel), s_fail);

        // PHP uses "localhost" while HTTP request uses the IP sequence.
        String s_serverIP = "";
        String s_localhost = ct.getResources().getString(R.string.s_local);
        String ip_local = ct.getResources().getString(R.string.localIP);
        if(webserverName.equals(s_localhost)){
            s_serverIP = ip_local;
        } else {
            s_serverIP = webserverName;
        }

        String s_serverPort = "";
        if(sharedp_serverPort.length() > 0) {
            s_serverPort = ":" + sharedp_serverPort;
            //Log.v("TASK: ", "MAIN "+ s_serverIP + " " + s_serverPort);
        } else {
            s_serverPort = "";
        }
        // </Info from shared preference file>

        // <get url strings>
        String s_http = ct.getResources().getString(R.string.http);
        String s_phpFolder = ct.getResources().getString(R.string.phpFolder);
        String s_imgFolder = ct.getResources().getString(R.string.imgFolder);

        String s_serverDir = s_http + s_serverIP + s_serverPort;
        String s_scriptDir = s_serverDir + s_phpFolder;
        String s_imgDir = s_serverDir + s_imgFolder;

        String s_iconPHP = ct.getResources().getString(R.string.icon_scr);
        String s_floorPHP = ct.getResources().getString(R.string.floor_scr);
        String s_doorPHP = ct.getResources().getString(R.string.door_scr);
        String s_imagePHP = ct.getResources().getString(R.string.image_scr);

        url_all_icons = s_scriptDir + s_iconPHP;
        url_all_floors = s_scriptDir + s_floorPHP;
        url_all_doors = s_scriptDir + s_doorPHP;
        url_all_images = s_scriptDir + s_imagePHP;

        url_server_img = s_imgDir;
        // </get url strings>

        // <JSON Node names>
        TAG_SUCCESS = ct.getResources().getString(R.string.j_success);
        // <image columns>
        TAG_IMAGES = ct.getResources().getString(R.string.j_imgIcons);
        TAG_IMG_PID = ct.getResources().getString(R.string.j_imgPID);
        TAG_IMG_NM = ct.getResources().getString(R.string.j_imgNm);
        TAG_IMG_PATH = ct.getResources().getString(R.string.j_imgPath);
        // </image columns>
        // <floor columns>
        TAG_FLOORS = ct.getResources().getString(R.string.j_arrFloor);
        TAG_PID = ct.getResources().getString(R.string.j_flrPID);
        TAG_NAME = ct.getResources().getString(R.string.j_flrNm);
        TAG_ORDER = ct.getResources().getString(R.string.j_flrOR);
        TAG_IMAGE = ct.getResources().getString(R.string.j_flrPath);
        TAG_DOORID = ct.getResources().getString(R.string.j_flrDr);
        // </floor columns>
        // <door columns>
        TAG_DOORS = ct.getResources().getString(R.string.j_arrDr);
        TAG_DR_PID = ct.getResources().getString(R.string.j_drPID);
        TAG_DR_NM = ct.getResources().getString(R.string.j_drNm);
        TAG_DR_ID = ct.getResources().getString(R.string.j_drID); // match with TAG_DOORID
        TAG_DR_ML = ct.getResources().getString(R.string.j_drML);
        TAG_DR_MT = ct.getResources().getString(R.string.j_drMT);
        TAG_DR_MR = ct.getResources().getString(R.string.j_drMR);
        TAG_DR_MB = ct.getResources().getString(R.string.j_drMB);
        TAG_DR_IP = ct.getResources().getString(R.string.j_drIP);
        TAG_DR_PORT = ct.getResources().getString(R.string.j_drPort);
        TAG_DR_DEV_NAME = ct.getResources().getString(R.string.j_drDevName);
        TAG_DR_DEV_MAC = ct.getResources().getString(R.string.j_drDevMAC);
        // </door columns>
        // </JSON Node names>

        // Initialize the floors and doors
        // wait for async task to finish
        //-LoadAllDoors();
        refreshDoors();

        //-LoadAllFloors();
        refreshFloors();

        // Download floor images
        loadFloorImages();

        // Sort floors and add corresponding doors
        organizeFloorInfo();

        // Alert pop up if there are any errors or log messages that the user should see.
        if(popUpError.length() > 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(ct).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage(popUpError);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public void refreshDoors(){
        try {
            Object result = new LoadAllDoors().execute().get();

            if (!result.equals(at_success)) {
                if (result.equals(at_error)) {
                    popUpError += "Error: Access to door information failed - may need to change server IP and/or port in the menu, it was a failed connection, or there are problems with the web server.\n";
                } else if (result.equals(at_portError)){
                    popUpError += "Error: Access to door information failed - incorrect server and/or port likely the cause, change the server settings in the menu.\n";
                }
            }
        } catch (Exception e) {
            Log.v("TASK: ", "door " + e.toString());
            popUpError += "Error: Access to door information failed - may need to change server IP and/or port in the menu, it was a failed connection, or there are problems with the web server.\n";
        }
    }

    public void refreshFloors(){
        try {
            Object result = new LoadAllFloors().execute().get();

            if (!result.equals(at_success)) {
                if (result.equals(at_error)) {
                    popUpError += "Error: Access to floor information failed - may need to change server IP and/or port in the menu, it was a failed connection, or there are problems with the web server.\n";
                } else if (result.equals(at_noFloors)) {
                    popUpError += "Log: No floors were found.\n";
                } else if (result.equals(at_portError)){
                    popUpError +=  "Error: Access to floor information failed - incorrect server and/or port likely the cause, change the server settings in the menu.\n";
                }
            }
        } catch (Exception e) {
            //Log.v("TASK: ", "flr " + e.toString());
            popUpError += "Error: Access to floor information failed - may need to change server IP and/or port in the menu, it was a failed connection, or there are problems with the web server.\n";
        }
    }

    public void loadFloorImages(){
        //Log.v("TASK: ", "POST: " + floorList.size());
        for (HashMap<String, String> path : floorList){
            int noImageFlag = 1;
            String dl_file = url_server_img + path.get(TAG_IMAGE);
            //Log.v("TASK: ",  "WEB SERVER "+ dl_file);
            // save img to folder
            String saveloc = save_folder + path.get(TAG_IMAGE);
            URL dl_url = null;
            Bitmap dl_bp = null;
            try {
                dl_url = new URL(dl_file);
            } catch(Exception e){
                Log.v("TASK: ", "FLR URL ERR: " + e.toString());
            }
            File save_loc = new File(saveloc);
            try {
                //Bitmap dl_bp = downloadImage(dl_file);
                try {
                    dl_bp = new webDownloadImage(dl_url).execute().get();

                    if(dl_bp == null){
                        popUpError += "Error: Could not retrieve from server image for " + path.get(TAG_IMAGE) + ".\n";

                        noImageFlag = 0;
                    }

                } catch (Exception e){
                    popUpError += "Error: Could not retrieve image from server for " + path.get(TAG_IMAGE) + ".\n";
                    noImageFlag = 0;
                }

                // Set default image if could not retrieve one from the web server
                if (noImageFlag == 0){
                    dl_bp = BitmapFactory.decodeResource(cont.getResources(), R.drawable.default_floor_image);
                }

                // save image on device
                try {
                    FileOutputStream out = new FileOutputStream(saveloc);
                    dl_bp.compress(Bitmap.CompressFormat.JPEG, 100, out); // dl_bp is your Bitmap instance

                } catch (IOException e) {
                    e.printStackTrace();
                    popUpError += "Error: Could not save image on device for " + path.get(TAG_IMAGE) + ".\n";
                }

            } catch (Exception e){
                popUpError += "Error: Could not save image on device for " + path.get(TAG_IMAGE) + ".\n";
            }

        }
    }

    public void setDoors(Data_Collection new_flr ,String doorID){
        //new_flr.arr_doors = new ArrayList<door_struct>();
        new_flr.newDoorList();

        for(HashMap<String, String> dr : doorList){
            if(doorID.equals(dr.get(TAG_DR_ID))){

                double[] door_margin_curr = new double[4];

                // set margins
                door_margin_curr[0] = Double.parseDouble(dr.get(TAG_DR_ML));
                door_margin_curr[1] = Double.parseDouble(dr.get(TAG_DR_MT));
                door_margin_curr[2] = Double.parseDouble(dr.get(TAG_DR_MR));
                door_margin_curr[3] = Double.parseDouble(dr.get(TAG_DR_MB));

                String dr_name = dr.get(TAG_DR_NM);
                String dr_IP = dr.get(TAG_DR_IP);
                int dr_Port = Integer.parseInt(dr.get(TAG_DR_PORT));
                String dr_devName = dr.get(TAG_DR_DEV_NAME);
                String dr_devMAC = dr.get(TAG_DR_DEV_MAC);

                //Log.v("TASK: ", "getdrs " + dr_name);

                // create and add door to door list for this floor
                new_flr.addDoor(dr_name, door_margin_curr,dr_IP,dr_Port,dr_devName,dr_devMAC);

            }
        }
    }

    public static Bitmap downloadImage(String url) throws MalformedURLException {
        return downloadImage(new URL(url));
    }

    public static Bitmap downloadImage(URL url){
        Bitmap bmImg = null;
        try {
            HttpURLConnection conn= (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            bmImg = BitmapFactory.decodeStream(is);
        } catch (Exception e) {

            //Log.v("TASK: ", "DOWNLOAD: " + e.toString());
            return null;
        }


        return bmImg;
    }

    private void organizeFloorInfo(){
        int max_count = 0;
        // get count
        for(HashMap<String, String> flr : floorList){
            max_count ++;
        }

        // Reset Data_shr floor list
        Data_shr.resetFloorList();

        //Log.v("TASK: ", String.valueOf(max_count));

        // Sort and Add
        // do until max rows, just in case the admin missed a floor number
        for(int i = 0; i < max_count; i ++){

            // check whole list for matching floor number,
            for (HashMap<String, String> curFlr : floorList) {

                if(curFlr.get(TAG_ORDER).equals(String.valueOf(i))){

                    // new floor collection
                    Data_Collection new_flr = new Data_Collection();

                    String floor_name = curFlr.get(TAG_NAME);
                    String floor_number = curFlr.get(TAG_ORDER);
                    String dis_name = String.valueOf(i) + ". " + floor_name;
                    String img_dir = save_folder + curFlr.get(TAG_IMAGE); // path to save image.
                    String doorID = curFlr.get(TAG_DOORID);

                    // set floor values
                    new_flr.set_floor(floor_name, floor_number, dis_name, img_dir, doorID);

                    Data_shr.flr_dr_class_list.add(new_flr);
                    //Log.v("TASK: ", "getflrs " + dis_name);

                    // Look through door list and add relevant doors into this floor's collection
                    setDoors(new_flr, doorID);

                }
            }
        }
    }

    // Since there will be 2 tasks that are very similar, we may refactor. Smelly code
    // May not even use async tasks, since getting images and floors both write to memory ; better to be sequential.
    // <Async Tasks to retrieve from Databases>

    //
    private class webDownloadImage extends AsyncTask<String, Void, Bitmap> {

        URL urldisplay;
        public webDownloadImage(URL ss) {
            this.urldisplay = ss;
        }

        protected Bitmap doInBackground(String... urls) {

            Bitmap bmImg = null;
            try {
                HttpURLConnection conn= (HttpURLConnection)urldisplay.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();


                bmImg = BitmapFactory.decodeStream(is);
                //Log.v("TASK: ", "INPUT " + bmImg.getByteCount());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.v("TASK: ", "DOWNLOAD: " + e.toString());
                return null;
            } catch (Exception e){
                return null;
            }
            return bmImg;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
        }
    }

    // <Task to get all images and save>
    // In WAMPServer may have different folders for different image extensions.
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllImages extends AsyncTask<String, String, String> {

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


                File f = new File(save_folder);
                if(f.exists()) {

                    String img_fold = save_folder;
                    //File img_f = new File(img_fold);
                    //if(img_f.exists()){
                        //Log.v("TASK: ", "image folder: " + img_fold);
                    //}

                    //String s_url = url_all_images + "?string1=" + save_folder;
                    //String s_url = "http://10.0.2.2:8080/android_connect/db_getImages.php?string1=" + save_folder;

                    //HttpClient httpclient = new DefaultHttpClient();
                    //HttpPost httppost = new HttpPost(s_url); // "&string2=" + string2
                    //HttpResponse response = httpclient.execute(httppost);

                    jParserImg = new JSONParser();
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("folder", save_folder));

                    // getting JSON string from URL
                    JSONObject json = jParserImg.makeHttpRequest(url_all_images, "POST", params);

                    // check all child files

                    // check if a file was saved
                    String s_file = save_folder + "/main_floor.txt";
                    File f_file = new File(s_file);
                    f_file.createNewFile();
                    //if(f_file.exists()){
                      //  Log.v("TASK: ", "FILE WAS SAVED");
                    //} else {
                      //  Log.v("TASK: ", "FILE FAILED");
                    //}
                }

            } catch (Exception e){
                Log.v("getImages: ", e.toString());
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
        }

    }

    // </Task to get all images and save>

    // <Task to get doors>
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllDoors extends AsyncTask<String, Integer, Integer> {

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
        protected Integer doInBackground(String... args) {
            JSONArray products = null;
            jParserDr = new JSONParser();

            doorList = new ArrayList<HashMap<String, String>>();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("ip", webserverName));
            // getting JSON string from URL
            JSONObject json = jParserDr.makeHttpRequest(url_all_doors, "POST", params);

            if(json == null){
                return at_portError;
            }

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // Images found found
                    // Getting Array of Images
                    products = json.getJSONArray(TAG_DOORS);

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_DR_PID);
                        String name = c.getString(TAG_DR_NM);
                        String drID = c.getString(TAG_DR_ID);
                        String mLeft = c.getString(TAG_DR_ML);
                        String mTop = c.getString(TAG_DR_MT);
                        String mRight = c.getString(TAG_DR_MR);
                        String mBot = c.getString(TAG_DR_MB);
                        String drIP = c.getString(TAG_DR_IP);
                        String drPort = c.getString(TAG_DR_PORT);
                        String drDevName = c.getString(TAG_DR_DEV_NAME);
                        String drDevMAC = c.getString(TAG_DR_DEV_MAC);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_DR_PID, id);
                        map.put(TAG_DR_NM, name);
                        map.put(TAG_DR_ID, drID);
                        map.put(TAG_DR_ML, mLeft);
                        map.put(TAG_DR_MT, mTop);
                        map.put(TAG_DR_MR, mRight);
                        map.put(TAG_DR_MB, mBot);
                        map.put(TAG_DR_IP, drIP);
                        map.put(TAG_DR_PORT, drPort);
                        map.put(TAG_DR_DEV_NAME, drDevName);
                        map.put(TAG_DR_DEV_MAC, drDevMAC);

                        // adding HashList to ArrayList
                        doorList.add(map);

                    }
                } else {
                    // no doors found
                    // do nothing
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return at_error;
            }

            return at_success;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(Integer file_url) {
            // dismiss the dialog after getting all products
        }

    }
    // </Task to get doors>

    /**
     * Background Async Task to Load all images by making HTTP Request
     * */
    // <Task to get door floors>
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllFloors extends AsyncTask<String, Integer, Integer> {

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
        protected Integer doInBackground(String... args) {
            JSONArray products = null;
            floorList = new ArrayList<HashMap<String, String>>();
            
            jParserFlr = new JSONParser();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("ip", webserverName));
            // getting JSON string from URL
            JSONObject json = jParserFlr.makeHttpRequest(url_all_floors, "POST", params);

            if(json == null){
                return at_portError;
            }

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // Images found found
                    // Getting Array of Floors
                    products = json.getJSONArray(TAG_FLOORS);

                    // looping through All floors
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String flrOrd = c.getString(TAG_ORDER);
                        String image = c.getString(TAG_IMAGE);
                        String drID = c.getString(TAG_DOORID);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_ORDER, flrOrd);
                        map.put(TAG_IMAGE, image);
                        map.put(TAG_DOORID, drID);

                        // adding HashList to ArrayList
                        floorList.add(map);
                    }
                } else {
                    // no floors found
                    return at_noFloors;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return at_error;
            }

            return at_success;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(Integer file_url) {
            // dismiss the dialog after getting all products

        }

    }

    // </Task to get floors>

}
