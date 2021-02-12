package com.bsotniczuk.safekiddoapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;
import com.bsotniczuk.safekiddoapp.R;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterRecyclerView extends RecyclerView.Adapter<AdapterRecyclerView.MyViewHolder> {

    private Context mContext;
    private List<MessageModel> messageModelList;
    private OnMessageClickListener onMessageClickListener;

    public AdapterRecyclerView(Context mContext, List<MessageModel> messageModelList, OnMessageClickListener onMessageClickListener) {
        this.mContext = mContext;
        this.messageModelList = messageModelList;
        this.onMessageClickListener = onMessageClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        v = layoutInflater.inflate(R.layout.message_item, parent, false);

        return new MyViewHolder(v, onMessageClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView1.setText(messageModelList.get(position).getTitle()); //parse int to String

        //Using Glide library to display images
        Glide.with(mContext)
                .load(messageModelList.get(position).getIcon())
                /*.placeholder(R.drawable.ic_launcher_foreground)*/
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView1; //title
        ImageView imageView; //image
        OnMessageClickListener onMessageClickListener;

        public MyViewHolder(@NonNull View itemView, OnMessageClickListener onMessageClickListener) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textViewRecycler1);
            imageView = itemView.findViewById(R.id.imageViewRecycler);
            this.onMessageClickListener = onMessageClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMessageClickListener.onMessageClick(getAdapterPosition());
        }
    }

    public interface OnMessageClickListener {
        void onMessageClick(int position);
    }
}