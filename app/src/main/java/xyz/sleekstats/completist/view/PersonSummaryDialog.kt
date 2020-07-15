package xyz.sleekstats.completist.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import xyz.sleekstats.completist.R

class PersonSummaryDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments ?: return super.onCreateDialog(savedInstanceState)
        val summary = bundle.getString(KEY_SUMMARY)
        @SuppressLint("InflateParams") val rootView = LayoutInflater.from(activity).inflate(R.layout.dialog_person_summary, null)
        val textView = rootView.findViewById<TextView>(R.id.dialog_person_summary)
        textView.text = summary
        textView.movementMethod = ScrollingMovementMethod()
        val alertDialog = AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
                .setView(rootView)
                .setTitle("Summary")
                .setNegativeButton(R.string.back) { dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                }
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        return alertDialog
    }

    companion object {
        private const val KEY_SUMMARY = "key_summary"
        fun newInstance(summary: String?): PersonSummaryDialog {
            val args = Bundle()
            val fragment = PersonSummaryDialog()
            args.putString(KEY_SUMMARY, summary)
            fragment.arguments = args
            return fragment
        }
    }
}