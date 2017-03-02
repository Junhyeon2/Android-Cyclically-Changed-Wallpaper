package com.boostcamp.hyeon.wallpaper.preview.adapter.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boostcamp.hyeon.wallpaper.R;
import com.boostcamp.hyeon.wallpaper.base.app.WallpaperApplication;
import com.boostcamp.hyeon.wallpaper.base.domain.Image;
import com.boostcamp.hyeon.wallpaper.base.listener.OnItemClickListener;
import com.boostcamp.hyeon.wallpaper.base.util.DisplayMetricsHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by hyeon on 2017. 2. 20..
 */

public class PreviewListViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private OnItemClickListener mOnItemCLickListener;
    private int mWidth;
    private int mHeight;
    @BindView(R.id.image_view) ImageView mImageView;
    @BindView(R.id.tv_number) TextView mNumberTextView;

    public PreviewListViewHolder(View itemView, Context mContext, OnItemClickListener mOnItemCLickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.mContext = mContext;
        this.mOnItemCLickListener = mOnItemCLickListener;

        mWidth = DisplayMetricsHelper.getInstance().getDeviceWidth()/3*2;
        mHeight = DisplayMetricsHelper.getInstance().getDeviceHeight()/3*2;
        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.width = mWidth;
        layoutParams.height = mHeight;
        mImageView.setLayoutParams(layoutParams);
    }

    public void bind(Image image, final int position) {
        mNumberTextView.setText(String.valueOf(image.getNumber()));
        Picasso.with(mContext)
                .load(new File(image.getImageUri()))
                .resize(mWidth, mHeight)
                .centerCrop()
                .into(mImageView);
    }
}
