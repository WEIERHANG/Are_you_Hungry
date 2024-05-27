package com.hangandkai.areyouhungry;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SelectAddressActivity extends AppCompatActivity {

    private Button addAddressButton;
    private LinearLayout addressInputLayout;
    private EditText addressEditText;
    private Button confirmButton;
    private TextView addressTextView;
    private TextView selectedItemsTitle;
    private ListView selectedItemsListView;
    private TextView totalPriceTextView;
    private Button payButton;
    private SelectedItemsAdapter selectedItemsAdapter;

    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        addAddressButton = findViewById(R.id.addAddressButton);
        addressInputLayout = findViewById(R.id.addressInputLayout);
        addressEditText = findViewById(R.id.addressEditText);
        confirmButton = findViewById(R.id.confirmButton);
        addressTextView = findViewById(R.id.addressTextView);
        selectedItemsTitle = findViewById(R.id.selectedItemsTitle);
        selectedItemsListView = findViewById(R.id.selectedItemsListView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        payButton = findViewById(R.id.payButton);

        ArrayList<OrderConfirmationActivity.CategoryItem2> selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        selectedItemsAdapter = new SelectedItemsAdapter(this, R.layout.selected_item, selectedItems);
        selectedItemsListView.setAdapter(selectedItemsAdapter);

        // 计算总金额
        calculateTotalPrice(selectedItems);

        addAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressInputLayout.setVisibility(View.VISIBLE);
                addAddressButton.setVisibility(View.GONE);
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
                // 处理支付逻辑
                Toast.makeText(SelectAddressActivity.this, "支付功能未实现", Toast.LENGTH_SHORT).show();
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
}
