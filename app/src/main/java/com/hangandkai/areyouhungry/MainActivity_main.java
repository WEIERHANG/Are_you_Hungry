package com.hangandkai.areyouhungry;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;

import com.hangandkai.areyouhungry.R;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity_main extends AppCompatActivity {
    private GridView menuGrid, menuGridByYou;
    private ArrayAdapter<String> adapter;
    private CategoryItemByYouAdapter adapterByYou;
    private List<String> menuNames = new ArrayList<>();
    private List<CategoryItem> categoryItems = new ArrayList<>();
    private String baseUrl;
    private Button submitButton;

    private void loadConfig() {
        Properties properties = new Properties();
        InputStream input = null;
        try {
            AssetManager assetManager = getAssets();
            input = assetManager.open("ipconfig.properties");
            properties.load(input);
            baseUrl = properties.getProperty("baseUrl1");
        } catch (IOException ex) {
            ex.printStackTrace();
            baseUrl = "http://default.url"; // 使用备用URL
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item_222);

        menuGridByYou = findViewById(R.id.menuGridByYou);
        adapterByYou = new CategoryItemByYouAdapter(this, R.layout.you_layout, new ArrayList<>());
        menuGridByYou.setAdapter(adapterByYou);

        loadConfig();
        fetchMenuItems();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 购物车 OrderConfirmationActivity
                Intent intent = new Intent(MainActivity_main.this, OrderConfirmationActivity.class);
                startActivity(intent);
            }
        });

        menuGrid.setOnItemClickListener((parent, view, position, id) -> {
            CategoryItem selectedItem = categoryItems.get(position);
            menuGrid.setSelector(android.R.color.holo_blue_light);
            menuGrid.setItemChecked(position, true);
            if (selectedItem.type == 1) {
                fetchRightMenuItems(selectedItem.id);
            } else {
                fetchRightMenuItemsBy2(selectedItem.id);
            }
        });



    }

    private void fetchMenuItems() {
        menuGrid = findViewById(R.id.menuGrid);
        adapter = new ArrayAdapter<>(this, R.layout.list_it111, R.id.text1, menuNames);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(baseUrl + "/category/list")
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    DataResponse dataResponse = gson.fromJson(responseData, DataResponse.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Collections.sort(dataResponse.data, Comparator.comparingInt(item -> item.sort));
                    }

                    runOnUiThread(() -> {
                        categoryItems.clear();
                        menuNames.clear();
                        for (CategoryItem item : dataResponse.data) {
                            categoryItems.add(item);
                            menuNames.add(item.name);
                            Log.i("API调用成功", "Loaded ID: " + item.id + " with name: " + item.name);
                        }
                        adapter.notifyDataSetChanged();
                        menuGrid.setAdapter(adapter);
                        if (!categoryItems.isEmpty()) {
                            fetchRightMenuItems(categoryItems.get(0).id);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 右边
     * @param categoryId
     */
    private void fetchRightMenuItems(Long categoryId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(baseUrl + "/dish/list?categoryId=" + categoryId)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseDataByYou = response.body().string();
                    Gson gson = new Gson();
                    DataResponseByYou dataResponseByYou = gson.fromJson(responseDataByYou, DataResponseByYou.class);
                    runOnUiThread(() -> {
                        adapterByYou.clear();
                        adapterByYou.addAll(dataResponseByYou.data);
                        adapterByYou.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchRightMenuItemsBy2(Long categoryId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(baseUrl + "/setmeal/list?categoryId=" + categoryId)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseDataByYou = response.body().string();
                    System.out.println("-------"+responseDataByYou.toString());

                    Gson gson = new Gson();
                    DataResponseByYou dataResponseByYou = gson.fromJson(responseDataByYou, DataResponseByYou.class);
                    runOnUiThread(() -> {
                        adapterByYou.clear();
                        adapterByYou.addAll(dataResponseByYou.data);
                        adapterByYou.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    class DataResponse {
        int code;
        String msg;
        List<CategoryItem> data;
    }

    class CategoryItem {
        String name;
        Long id;
        int sort;
        int type;
    }

    class DataResponseByYou {
        int code;
        String msg;
        List<CategoryItemByYou> data;
    }

    class CategoryItemByYou {
        String name;
        Long id;
        String price;
        String image;
        String description;
        int sort;
    }
}