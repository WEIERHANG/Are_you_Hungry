package com.hangandkai.areyouhungry.layBot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class CircleImageView extends AppCompatImageView {

    private int mSize;
    private Paint mPaint;

    public CircleImageView(Context context) {
        this(context,null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleImageView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mSize = Math.min(width,height);  //取宽高的最小值
        setMeasuredDimension(mSize,mSize);    //设置CircleImageView为等宽高
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取sourceBitmap，即通过xml或者java设置进来的图片
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        Bitmap sourceBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        if (sourceBitmap != null){
            //对图片进行缩放，以适应控件的大小
            Bitmap bitmap = resizeBitmap(sourceBitmap,getWidth(),getHeight());
            drawCircleBitmapByShader(canvas,bitmap);    //利用BitmapShader实现
        }
    }

    private Bitmap resizeBitmap(Bitmap sourceBitmap,int dstWidth,int dstHeight){
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        float widthScale = ((float)dstWidth) / width;
        float heightScale = ((float)dstHeight) / height;

        //取最大缩放比
        float scale = Math.max(widthScale,heightScale);
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);

        return Bitmap.createBitmap(sourceBitmap,0,0,width,height,matrix,true);
    }

    private void drawCircleBitmapByShader(Canvas canvas,Bitmap bitmap){
        BitmapShader shader = new BitmapShader(bitmap,BitmapShader.TileMode.CLAMP,BitmapShader.TileMode.CLAMP);
        mPaint.setShader(shader);
        canvas.drawCircle(mSize / 2,mSize /2 ,mSize / 2,mPaint);
    }


}

