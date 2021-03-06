package com.example.savepenguin.mypage;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savepenguin.GetPointTask;
import com.example.savepenguin.IpSetting;
import com.example.savepenguin.R;
import com.example.savepenguin.account.SharedPreference;
import com.example.savepenguin.mainpage.MainActivity;
import com.example.savepenguin.mainpage.PenguinFragement;
import com.example.savepenguin.model.PenguinItem;
import com.example.savepenguin.model.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class PenguinShopActivity extends AppCompatActivity {

    LinearLayoutManager linearLayoutManager;
    RecyclerView penguinItemList;
    PenguinItemListAdapter adapter;
    TextView text_user, text_point;
    ArrayList<PenguinItem> penguinItems;
    public static Context mContext;
    private String userid;
    public int userPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penguinshop);
        userid = SharedPreference.getAttribute(getApplicationContext(), "userid");
        mContext = this;
        text_user = findViewById(R.id.textView_userid_penguinshop);
        text_point = findViewById(R.id.textView_userpoint_penguinshop);

        //????????? ???????????????
        try {
            GetPointTask getPointTask = new GetPointTask();
            userPoint = Integer.parseInt(getPointTask.execute(userid).get());
            Log.v("?????? ?????????", userid + "??? ?????? ???????????? " + userPoint);

        } catch (Exception e) {

        }

        text_user.setText(userid + "???");
        text_point.setText(userPoint + "???");

        penguinItemList = findViewById(R.id.ListView_penguinItem);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        penguinItemList.setLayoutManager(manager);
        penguinItems = new ArrayList<>();
        dummyData();
        adapter = new PenguinItemListAdapter(penguinItems);

        penguinItemList.setAdapter(adapter);

        System.out.println(adapter.imageCode);

    }

    public void dummyData() {

        penguinItems.add(new PenguinItem("item2", getResources().getDrawable(R.drawable.penguin2), 1000));
        penguinItems.add(new PenguinItem("item3", getResources().getDrawable(R.drawable.penguin3), 700));
        penguinItems.add(new PenguinItem("item4", getResources().getDrawable(R.drawable.penguin4), 500));
        penguinItems.add(new PenguinItem("item5", getResources().getDrawable(R.drawable.penguin5), 7000));
        penguinItems.add(new PenguinItem("item6", getResources().getDrawable(R.drawable.penguin6), 2500));

    }

    public void changePenguin(int imageCode) {
        Log.v("????????? ?????????", "?????? ????????? ??????");

        ((MainActivity) MainActivity.context).penguinView.setImageDrawable(penguinItems.get(imageCode).getItemImage());
        //????????? ?????? ??????

    }

    public int getUserPoint() {
        int curPoint = userPoint;
        try {
            getPointTask task = new getPointTask();
            String result = task.execute(userid).get();
            Log.v("????????? ?????????", "?????? ????????? : " + result);
        } catch (Exception e) {

        }
        userPoint = curPoint;
        updatePointText();

        return curPoint;
    }

    public void usePoint(int price) {
        userPoint = userPoint - price;
        Log.v("????????? ?????????", "");
        try {
            updatePointTask task = new updatePointTask();
            String result = task.execute(userid, String.valueOf(userPoint)).get();
            Log.v("????????? ?????????", "?????? ????????? : " + result);
        } catch (Exception e) {

        }
        updatePointText();

    }

    public void updatePointText() {
        text_point.setText(userPoint + "???");
        ((MainActivity) MainActivity.context).text_userPoint.setText(userPoint + "???");
    }

    class getPointTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        String id;
        IpSetting ipSetting = new IpSetting();
        @Override
        // doInBackground??? ???????????? ?????? ???????????? ????????? ?????? ?????????
        protected String doInBackground(String... strings) {
            try {
                id = strings[0];
                String str;
                URL url = new URL(ipSetting.getBaseUrl() + "/user/pointlist/" + strings[0]);  // ?????? ????????? ????????????(localhost ??????.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //???????????? POST ???????????? ???????????????.
                conn.setDoOutput(true);
                conn.setConnectTimeout(1000);

                // ????????? ?????? ??? ????????? ?????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "userid="+strings[0]; // GET???????????? ????????? POST??? ?????? ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter??? ?????? ??????
                osw.flush();

                // jsp??? ????????? ??? ??????, ???????????? ?????? ??? ??????.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // ????????? ????????? ????????? ???????????? ??????
                    Log.i("?????? ??????", conn.getResponseCode()+"??????");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // ???????????? ?????? ?????? ???????????????.
            return receiveMsg;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            System.out.println(s);
        }
    }
    class updatePointTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        String id;
        IpSetting ipSetting = new IpSetting();
        @Override
        // doInBackground??? ???????????? ?????? ???????????? ????????? ?????? ?????????
        protected String doInBackground(String... strings) {
            try {
                id = strings[0];
                String str;
                URL url = new URL(ipSetting.getBaseUrl() + "/user/pointUpdate/" + strings[0]);  // ?????? ????????? ????????????(localhost ??????.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //???????????? POST ???????????? ???????????????.
                conn.setDoOutput(true);
                conn.setConnectTimeout(1000);

                // ????????? ?????? ??? ????????? ?????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "userid="+strings[0]+"&userpoint="+strings[1]; // GET???????????? ????????? POST??? ?????? ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter??? ?????? ??????
                osw.flush();

                // jsp??? ????????? ??? ??????, ???????????? ?????? ??? ??????.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // ????????? ????????? ????????? ???????????? ??????
                    Log.i("?????? ??????", conn.getResponseCode()+"??????");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // ???????????? ?????? ?????? ???????????????.
            return receiveMsg;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            System.out.println(s);
        }
    }
}



class PenguinItemListAdapter extends RecyclerView.Adapter<PenguinItemListAdapter.Holder> {

    ArrayList<PenguinItem> items = new ArrayList<>();
    public int imageCode = 0;


    public PenguinItemListAdapter(ArrayList<PenguinItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PenguinItemListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_penguinitem, parent, false);
        return new PenguinItemListAdapter.Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PenguinItemListAdapter.Holder holder, int position) {
        PenguinItem item = items.get(position);
        holder.itemImage.setImageDrawable(item.getItemImage());
        holder.itemName.setText(item.getItemName());
        holder.itemPrice.setText(item.getItemPrice() + "???");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView itemName, itemPrice;
        private ImageView itemImage;
        private Button buyBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.textView_itemName);
            itemImage = itemView.findViewById(R.id.penguin_Item_img);
            itemPrice = itemView.findViewById(R.id.textView_itemPrice);
            buyBtn = itemView.findViewById(R.id.button_buyItem);

            buyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("?????? ???", itemName.getText().toString() + " ????????? ??????");

                    // ?????? ???????????? ????????????????????? ?????? ???????????? => ?????? ?????? ????????? ??????, ????????? ?????? ????????? ??????
                    // + ?????? ????????? ?????? ?????? ??????
                    String strP = itemPrice.getText().toString();
                    int price = Integer.parseInt(strP.substring(0, strP.length() - 1));
                    int userpoint = ((PenguinShopActivity) PenguinShopActivity.mContext).getUserPoint();
                    if (price <= userpoint) {
                        Log.v("?????? ???", "????????? ?????? ??????");

                        ((PenguinShopActivity) PenguinShopActivity.mContext).usePoint(price);
                        ((PenguinShopActivity) PenguinShopActivity.mContext).changePenguin(getAdapterPosition());
                    } else {
                        Log.v("?????? ???", "????????? ?????? ??????, ????????? ??????");

                    }


                }
            });
        }
    }
}
