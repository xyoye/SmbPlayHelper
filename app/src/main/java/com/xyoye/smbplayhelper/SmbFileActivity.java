package com.xyoye.smbplayhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.info.SmbFileInfo;
import com.xyoye.smbplayhelper.service.SmbService;
import com.xyoye.smbplayhelper.smb.SmbServer;
import com.xyoye.smbplayhelper.utils.CommonUtils;
import com.xyoye.smbplayhelper.utils.SmbFileAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 局域网文件浏览界面
 */

public class SmbFileActivity extends AppCompatActivity {

    private TextView pathTv;

    private List<SmbFileInfo> smbFileList;
    private SmbFileAdapter smbFileAdapter;

    private SmbManager smbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smb_file);

        initView();

        smbManager = SmbManager.getInstance();

        startService();

        getSelfData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            new Thread(() -> {
                SmbManager.getInstance().getController().release();
                SmbFileActivity.this.finish();
            }).start();
        } else if (item.getItemId() == R.id.previous_item) {
            getParentData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        setTitle("文件列表");

        pathTv = findViewById(R.id.path_tv);

        smbFileList = new ArrayList<>();
        smbFileAdapter = new SmbFileAdapter(R.layout.item_smb, smbFileList);
        smbFileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            String fileName = smbFileList.get(position).getFileName();
            if (smbFileList.get(position).isDirectory()) {
                openDirectory(fileName);
            } else {
                openFile(fileName);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(smbFileAdapter);
    }

    /**
     * 获取当前文件文件列表
     */
    private void getSelfData() {
        new Thread(() -> {
            List<SmbFileInfo> fileList = smbManager.getController().getSelfList();
            String currentPath = smbManager.getController().getCurrentPath();
            String type = smbManager.getController().getClass().getSimpleName();
            runOnUiThread(() -> {
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                pathTv.setText(currentPath);
                setTitle(type.replace("Controller", ""));
            });
        }).start();
    }

    /**
     * 获取父目录文件列表
     */
    private void getParentData() {
        if (smbManager.getController().isRootDir()) {
            showToast("无父目录");
            return;
        }

        new Thread(() -> {
            List<SmbFileInfo> fileList = smbManager.getController().getParentList();
            String currentPath = smbManager.getController().getCurrentPath();
            runOnUiThread(() -> {
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                pathTv.setText(currentPath);
            });
        }).start();
    }

    /**
     * 打开文件夹
     */
    private void openDirectory(String dirName) {
        new Thread(() -> {
            List<SmbFileInfo> fileList = smbManager.getController().getChildList(dirName);
            String currentPath = smbManager.getController().getCurrentPath();
            runOnUiThread(() -> {
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                pathTv.setText(currentPath);
            });
        }).start();
    }

    /**
     * 打开文件
     */
    private void openFile(String fileName) {
        if (!CommonUtils.isMediaFile(fileName)) {
            showToast("不是可播放的视频文件");
            return;
        }

        //文件Url由开启监听的IP和端口及视频地址组成
        String httpUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT;
        String videoUrl = httpUrl + "/smb/" + fileName;
        SmbServer.SMB_FILE_NAME = fileName;

        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }

    /**
     * 启动后台服务
     */
    private void startService() {
        Intent intent = new Intent(SmbFileActivity.this, SmbService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
