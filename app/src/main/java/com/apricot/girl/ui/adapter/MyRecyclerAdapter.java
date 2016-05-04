package com.apricot.girl.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.apricot.girl.R;
import com.apricot.girl.model.Girl;
import com.apricot.girl.ui.widget.RatioImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Created by Apricot on 2016/4/26.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>{
    private Context mContext;
    private List<Girl> girls;
    private onGirlClickListener onGirlClickListener;

    public MyRecyclerAdapter(Context context,List<Girl> girls){
        mContext=context;
        this.girls=girls;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Girl girl=girls.get(position);
        Glide.with(mContext)
                .load(girl.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        if(girl.getWidth()!=0&&girl.getHeight()!=0){
            holder.imageView.setOriginalSize(girl.getWidth(),girl.getHeight());
        }
    }

    @Override
    public int getItemCount() {
        return girls.size();
    }

    public void setOnGirlClickListener(onGirlClickListener listener){
        onGirlClickListener=listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public RatioImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView= (RatioImageView) itemView.findViewById(R.id.iamge);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGirlClickListener.onGirlClick(v,getAdapterPosition());
                }
            });
        }

    }
}
