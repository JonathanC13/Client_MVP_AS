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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;


public class DB_Controller {

    // Global flag for if the one time connection to the main server has been made. This only needed if redundancy DBs stored on RPis in case of connection
    // failure to main server.
    private int redundancy_flag = 11; // 11 for false.
    private int nextIP_flag = 11;

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

    String save_folder;

    // <Redundancy Handling>
    // File to store information on backup IPs. Save at save_folder, locally on phone.
    String s_RedunancyFile = "Redundancy.txt"; // just keep it as a simple text file
    String s_tempRedunFile = "temp.txt";
    String s_RedundancyPath = save_folder + s_RedunancyFile;
    // </Redundancy Handling>

    // products JSONArray
    JSONArray products = null;

    Data_Controller Data_shr;

    public DB_Controller(){}


    public DB_Controller(Context ct, Data_Controller Data_c) {
        Data_shr = Data_c;
        save_folder = Data_c.getFullImgPath() + "/images/";

        // <get url strings>
        String s_http = ct.getResources().getString(R.string.http);
        String s_serverIP = ct.getResources().getString(R.string.serverIP); //Hard coded in for now, but may need user to input this since the company will host it.
        String s_serverPort = ct.getResources().getString(R.string.serverPort); //Hard coded in for now, but may need user to input this since the company will host it.
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

        // <Redundancy Handling>
        // User may be provided so the user can input to the app.
        // check if first time connection, NEEDS to be to the main server since it doesn't have the list of the redundancy servers on the RPis.
        // if no file of redundancy information exists, need to only use the IP of the main server.
        File f_redun = new File(s_RedundancyPath);
        BufferedReader br_redun;
        // If it doesn't exist, create it so we can populate it later.
        try {
            if (f_redun.createNewFile()) {
                // If new, need to add the main IP.
                FileOutputStream fos = new FileOutputStream(f_redun);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write(s_serverIP);

                redundancy_flag = 0;
            } else {
                // If it exists, it means it has connected to the main server before and redundancy was already set up
                // check if first IP correct. (s_serverIP). may need to add port later
                br_redun = new BufferedReader(new FileReader(s_RedundancyPath));
                // if first line empty
                String line = br_redun.readLine();
                if(line == null || line.length() == 0){
                    // add the main server IP
                } else {
                    // check if not correct
                    if(!(s_serverIP.equals(line))){
                        // create tmp file to modify then replace
                        File fout = new File(s_tempRedunFile);
                        if(fout.createNewFile()){
                            FileOutputStream fos = new FileOutputStream(fout);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                            bw.write(s_serverIP);

                            // copy all info after the first line into temp file
                            //next line
                            line = br_redun.readLine();
                            while(line != null){
                                bw.newLine();
                                bw.write(line);
                                // next line
                                line = br_redun.readLine();
                            }
                            br_redun.close();
                            bw.close();

                        }

                        // delete original file
                        f_redun.delete();
                        // replace the original file
                        boolean isMoved = fout.renameTo(f_redun);
                        if (!isMoved) {
                            redundancy_flag = 11;
                        } else {
                            // if copy done without error
                            redundancy_flag = 0;
                        }

                    } else {
                        redundancy_flag = 0;
                    }
                }
            }
        } catch (IOException e){
            // error creating or modifying files, force app to only connect to main server because we are not sure redundancy info was set up properly.
            redundancy_flag = 11;
        } catch (Exception ex){
            redundancy_flag = 11;
        }

        // </Redundancy Handling>

        // <Redundancy Loop on IP List. Force all updates from same IP or it will move to the next

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
            DL FILE alter server // TODO

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

    // return 0 on success.
    public int refreshDoors(){
        try {
            Object result = new LoadAllDoors().execute().get();
            return 0;
        } catch (Exception e) {
            Log.v("TASK: ", "door " + e.toString());
            return 1;
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
                    File img_f = new File(img_fold);
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

                    //JSONObject json = null;
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

            // <loop for redundancy>
            /*
            if(redundancy_flag == 11) {
                json = jParserDr.makeHttpRequest(url_all_doors, "POST", params);
            } else {
                // redundancy was setup, so loop IP list if necessary.
                File f_red = new File(s_RedundancyPath);
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(f_red));
                    String line = reader.readLine();
                    while (line != null) {
                        String params = 12121; // TODO with POST
                        url_all_doorsNEW = // change url since server change

                        if ((json = jParserDr.makeHttpRequest(url_all_doorsNEW, "POST", params)) == null) {
                            // if failed connection
                            // next line
                            reader.readLine();
                        }
                    }
                } catch (Exception e) {

                }
            }
            */
            // </loop for redundancy>

            // Will not be null if success connection made.
            if(json != null) {
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
                return null; // success
            }
            return "1"; // failed
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String success) {

            // check if the thread did not error.

            // If not null, means is had an error. // assume connection to desired server failed
            if(success != null){
                nextIP_flag = 0;    // indicate try next IP
            }
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

            //JSONObject json = null;
            JSONObject json = jParserFlr.makeHttpRequest(url_all_floors, "POST", params);

            // </loop for redundancy>
            if(json != null) {
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
                for (HashMap<String, String> flr : floorList) {
                    max_count++;
                }

                //Log.v("TASK: ", String.valueOf(max_count));

                // Reset Data_shr floor list
                Data_shr.resetFloorList(); 
                
                // Sort and Add
                // do until max rows, just in case the admin missed a floor number
                for (int i = 0; i < max_count; i++) {

                    // check whole list for matching floor number,
                    for (HashMap<String, String> curFlr : floorList) {

                        if (curFlr.get(TAG_ORDER).equals(String.valueOf(i))) {

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

                return null; // success
            }
            return "1"; // failed
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
