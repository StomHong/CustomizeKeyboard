package com.stomhong.customkeyboard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.stomhong.library.KeyboardTouchListener;
import com.stomhong.library.KeyboardUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_test_activity).setOnClickListener(this);
        findViewById(R.id.btn_test_fragment).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test_activity:
                startActivity(new Intent(this,FirstActivity.class));
                break;
            case R.id.btn_test_fragment:
                startActivity(new Intent(this,SecondActivity.class));
                break;
        }
    }
}