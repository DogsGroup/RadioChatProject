package com.dogs.radiochat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;



/**
 * Created by XRPQ48 on 12/9/13.
 */
public class NetworkService extends Activity {
    private static final String DEBUG_TAG = "HttpExample";
    private EditText urlText;
    private TextView textView;
    private Context context;
    private ProgressDialog pd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_message);
        context = this;
        textView = (TextView) findViewById(R.id.networkResultText);
        Intent intent = getIntent();


        String stringUrl = "http://ashok.caster.fm";
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
        }
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.

    }

    @Override
    protected void onDestroy() {
        if (pd!=null) {
            pd.dismiss();

        }
        super.onDestroy();
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        String link = null;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setTitle("Connecting...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String ret;
            // params comes from the execute() call: params[0] is the url.
            try {
                //FullscreenActivity.xmppconnect.connect();

                ret = downloadUrl(urls[0]);
                 if (ret  == null)
                 {
                     return "http://dogsgroup.mooo.com:8000/ashok.mp3";
                 }
                else
                     return ret;
            } catch (IOException e) {
                pd.setMessage("Cannot connect to Server");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
                return "Unable to retrieve web page. URL may be invalid.";
            }

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                pd.setMessage("Cannot connect to Server");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
                result = "0";
            }
            textView.setText(result);
            Intent newintent = new Intent();
            newintent.putExtra("RESULT", result);
            Log.v(this.getClass().getSimpleName(),result);
            setResult(Activity.RESULT_OK, newintent);
            finish();
        }
    }

        /* Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
    // a string. 
    */
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        StringBuffer html = new StringBuffer();
        String line = null;
        String link = null;
        String data = null;
        String ret_url = new String();
        BufferedReader reader = null;

        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "ServeStream");
            conn.setReadTimeout(6000 /* milliseconds */);
            conn.setConnectTimeout(6000 /* milliseconds */);
            conn.setRequestMethod("GET");
            //conn.setDoInput(true);
            // Starts the query
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            //is = conn.getInputStream();

            while ((line = reader.readLine()) != null) {
                html = html.append(line);
            }
            Log.v(this.getClass().getName(),html.toString());
            // Convert the InputStream into a string
            //String contentAsString = readIt(is, len);
            //return contentAsString;
            Document doc = Jsoup.parse(html.toString());
            Elements links = doc.select("a[href]");
            Log.v(this.getClass().getName(), "link size=" + links.size());
            if ( links == null || links.size() < 3){
                return null;
            }
            links.get(2).setBaseUri(myurl);
            link = links.get(2).attr("abs:href");
            if ( link == null)
                return null;
            Log.v(this.getClass().getName(), link);
            //link.getChars(48,121,data,0);
            if (link.length() < 121)
                return null;
            data = link.substring(84,121);
            ret_url = "http://shaincast.caster.fm:18255/listen.mp3?" + data;
            //ret_url.concat(data);

            data = data.replace('?',':');
            Log.v(this.getClass().getName(), ret_url);
            if ( data == null)
                return null;
            return ret_url;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}