package se.standersson.icingalert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import se.standersson.icingalert.HostListFragment2.OnListFragmentInteractionListener;

import java.util.List;

public class MyHostListRecyclerViewAdapter extends RecyclerView.Adapter<MyHostListRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final Context context;
    private List<HostList> hosts;
    private RecyclerView parentRecyclerView;
    private Transition transition;


    MyHostListRecyclerViewAdapter(Context context, List<HostList> hosts, OnListFragmentInteractionListener listener) {
        this.hosts = hosts;
        this.mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_hostlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.host = hosts.get(position);
        holder.hostName.setText(hosts.get(position).getHostName());

        // Set hostName background
        if (holder.host.isDown()) {
            if (holder.host.isAcknowledged()) {
                holder.hostName.setBackground(context.getDrawable(R.drawable.host_down_ack_box));
            } else {
                holder.hostName.setBackground(context.getDrawable(R.drawable.host_down_box));
            }
        } else {
            holder.hostName.setBackgroundColor(0x00000000);
        }


        // Set and show the number of failing services

        Integer stateCount = holder.host.getStateCount(1);
        Integer stateAckCount = holder.host.getStateAckCount(1);
        String stateCountString;


        if (stateCount == 0 && stateAckCount == 0) {
            holder.warningCount.setVisibility(View.GONE);
            holder.warningAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.warningCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.warningAckCount.setText(stateCountString);
            holder.warningCount.setVisibility(View.VISIBLE);
            holder.warningAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = holder.host.getStateCount(2);
        stateAckCount = holder.host.getStateAckCount(2);

        if (stateCount == 0 && stateAckCount == 0) {
            holder.criticalCount.setVisibility(View.GONE);
            holder.criticalAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.criticalCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.criticalAckCount.setText(stateCountString);
            holder.criticalCount.setVisibility(View.VISIBLE);
            holder.criticalAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = holder.host.getStateCount(3);
        stateAckCount = holder.host.getStateAckCount(3);

        if (stateCount == 0 && stateAckCount == 0) {
            holder.unknownCount.setVisibility(View.GONE);
            holder.unknownAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.unknownCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.unknownAckCount.setText(stateCountString);
            holder.unknownCount.setVisibility(View.VISIBLE);
            holder.unknownAckCount.setVisibility(View.VISIBLE);
        }


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (holder.hostComment.getVisibility() == View.GONE) {
                        holder.hostComment.setVisibility(View.VISIBLE);
                    } else {
                        holder.hostComment.setVisibility(View.GONE);
                    }
                    TransitionManager.beginDelayedTransition(parentRecyclerView, transition);
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.host);
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.parentRecyclerView = recyclerView;
        transition = TransitionInflater.from(context).inflateTransition(R.transition.main_list_transition);
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView hostName;
        final TextView hostComment;
        final TextView criticalCount;
        final TextView criticalAckCount;
        final TextView warningCount;
        final TextView warningAckCount;
        final TextView unknownCount;
        final TextView unknownAckCount;
        HostList host;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            hostName = view.findViewById(R.id.main_list_hostname);
            hostComment = view.findViewById(R.id.main_list_host_comment);
            criticalCount = view.findViewById(R.id.main_list_critical_count);
            criticalAckCount = view.findViewById(R.id.main_list_critical_ack_count);
            warningCount = view.findViewById(R.id.main_list_warning_count);
            warningAckCount = view.findViewById(R.id.main_list_warning_ack_count);
            unknownCount = view.findViewById(R.id.main_list_unknown_count);
            unknownAckCount = view.findViewById(R.id.main_list_unknown_ack_count);
        }
    }

    void updateHostList(List<HostList> hosts){
        this.hosts = hosts;
    }
}
