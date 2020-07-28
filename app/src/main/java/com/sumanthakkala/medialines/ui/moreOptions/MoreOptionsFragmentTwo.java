package com.sumanthakkala.medialines.ui.moreOptions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.MoreOptionsListener;

public class MoreOptionsFragmentTwo extends Fragment {
    Boolean isExistingNote;
    MoreOptionsListener moreOptionsListener;

    public MoreOptionsFragmentTwo(Boolean isExistingNoteVal, MoreOptionsListener listener) {
        this.moreOptionsListener = listener;
        this.isExistingNote = isExistingNoteVal;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_more_options_two, container, false);


//        if(isExistingNote){
//            root.findViewById(R.id.exportPdfLayout).setVisibility(View.VISIBLE);
//            root.findViewById(R.id.exportPdfLayout).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    moreOptionsListener.onOptionCLicked(R.id.exportPdfLayout);
//                }
//            });
//        }
//        else {
//            root.findViewById(R.id.exportPdfLayout).setVisibility(View.GONE);
//        }


        root.findViewById(R.id.exportPdfLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.exportPdfLayout);
            }
        });

        root.findViewById(R.id.checkboxesOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.checkboxesOptionLayout);
            }
        });

        root.findViewById(R.id.sketchOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.sketchOptionLayout);
            }
        });

        if(isExistingNote){
            root.findViewById(R.id.infoNoteOptionLayout).setVisibility(View.VISIBLE);
            root.findViewById(R.id.infoNoteOptionLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreOptionsListener.onOptionCLicked(R.id.infoNoteOptionLayout);
                }
            });
        }
        else {
            root.findViewById(R.id.infoNoteOptionLayout).setVisibility(View.GONE);
        }


        return root;
    }
}