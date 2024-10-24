package com.example.xemphim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.R;
import com.example.xemphim.model.ApiModel;

import java.util.List;

import retrofit2.Retrofit;

public class ApiAdapter extends RecyclerView.Adapter<ApiAdapter.ApiViewHolder> {

    private List<ApiModel> apiList;
    private OnApiActionListener listener;

    public interface OnApiActionListener {
        void onEditClick(ApiModel api);
        void onDeleteClick(ApiModel api);
        void onSelectClick(ApiModel api);
    }

    public ApiAdapter(List<ApiModel> apiList, OnApiActionListener listener) {
        this.apiList = apiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_api, parent, false);
        return new ApiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApiViewHolder holder, int position) {
        ApiModel api = apiList.get(position);
        holder.tvApiName.setText(api.getName());
        holder.tvApiUrl.setText(api.getUrl());

        // Sự kiện chỉnh sửa
        holder.btnEditApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditClick(api);
            }
        });

        // Sự kiện xóa
        holder.btnDeleteApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClick(api);
            }
        });

        // Sự kiện chọn API
        holder.btnUseSelectedApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelectClick(api);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apiList.size();
    }

    public static class ApiViewHolder extends RecyclerView.ViewHolder {

        TextView tvApiName, tvApiUrl;
        Button btnEditApi, btnDeleteApi, btnUseSelectedApi;

        public ApiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApiName = itemView.findViewById(R.id.tvApiName);
            tvApiUrl = itemView.findViewById(R.id.tvApiUrl);
            btnEditApi = itemView.findViewById(R.id.btnEditApi);
            btnDeleteApi = itemView.findViewById(R.id.btnDeleteApi);
            btnUseSelectedApi = itemView.findViewById(R.id.btnUseSelectedApi);
        }
    }
}

