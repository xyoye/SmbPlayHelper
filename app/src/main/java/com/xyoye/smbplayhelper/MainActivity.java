package com.xyoye.smbplayhelper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.info.SmbLinkInfo;
import com.xyoye.smbplayhelper.utils.LoadingDialog;
import com.xyoye.smbplayhelper.utils.SPUtils;

/**
 * 登录界面
 */
public class MainActivity extends AppCompatActivity {
    private EditText ipEt, accountEt, passwordEt, domainEt, shareEt;
    private CheckBox isAnonymousCb;
    private CheckBox smbJ_RPCCb, smbJCb, jcifs_NGCb, jcifsCb;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        setTitle("SmbPlayHelper");
        loadingDialog = new LoadingDialog(this, "登录中");

        ipEt = findViewById(R.id.ip_et);
        accountEt = findViewById(R.id.account_et);
        passwordEt = findViewById(R.id.password_et);
        domainEt = findViewById(R.id.domain_et);
        shareEt = findViewById(R.id.share_et);
        isAnonymousCb = findViewById(R.id.anonymous_cb);

        smbJ_RPCCb = findViewById(R.id.smb_rpc_cb);
        smbJCb = findViewById(R.id.smb_cb);
        jcifs_NGCb = findViewById(R.id.jcifs_ng_cb);
        jcifsCb = findViewById(R.id.jcifs_cb);

        smbJ_RPCCb.setChecked(SPUtils.getInstance().getBoolean("smb_j_rpc_check", true));
        smbJCb.setChecked(SPUtils.getInstance().getBoolean("smb_j_check", true));
        jcifs_NGCb.setChecked(SPUtils.getInstance().getBoolean("jcifs_ng_check", true));
        jcifsCb.setChecked(SPUtils.getInstance().getBoolean("jcifs_check", true));

        ipEt.setText(SPUtils.getInstance().getString("smb_ip"));
        accountEt.setText(SPUtils.getInstance().getString("smb_account"));
        passwordEt.setText(SPUtils.getInstance().getString("smb_password"));
        shareEt.setText(SPUtils.getInstance().getString("smb_share"));
        isAnonymousCb.setChecked(SPUtils.getInstance().getBoolean("anonymous_check"));

        findViewById(R.id.login_bt).setOnClickListener(v -> login());
    }

    private void login() {
        String ip = ipEt.getEditableText().toString().trim();
        String account = accountEt.getEditableText().toString().trim();
        String password = passwordEt.getEditableText().toString().trim();
        String domain = domainEt.getEditableText().toString().trim();
        String share = shareEt.getEditableText().toString().trim();
        boolean isAnonymous = isAnonymousCb.isChecked();

        SmbLinkInfo smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setIP(ip);
        smbLinkInfo.setAccount(account);
        smbLinkInfo.setPassword(password);
        smbLinkInfo.setDomain(domain);
        smbLinkInfo.setRootFolder(share);
        smbLinkInfo.setAnonymous(isAnonymous);

        boolean smbJRPCEnable = smbJ_RPCCb.isChecked();
        boolean smbJEnable = smbJCb.isChecked();
        boolean jcifsNGEnable = jcifs_NGCb.isChecked();
        boolean jcifsEnable = jcifsCb.isChecked();


        SPUtils.getInstance().putBoolean("smb_j_rpc_check", smbJRPCEnable);
        SPUtils.getInstance().putBoolean("smb_j_check", smbJEnable);
        SPUtils.getInstance().putBoolean("jcifs_ng_check", jcifsNGEnable);
        SPUtils.getInstance().putBoolean("jcifs_check", jcifsEnable);
        SPUtils.getInstance().putString("smb_ip", ip);
        SPUtils.getInstance().putString("smb_account", account);
        SPUtils.getInstance().putString("smb_password", password);
        SPUtils.getInstance().putString("smb_share", share);
        SPUtils.getInstance().putBoolean("anonymous_check", isAnonymous);

        loadingDialog.show();
        new Thread(() -> {
            SmbManager smbManager = SmbManager.getInstance();
            smbManager.setEnable(smbJRPCEnable, smbJEnable, jcifsNGEnable, jcifsEnable);
            smbManager.setLinkCallback(new SmbManager.LinkCallback() {
                @Override
                public void onLinkChange(String type) {
                    runOnUiThread(() -> loadingDialog.updateText("当前登录方式\n"+type));
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Log.d(MainActivity.class.getSimpleName(), smbManager.getSmbType() + ": Login Success");
                        startActivity(new Intent(MainActivity.this, SmbFileActivity.class));
                    });
                }

                @Override
                public void onFailed() {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Log.d(MainActivity.class.getSimpleName(), smbManager.getException().getExceptionString());
                        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            smbManager.linkStart(smbLinkInfo);
        }).start();
    }
}
