package csa.iisc.gaitanalysis.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Created by root on 16/3/16.
 */
public class ConfirmDialog {

    private OnResultListener onResultListener;

    public ConfirmDialog() {
        onResultListener = null;
    }

    public OnResultListener getOnResultListener() {
        return onResultListener;
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public void showDialog(final Activity activity, String title, String message) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResultListener.OnResultChange(false);
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (onResultListener != null) {
                            onResultListener.OnResultChange(true);
                        }
                    }
                })

                .show();
    }

    public interface OnResultListener {
        void OnResultChange(Boolean result);
    }
}
