package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自己画的柱状图控件：把每个月的资产画成一根柱子
 * 【课外知识】继承 View、重写 onDraw、用 Canvas 画图形、用 Paint 设置画笔
 */
public class BarChartView extends View {

    private double[] values = new double[0];      // 要画的数据

    private Paint paintBar = new Paint();         // 画笔：柱子
    private Paint paintText = new Paint();        // 画笔：文字
    private Paint paintAxis = new Paint();        // 画笔：坐标轴

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintBar.setColor(Color.parseColor("#0F766E"));    // 墨玉青
        paintBar.setAntiAlias(true);
        paintText.setColor(Color.parseColor("#115E59"));
        paintText.setTextSize(24);
        paintText.setAntiAlias(true);
        paintAxis.setColor(Color.parseColor("#CBD9D7"));
        paintAxis.setStrokeWidth(2);
    }

    // 外部调用这个方法把数据传进来
    public void setValues(double[] data) {
        this.values = data;
        invalidate();          // 通知系统重新画（会再走一次 onDraw）
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) {
            return;
        }

        int w = getWidth();
        int h = getHeight();
        int left = 30, right = w - 20, top = 20, bottom = h - 40;

        // 找最大值、最小值（柱子按这两个值缩放）
        double max = values[0];
        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
            if (values[i] < min) {
                min = values[i];
            }
        }

        // 画 L 形坐标轴
        canvas.drawLine(left, top, left, bottom, paintAxis);
        canvas.drawLine(left, bottom, right, bottom, paintAxis);

        int n = values.length;
        float gap = 4;
        float barW = (right - left - gap * (n - 1)) / n;
        if (barW < 1) {
            barW = 1;
        }

        // 逐月画柱子（以最低值为底，涨跌更明显）
        for (int i = 0; i < n; i++) {
            float ratio = (float) ((values[i] - min) / (max - min + 0.000001));
            float barH = ratio * (bottom - top);
            float x = left + i * (barW + gap);
            canvas.drawRect(x, bottom - barH, x + barW, bottom, paintBar);
        }

        // 标注最高最低
        canvas.drawText("最高 " + Math.round(max), left + 6, top + 16, paintText);
        canvas.drawText("最低 " + Math.round(min), left + 6, bottom - 6, paintText);
    }
}