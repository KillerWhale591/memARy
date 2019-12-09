package com.killerwhale.memary.ARComponent.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import com.killerwhale.memary.R;


/**
 * Author: Qili Zeng (qzeng@bu.edu)
 * With reference to: justaline APP by Google
 * Dialog brought up when user selects to upload their drawing
 */

public class UploadDrawingDialog extends BaseDialog {

    private Listener mListener;

    private static final String DRAWING_SESSION = "drawingSession";

    public static UploadDrawingDialog newInstance(boolean paired) {
        UploadDrawingDialog dialog = new UploadDrawingDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putBoolean(DRAWING_SESSION, paired);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int titleRes = -1;
        int messageRes = R.string.upload_confirmation_message;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setMessage(messageRes);

        if (titleRes > -1) builder.setTitle(titleRes);

        // Set up the buttons
        builder.setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onUploadDrawingConfirmed();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        setCancelable(false);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement Listener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    public interface Listener {

        void onUploadDrawingConfirmed();
    }
}
