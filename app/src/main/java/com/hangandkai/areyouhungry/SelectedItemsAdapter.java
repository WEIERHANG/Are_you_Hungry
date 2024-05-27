package com.hangandkai.areyouhungry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SelectedItemsAdapter extends ArrayAdapter<OrderConfirmationActivity.CategoryItem2> {
    private int resourceLayout;
    private Context mContext;

    public SelectedItemsAdapter(Context context, int resource, List<OrderConfirmationActivity.CategoryItem2> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.image);
            holder.name = convertView.findViewById(R.id.name);
            holder.amount = convertView.findViewById(R.id.amount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OrderConfirmationActivity.CategoryItem2 item = getItem(position);
        if (item != null) {
            holder.name.setText(item.name);
            holder.amount.setText(item.amount);

            if (item.image != null) {
                Glide.with(mContext)
                        .load("https://gitee.com/lucky_h/img/raw/master/" + item.image)
                        .into(holder.image);
            } else {
                holder.image.setImageResource(R.drawable.must_buy_one); // 设置一个默认的占位图
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView amount;
    }
}
