package com.xyoye.smbplayhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.xyoye.smbplayhelper.bean.LoginBean;

/**
 * 登录界面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText ipEt = findViewById(R.id.ip_et);
        EditText accountEt = findViewById(R.id.account_et);
        EditText passwordEt = findViewById(R.id.password_et);
        EditText domainEt = findViewById(R.id.domain_et);

        findViewById(R.id.login_bt).setOnClickListener(v -> {
            String ip = ipEt.getText().toString();
            String account = accountEt.getText().toString();
            String password = passwordEt.getText().toString();
            String domain = domainEt.getText().toString();

            LoginBean loginBean = new LoginBean();
            loginBean.setIp(ip);
            loginBean.setAccount(account);
            loginBean.setPassword(password);
            loginBean.setDomain(domain);

            Intent intent = new Intent(this, SmbFileActivity.class);
            intent.putExtra("login_data", loginBean);
            MainActivity.this.startActivity(intent);
        });
    }
}
