package se.standersson.icingalert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class mainServiceRecyclerViewAdapter extends RecyclerView.Adapter<mainServiceRecyclerViewAdapter.ViewHolder> {
    private final HostList host;
    private final Context context;

    mainServiceRecyclerViewAdapter(Context context, HostList host) {
        this.context = context;
        this.host = host;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_servicelist_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mainServiceRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.service = host.getService(position);
        holder.serviceName.setText(holder.service.getServiceName());
    }

    @Override
    public int getItemCount() {
        return host.getServiceCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView serviceName;

        Service service;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            serviceName = view.findViewById(R.id.main_list_service_name);
        }
    }
}
