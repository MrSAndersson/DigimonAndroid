package se.standersson.icingalert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import se.standersson.icingalert.HostListFragment2.OnListFragmentInteractionListener;
import se.standersson.icingalert.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyHostListRecyclerViewAdapter extends RecyclerView.Adapter<MyHostListRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final Context context;
    private List<HostList> hosts;

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


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Toast.makeText(context, holder.host.getHostName(),Toast.LENGTH_SHORT).show();
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.host);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView hostName;
        HostList host;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            hostName = view.findViewById(R.id.main_list_hostname);
        }
    }

    void updateHostList(List<HostList> hosts){
        this.hosts = hosts;
    }
}
