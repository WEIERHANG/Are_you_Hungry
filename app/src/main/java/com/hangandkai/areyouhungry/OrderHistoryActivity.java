package com.hangandkai.areyouhungry;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private ListView orderListView;
    private List<OrderItem> orderList;
    private OrderAdapter adapter;
    private String baseUrl;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderListView = findViewById(R.id.orderListView);

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList);
        orderListView.setAdapter(adapter);

        loadConfig();
        fetchOrderHistory();

        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OrderItem order = orderList.get(position);
                order.setExpanded(!order.isExpanded());
                adapter.notifyDataSetChanged();
            }
        });
    }

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

    private void fetchOrderHistory() {
        String url = baseUrl + "/order/list";
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    OrderResponse orderResponse = gson.fromJson(responseData, OrderResponse.class);
                    runOnUiThread(() -> {
                        orderList.clear();
                        orderList.addAll(mergeOrders(orderResponse.data));
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<OrderItem> mergeOrders(List<OrderItem> orders) {
        Map<String, OrderItem> mergedOrders = new HashMap<>();
        for (OrderItem order : orders) {
            if (!mergedOrders.containsKey(order.getOrderId())) {
                mergedOrders.put(order.getOrderId(), order);
            } else {
                OrderItem existingOrder = mergedOrders.get(order.getOrderId());
                existingOrder.addSubOrder(order);
            }
        }
        return new ArrayList<>(mergedOrders.values());
    }

    class OrderResponse {
        int code;
        String msg;
        List<OrderItem> data;
    }
}
