package com.hangandkai.areyouhungry;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderConfirmationActivity extends AppCompatActivity implements CarAdapter.OnTotalPriceChangeListener {

    private GridView menuGrid2;
    private CarAdapter adapter1;
    private CheckBox checkBoxAll;
    private TextView priceAll;
    private TextView deleteButton;
    private TextView clearButton;
    private Button buttonAll;

    private static final int REQUEST_CODE_SELECT_ADDRESS = 1;
    private String baseUrl;
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
        loadConfig();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 初始化 GridView 和 CarAdapter
        menuGrid2 = findViewById(R.id.menuGrid2);
        adapter1 = new CarAdapter(this, R.layout.list_item, new ArrayList<>());
        menuGrid2.setAdapter(adapter1);
        adapter1.setOnTotalPriceChangeListener(this);

        // 初始化全选 CheckBox 和总价 TextView
        checkBoxAll = findViewById(R.id.checkBoxall);
        priceAll = findViewById(R.id.priceall);
        deleteButton = findViewById(R.id.delect);
        clearButton = findViewById(R.id.clear);
        buttonAll = findViewById(R.id.buttonall);

        checkBoxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (OrderConfirmationActivity.CategoryItem2 item : adapter1.getItems()) {
                item.isSelected = isChecked;
            }
            adapter1.notifyDataSetChanged();
            calculateTotalPrice();
        });


        deleteButton.setOnClickListener(v -> {
            adapter1.deleteSelectedItems();
            calculateTotalPrice();
        });

        clearButton.setOnClickListener(v -> {
            clearByCar();
            adapter1.clearAllItems();
            calculateTotalPrice();
        });

        buttonAll.setOnClickListener(v -> {
            ArrayList<OrderConfirmationActivity.CategoryItem2> selectedItems = new ArrayList<>();
            for (OrderConfirmationActivity.CategoryItem2 item : adapter1.getItems()) {
                if (item.isSelected) {
                    selectedItems.add(item);
                }
            }
            Intent intent = new Intent(OrderConfirmationActivity.this, SelectAddressActivity.class);
            intent.putParcelableArrayListExtra("selectedItems", selectedItems);
            startActivityForResult(intent, REQUEST_CODE_SELECT_ADDRESS);
        });

        // 发送GET请求获取购物车数据
        fetchShoppingCartData();
    }




    private void fetchShoppingCartData() {
        OkHttpClient client = new OkHttpClient();

        // 构建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/shoppingCart/listById")  // 根据实际的URL进行修改
                .get()
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData1 = response.body().string();
                    Gson gson = new Gson();
                    DataResponse2 dataResponse1 = gson.fromJson(responseData1, DataResponse2.class);
                    runOnUiThread(() -> {
                        adapter1.clear();
                        adapter1.addAll(dataResponse1.data);
                        adapter1.notifyDataSetChanged();
                        calculateTotalPrice();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }





    @Override
    public void onTotalPriceChange() {
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double totalPrice = 0.0;
        for (OrderConfirmationActivity.CategoryItem2 item : adapter1.getItems()) {
            if (item.isSelected) {
                try {
                    totalPrice += Double.parseDouble(item.amount);
                } catch (NumberFormatException e) {
                    Log.e("Price Calculation", "Invalid amount format: " + item.amount);
                }
            }
        }
        priceAll.setText(String.valueOf(totalPrice));
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resetTotalPrice", true);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resetTotalPrice", true);
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_ADDRESS && resultCode == RESULT_OK) {
            if (data != null) {
                String address = data.getStringExtra("address");
                // 处理地址信息
                Toast.makeText(this, "地址: " + address, Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * 新增方法，用于发送购物车数据到服务器
     */
    private void clearByCar() {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());  // 修改此行
        Request request = new Request.Builder()
                .url(baseUrl+"/shoppingCart/clean")
                .delete()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("delect", "delect失败: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("delect", "成功");
                } else {
                    Log.d("delect", "失败: " + response.message());
                }
            }
        });
    }


    class DataResponse2 {
        int code;
        String msg;
        List<OrderConfirmationActivity.CategoryItem2> data;
    }

    public static class CategoryItem2 implements Parcelable {
        String name;
        String amount;
        String image;
        int number;
        boolean isSelected;

        public CategoryItem2() {
        }

        protected CategoryItem2(Parcel in) {
            name = in.readString();
            amount = in.readString();
            image = in.readString();
            number = in.readInt();
            isSelected = in.readByte() != 0;
        }

        public static final Creator<CategoryItem2> CREATOR = new Creator<CategoryItem2>() {
            @Override
            public CategoryItem2 createFromParcel(Parcel in) {
                return new CategoryItem2(in);
            }

            @Override
            public CategoryItem2[] newArray(int size) {
                return new CategoryItem2[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(amount);
            dest.writeString(image);
            dest.writeInt(number);
            dest.writeByte((byte) (isSelected ? 1 : 0));
        }
    }
}
