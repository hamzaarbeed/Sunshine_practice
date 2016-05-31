package com.hamza.sunshine;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

public class MainFragment extends Fragment {

    ListView theListView;
    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String[] info = {"1","2","3","4","5","6","7","8","9","0"};
        View v = inflater.inflate(R.layout.main_fragment,container,false);
        theListView = (ListView)v.findViewById(R.id.theListView);

        GetForecastTask netTask= new GetForecastTask();
        netTask.execute("http://api.openweathermap.org/data/2.5/forecast/daily?zip=77063&mode=json&units=metric&cnt=7&appid=e51335dda8ebe7435e0a33126af738e3");
        //adapter = new ArrayAdapter<String>(getActivity(),R.layout.row,R.id.textView2,info);
        //theListView.setAdapter(adapter);
        return v;
    }
    public class GetForecastTask extends AsyncTask<String,String,String[]> {

        @Override
        protected void onPostExecute(String[] strings) {
            adapter = new ArrayAdapter<String>(getActivity(),R.layout.row,R.id.textView2,strings);
            theListView.setAdapter(adapter);
        }

        @Override
        protected String[] doInBackground(String... params) {

            return parseJson(getForecastFromWeb(params[0]));
        }

        private String[] parseJson(String forecastJson){
            String[] results=null;
            if (forecastJson.length()==0)
                return null;
            try {
                JSONObject allWeather = null;
                allWeather = new JSONObject(forecastJson);
                JSONArray list = allWeather.getJSONArray("list");
                SimpleDateFormat reformatedDate = new SimpleDateFormat("EEE MMM dd");
                results= new String[list.length()];
                for (int i=0;i<list.length();i++) {
                    JSONObject dayNum = list.getJSONObject(i);
                    String dateStr = reformatedDate.format(System.currentTimeMillis() + 86400000*i);
                    String weather = dayNum.getJSONArray("weather").getJSONObject(0).getString("main");
                    JSONObject temp = dayNum.getJSONObject("temp");
                    Double maxTemp = temp.getDouble("max");
                    Double minTemp = temp.getDouble("min");
                    results[i] = dateStr + " - " + weather + " - " + Math.round( maxTemp) + "/" + Math.round(minTemp);
                }
                return results;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return results;
        }

        private String getForecastFromWeb(String urlstr){
            String forecastJson="";
            HttpURLConnection urlConnection=null;
            BufferedReader reader = null;
            try {
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?zip=77063&mode=json&units=metric&cnt=7&appid=e51335dda8ebe7435e0a33126af738e3");
                URL url =new URL(urlstr);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                forecastJson = reader.readLine();

                Log.v("finalJson", "Forecast string: " + forecastJson);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return forecastJson;
        }
    }

}
