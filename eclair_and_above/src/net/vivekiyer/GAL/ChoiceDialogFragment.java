package net.vivekiyer.GAL;

import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ChoiceDialogFragment extends DialogFragment implements OnClickListener {
    private static final String ARG_TITLE = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_TITLE";
    private static final String ARG_MESSAGE = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_MESSAGE";
    private static final String ARG_POSITIVE_BUTTON_TEXT = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_POSITIVE_BUTTON_TEXT";
    private static final String ARG_NEGATIVE_BUTTON_TEXT = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_NEGATIVE_BUTTON_TEXT";
    private static final String ARG_POSITIVE_ACTION = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_POSITIVE_ACTION";
    private static final String ARG_NEGATIVE_ACTION = "net.vivekiyer.GAL.ChoiceDialogFragment.ARG_NEGATIVE_ACTION";
 
    private OnChoiceDialogOptionClickListener clickListener;
    private int positiveAction;
    private int negativeAction;
 
    public interface OnChoiceDialogOptionClickListener {
        void onChoiceDialogOptionPressed(int action);
    }
 
    public static ChoiceDialogFragment newInstance(String title, String message,
    		String positiveButtonText, String negativeButtonText){
    	return newInstance(title, message, positiveButtonText, negativeButtonText, 1, 0);
    }
    public static ChoiceDialogFragment newInstance(String title, String message,
   		String positiveButtonText, String negativeButtonText, int positiveAction, int negativeAction){
    	ChoiceDialogFragment fragment = new ChoiceDialogFragment();
 
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        args.putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        args.putInt(ARG_POSITIVE_ACTION, positiveAction);
        args.putInt(ARG_NEGATIVE_ACTION, negativeAction);
        fragment.setArguments(args);
 
        return fragment;
    }
 
    public void setListener(OnChoiceDialogOptionClickListener clickListener){
        this.clickListener = clickListener;
    }
 
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE);
        String message = args.getString(ARG_MESSAGE);
        String positiveButtonText = args.getString(ARG_POSITIVE_BUTTON_TEXT);
        String negativeButtonText = args.getString(ARG_NEGATIVE_BUTTON_TEXT);
        positiveAction = args.getInt(ARG_POSITIVE_ACTION);
        negativeAction = args.getInt(ARG_NEGATIVE_ACTION);
 
        return new AlertDialog.Builder(getActivity())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText, this)
        .setNegativeButton(negativeButtonText, this).create();
    }
 
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(this.clickListener != null){
        		this.clickListener.onChoiceDialogOptionPressed(
       				which == DialogInterface.BUTTON_POSITIVE ? positiveAction : negativeAction);
        }
    }
}