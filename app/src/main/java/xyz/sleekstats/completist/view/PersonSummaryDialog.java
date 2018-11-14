package xyz.sleekstats.completist.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import xyz.sleekstats.completist.R;

public class PersonSummaryDialog extends DialogFragment {

    private static final String KEY_SUMMARY = "key_summary";

    public PersonSummaryDialog() {
    }

    public static PersonSummaryDialog newInstance(String summary) {

        Bundle args = new Bundle();
        PersonSummaryDialog fragment = new PersonSummaryDialog();
        args.putString(KEY_SUMMARY, summary);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if(bundle == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        String summary = bundle.getString(KEY_SUMMARY);
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_person_summary, null);
        TextView textView = rootView.findViewById(R.id.dialog_person_summary);
        textView.setText(summary);
        textView.setMovementMethod(new ScrollingMovementMethod());

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                .setView(rootView)
                .setTitle("Summary")
                .setNegativeButton(R.string.back, (dialog, id) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return alertDialog;
    }
}