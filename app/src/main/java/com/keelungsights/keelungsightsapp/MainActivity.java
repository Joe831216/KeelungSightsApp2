package com.keelungsights.keelungsightsapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> sights;
    private ArrayList<ArrayList<String>> sightsDetial;
    private ExpandableListView expandableListView;
    private ScrollView scrollView;
    private ExpandableListAdapter listAdapter;
    private EditText edUrl;
    private Button btnGetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        // 配置expandableListView的子項目監聽器
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("Click", "Child clicked");
                // 開啟Google map搜尋地點
                if (childPosition == 2) {
                    Toast.makeText(getApplicationContext(), "開啟地圖", Toast.LENGTH_SHORT).show();

                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(listAdapter.getChild(groupPosition, childPosition).toString()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW,gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
                return true;
            }
        });
    }

    // 初始化View
    private void initView() {
        edUrl = (EditText) findViewById(R.id.edUrl);
        btnGetData = (Button) findViewById(R.id.btnGetData);
        expandableListView = (ExpandableListView) findViewById(R.id.exListSights);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
    }

    // 配置ExpandableListAdapter
    private void newExpandableListAdapter() {
        listAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return sights.size();
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return sightsDetial.get(groupPosition).size();
            }

            @Override
            public Object getGroup(int groupPostion) {
                return sights.get(groupPostion);
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return sightsDetial.get(groupPosition).get(childPosition);
            }

            @Override
            public long getGroupId(int groupPositon) {
                return groupPositon;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
                TextView textView = getTextView();
                textView.setText(getGroup(groupPosition).toString());
                textView.setBackground((ContextCompat.getDrawable(MainActivity.this, R.drawable.myrect)));
                textView.setElevation(6);
                return textView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
                LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setBackgroundColor(Color.parseColor("#E6E6E6"));
                if (childPosition == 0) {
                    ImageView imageView = new SquareImageView(MainActivity.this);
                    new DownloadImageTask(imageView).execute(getChild(groupPosition, childPosition).toString());
                    linearLayout.addView(imageView);
                } else {
                    TextView textView = getTextView();
                    textView.setText(getChild(groupPosition, childPosition).toString());
                    linearLayout.addView(textView);
                }
                return linearLayout;
            }

            @Override
            public boolean isChildSelectable(int i, int i1) {
                return true;
            }

            private TextView getTextView() {
                TextView textView = new TextView(MainActivity.this);
                textView.setPadding(150, 0, 0, 0);
                return textView;
            }
        };
    }

    // 依子項目計算ListView總高度，可解決ListView高度問題 ( ExpandableListView顯示高度不正常的解法之一 )
    public static void setListViewHeightBaseOnChild(ExpandableListView listView) {
        // 取得expanableListView對應的Adapter
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {// expandableListAdapter.getGroupCount()返回資料項的數目
            View listGroupItem = listAdapter.getGroupView(i, true, null, listView);
            listGroupItem.measure(0, 0);// 計算子項View的寬高
            totalHeight += listGroupItem.getMeasuredHeight();// 統計所有子項的總高度
            System.out.println("height: group" + i + "次" + totalHeight);
            for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                View listChildItem = listAdapter.getChildView(i, j, false,null, listView);
                listChildItem.measure(0, 0);// 計算子項View的寬高
                totalHeight += listChildItem.getMeasuredHeight();// 統計所有子項的總高度
                System. out.println("height : group:" +i +" child:"+j+"次"+ totalHeight);
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        // listView.getDividerHeight()取得子項分隔符佔用的高度
        // params.height最後得到完整顯示整個Lisview需要的高度
        listView.setLayoutParams(params);
    }

    // 從Servlet取得資料
    public void getData(View v) {
        String url = edUrl.getText().toString();
        Log.d("url", url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        // 測試用，查看JSON內容-----------------------------------------------------------------------------
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jresponse = response.getJSONObject(i);
                                String sightName = jresponse.getString("sightName");
                                String zone = jresponse.getString("zone");
                                String category = jresponse.getString("category");
                                String photoURL = jresponse.getString("photoURL");;
                                String description = jresponse.getString("description");
                                String address = jresponse.getString("address");
                                Log.d("sightName", sightName);
                                Log.d("zone", zone);
                                Log.d("category", category);
                                Log.d("photoURL", photoURL);
                                Log.d("description", description);
                                Log.d("address", address);
                                Log.d("nextSight", " ********************************");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //--------------------------------------------------------------------------------------------------------------

                        // 解析接收到的JSON
                        parseJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", "error : " + error.toString());
                        Toast.makeText(getApplicationContext(), "資料取得失敗", Toast.LENGTH_LONG).show();
                    }
                }
        );
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    // 解析Json
    public void parseJson(JSONArray jsonArray) {

        // ListView的所有母項資料
        sights = new ArrayList<String>();

        // ListView的所有子項資料
        sightsDetial = new ArrayList<ArrayList<String>>();

        try {

            // 將JSON內容解析並存入ListView要用的ArrayList
            for(int i = 0; i < jsonArray.length();i++) {

                // ListView單個地點的子項 ( 圖片、說明、地址 )
                ArrayList<String> detial = new ArrayList<String>();

                sights.add(
                        "名稱 : " + jsonArray.getJSONObject(i).getString("sightName") + "\n" +
                        "區域 : " + jsonArray.getJSONObject(i).getString("zone") + "\n" +
                        "分類 : " + jsonArray.getJSONObject(i).getString("category")
                );
                detial.add(jsonArray.getJSONObject(i).getString("photoURL"));
                detial.add("說明 : " + jsonArray.getJSONObject(i).getString("description"));
                detial.add("地址 : " + jsonArray.getJSONObject(i).getString("address"));
                sightsDetial.add(detial);

            }

            newExpandableListAdapter();
            scrollView.setElevation(3);
            expandableListView.setAdapter(listAdapter);
            Toast.makeText(getApplicationContext(), "資料取得成功", Toast.LENGTH_LONG).show();

            //setListViewHeightBaseOnChild(expandableListView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
