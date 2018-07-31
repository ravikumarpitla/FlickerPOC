package com.example.ravip.myapplication.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.ravip.myapplication.R;
import com.example.ravip.myapplication.adapter.GalleryAdapter;
import com.example.ravip.myapplication.app.AppController;
import com.example.ravip.myapplication.model.FlickerData;
import com.example.ravip.myapplication.model.Item;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "http://api.androidhive.info/json/glide.json";

    private ProgressDialog pDialog;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);


      //  fetchImages
      new WebService().execute();
    }



    String executeService(){
        HttpURLConnection urlConnection=null;
        String resp="";
        try {
        URL url = new URL("https://api.flickr.com/services/feeds/photos_public.gne?format=json");
         urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            resp= readStream(in);
           resp = resp.replace("jsonFlickrFeed(","");
           resp = resp.substring(0,resp.length()-2);
           Log.e("RESP",resp);

        }catch (IOException ioe){
            Log.e("ERROR",ioe.getMessage());
        } finally{
            urlConnection.disconnect();
            return resp;
        }
    }

    String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    class WebService extends AsyncTask<String,String,FlickerData>{

        @Override
        protected FlickerData doInBackground(String... strings) {
            String resp = executeService();
            FlickerData flickerData = new Gson().fromJson(resp,FlickerData.class);
            return flickerData;
        }

        @Override
        protected void onPostExecute(FlickerData flickerData) {
            super.onPostExecute(flickerData);

            setAdapterData(flickerData.getItems());
        }
    }

 void   setAdapterData(final List<Item> items){
        mAdapter = new GalleryAdapter(getApplicationContext(), items);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", (Serializable) items);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }
}