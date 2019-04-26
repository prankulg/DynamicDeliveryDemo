package com.test.dynamictest;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class ModulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<ModuleItem> mArrayList;

    public ModulesAdapter(Context context, ArrayList<ModuleItem> modulesArrayList) {
        mContext = context;
        mArrayList = modulesArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_module, viewGroup, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final ModuleViewHolder mvh = (ModuleViewHolder) viewHolder;
        mvh.tvModuleName.setText(mArrayList.get(i).getName());
        mvh.swToggleInstall.setChecked(mArrayList.get(i).isInstalled());

        mvh.swToggleInstall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DynamicModulesDownloadManager.getInstance(mContext).installModule(mvh.tvModuleName.getText().toString());
                } else {
                    DynamicModulesDownloadManager.getInstance(mContext).unInstallModule(mvh.tvModuleName.getText().toString());
                }
            }
        });
    }

    public void setNewData(ArrayList<ModuleItem> moduleItemArrayList){
        mArrayList = moduleItemArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mArrayList != null ? mArrayList.size() : 0;
    }

    public class ModuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvModuleName;
        Switch swToggleInstall;

        ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleName = itemView.findViewById(R.id.tv_module_name);
            swToggleInstall = itemView.findViewById(R.id.sw_toggle_install);
        }
    }
}

