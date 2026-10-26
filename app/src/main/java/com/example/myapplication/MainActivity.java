package com.example.myapplication;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 第 1 个页面（首页）：输入理财参数，点按钮跳到结果页
 * 用到的控件：EditText、RadioButton、Spinner、CheckBox、ImageButton、GridView
 */
public class MainActivity extends AppCompatActivity {

    private EditText etMoney;       // 本金 / 每月投入
    private EditText etRate;        // 年化收益率
    private EditText etYear;        // 投资年限
    private RadioButton rbOnce;     // 一次性投入
    private RadioButton rbMonth;    // 每月定投
    private Spinner spRisk;         // 风险等级
    private CheckBox cbCompound;    // 是否按复利
    private ImageButton btnCalc;    // 计算按钮（图片按钮）
    private GridView gvProduct;     // 热门产品网格

    // 网格里显示的 6 个产品（3 列 2 行）
    private final String[] products = {"余额宝", "国债", "货币基金", "债券基金", "指数基金", "股票基金"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   // 加载布局

        // 第一步：findViewById 把 Java 变量和布局里的控件按 id 绑定
        etMoney = (EditText) findViewById(R.id.et_money);
        etRate = (EditText) findViewById(R.id.et_rate);
        etYear = (EditText) findViewById(R.id.et_year);
        rbOnce = (RadioButton) findViewById(R.id.rb_once);
        rbMonth = (RadioButton) findViewById(R.id.rb_month);
        spRisk = (Spinner) findViewById(R.id.sp_risk);
        cbCompound = (CheckBox) findViewById(R.id.cb_compound);
        btnCalc = (ImageButton) findViewById(R.id.btn_calc);
        gvProduct = (GridView) findViewById(R.id.gv_product);

        // 第二步：Spinner 下拉列表 —— 用 ArrayAdapter 把数组装进去
        String[] riskNames = {"低风险（建议 2.5%）", "中风险（建议 4.5%）", "高风险（建议 8.0%）"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, riskNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRisk.setAdapter(spinnerAdapter);          // 适配器和下拉列表关联

        // 第三步：监听下拉列表的选择 —— 选完自动把建议收益率填进输入框
        spRisk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    etRate.setText("2.5");
                } else if (i == 1) {
                    etRate.setText("4.5");
                } else {
                    etRate.setText("8.0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 接口要求必须重写，没有选中时什么都不做
            }
        });

        // 第四步：GridView 网格 —— 同样用 ArrayAdapter，item 用系统自带的布局
        ArrayAdapter<String> gridAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, products);
        gvProduct.setAdapter(gridAdapter);

        // 第五步：点网格里的某一项 —— 弹 Toast 提示（用 getItemAtPosition 取到内容）
        gvProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(MainActivity.this, "你选中了：" + name, Toast.LENGTH_SHORT).show();
            }
        });

        // 第六步：点"开始计算"按钮 —— 先检查输入，再跳结果页（Intent 传值）
        btnCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1) 取出三个输入框里的文字
                String moneyStr = etMoney.getText().toString().trim();
                String rateStr = etRate.getText().toString().trim();
                String yearStr = etYear.getText().toString().trim();

                // 2) 有一个没填就提示，直接 return 不往下走
                if (moneyStr.length() == 0 || rateStr.length() == 0 || yearStr.length() == 0) {
                    Toast.makeText(MainActivity.this, "请把本金、年化收益率、年限都填上",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3) 字符串转成数字
                double money = Double.parseDouble(moneyStr);
                double rate = Double.parseDouble(rateStr);
                int year = (int) Double.parseDouble(yearStr);

                // 4) 范围检查
                if (money <= 0 || rate < 0 || year <= 0 || year > 50) {
                    Toast.makeText(MainActivity.this, "本金要大于 0，年限填 1~50 年",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 5) 单选按钮和复选框：用 isChecked() 判断是否被选中
                boolean isMonth = rbMonth.isChecked();
                boolean compound = cbCompound.isChecked();

                // 6) 创建 Intent，用 putExtra 把数据放进去，再 startActivity 跳转
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("money", money);
                intent.putExtra("rate", rate);
                intent.putExtra("year", year);
                intent.putExtra("isMonth", isMonth);
                intent.putExtra("compound", compound);
                startActivity(intent);
            }
        });

        // 返回主菜单
        Button btnBack = (Button) findViewById(R.id.btn_back_menu);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();       // 关掉当前页面，回到主菜单
            }
        });
    }
}