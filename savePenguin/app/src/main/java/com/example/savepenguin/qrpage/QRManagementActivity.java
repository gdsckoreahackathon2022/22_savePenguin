package com.example.savepenguin.qrpage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savepenguin.IpSetting;
import com.example.savepenguin.R;
import com.example.savepenguin.account.SharedPreference;
import com.example.savepenguin.mainpage.MainActivity;
import com.example.savepenguin.model.QR;
import com.example.savepenguin.mypage.PenguinShopActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class QRManagementActivity extends AppCompatActivity {

    RecyclerView userList;
    LinearLayoutManager linearLayoutManager;
    public UserListAdapter adapter;
    public static Context context;
    public ArrayList<QR> items = new ArrayList<>();
    private String userid;
    IpSetting ipSetting = new IpSetting();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_management);
        userid = SharedPreference.getAttribute(getApplicationContext(), "userid");
        context = this;
        Log.v("QR ?????? ?????????", "QR ?????? Activity ??????");

        try {
            getQRList task = new getQRList();
            String result = task.execute(userid).get();
            Log.v("????????? ?????????", "?????? ????????? : " + result);
        } catch (Exception e) {

        }

        Button createQrBtn = findViewById(R.id.button_createQR);
        createQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("QR ?????? ?????????", "QR ?????? ?????? ??????");

                Intent intent = new Intent(getApplicationContext(), CreateQRActivity.class);
                startActivity(intent);
            }
        });


        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_qr);

        userList = findViewById(R.id.ListView_QRList);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        adapter = new UserListAdapter(items, userid);
        userList.setLayoutManager(linearLayoutManager);
        userList.setAdapter(adapter);
    }

    public void showQR(String qrname) {

        //????????? ?????? ??? ??????
        Intent intent = new Intent(this, ShowQRActivity.class);

        //????????? input?????? intent??? ????????????.
        intent.putExtra("qrname", qrname);
        //???????????? ??????
        startActivity(intent);
    }

    class getQRList extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        String id;

        @Override
        // doInBackground??? ???????????? ?????? ???????????? ????????? ?????? ?????????
        protected String doInBackground(String... strings) {
            try {
                id = strings[0];
                String str;
                URL url = new URL(ipSetting.getBaseUrl() + "/qrcode/" + strings[0] );  // ?????? ????????? ????????????(localhost ??????.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //???????????? POST ???????????? ???????????????.
                conn.setDoOutput(true);
                conn.setConnectTimeout(1000);

                // ????????? ?????? ??? ????????? ?????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "userid=" + strings[0]; // GET???????????? ????????? POST??? ?????? ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter??? ?????? ??????
                osw.flush();

                // jsp??? ????????? ??? ??????, ???????????? ?????? ??? ??????.
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // ????????? ????????? ????????? ???????????? ??????
                    Log.i("?????? ??????", conn.getResponseCode() + "??????");
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

            if (s != null) {
                JSONObject jsonObj = null;
                JSONArray qrList = null;
                try {
                    jsonObj = new JSONObject(s);
                    qrList = jsonObj.getJSONArray("qrlist");
                    System.out.println("?????? " +qrList.length());
                    for (int i = 0; i < qrList.length(); i++) {
                        JSONObject qr = qrList.getJSONObject(i);
                        String qrname = qr.getString("qrname");
                        String imageData = qr.getString("data");
                        System.out.println("qrname = " + qrname);
                        System.out.println("imageData = " + imageData);
                        //byte[] image = imageData.getBytes(StandardCharsets.UTF_8);
                        byte[] image = Base64.decode(imageData, Base64.DEFAULT);

                        //byte[] image = imageData.getBytes();
                        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);

                        //items.add(new QR(qrname, "test", bmp));
                        add(new QR(qrname, "test", bmp));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }

            System.out.println(s);
        }
    }
    public void add(QR qr) {
        boolean isValid = true;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getQrName().equals(qr.getQrName())) {
                isValid = false;
                break;
            }
        }

        if (isValid) {
            items.add(qr);
        }
    }
    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        String id;

        @Override
        // doInBackground??? ???????????? ?????? ???????????? ????????? ?????? ?????????
        protected String doInBackground(String... strings) {
            try {
                id = strings[0];
                String str;
                URL url = new URL(ipSetting.getBaseUrl() + "/TestUpdateQR");  // ?????? ????????? ????????????(localhost ??????.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //???????????? POST ???????????? ???????????????.
                conn.setDoOutput(true);
                conn.setConnectTimeout(1000);

                // ????????? ?????? ??? ????????? ?????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "userid=" + strings[0]; // GET???????????? ????????? POST??? ?????? ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter??? ?????? ??????
                osw.flush();

                // jsp??? ????????? ??? ??????, ???????????? ?????? ??? ??????.
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // ????????? ????????? ????????? ???????????? ??????
                    Log.i("?????? ??????", conn.getResponseCode() + "??????");
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

            //doInBackground()??? ?????? ????????? ?????? onPostExecute()??? ??????????????? ??????????????? s??? ????????????.

            System.out.println(s);
        }
    }

}


