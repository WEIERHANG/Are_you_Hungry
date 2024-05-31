package com.hangandkai.areyouhungry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends BaseAdapter {

    private Context context;
    private List<OrderItem> orderList;

    public OrderAdapter(Context context, List<OrderItem> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        }

        OrderItem order = orderList.get(position);

        TextView orderIdTextView = convertView.findViewById(R.id.orderIdTextView);
        TextView amountTextView = convertView.findViewById(R.id.amountTextView);
        TextView orderTimeTextView = convertView.findViewById(R.id.orderTimeTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);
        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView consigneeTextView = convertView.findViewById(R.id.consigneeTextView);
        TextView remarkTextView = convertView.findViewById(R.id.remarkTextView);
        LinearLayout subOrderLayout = convertView.findViewById(R.id.subOrderLayout);

        // 设置订单的基本信息
        orderIdTextView.setText("订单ID: " + order.getOrderId());

        // 将时间戳转换为日期字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String orderTime = sdf.format(new Date(order.getOrderTime()));
        orderTimeTextView.setText("时间: " + orderTime);

        amountTextView.setText("总金额: " + order.getTotalAmount());

        statusTextView.setText("状态: " + order.getStatusString());

        addressTextView.setText("地址: " + order.getAddress());
        consigneeTextView.setText("收货人: " + order.getConsignee());
        remarkTextView.setText("备注: " + order.getRemark());

        // 设置展开和折叠逻辑
        subOrderLayout.removeAllViews();
        if (order.isExpanded() && order.getSubOrders() != null) {
            Map<String, Integer> productQuantities = order.getProductQuantities();
            for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
                String productName = entry.getKey();
                int quantity = entry.getValue();

                View subOrderView = LayoutInflater.from(context).inflate(R.layout.sub_order_item, subOrderLayout, false);

                TextView subOrderNameTextView = subOrderView.findViewById(R.id.subOrderNameTextView);
                TextView subOrderQuantityTextView = subOrderView.findViewById(R.id.subOrderQuantityTextView);
                ImageView subOrderImageView = subOrderView.findViewById(R.id.subOrderImageView);

                subOrderNameTextView.setText(productName);
                subOrderQuantityTextView.setText("数量: " + quantity + "个");

                // 假设所有相同名称的商品图片相同，加载第一个相同名称的商品图片
                for (OrderItem subOrder : order.getSubOrders()) {
                    if (subOrder.getName().equals(productName)) {
                        Glide.with(context)
                                .load("https://gitee.com/lucky_h/img/raw/master/" + subOrder.getImage())
                                .into(subOrderImageView);
                        break;
                    }
                }

                subOrderLayout.addView(subOrderView);
            }
        }

        return convertView;
    }
}
