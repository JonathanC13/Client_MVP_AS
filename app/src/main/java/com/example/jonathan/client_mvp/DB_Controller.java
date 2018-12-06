package com.example.jonathan.client_mvp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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

    // Creating JSON Parser object
    JSONParser jParserFlr;
    JSONParser jParserDr;
    JSONParser jParseImg;

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
    private static String TAG_DOORS;
    private static String TAG_DR_PID;
    private static String TAG_DR_NM;
    private static String TAG_DR_ID; // match with TAG_DOORID
    private static String TAG_DR_ML;
    private static String TAG_DR_MT;
    private static String TAG_DR_MR;
    private static String TAG_DR_MB;
    private static String TAG_DR_IP;

    // </door columns>
    // </JSON Node names>

    // <JSON Node names>
    /*
    private static final String TAG_SUCCESS = "success";
    // <image columns>
    private static final String TAG_IMAGES = "door_icons";
    private static final String TAG_IMG_PID = "idimg";
    private static final String TAG_IMG_NM = "nm_img";
    private static final String TAG_IMG_PATH = "path_img";
    // </image columns>
    // <floor columns>
    private static final String TAG_FLOORS = "arr_floor_rows";
    private static final String TAG_PID = "idfloor";
    private static final String TAG_NAME = "nm_floor";
    private static final String TAG_ORDER = "or_floor";
    private static final String TAG_IMAGE = "img_path";
    private static final String TAG_DOORID = "iddoor";
    // </floor columns>
    // <door columns>
    private static final String TAG_DOORS = "arr_doors";
    private static final String TAG_DR_PID = "iddr";
    private static final String TAG_DR_NM = "nm_door";
    private static final String TAG_DR_ID = "iddoor"; // match with TAG_DOORID
    private static final String TAG_DR_ML = "MarginLeft";
    private static final String TAG_DR_MT = "MarginTop";
    private static final String TAG_DR_MR = "MarginRight";
    private static final String TAG_DR_MB = "MarginBot";
    private static final String TAG_DR_IP = "IP_door";
    */
    // </door columns>
    // </JSON Node names>



    String save_folder;

    // products JSONArray
    JSONArray products = null;

    Data_Controller Data_shr;

    public DB_Controller(){}


    public DB_Controller(Context ct, Data_Controller Data_c) {
        Data_shr = Data_c;
        save_folder = Data_c.getFullImgPath() + "/images/";

        // <get url strings>
        String s_http = ct.getResources().getString(R.string.http);
        String s_serverIP = ct.getResources().getString(R.string.serverIP);
        String s_serverPort = ct.getResources().getString(R.string.serverPort);
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
        // </door columns>
        // </JSON Node names>

        // initialize by getting the door icons, right not they are just in drawable folder of app
        /*
        File mydir = ct.getDir("images", Context.MODE_PRIVATE); //Creating an internal dir;
        if (!mydir.exists()) {
            mydir.mkdirs();
        }

        try {
            Object result = new LoadAllIcons().execute().get();
        } catch (Exception e) {
            Log.v("TASK: ", "img " + e.toString());
        }

        // Download door icons, get names from the iconList
        //Log.v("TASK: ", "POST: " + iconList.size());
        for (String path : iconList){
            String dl_file = url_server_img + path;
            // save img to folder
            String saveloc = save_folder + path;

            URL dl_url = null;
            Bitmap dl_bp = null;
            try {
                dl_url = new URL(dl_file);
            } catch(Exception e){
                Log.v("TASK: ", "DR: URL ERR: " + e.toString());
            }
            File save_loc = new File(saveloc);

            //Log.v("TASK: ", "saving to" + saveloc );
            try {
                //Bitmap dl_bp = downloadImage(dl_file);
                try {
                    dl_bp = new webDownloadImage(dl_url).execute().get();
                } catch (Exception e){

                }

                //Log.v("TASK: ", "BITMAP SIZE: " + dl_file.length());
                try {
                    FileOutputStream out = new FileOutputStream(saveloc);
                    dl_bp.compress(Bitmap.CompressFormat.JPEG, 100, out); // dl_bp is your Bitmap instance

                    // check exist
                    //if(save_loc.exists()){
                        //Log.v("TASK: ", "CONFIRM DL: " + saveloc);
                        //Log.v("TASK: ", "SIZE " + save_loc.length());


                    //}
                    out.flush();

                } catch (IOException e) {
                    Log.v("TASK: ", "TO file: " + e.toString());
                }

            } catch (Exception e){}
        }
        */

        // Initialize the floors and doors
        // wait for async task to finish
        //-LoadAllDoors();
        refreshDoors();

        //-LoadAllFloors();
        try {
            Object result = new LoadAllFloors().execute().get();
        } catch (Exception e) {
            Log.v("TASK: ", "flr " + e.toString());
        }


        // Download floor image
        //Log.v("TASK: ", "POST: " + floorList.size());
        for (HashMap<String, String> path : floorList){
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
                } catch (Exception e){}


                try {
                    FileOutputStream out = new FileOutputStream(saveloc);
                    dl_bp.compress(Bitmap.CompressFormat.JPEG, 100, out); // dl_bp is your Bitmap instance

                    //if(save_loc.exists()){
                      //  Log.v("TASK: ",  "DL: " + saveloc);
                    //}

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e){}

        }
        //
    }

    public void refreshDoors(){
        try {
            Object result = new LoadAllDoors().execute().get();
        } catch (Exception e) {
            Log.v("TASK: ", "door " + e.toString());
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
                //Log.v("TASK: ", "getdrs " + dr_name);

                // create and add door to door list for this floor
                new_flr.addDoor(dr_name, door_margin_curr,dr_IP);

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
            // TODO Auto-generated catch block
            Log.v("TASK: ", "DOWNLOAD: " + e.toString());
            return null;
        }


        return bmImg;
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
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.v("TASK: ", "DOWNLOAD: " + e.toString());
                return null;
            }
            return bmImg;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
        }
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllIcons extends AsyncTask<String, String, String> {

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

            jParserFlr = new JSONParser();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParserFlr.makeHttpRequest(url_all_icons, "POST", params);

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // Images found found
                    // Getting Array of Floors
                    products = json.getJSONArray(TAG_IMAGES);

                    // looping through All floors
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String img_name = c.getString(TAG_IMG_PATH);
                        //Log.v("TASK: ", "EX: " + img_name);
                        // adding HashList to ArrayList
                        iconList.add(img_name);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products

            //
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

                    jParseImg = new JSONParser();
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("folder", save_folder));

                    // getting JSON string from URL
                    JSONObject json = jParseImg.makeHttpRequest(url_all_images, "POST", params);

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
    class LoadAllDoors extends AsyncTask<String, String, String> {

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
            jParserDr = new JSONParser();

            doorList = new ArrayList<HashMap<String, String>>();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParserDr.makeHttpRequest(url_all_doors, "POST", params);

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

                        // adding HashList to ArrayList
                        doorList.add(map);

                    }
                } else {
                    // no products found
                    // Launch Add New product Activity

                }
            } catch (JSONException e) {
                e.printStackTrace();
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
    // </Task to get doors>

    /**
     * Background Async Task to Load all images by making HTTP Request
     * */
    // <Task to get door floors>
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllFloors extends AsyncTask<String, String, String> {

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
            
            floorList = new ArrayList<HashMap<String, String>>();
            
            jParserFlr = new JSONParser();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParserFlr.makeHttpRequest(url_all_floors, "POST", params);

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
                    // no products found
                    // Launch Add New product Activity

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products

        }

    }

    // </Task to get floors>

}
