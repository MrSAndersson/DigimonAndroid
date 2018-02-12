package se.standersson.icingalert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class mainServiceRecyclerViewAdapter extends RecyclerView.Adapter<mainServiceRecyclerViewAdapter.ViewHolder> {
    private final HostAbstract host;
    private final Context context;
    private RecyclerView parentRecyclerView;
    private android.support.transition.Transition transition;

    mainServiceRecyclerViewAdapter(Context context, HostAbstract host) {
        this.context = context;
        this.host = host;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_servicelist_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final mainServiceRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.service = host.getService(position);
        holder.serviceName.setText(holder.service.getServiceName());

        // Set Last State Change
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm  dd/MM/yy", Locale.getDefault());
        String timeString = dateFormat.format(new Date(holder.service.getLastStateChange() * 1000));
        holder.lastStateChange.setText(timeString);

        // Set Service Details
        holder.serviceDetails.setText(holder.service.getDetails());

        // Configure Service Comment
        if (!holder.service.getComment().equals("") && !holder.service.getCommentAuthor().equals(""))
        {
            String comment = "Comment:\n" + holder.service.getComment() + "\n/" + holder.service.getCommentAuthor();
            holder.serviceComment.setText(comment);
        } else {
            holder.serviceComment.setText("");
        }

        if (holder.serviceComment.getText() == "") {
            holder.serviceComment.setVisibility(View.GONE);
        } else {
            holder.serviceComment.setVisibility(View.VISIBLE);
        }

        if (holder.service.isExpanded()) {
            holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.VISIBLE);
            holder.lastStateChange.setVisibility(View.VISIBLE);
        } else {
            holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.GONE);
            holder.lastStateChange.setVisibility(View.GONE);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.service.isExpanded()) {
                    holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.GONE);
                    holder.lastStateChange.setVisibility(View.GONE);
                    holder.service.setIsExpanded(false);
                } else {
                    holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.VISIBLE);
                    holder.lastStateChange.setVisibility(View.VISIBLE);
                    holder.service.setIsExpanded(true);
                }
                // Stop all currently running transitions and start a new one
                android.support.transition.TransitionManager.endTransitions(parentRecyclerView);
                android.support.transition.TransitionManager.beginDelayedTransition(parentRecyclerView, transition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return host.getServiceCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView serviceName;
        final TextView lastStateChange;
        final TextView serviceDetails;
        final TextView serviceComment;

        Service service;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            serviceName = view.findViewById(R.id.main_list_service_name);
            lastStateChange = view.findViewById(R.id.main_list_service_last_state_change);
            serviceDetails = view.findViewById(R.id.main_list_service_details);
            serviceComment = view.findViewById(R.id.main_list_service_comment);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.parentRecyclerView = recyclerView;
        transition = android.support.transition.TransitionInflater.from(context).inflateTransition(R.transition.main_list_transition);
    }
}
