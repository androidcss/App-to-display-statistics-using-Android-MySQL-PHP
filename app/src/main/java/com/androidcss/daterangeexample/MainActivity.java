package com.androidcss.daterangeexample;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    Spinner spinner;
    TextView textOrders;
    TextView textTotal;
    TextView textDiscount;
    TextView textNetTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textOrders = (TextView) findViewById(R.id.textOrders);
        textTotal = (TextView) findViewById(R.id.textTotal);
        textDiscount = (TextView) findViewById(R.id.textDiscount);
        textNetTotal = (TextView) findViewById(R.id.textNetTotal);

        spinner = (Spinner) findViewById(R.id.spinner);
        // Setup item selected listener
        spinner.setOnItemSelectedListener(this);

        // Apply Adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.date_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        new AsyncRetrieve(position).execute();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class AsyncRetrieve extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;
        int position;

        public AsyncRetrieve(int position){
            this.position = position;
        }

        //this method will interact with UI, here display loading message
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        // This method does not interact with UI, You need to pass result to onPostExecute to display
        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your php file resides
                url = new URL("http://192.168.1.7/revenue-fetch.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoOutput to true as we recieve data from json file
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("position", position + "");
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                conn.disconnect();
            }

            return("error");

        }

        // this method will interact with UI, display result sent from doInBackground method
        @Override
        protected void onPostExecute(String result) {

            pdLoading.dismiss();
            JSONArray jArray = null;

            if(result.equals("unsuccessful"))
            {
                Toast.makeText(MainActivity.this,"Connection Problem. Http is not ok.",Toast.LENGTH_LONG).show();

            }else if(result.equals("error")){

                Toast.makeText(MainActivity.this,"Possibly Exception! Check your terminal for errors.",Toast.LENGTH_LONG).show();

            }else{

                try {
                    //Toast.makeText(MainActivity.this,result.toString(),Toast.LENGTH_LONG).show();
                    JSONObject json_data = new JSONObject(result);
                    textOrders.setText(json_data.getInt("orders") + "");
                    textTotal.setText("Rs. " + json_data.getInt("total"));
                    textDiscount.setText("Rs. " + json_data.getInt("discount"));
                    textNetTotal.setText("Rs. " + json_data.getInt("net_total"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
