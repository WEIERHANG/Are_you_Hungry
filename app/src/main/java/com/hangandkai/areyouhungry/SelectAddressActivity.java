package com.hangandkai.areyouhungry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.content.Intent;

public class SelectAddressActivity extends AppCompatActivity {

    private static final String PREFERENCES_FILE = "MyAppPrefs";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_ID_LONG_KEY = "userIdLong";

    private Long userIdLong;
    private String userIdString;

    private Button addAddressButton;
    private LinearLayout addressInputLayout;
    private EditText addressEditText;
    private Button confirmButton;
    private TextView addressTextView;
    private TextView selectedItemsTitle;
    private ListView selectedItemsListView;
    private TextView totalPriceTextView;
    private Button payButton;
    private EditText orderNotesEditText;
    private SelectedItemsAdapter selectedItemsAdapter;
    private ArrayList<AddressItem> addressList = new ArrayList<>();
    private AddressListAdapter addressListAdapter;
    private ListView addressListView;
    private TextView noAddressTextView;
    private int selectedAddressPosition = -1;

    private double totalPrice = 0.0;
    private OkHttpClient client = new OkHttpClient();
    private String baseUrl1;

    private void loadConfig() {
        Properties properties = new Properties();
        InputStream input = null;
        try {
            AssetManager assetManager = getAssets();
            input = assetManager.open("ipconfig.properties");
            properties.load(input);
            baseUrl1 = properties.getProperty("baseUrl1");
        } catch (IOException ex) {
            ex.printStackTrace();
            baseUrl1 = "http://default.url"; // 使用备用URL
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
        setContentView(R.layout.activity_select_address);
        loadConfig();

        // 获取保存的 userid 和 userIdLong
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        userIdString = sharedPref.getString(USER_ID_KEY, "");
        userIdLong = sharedPref.getLong(USER_ID_LONG_KEY, -1);
        if (userIdLong == -1) {
            Toast.makeText(this, "无法获取用户ID", Toast.LENGTH_SHORT).show();
            return;
        }

        addAddressButton = findViewById(R.id.addAddressButton);
        addressInputLayout = findViewById(R.id.addressInputLayout);
        addressEditText = findViewById(R.id.addressEditText);
        confirmButton = findViewById(R.id.confirmButton);
        addressTextView = findViewById(R.id.addressTextView);
        selectedItemsTitle = findViewById(R.id.selectedItemsTitle);
        selectedItemsListView = findViewById(R.id.selectedItemsListView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        payButton = findViewById(R.id.payButton);
        orderNotesEditText = findViewById(R.id.remark);
        addressListView = findViewById(R.id.addressListView);
        noAddressTextView = findViewById(R.id.noAddressTextView);

        addressListAdapter = new AddressListAdapter(this, addressList);
        addressListView.setAdapter(addressListAdapter);

        ArrayList<OrderConfirmationActivity.CategoryItem2> selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        selectedItemsAdapter = new SelectedItemsAdapter(this, R.layout.selected_item, selectedItems);
        selectedItemsListView.setAdapter(selectedItemsAdapter);

        // 计算总金额
        calculateTotalPrice(selectedItems);

        addAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAddressDialog();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressEditText.getText().toString().trim();
                if (!address.isEmpty()) {
                    addressTextView.setText("收货地址: " + address);
                    addressTextView.setVisibility(View.VISIBLE);
                    addressInputLayout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(SelectAddressActivity.this, "请输入地址", Toast.LENGTH_SHORT).show();
                }
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderNotes = orderNotesEditText.getText().toString().trim();
                createOrderAndSubmit(orderNotes);
            }
        });

        addressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAddressList();
            }
        });

        // 启动时获取并展示地址列表
        fetchAddressList();
    }

    private void fetchAddressList() {
        String url = baseUrl1 + "/addressBook/list";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SelectAddressActivity.this, "获取地址列表失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> handleAddressListResponse(responseBody));
                }
            }
        });
    }

    private void handleAddressListResponse(String responseBody) {
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONArray data = response.getJSONArray("data");
            addressList.clear();
            if (data.length() == 0) {
                noAddressTextView.setVisibility(View.VISIBLE);
                addressListView.setVisibility(View.GONE);
            } else {
                noAddressTextView.setVisibility(View.GONE);
                addressListView.setVisibility(View.VISIBLE);
                for (int i = 0; i < data.length(); i++) {
                    JSONObject addressObject = data.getJSONObject(i);
                    String consignee = addressObject.getString("consignee");
                    String phone = addressObject.getString("phone");
                    String detail = addressObject.getString("detail");
                    String label = addressObject.getString("label");
                    String addressId = addressObject.getString("id");
                    addressList.add(new AddressItem(consignee, phone, detail, label, addressId));
                }
                addressListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddAddressDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_address, null);
        final EditText consigneeEditText = dialogView.findViewById(R.id.consigneeEditText);
        final EditText phoneEditText = dialogView.findViewById(R.id.phoneEditText);
        final RadioGroup sexRadioGroup = dialogView.findViewById(R.id.sexRadioGroup);
        final EditText detailEditText = dialogView.findViewById(R.id.detailEditText);
        final EditText labelEditText = dialogView.findViewById(R.id.labelEditText);
        Button finishButton = dialogView.findViewById(R.id.finishButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String consignee = consigneeEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                int selectedId = sexRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
                int sex = Integer.parseInt((String) selectedRadioButton.getTag());
                String detail = detailEditText.getText().toString().trim();
                String label = labelEditText.getText().toString().trim();

                if (!consignee.isEmpty() && !phone.isEmpty() && selectedId != -1 && !detail.isEmpty() && !label.isEmpty()) {
                    addNewAddress(consignee, phone, sex, detail, label);
                    dialog.dismiss();
                } else {
                    Toast.makeText(SelectAddressActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void addNewAddress(String consignee, String phone, int sex, String detail, String label) {
        String url = baseUrl1 + "/addressBook";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("consignee", consignee);
            json.put("phone", phone);
            json.put("sex", sex);
            json.put("detail", detail);
            json.put("label", label);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SelectAddressActivity.this, "地址添加失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SelectAddressActivity.this, "地址添加成功", Toast.LENGTH_SHORT).show();
                        fetchAddressList(); // 重新获取地址列表并展示
                    });
                }
            }
        });
    }

    private void calculateTotalPrice(ArrayList<OrderConfirmationActivity.CategoryItem2> selectedItems) {
        totalPrice = 0.0;
        for (OrderConfirmationActivity.CategoryItem2 item : selectedItems) {
            try {
                totalPrice += Double.parseDouble(item.amount);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        totalPriceTextView.setText("总金额: " + totalPrice);
    }

    private void createOrderAndSubmit(String orderNotes) {
        if (selectedAddressPosition == -1) {
            Toast.makeText(this, "请选择一个地址", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressItem selectedAddress = addressList.get(selectedAddressPosition);

        try {
            JSONObject orderJson = new JSONObject();
            orderJson.put("userId", userIdLong);
            orderJson.put("addressBookId", selectedAddress.id);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
                orderJson.put("orderTime", LocalDateTime.now().format(formatter));
                orderJson.put("checkoutTime", LocalDateTime.now().format(formatter));
            }
            orderJson.put("amount", new BigDecimal(totalPrice));
            orderJson.put("remark", orderNotes);
            orderJson.put("userName", "用户名"); // 替换为实际的用户名
            orderJson.put("phone", selectedAddress.phone);
            orderJson.put("address", selectedAddress.detail);
            orderJson.put("consignee", selectedAddress.consignee);
            orderJson.put("status", 1); // 初始状态为待付款

            String url = baseUrl1 + "/order/submit";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, orderJson.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SelectAddressActivity.this, "提交订单失败", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(SelectAddressActivity.this, "订单提交成功", Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(SelectAddressActivity.this, MainActivity_main.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "创建订单失败", Toast.LENGTH_SHORT).show();
        }
    }

    private static class AddressItem {
        String consignee;
        String phone;
        String detail;
        String label;
        String id;

        AddressItem(String consignee, String phone, String detail, String label, String id) {
            this.consignee = consignee;
            this.phone = phone;
            this.detail = detail;
            this.label = label;
            this.id = id;
        }
    }

    private class AddressListAdapter extends ArrayAdapter<AddressItem> {
        private final LayoutInflater inflater;

        AddressListAdapter(SelectAddressActivity context, ArrayList<AddressItem> addressList) {
            super(context, 0, addressList);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.address_item, parent, false);
            }

            AddressItem addressItem = getItem(position);

            RadioButton addressRadioButton = convertView.findViewById(R.id.addressRadioButton);
            TextView addressInfoTextView = convertView.findViewById(R.id.addressInfoTextView);

            String addressInfo = addressItem.consignee + "，" + addressItem.phone + "，" + addressItem.detail + "，" + addressItem.label;
            addressInfoTextView.setText(addressInfo);

            addressRadioButton.setOnCheckedChangeListener(null);
            addressRadioButton.setChecked(position == selectedAddressPosition);

            addressRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAddressPosition = position;
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
