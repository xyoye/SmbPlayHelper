package com.xyoye.smbplayhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.hierynomus.mssmb2.SMBApiException;
import com.xyoye.smbplayhelper.smb.SmbConnectManager;
import com.xyoye.smbplayhelper.smb.SmbServer;
import com.xyoye.smbplayhelper.smb.file.SmbChildFile;
import com.xyoye.smbplayhelper.smb.file.SmbFile;
import com.xyoye.smbplayhelper.smb.file.SmbShareFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SmbFile nowSmbFile = null;

    private TextView textView;
    private RecyclerView recyclerView;

    private List<SmbFile> smbFileList;
    private SmbAdapter smbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tv);
        recyclerView = findViewById(R.id.rv);

        smbFileList = new ArrayList<>();
        smbAdapter = new SmbAdapter(R.layout.item_smb, smbFileList);
        smbAdapter.setOnItemChildClickListener((adapter, view, position) ->
               new Thread(() -> openSmbFile(smbFileList.get(position))).start());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(smbAdapter);

        new Thread(() ->
                doLogin("192.168.1.240", "YE", "111111", "")
        ).start();

        Intent intent = new Intent(MainActivity.this, SmbService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else {
            startService(intent);
        }

        textView.setOnClickListener(v -> {
            if(nowSmbFile == null){
                showToast("已是最顶层文件夹");
            } else if (nowSmbFile.isSmbShareFile()){
                List<SmbShareFile> smbShareFileList = SmbConnectManager.getInstance().getShareFileList();
                if (smbShareFileList != null){
                    nowSmbFile = null;
                    smbFileList.clear();
                    smbFileList.addAll(smbShareFileList);
                    runOnUiThread(() -> smbAdapter.notifyDataSetChanged());
                }else {
                    showToast("获取父文件夹内容失败");
                }
            }else {
                new Thread(() -> listFile(nowSmbFile.getParentFile())).start();
            }
        });

    }

    private void doLogin(String ip, String userName, String userPassword, String domain){
        try {
            SmbConnectManager.getInstance().auth(ip, userName, userPassword, domain);
        } catch (IOException e){
            e.printStackTrace();
            showToast("登录失败，无法连接当前IP："+ip);
        }

        List<SmbShareFile> smbShareFileList = SmbConnectManager.getInstance().getShareFileList();
        if (smbShareFileList != null){
            smbFileList.clear();
            smbFileList.addAll(smbShareFileList);
            runOnUiThread(() -> smbAdapter.notifyDataSetChanged());
        }else {
            SMBApiException apiException = SmbConnectManager.getInstance().getApiException();
            String errorMsg = apiException == null ? "" : apiException.getMessage();
            showToast("登录失败："+ errorMsg);
        }
    }

    private void openSmbFile(SmbFile smbFile){
        if (smbFile == null){
            return;
        }

        if (smbFile.isDirectory()){
            listFile(smbFile);
        }else if (smbFile.isFile()){
            String smbPath = smbFile.getSmbPath();
            //例：\\192.168.1.240\Users\admin\Desktop\temp\10.mp4
            int startIndex = smbPath.indexOf("\\", 2);
            //result：/Users/admin/Desktop/temp/10.mp4
            String pcPath =  smbPath.substring(startIndex).replace("\\", "/");

            String videoUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT+"/smb="+pcPath;
            SmbServer.setPlaySmbFile((SmbChildFile) smbFile);

            Uri uri = Uri.parse(videoUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            startActivity(intent);
        }
    }

    private void listFile(SmbFile smbFile){
        try {
            nowSmbFile = smbFile;
            List<SmbChildFile> childSmbChildFileList = smbFile.listFile();
            smbFileList.clear();
            smbFileList.addAll(childSmbChildFileList);
            runOnUiThread(() -> smbAdapter.notifyDataSetChanged());
        }catch (Exception e){
            e.printStackTrace();
            showToast("打开文件夹失败，请重试");
        }

    }
    
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
