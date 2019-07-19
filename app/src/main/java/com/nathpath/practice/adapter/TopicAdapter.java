package com.nathpath.practice.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nathpath.practice.R;
import com.nathpath.practice.models.Topic;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder>{
    private Context context;
    private List<Topic> topicList;

    public TopicAdapter(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic topic = topicList.get(position);
        holder.tv_title.setText(topic.getTitle());
        holder.img_icon.setImageResource(topic.getIcon());
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_icon;
        TextView tv_title;

        public ViewHolder(View itemView) {
            super(itemView);

            img_icon = itemView.findViewById(R.id.img_icon_topic);
            tv_title = itemView.findViewById(R.id.tv_title_topic);

            itemView.setOnClickListener(v -> {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(itemView, getAdapterPosition());
                }
            });
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int posotion);
    }
}
