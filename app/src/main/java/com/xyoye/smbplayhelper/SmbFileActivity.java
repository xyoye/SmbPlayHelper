package com.xyoye.smbplayhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xyoye.smbplayhelper.bean.LoginBean;
import com.xyoye.smbplayhelper.bean.SmbFileBean;
import com.xyoye.smbplayhelper.service.SmbService;
import com.xyoye.smbplayhelper.smb.SmbServer;
import com.xyoye.smbplayhelper.utils.CommonUtils;
import com.xyoye.smbplayhelper.utils.SmbFileAdapter;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jcifs.Address;
import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * 局域网文件浏览界面
 */

public class SmbFileActivity extends AppCompatActivity {

    private TextView parentTv;
    private RecyclerView smbFileRv;

    private List<SmbFileBean> smbFileList;
    private SmbFileAdapter smbFileAdapter;

    //登录信息
    private LoginBean loginBean;
    //cifs上下文
    private CIFSContext cifsContext;
    //根目录地址
    private String rootSmbPath;
    //父目录地址
    private String parentSmbPath;

    private View.OnClickListener parentTvClickListener;
    private BaseQuickAdapter.OnItemChildClickListener adapterItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smb_file);

        initData();

        initListener();

        initView();

        new Thread(this::login).start();
    }

    private void initData() {
        smbFileList = new ArrayList<>();
        loginBean = getIntent().getParcelableExtra("login_data");
    }

    private void initView() {
        parentTv = findViewById(R.id.parent_tv);
        parentTv.setVisibility(View.GONE);
        parentTv.setOnClickListener(parentTvClickListener);

        smbFileAdapter = new SmbFileAdapter(R.layout.item_smb, smbFileList);
        smbFileAdapter.setOnItemChildClickListener(adapterItemClickListener);

        smbFileRv = findViewById(R.id.smb_file_rv);
        smbFileRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        smbFileRv.setAdapter(smbFileAdapter);
    }

    private void initListener() {
        parentTvClickListener = v -> listParentFiles();

        adapterItemClickListener = (adapter, view, position) -> {
            SmbFileBean smbFileBean = smbFileList.get(position);
            if (smbFileBean.isDirectory()){
                new Thread(() -> listFiles(smbFileBean.getSmbFilePath())).start();
            }else {
                openFile(smbFileBean.getSmbFilePath());
            }
        };
    }

    /**
     * 登录
     */
    private void login() {
        String loginPath;
        if (TextUtils.isEmpty(loginBean.getAccount())) {
            loginPath = "smb://" + loginBean.getIp() + "/";
        } else {
            loginPath = "smb://" + loginBean.getAccount() + ":" + loginBean.getPassword() + "@" + loginBean.getIp() + "/";
        }

        try {
            //登录验证信息
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(loginBean.getDomain(), loginBean.getAccount(), loginBean.getPassword());
            //登录配置信息
            Properties properties = new Properties();
            properties.setProperty("jcifs.smb.client.responseTimeout", "5000");
            PropertyConfiguration configuration = new PropertyConfiguration(properties);

            cifsContext = new BaseContext(configuration).withCredentials(auth);
            //此处IP未null会被阻塞
            Address address = cifsContext.getNameServiceClient().getByName(loginBean.getIp());
            cifsContext.getTransportPool().logon(cifsContext, address);

            //使用listFiles获取子文件集合，同时也是在验证登录是否成功
            List<SmbFileBean> fileBeanList = new ArrayList<>();
            SmbFile rootFile = new SmbFile(loginPath, cifsContext);
            for (SmbFile smbFile : rootFile.listFiles()) {
                SmbFileBean fileBean = new SmbFileBean();
                fileBean.setSmbFileName(smbFile.getName());
                fileBean.setSmbFilePath(smbFile.getPath());
                fileBean.setDirectory(smbFile.isDirectory());
                fileBeanList.add(fileBean);
            }
            //父目录文件夹路径
            parentSmbPath = loginPath;
            //根目录文件夹路径
            rootSmbPath = loginPath;

            //更新数据
            runOnUiThread(() -> {
                startService();

                smbFileList.addAll(fileBeanList);
                smbFileAdapter.notifyDataSetChanged();
            });
        } catch (CIFSException e) {
            e.printStackTrace();
            showToast("登录失败");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            showToast("错误的IP地址");
        } catch (MalformedURLException malformedURLException){
            malformedURLException.printStackTrace();
            showToast("错误的文件链接");
        }
    }

    /**
     * 打开上一层文件夹
     */
    private void listParentFiles(){
        if (parentSmbPath.endsWith("/")){
            parentSmbPath = parentSmbPath.substring(0, parentSmbPath.length() - 1);
        }
        int lastIndex = parentSmbPath.lastIndexOf("/");
        parentSmbPath = parentSmbPath.substring(0, lastIndex) + "/";

        new Thread(() -> listFiles(parentSmbPath)).start();
    }

    /**
     * 打开文件夹
     */
    private void listFiles(String parentPath){

        List<SmbFileBean> fileBeanList = new ArrayList<>();
        try {
            SmbFile parentFile = new SmbFile(parentPath, cifsContext);
            for (SmbFile smbFile : parentFile.listFiles()){
                SmbFileBean fileBean = new SmbFileBean();
                fileBean.setSmbFileName(smbFile.getName());
                fileBean.setSmbFilePath(smbFile.getPath());
                fileBean.setDirectory(smbFile.isDirectory());
                fileBeanList.add(fileBean);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e){
            e.printStackTrace();
        }

        //更新父目录文件夹路径
        this.parentSmbPath = parentPath;

        runOnUiThread(() -> {
            if (parentSmbPath.equals(rootSmbPath)){
                parentTv.setVisibility(View.GONE);
            }else {
                parentTv.setVisibility(View.VISIBLE);
            }

            smbFileList.clear();
            smbFileList.addAll(fileBeanList);
            smbFileAdapter.notifyDataSetChanged();
        });
    }

    /**
     * 播放视频文件
     */
    private void openFile(String filePath){
        if (!CommonUtils.isMediaFile(filePath)){
            showToast("不是可播放的视频文件");
            return;
        }

        try {
            //设置需要播放的文件
            SmbFile smbFile = new SmbFile(filePath, cifsContext);
            SmbServer.setPlaySmbFile(smbFile);

            //文件Url由开启监听的IP和端口及视频地址组成
            String httpUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT+"/";
            String videoUrl = httpUrl + filePath.replace("smb://", "smb=");

            Uri uri = Uri.parse(videoUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            startActivity(intent);

        } catch (MalformedURLException e) {
            e.printStackTrace();
           showToast("无法创建可播放视频流");
        }
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
