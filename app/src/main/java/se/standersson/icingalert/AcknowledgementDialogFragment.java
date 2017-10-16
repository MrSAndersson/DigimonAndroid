package se.standersson.icingalert;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

/**
 * Holds the Acknowledgement Dialog
 */

public class AcknowledgementDialogFragment extends DialogFragment {

    static AcknowledgementDialogFragment newInstance(List<HostList> hosts, int groupPosition, int childPosition) {
        AcknowledgementDialogFragment fragment = new AcknowledgementDialogFragment();
        Bundle args = new Bundle();
        args.putInt("groupPosition", groupPosition);
        args.putInt("childPosition", childPosition);
        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    static AcknowledgementDialogFragment newInstance(List<HostList> hosts, int groupPosition) {
        AcknowledgementDialogFragment fragment = new AcknowledgementDialogFragment();
        Bundle args = new Bundle();
        args.putInt("groupPosition", groupPosition);
        args.putBoolean("hostOnly", true);
        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        int childPosition = 0;
        int groupPosition = getArguments().getInt("groupPosition");
        if (!getArguments().getBoolean("hostOnly", false)) {
            childPosition = getArguments().getInt("childPosition");
        }
        // noinspection unchecked
        List <HostList> hosts = (List<HostList>) getArguments().getSerializable("hosts");

        // Build a dialog using the AlertDialog.Builder class
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.acknowledgement_dialog_title);

        // Configure the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.acknowledgement_layout, null);
        assert hosts != null;
        if (!getArguments().getBoolean("hostOnly", false)) {
            String serviceString = hosts.get(groupPosition).getServiceName(childPosition);
            ((TextView) view.findViewById(R.id.acknowledgement_servicename)).setText(serviceString);
        }
        String hostString = hosts.get(groupPosition).getHostName();
        ((TextView)view.findViewById(R.id.acknowledgement_hostname)).setText(hostString);

        builder.setView(view)
         // Add action buttons
           .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
                   Toast.makeText(getActivity(), "Stuff", Toast.LENGTH_SHORT).show();
               }
           })
           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   AcknowledgementDialogFragment.this.getDialog().cancel();
               }
           });
    return builder.create();
    }

}
