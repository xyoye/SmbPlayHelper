package com.xyoye.smbplayhelper;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.smbplayhelper.smb.file.SmbFile;

import java.util.List;

public class SmbAdapter extends BaseQuickAdapter<SmbFile, BaseViewHolder> {

        SmbAdapter(@LayoutRes int layoutResId, @Nullable List<SmbFile> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, SmbFile item) {
            helper.setText(R.id.item_name_tv, item.getFileName())
                    .addOnClickListener(R.id.item_layout);
        }
    }