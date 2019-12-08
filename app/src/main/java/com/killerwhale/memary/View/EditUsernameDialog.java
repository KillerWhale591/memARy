package com.killerwhale.memary.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.killerwhale.memary.R;

public class EditUsernameDialog extends AppCompatDialogFragment {

    private EditText edtEditUsername;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_username, null);

        edtEditUsername = view.findViewById(R.id.edtEditUsername);

        builder.setView(view)
                .setTitle("Please enter a new username")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = edtEditUsername.getText().toString();
                        EUDL.sendUsername(username);
                    }
                });

        return builder.create();
    }

    public interface EditUsernameDialogListener{
        public void sendUsername(String username);
    }

    EditUsernameDialogListener EUDL;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EUDL = (EditUsernameDialogListener) context;
    }
}
