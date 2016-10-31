package com.tomato.z.meterview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2016/10/31.
 * 尽际 QQ 616616769
 */

public class MeterView extends View {
    private final String TAG = "MeterView";

    private Context context;

    //仪表的宽高
    private int meterViewWidth;
    private int meterViewHeight;

    //颜色变量
    private int arcColor;
    private int smallCircleColor;
    private int pointerColor;

    //刻度数量
    private int levelCount;

    //文字内容
    private String text;
    //文字大小
    private int textSize;
    //文字粗度
    private int textStrokeWidth = 1;

    int strokeWidth = 3;
    //内弧粗度
    private int arcWidth;

    //内弧填充渐变
    Shader shader;



    //当前进度
    private int percent;

    private Paint basePaint;
    private Canvas baseCanvas;
    private Bitmap baseBitmap;

    private Paint timelyPaint;

    public MeterView(Context context) {
        super(context);
    }

    public MeterView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.MeterViewInStyle);
    }

    public MeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "MeterView");
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MeterView, defStyleAttr, 0);
        arcColor = typedArray.getColor(R.styleable.MeterView_arcColor, Color.parseColor("#5FB1ED"));
        smallCircleColor = typedArray.getColor(R.styleable.MeterView_innerCircleColor, Color.parseColor("#C9DEEE"));
        pointerColor = typedArray.getColor(R.styleable.MeterView_pointerColor, Color.parseColor("#C9DEEE"));
        levelCount = typedArray.getInt(R.styleable.MeterView_levelCount, 12);
        textSize = typedArray.getDimensionPixelSize(R.styleable.MeterView_textSize, 24);
        text = typedArray.getString(R.styleable.MeterView_text);
        if(text == null || text.length() <= 0){
            text = "当前速度";
        }
        arcWidth = typedArray.getDimensionPixelOffset(R.styleable.MeterView_arcWidth, 50);

        timelyPaint = new Paint();
        timelyPaint.setAntiAlias(true);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);

        /*1、UNSPECIFIED
                父控件没有对子控件施加任何约束，子控件可以是任意大小（也就是未指定）
                UNSPECIFIED在源码里处理和EXACTLY一样，当View的宽高值为0的时候或者没有设置
                宽高值的时候，模式为UNSPECIFIED
         *2、EXACTLY
                父控件决定子控件确切的大小，子控件被限定在给定的边界里面，忽略本身的大小
                当设置width为match_parent时，模式为EXACTLY，因为子控件View会占据剩余的父空间，
                所以大小是确定的。
         *3、AT_MOST
         *      子控件最大能够达到的指定大小
         *      当设置wrap_content时，模式为AT_MOST，表示子控件View的大小最多是多少，
         *      这样这个子控件View会根据这个上限来设置自己的尺寸
         */
        if(widthMode == MeasureSpec.EXACTLY){
            meterViewWidth = widthSize;
        }else{
            meterViewWidth = 700;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            meterViewHeight = heightSize;
        }else{
            meterViewHeight = 700;
        }

        Log.i(TAG, meterViewWidth + " , " + meterViewHeight);

        //应用测量值
        setMeasuredDimension(meterViewWidth, meterViewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, "onLayout");
        baseBitmap = Bitmap.createBitmap(meterViewWidth, meterViewHeight, Bitmap.Config.ARGB_8888);
        baseCanvas = new Canvas(baseBitmap);



        basePaint = new Paint();
        basePaint.setAntiAlias(true);
        basePaint.setColor(arcColor);
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(strokeWidth);

        //画最外层弧
        RectF outterArc = new RectF(strokeWidth, strokeWidth, meterViewWidth - strokeWidth, meterViewHeight - strokeWidth);
        baseCanvas.drawArc(outterArc, 145, 250, false, basePaint);

        //画内层弧
        basePaint.setColor(Color.WHITE);
        basePaint.setStrokeWidth(arcWidth);
        RectF innerArc = new RectF(strokeWidth + 50, strokeWidth + 50, meterViewWidth - strokeWidth - 50, meterViewHeight - strokeWidth - 50);
        baseCanvas.drawArc(innerArc, 145, 250, false, basePaint);

        //画大圆
        basePaint.setColor(arcColor);
        basePaint.setStrokeWidth(strokeWidth);
        baseCanvas.drawCircle(meterViewWidth / 2, meterViewHeight / 2, 30, basePaint);
        //画小圆
        basePaint.setColor(smallCircleColor);
        basePaint.setStrokeWidth(strokeWidth + 5);
        baseCanvas.drawCircle(meterViewWidth / 2, meterViewHeight / 2, 15, basePaint);

        //绘制刻度
        basePaint.setColor(arcColor);
        basePaint.setStrokeWidth(strokeWidth);
        baseCanvas.drawLine(meterViewWidth / 2, 0, meterViewWidth / 2, levelCount, basePaint);

        //绘制右边刻度
        //旋转角度
        float roundAngle = 250f / levelCount;

        //右刻度
        //当画布进行任何位置变换后，最终均将回到最初状态
        baseCanvas.save();
        for(int i = 0; i < levelCount / 2; i++){
            baseCanvas.rotate(roundAngle, meterViewWidth / 2, meterViewHeight / 2);
            baseCanvas.drawLine(meterViewWidth / 2, 0, meterViewWidth / 2, levelCount, basePaint);
        }
        baseCanvas.restore();

        //左刻度
        baseCanvas.save();
        for(int i = 0; i < levelCount / 2; i++){
            baseCanvas.rotate(-roundAngle, meterViewWidth / 2, meterViewHeight / 2);
            baseCanvas.drawLine(meterViewWidth / 2, 0, meterViewWidth / 2, levelCount, basePaint);
        }
        baseCanvas.restore();

        //绘制矩形区域
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(arcColor);
        baseCanvas.drawRect(meterViewWidth / 2 - 60,
                meterViewHeight / 2 + 60,
                meterViewWidth / 2 + 60,
                meterViewHeight / 2 + 60 + 50,
                basePaint
                );

        shader = new LinearGradient(0, 0, meterViewWidth, meterViewHeight, Color.parseColor("#9d98cf"), Color.parseColor("#f0445e"), Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");
        canvas.drawBitmap(baseBitmap, 0, 0, basePaint);

        //绘制提示字 onDraw绘制
        timelyPaint.setTextSize(textSize);
        timelyPaint.setStrokeWidth(textStrokeWidth);
        timelyPaint.setStyle(Paint.Style.FILL);
        timelyPaint.setColor(Color.WHITE);
        float textLength = timelyPaint.measureText(text);
        canvas.drawText(text, (meterViewWidth - textLength) / 2, meterViewHeight / 2 + 60 + 60 / 2, timelyPaint);

        //绘制填充色 onDraw绘制
        timelyPaint.setShader(shader);
        timelyPaint.setStyle(Paint.Style.STROKE);
//        timelyPaint.setColor(Color.parseColor("#fe696d"));
        timelyPaint.setStrokeWidth(arcWidth);
        RectF innerArc = new RectF(strokeWidth + 50, strokeWidth + 50, meterViewWidth - strokeWidth - 50, meterViewHeight - strokeWidth - 50);
        canvas.drawArc(innerArc, 145, 250 * percent / 100, false, timelyPaint);

        //绘制表针 onDraw绘制
        timelyPaint.reset();
        timelyPaint.setStyle(Paint.Style.STROKE);
        timelyPaint.setColor(pointerColor);
        timelyPaint.setStrokeWidth(strokeWidth);

        canvas.save();
        canvas.rotate(250 * percent / 100 - 250 * 1.0f / 2, meterViewWidth / 2, meterViewHeight / 2);
        canvas.drawLine(meterViewWidth / 2, meterViewHeight / 2, meterViewWidth / 2, strokeWidth + arcWidth / 2, timelyPaint);
        canvas.restore();
    }

    public void setPercent(int percent) {
        this.percent = percent;
        this.text = percent == 100 ? "下载完成" : percent + "%";
        invalidate();
    }

    public void setText(String text) {
        this.text = text;
    }
}
