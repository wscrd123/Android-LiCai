package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * 股票收益模拟页
 * 三支预设股票 + 自定义参数 → 随机模拟未来走势 → 结果 + 分析 + 柱状图
 * 【课外知识】Random 正态分布随机数、蒙特卡洛思想、最大回撤
 */
public class StockActivity extends AppCompatActivity {

    private Spinner spStock;
    private EditText etMoney;
    private EditText etMonths;
    private EditText etReturn;
    private EditText etVol;
    private CheckBox cbMulti;
    private ImageButton btnRun;
    private BarChartView chart;
    private TextView tvResult;
    private TextView tvAnalysis;

    // ===== 三支预设股票（示例数据，不是真实行情）=====
    private final String[] stockNames = {
            "贵州茅台（年化 18% / 波动 28%）",
            "宁德时代（年化 25% / 波动 40%）",
            "工商银行（年化 6% / 波动 15%）",
            "自定义参数"};
    private final double[] stockReturns = {18, 25, 6, 0};
    private final double[] stockVols = {28, 40, 15, 0};

    // ===== 模拟结果的统计量 =====
    private double endValue;      // 期末资产
    private double maxValue;      // 期间最高
    private double minValue;      // 期间最低
    private double maxDrawdown;   // 最大回撤

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // 绑定控件
        spStock = (Spinner) findViewById(R.id.sp_stock);
        etMoney = (EditText) findViewById(R.id.et_s_money);
        etMonths = (EditText) findViewById(R.id.et_s_months);
        etReturn = (EditText) findViewById(R.id.et_s_return);
        etVol = (EditText) findViewById(R.id.et_s_vol);
        cbMulti = (CheckBox) findViewById(R.id.cb_multi);
        btnRun = (ImageButton) findViewById(R.id.btn_run);
        chart = (BarChartView) findViewById(R.id.chart);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvAnalysis = (TextView) findViewById(R.id.tv_analysis);

        // Spinner：4 个选项（3 支预设 + 自定义）
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, stockNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStock.setAdapter(adapter);

        // 选中预设股票 → 自动把它的参数填进输入框（选"自定义"时不动，让用户自己填）
        spStock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 3) {
                    etReturn.setText("" + stockReturns[i]);
                    etVol.setText("" + stockVols[i]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 开始模拟
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runSimulation();
            }
        });

        // 返回主菜单
        Button btnBack = (Button) findViewById(R.id.btn_stock_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // ================= 点击"开始模拟"后执行 =================
    private void runSimulation() {
        // 1) 取四个参数
        String moneyStr = etMoney.getText().toString().trim();
        String monthStr = etMonths.getText().toString().trim();
        String returnStr = etReturn.getText().toString().trim();
        String volStr = etVol.getText().toString().trim();

        if (moneyStr.length() == 0 || monthStr.length() == 0
                || returnStr.length() == 0 || volStr.length() == 0) {
            Toast.makeText(StockActivity.this, "四个参数都要填", Toast.LENGTH_SHORT).show();
            return;
        }

        double money = Double.parseDouble(moneyStr);
        int months = (int) Double.parseDouble(monthStr);
        double yearReturn = Double.parseDouble(returnStr);
        double yearVol = Double.parseDouble(volStr);

        if (money <= 0 || months < 1 || months > 120 || yearVol < 0 || yearVol > 100) {
            Toast.makeText(StockActivity.this, "金额要大于0，月数1~120，波动率0~100",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) 模拟一次，拿到每个月的数据
        double[] values = simulate(money, months, yearReturn, yearVol);

        // 3) 画柱状图
        chart.setValues(values);

        // 4) 结果文字
        double profit = endValue - money;
        double profitRate = profit / money * 100;
        tvResult.setText("买入金额：" + show(money) + " 元    模拟月数：" + months + " 个月\n"
                + "期末资产：" + show(endValue) + " 元\n"
                + "总收益：" + show(profit) + " 元（" + show(profitRate) + "%）\n"
                + "期间最高：" + show(maxValue) + " 元    期间最低：" + show(minValue) + " 元");

        // 5) 分析文字
        String text = "";
        if (profit >= 0) {
            text = text + "【结论】模拟期内盈利 " + show(profit) + " 元，收益率 " + show(profitRate) + "%。\n";
        } else {
            text = text + "【结论】模拟期内亏损 " + show(-profit) + " 元，收益率 " + show(profitRate) + "%。\n";
        }

        double realYear = (Math.pow(endValue / money, 12.0 / months) - 1) * 100;
        text = text + "【年化对比】这段时间折算成年化约 " + show(realYear)
                + "%，而设定的是 " + show(yearReturn) + "%，差距来自随机波动的运气。\n";

        text = text + "【最大回撤】中途最多从最高点跌了 " + show(maxDrawdown * 100)
                + "%，也就是账面浮亏最惨的时候，这是衡量风险的重要指标。\n";

        if (yearVol >= 30) {
            text = text + "【风险提示】波动率 ≥30%，属于高波动品种，短期可能大幅亏损，适合长期持有或小仓位。";
        } else if (yearVol >= 15) {
            text = text + "【风险提示】波动率中等，收益和风险都比较适中，建议持有一年以上。";
        } else {
            text = text + "【风险提示】波动率较低，比较稳健，但预期收益也有限。";
        }

        // 6) 勾选了就再跑 100 次，统计盈利概率（蒙特卡洛）
        if (cbMulti.isChecked()) {
            int win = 0;
            double best = -99999999;
            double worst = 99999999;
            for (int t = 0; t < 100; t++) {
                simulate(money, months, yearReturn, yearVol);
                if (endValue > money) {
                    win = win + 1;
                }
                if (endValue > best) {
                    best = endValue;
                }
                if (endValue < worst) {
                    worst = endValue;
                }
            }
            text = text + "\n\n【模拟 100 次】盈利 " + win + " 次、亏损 " + (100 - win)
                    + " 次，盈利概率约 " + win + "%。\n"
                    + "运气最好时变成 " + show(best) + " 元，最差时变成 " + show(worst) + " 元。";
        }

        tvAnalysis.setText(text);
    }

    /**
     * 核心：随机模拟
     * 每个月：收益率 = 平均月收益 + 正态分布随机波动
     * 返回每个月末的资产金额（给柱状图用），统计量存进成员变量
     */
    private double[] simulate(double money, int months, double yearReturn, double yearVol) {
        Random random = new Random();
        double monthReturn = yearReturn / 100 / 12;          // 平均月收益
        double monthVol = yearVol / 100 / Math.sqrt(12);     // 月波动率

        double[] values = new double[months];
        double now = money;
        double peak = money;      // 历史最高点，用来算最大回撤

        maxValue = money;
        minValue = money;
        maxDrawdown = 0;

        for (int i = 0; i < months; i++) {
            // nextGaussian() 返回正态分布随机数（均值0、标准差1），模拟涨跌更真实
            double r = monthReturn + random.nextGaussian() * monthVol;
            now = now * (1 + r);
            values[i] = now;

            if (now > maxValue) {
                maxValue = now;
            }
            if (now < minValue) {
                minValue = now;
            }
            if (now > peak) {
                peak = now;
            }
            double dd = (peak - now) / peak;
            if (dd > maxDrawdown) {
                maxDrawdown = dd;
            }
        }
        endValue = now;
        return values;
    }

    // 数字保留两位小数
    private String show(double v) {
        double t = Math.round(v * 100) / 100.0;
        return "" + t;
    }
}