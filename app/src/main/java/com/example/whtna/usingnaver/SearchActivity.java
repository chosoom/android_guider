package com.example.whtna.usingnaver;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.nhn.android.maps.maplib.NGeoPoint;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchActivity extends AppCompatActivity{
    public static StringBuilder sb;//

    EditText addressData;
    ListView listView;
    Geocoder geocoder;
    int btclick = 0;
    ArrayList<list_item> list_itemArrayList;
    MyListAdapter myListAdapter;
    Thread th;
    Thread thr;
    Handler han;
    String[] title;
    String[] choiceTitle;
    String[] address;
    double[] mapx;
    double[] mapy;
    int posi;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        addressData = (EditText) findViewById(R.id.searchData);
        geocoder = new Geocoder(this);
        listView = (ListView) findViewById(R.id.findData);
        list_itemArrayList = new ArrayList<list_item>();
        myListAdapter = new MyListAdapter(SearchActivity.this,list_itemArrayList);
        listView.setAdapter(myListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posi = position;
                parent.getChildAt(position).setBackgroundColor(Color.BLUE);
                //ex.setText(title[addr.size()-1]);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FindData(title[posi]);
                                //ex.setText("위도 "+mapx[count]+"경도 "+mapy[count++]);
                            }
                        });
                    }
                }).start();
            }
        });
        han = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                makeList();
            }
        };
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                String line = naver();
                Bundle bun = new Bundle();
                bun.putString("result",line);
                Message msg = han.obtainMessage();
                msg.setData(bun);
                han.sendMessage(msg);
            }
        });
    }


    public void SearchBtClicked(View v){
        th.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FindData();
                    }
                });
            }
        }).start();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(addressData.getWindowToken(),0);
        btclick=1;
    }
    public void mapBtClicked(View v){
        if(btclick != 0) {
            Intent intent = new Intent(SearchActivity.this, MapsActivity.class);
            intent.putExtra("title",choiceTitle);
            intent.putExtra("mapx",mapx);
            intent.putExtra("mapy",mapy);
            startActivity(intent);
        }else{
            Toast.makeText(SearchActivity.this,"주소 입력해주세요",Toast.LENGTH_SHORT);
        }
    }
    public void FindData(){
        ArrayList<Address> a = null;
        try{
            a = (ArrayList)geocoder.getFromLocationName(addressData.getText().toString(),1);
            choiceTitle[count] = a.get(0).getFeatureName();
            mapx[count] = a.get(0).getLatitude();
            mapy[count++] = a.get(0).getLongitude();
        }catch(Exception e){
            e.printStackTrace();
            Log.e("테스터","입출력 오류발생");
            Toast.makeText(SearchActivity.this,"안됨",Toast.LENGTH_SHORT);
        }
    }
    public void FindData(String Address){
        ArrayList<Address> a = null;
        try{
            a = (ArrayList) geocoder.getFromLocationName(Address, 1);
            choiceTitle[count] = title[posi];
            mapx[count] = a.get(0).getLatitude();
            mapy[count++] = a.get(0).getLongitude();
        }catch(Exception e){
            e.printStackTrace();
            Log.e("테스터","입출력 오류발생");
            Toast.makeText(SearchActivity.this,"안됨",Toast.LENGTH_SHORT);
        }
    }

    public void makeList() {
        for(int i=0;i<title.length;i++) {
            list_itemArrayList.add(new list_item(title[i], address[i]));
        }
    }

    public String naver() {
        String addressname = addressData.getText().toString();
        String clientId = "gKm917rSnKyL7Dw7sDKm";
        String clientSecret = "lKezxTa6IS";
        int display = 15;

        try {
            String text = URLEncoder.encode(addressname+" 관광명소", "utf-8");
            String apiURL = "https://openapi.naver.com/v1/search/local?query=" + text + "&display=" + display + "&sort=comment&";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
                line = line+line;
            }

            br.close();
            con.disconnect();
            splitJson(display);
            return line;

        } catch (Exception e) {
            System.out.println(e);
            return e.getMessage();
        }
    }
    public void splitJson(int display){
        String data = sb.toString();
        String[] array;
        array = data.split("\"");
        title = new String[display];
        address = new String[display];
        mapx = new double[display];
        mapy = new double[display];
        choiceTitle = new String[display];
        int k = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals("title"))
                title[k] = array[i + 2];
            if (array[i].equals("address")) {
                address[k] = array[i + 2];
                k++;
            }
        }
    }
}