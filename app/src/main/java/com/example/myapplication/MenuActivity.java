package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 主菜单页面：两个功能入口
 * 1. 理财收益计算 → 跳到 MainActivity
 * 2. 股票预测     → 内容还没写，先弹个提示
 */
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);   // 加载主菜单布局

        // 功能一：理财收益计算，点击跳到 MainActivity
        Button btnCalc = (Button) findViewById(R.id.btn_menu_calc);
        btnCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 功能二：股票预测（还没做）
        Button btnStock = (Button) findViewById(R.id.btn_menu_stock);
        btnStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, StockActivity.class);
                startActivity(intent);
            }
        });
    }
}