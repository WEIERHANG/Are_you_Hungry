package com.hangandkai.areyouhungry;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryItemByYouAdapter extends ArrayAdapter<MainActivity_main.CategoryItemByYou> {
    private int resourceLayout;
    private Context mContext;
    private String userId; //
    private String serverIp; // 新增变量


    // 购物车
    private List<ShoppingCart> cartItems = new ArrayList<>();
    // 存储每个商品的数量
    private Map<String, Integer> itemCounts = new HashMap<>();
    private int totalItems = 0; // 购物车中的商品总数量

    public CategoryItemByYouAdapter(Context context, int resource, List<MainActivity_main.CategoryItemByYou> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        serverIp = ConfigUtils.getProperty(mContext, "ipconfig.properties", "baseUrl1");

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.image);
            holder.nameView = convertView.findViewById(R.id.name);
            holder.descriptionView = convertView.findViewById(R.id.description);
            holder.priceView = convertView.findViewById(R.id.price);

            holder.cartIcon = convertView.findViewById(R.id.add_to_cart);
            holder.countView = convertView.findViewById(R.id.count);
            holder.minusIcon = convertView.findViewById(R.id.minus);

            // 获取购物车商品总数量视图
            holder.cartItemCountView = ((Activity) mContext).findViewById(R.id.cartItemCount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MainActivity_main.CategoryItemByYou item = getItem(position);
        if (item != null) {

            holder.nameView.setText(item.name);
            // 截断描述文字，限制为8个字符
            String truncatedDescription = item.description.length() > 8 ? item.description.substring(0, 8) + "..." : item.description;
            holder.descriptionView.setText(truncatedDescription);
            holder.priceView.setText("₩" + item.price);

            // 显示图片
            Glide.with(mContext).load(item.image).into(holder.imageView);
            Glide.with(mContext)
                    .load("https://gitee.com/lucky_h/img/raw/master/" + item.image)
                    .into(holder.imageView);


            // 获取当前项目的数量
            int count = itemCounts.containsKey(item.name) ? itemCounts.get(item.name) : 0;
            holder.countView.setText(String.valueOf(count));
            holder.countView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            holder.minusIcon.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

            holder.cartIcon.setOnClickListener(v -> {
                int newCount = itemCounts.containsKey(item.name) ? itemCounts.get(item.name) : 0;
                newCount++;
                itemCounts.put(item.name, newCount);
                holder.countView.setText(String.valueOf(newCount));
                holder.countView.setVisibility(View.VISIBLE);
                holder.minusIcon.setVisibility(View.VISIBLE);
                totalItems++; // 增加总商品数量
                updateCartItemCount(holder.cartItemCountView);


                SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                userId = sharedPreferences.getString("userId", null);

                 // 封装成ShoppingCart对象并保存
                ShoppingCart cartItem = new ShoppingCart(
                        null, // 假设ID自动生成或另行处理
                        item.name,
                        userId,
                        null, // dishId，可根据需要设置
                        null, // setmealId，可根据需要设置
                        newCount,
                        new BigDecimal(item.price),
                        item.image
                );
                sendCartItemToServer(cartItem);
                showCartItems();
            });

            holder.minusIcon.setOnClickListener(v -> {
                int newCount = itemCounts.containsKey(item.name) ? itemCounts.get(item.name) : 0;
                if (newCount > 0) {
                    newCount--;
                    itemCounts.put(item.name, newCount);
                    holder.countView.setText(String.valueOf(newCount));
                    if (newCount == 0) {
                        holder.countView.setVisibility(View.GONE);
                        holder.minusIcon.setVisibility(View.GONE);
                    }
                    totalItems--; // 减少总商品数量
                    updateCartItemCount(holder.cartItemCountView);
                    // 更新ShoppingCart对象数量
                    for (ShoppingCart cartItem : cartItems) {
                        if (cartItem.getName().equals(item.name)) {
                            cartItem.setNumber(newCount);
                            if (newCount == 0) {
                                cartItems.remove(cartItem);
                            }
                            break;
                        }
                    }
                }
            });
        }
        return convertView;
    }

    private int getDrawableResourceId(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    /**
     * 更新购物车中的商品总数量
     */
    public void updateCartItemCount(TextView cartItemCountView) {
        cartItemCountView.setText("购物车商品数: " + totalItems);
    }


    public void showCartItems() {
        if (cartItems.isEmpty()) {
            Log.d("ShoppingCartList", "购物车为空");
        } else {
            for (ShoppingCart item : cartItems) {
                Log.d("ShoppingCartList", item.toString());
            }
        }
    }


    /**
     * 新增方法，用于发送购物车数据到服务器
     */
    private void sendCartItemToServer(ShoppingCart cartItem) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", cartItem.getName());
            jsonObject.put("userId", cartItem.getUserId());
            jsonObject.put("dishId", cartItem.getDishId());
            jsonObject.put("setmealId", cartItem.getSetmealId());
            jsonObject.put("number", cartItem.getNumber());
            jsonObject.put("amount", cartItem.getAmount());
            jsonObject.put("image", cartItem.getImage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());  // 修改此行
        Request request = new Request.Builder()
                .url(serverIp+"/shoppingCart/add")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ShoppingCart", "发送购物车数据失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("ShoppingCart", "成功发送购物车数据");
                } else {
                    Log.d("ShoppingCart", "发送购物车数据失败: " + response.message());
                }
            }
        });
    }
    static class ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView descriptionView;
        TextView priceView;
        ImageView cartIcon;
        TextView countView;
        ImageView minusIcon;
        TextView cartItemCountView;
    }
}
