package com.xyoye.smbplayhelper.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/7/23.
 */

public class LoginBean implements Parcelable {
    private String ip;
    private String account;
    private String password;
    private String domain;

    protected LoginBean(Parcel in) {
        ip = in.readString();
        account = in.readString();
        password = in.readString();
        domain = in.readString();
    }

    public LoginBean() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeString(account);
        dest.writeString(password);
        dest.writeString(domain);
    }

    public static final Creator<LoginBean> CREATOR = new Creator<LoginBean>() {
        @Override
        public LoginBean createFromParcel(Parcel in) {
            return new LoginBean(in);
        }

        @Override
        public LoginBean[] newArray(int size) {
            return new LoginBean[size];
        }
    };
}
