package com.hangandkai.areyouhungry;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Iterator;

public class CarAdapter extends ArrayAdapter<OrderConfirmationActivity.CategoryItem2> {
    private int resourceLayout;
    private Context mContext;
    private List<OrderConfirmationActivity.CategoryItem2> items;
    private OnTotalPriceChangeListener onTotalPriceChangeListener;

    public CarAdapter(Context context, int resource, List<OrderConfirmationActivity.CategoryItem2> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        this.items = items;
    }

    public List<OrderConfirmationActivity.CategoryItem2> getItems() {
        return items;
    }

    public void setOnTotalPriceChangeListener(OnTotalPriceChangeListener listener) {
        this.onTotalPriceChangeListener = listener;
    }

    public void deleteSelectedItems() {
        Iterator<OrderConfirmationActivity.CategoryItem2> iterator = items.iterator();
        while (iterator.hasNext()) {
            OrderConfirmationActivity.CategoryItem2 item = iterator.next();
            if (item.isSelected) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();
        if (onTotalPriceChangeListener != null) {
            onTotalPriceChangeListener.onTotalPriceChange();
        }
    }

    public void clearAllItems() {
        items.clear();
        notifyDataSetChanged();
        if (onTotalPriceChangeListener != null) {
            onTotalPriceChangeListener.onTotalPriceChange();
        }
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
            holder.number = convertView.findViewById(R.id.number);
            holder.checkbox = convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OrderConfirmationActivity.CategoryItem2 item = getItem(position);
        if (item != null) {
            holder.name.setText(item.name != null ? item.name : "");
            holder.amount.setText(item.amount != null ? item.amount : "");
            holder.number.setText(String.valueOf(item.number));

            if (item.image != null) {
                String imageUrl = "https://gitee.com/lucky_h/img/raw/master/" + item.image;
                Glide.with(mContext)
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.must_buy_three) // 占位符图片
                                .error(R.drawable.must_buy_three)) // 错误占位符
                        .into(holder.image);
                Log.d("图片 URL", imageUrl);
            } else {
                holder.image.setImageResource(R.drawable.must_buy_three); // 设置一个默认的占位图
            }

            // 设置CheckBox状态和监听器
            holder.checkbox.setOnCheckedChangeListener(null);
            holder.checkbox.setChecked(item.isSelected);
            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
                if (onTotalPriceChangeListener != null) {
                    onTotalPriceChangeListener.onTotalPriceChange();
                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView amount;
        TextView number;
        CheckBox checkbox;
    }

    public interface OnTotalPriceChangeListener {
        void onTotalPriceChange();
    }
}
