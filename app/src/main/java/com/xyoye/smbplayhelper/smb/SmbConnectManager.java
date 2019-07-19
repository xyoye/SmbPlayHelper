package com.xyoye.smbplayhelper.smb;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.rapid7.client.dcerpc.mssrvs.ServerService;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo0;
import com.rapid7.client.dcerpc.transport.RPCTransport;
import com.rapid7.client.dcerpc.transport.SMBTransportFactories;
import com.xyoye.smbplayhelper.smb.file.SmbShareFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xyoye on 2019/7/15.
 */

public class SmbConnectManager {

    private SMBClient mSmbClient = null;
    private String rootIP = null;
    private Connection mConnection = null;
    private Session mSession = null;
    private AuthenticationContext mAuthenticationContext = null;
    private List<SmbShareFile> smbShareFileList;
    private SMBApiException apiException;

    private static class ManagerHolder{
        private static SmbConnectManager manager = new SmbConnectManager();
    }

    private SmbConnectManager(){

    }

    public static SmbConnectManager getInstance(){
        return ManagerHolder.manager;
    }

    public void auth(@NonNull String ip, @NonNull String userName, @NonNull String userPassword, @Nullable String domain) throws IOException {
        SmbConfig smbConfig = SmbConfig.builder()
                                .withSigningRequired(true)
                                .withDfsEnabled(true)
                                .withMultiProtocolNegotiate(true)
                                .withWriteTimeout(3, TimeUnit.SECONDS)
                                .withReadTimeout(3, TimeUnit.SECONDS)
                                .withTransactTimeout(3, TimeUnit.SECONDS)
                                .build();
        mSmbClient = new SMBClient(smbConfig);
        mAuthenticationContext = new AuthenticationContext(userName, userPassword.toCharArray(), domain);
        mConnection = mSmbClient.connect(ip);
        mSession = mConnection.authenticate(mAuthenticationContext);
        rootIP = ip;
    }

    public void close() {
        try {
            if (mSession != null) {
                mSession.close();
            }
            if (mConnection != null) {
                mConnection.close(true);
            }
            if (mSmbClient != null) {
                mSmbClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthenticationContext getAuthenticationContext() {
        return mAuthenticationContext;
    }

    public String getRootIP() {
        return rootIP;
    }

    public Session getSession() {
        return mSession;
    }

    public @Nullable List<SmbShareFile> getShareFileList(){
        if (smbShareFileList == null || smbShareFileList.size() == 0){
            smbShareFileList = new ArrayList<>();

            try {
                RPCTransport transport = SMBTransportFactories.SRVSVC.getTransport(mSession);
                ServerService serverService = new ServerService(transport);
                List<NetShareInfo0> shareInfoList = serverService.getShares0();

                if (shareInfoList != null && shareInfoList.size() > 0) {
                    for (NetShareInfo0 shareInfo : shareInfoList) {
                        SmbShareFile shareFile = new SmbShareFile(shareInfo.getNetName());
                        smbShareFileList.add(shareFile);
                    }
                }
            }catch (SMBApiException e){
                e.printStackTrace();
                apiException = e;
                return null;
            } catch (IOException e){
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return smbShareFileList;
    }

    public SMBApiException getApiException(){
        return apiException;
    }
}