class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.Holder> {

    ArrayList<QR> items = new ArrayList<>();
    private String userid;

    public UserListAdapter(ArrayList<QR> items, String userid) {
        this.items = items;
        this.userid = userid;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_qritem, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        QR item = items.get(position);
        holder.profileImg.setImageBitmap(item.getProfile());
        holder.qrName.setText(item.getQrName());
        holder.about.setText(item.getAbout());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private ImageView profileImg;
        private EditText qrName, about;
        private Button qrBtn, qrEditBtn;


        public Holder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.penguin_Item_img);
            qrName = itemView.findViewById(R.id.edittext_qrname);
            about = itemView.findViewById(R.id.edittext_aboutqr);
            qrBtn = itemView.findViewById(R.id.button_qrBtn);
            qrEditBtn = itemView.findViewById(R.id.button_buyItem);

            qrName.setTag(qrName.getKeyListener());
            qrName.setKeyListener(null);
            about.setTag(about.getKeyListener());
            about.setKeyListener(null);

            // ????????? ?????????
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    // ???????????? ????????? ????????? ??????
                    if (pos != RecyclerView.NO_POSITION) {
                        QR qr = items.get(pos);
                        Log.v("QR ?????? ?????????", pos + "?????? QR ??????");
                    }
                }
            });

            //qr ?????? ?????? => qr ???????????? ???????????? ???????????????
            qrBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Log.v("QR ?????? ?????????", pos + "?????? QR ?????? ?????? ??????");
                        ((QRManagementActivity) QRManagementActivity.context).showQR(items.get(pos).getQrName());

                    }
                }
            });

            //qr ?????? ?????? ??????
            qrEditBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Log.v("QR ?????? ?????????", pos + "?????? QR ?????? ?????? ??????");

                        if (qrName.getKeyListener() == null) {
                            Log.v("QR ?????? ?????????", pos + "?????? qr ?????? ?????? ??????");
                            qrName.setKeyListener((KeyListener) qrName.getTag());
                            about.setKeyListener((KeyListener) about.getTag());
                        } else {
                            Log.v("QR ?????? ?????????", pos + "?????? qr ?????? ????????????");
                            qrName.setKeyListener(null);
                            about.setKeyListener(null);

                            //????????? ????????? qr????????? ?????? ???????????? ?????????
                            //????????? qr ????????? ?????? ?????? ?????????
                            ContentValues qrInfo = new ContentValues();
                            qrInfo.put("qrname", qrName.getText().toString());
                            qrInfo.put("about", about.getText().toString());
                            qrInfo.put("userid", userid);
                            //qrInfo.put("cuptype",);
                        }

                    }
                }
            });
        }
    }
}