package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SectionItem> itemList;
    private OnItemClickListener clickListener;

    public SectionAdapter(List<SectionItem> itemList, OnItemClickListener clickListener) {
        this.itemList = itemList;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SectionItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SectionItem item = itemList.get(position);
        if (item.getType() == SectionItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).headerTitle.setText(item.getText());
        } else {
            ((ItemViewHolder) holder).itemText.setText(item.getText());

            // Set click listener for item
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.headerTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;

        ItemViewHolder(View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
        }
    }
}
