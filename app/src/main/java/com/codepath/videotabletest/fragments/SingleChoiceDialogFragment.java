package com.codepath.videotabletest.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.codepath.videotabletest.activities.MainActivity;

import java.util.List;

public class SingleChoiceDialogFragment extends DialogFragment
{
    public static final String DATA = "items";

    public static final String SELECTED = "selected";

    String mSortType;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Resources res = getActivity().getResources();
        Bundle bundle = getArguments();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        dialog.setTitle("Sort by:");
        dialog.setNegativeButton("Cancel", null);
        dialog.setPositiveButton("OK", new PositiveButtonClickListener());

        List<String> list = (List<String>)bundle.get(DATA);
        int position = bundle.getInt(SELECTED);
        if (position == 0) {
            mSortType = "date";
        } else {
            mSortType = "name";
        }

        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
        dialog.setSingleChoiceItems(cs, position, selectItemListener);

        return dialog.create();
    }

    class PositiveButtonClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            MainActivity activity = (MainActivity) getActivity();
            activity.setSortType(mSortType);
            dialog.dismiss();
        }
    }

    DialogInterface.OnClickListener selectItemListener = new DialogInterface.OnClickListener()
    {

        @Override
        public void onClick(DialogInterface dialog, int selection)
        {
            if (selection == 0) {
                mSortType = "date";
            } else {
                mSortType = "name";
            }
        }

    };
}