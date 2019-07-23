package com.xyoye.smbplayhelper.utils;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.smbplayhelper.R;
import com.xyoye.smbplayhelper.bean.SmbFileBean;

import java.util.List;

public class SmbFileAdapter extends BaseQuickAdapter<SmbFileBean, BaseViewHolder> {

        public SmbFileAdapter(@LayoutRes int layoutResId, @Nullable List<SmbFileBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, SmbFileBean item) {

            String smbFileName = item.getSmbFileName();

            //文件夹名通常会以"/"结尾
            if (!TextUtils.isEmpty(smbFileName) && smbFileName.endsWith("/") && smbFileName.length() > 2) {
                smbFileName = smbFileName.substring(0, smbFileName.length() - 1);
            }

            helper.setText(R.id.item_name_tv, smbFileName)
                    .addOnClickListener(R.id.item_layout);
        }
    }