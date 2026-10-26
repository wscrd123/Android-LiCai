package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * 第 2 个页面（结果页）：接收首页传过来的数据，算出结果，用 ListView 显示逐年明细
 */
public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 第一步：取上一个页面传过来的数据（名字要和 putExtra 时完全一样）
        Intent intent = getIntent();
        double money = intent.getDoubleExtra("money", 0);
        double rate = intent.getDoubleExtra("rate", 0);
        int year = intent.getIntExtra("year", 1);
        boolean isMonth = intent.getBooleanExtra("isMonth", false);
        boolean compound = intent.getBooleanExtra("compound", true);

        // 第二步：算结果，每一年的文字存进 ArrayList
        ArrayList<String> detailList = new ArrayList<String>();
        double total;    // 期末总金额
        double invest;   // 一共投进去多少钱

        if (isMonth == false) {
            // ---------- 情况一：一次性投入 ----------
            invest = money;
            for (int y = 1; y <= year; y++) {
                double value = money;
                if (compound) {
                    // 复利：乘 y 次（1 + 利率），就是 (1+利率) 的 y 次方
                    for (int k = 0; k < y; k++) {
                        value = value * (1 + rate / 100);
                    }
                } else {
                    // 单利：只有本金生利息
                    value = money * (1 + rate / 100 * y);
                }
                detailList.add("第 " + y + " 年    年末金额 " + show(value) + " 元");
            }
            total = money;
            if (compound) {
                for (int k = 0; k < year; k++) {
                    total = total * (1 + rate / 100);
                }
            } else {
                total = money * (1 + rate / 100 * year);
            }
        } else {
            // ---------- 情况二：每月定投 ----------
            // 一个月一个月地算：先投钱，再按月利率涨一次
            double monthRate = rate / 100 / 12;   // 月利率
            double balance = 0;                   // 账户里的钱
            double totalIn = 0;                   // 累计投入
            for (int y = 1; y <= year; y++) {
                for (int m = 1; m <= 12; m++) {
                    balance = (balance + money) * (1 + monthRate);
                    totalIn = totalIn + money;
                }
                detailList.add("第 " + y + " 年    共投入 " + show(totalIn)
                        + " 元    年末金额 " + show(balance) + " 元");
            }
            total = balance;
            invest = totalIn;
        }

        // 第三步：把参数和结果写到 TextView 上（证明传值成功）
        TextView tvParams = (TextView) findViewById(R.id.tv_params);
        TextView tvTotal = (TextView) findViewById(R.id.tv_total);
        TextView tvProfit = (TextView) findViewById(R.id.tv_profit);

        String way = "一次性投入";
        if (isMonth) {
            way = "每月定投";
        }
        String compoundText = "单利";
        if (compound) {
            compoundText = "复利";
        }

        tvParams.setText("投入金额：" + show(money) + " 元/期\n"
                + "年化收益率：" + rate + "%\n"
                + "投资方式：" + way + "（" + compoundText + "）\n"
                + "投资年限：" + year + " 年");
        tvTotal.setText("期末总金额：" + show(total) + " 元");
        tvProfit.setText("总收益：" + show(total - invest) + " 元    累计投入：" + show(invest) + " 元");

        // 第四步：ListView 显示逐年明细
        ListView lvDetail = (ListView) findViewById(R.id.lv_detail);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, detailList);
        lvDetail.setAdapter(listAdapter);

        // 第五步：返回按钮 —— finish() 关掉当前页面，回到首页
        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 自己写的小方法：把数字四舍五入保留两位小数再转成文字
    private String show(double value) {
        double v = Math.round(value * 100) / 100.0;
        return "" + v;
    }
}