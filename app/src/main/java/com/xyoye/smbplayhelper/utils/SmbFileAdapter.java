package com.xyoye.smbplayhelper.utils;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.libsmb.info.SmbFileInfo;
import com.xyoye.smbplayhelper.R;

import java.util.List;

public class SmbFileAdapter extends BaseQuickAdapter<SmbFileInfo, BaseViewHolder> {

    public SmbFileAdapter(@LayoutRes int layoutResId, @Nullable List<SmbFileInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SmbFileInfo item) {
        helper.setImageResource(R.id.iv, item.isDirectory() ? R.mipmap.ic_smb_folder : R.mipmap.ic_smb_video)
                .setText(R.id.tv, item.getFileName())
                .addOnClickListener(R.id.item_layout);
    }
}