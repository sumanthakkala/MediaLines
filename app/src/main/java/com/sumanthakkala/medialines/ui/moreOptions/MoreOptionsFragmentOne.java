package com.sumanthakkala.medialines.ui.moreOptions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.MoreOptionsListener;

public class MoreOptionsFragmentOne extends Fragment {

    Boolean isExistingNote;
    MoreOptionsListener moreOptionsListener;
    public MoreOptionsFragmentOne(Boolean isExistingNoteVal, MoreOptionsListener listener) {
        moreOptionsListener = listener;
        isExistingNote = isExistingNoteVal;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_more_options_one, container, false);

        root.findViewById(R.id.addImageLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.addImageLayout);
            }
        });
        root.findViewById(R.id.takePhotoLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.takePhotoLayout);
            }
        });
        root.findViewById(R.id.addUrlLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.addUrlLayout);
            }
        });
        root.findViewById(R.id.transcribeAudioLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.transcribeAudioLayout);
            }
        });
        root.findViewById(R.id.recordAudioLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.recordAudioLayout);
            }
        });


        return root;
    }
}