package com.sumanthakkala.medialines.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.MoreOptionsListener;
import com.sumanthakkala.medialines.ui.moreOptions.MoreOptionsFragmentOne;
import com.sumanthakkala.medialines.ui.moreOptions.MoreOptionsFragmentTwo;

public class MoreOptionsAdapter extends FragmentStateAdapter {
    Fragment fragment;
    Boolean isExistingNote;
    MoreOptionsListener moreOptionsListener;
    public MoreOptionsAdapter(@NonNull FragmentActivity fragmentActivity, Boolean isExistingNoteVal, MoreOptionsListener listener) {
        super(fragmentActivity);
        moreOptionsListener = listener;
        isExistingNote = isExistingNoteVal;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 1:
                fragment = new MoreOptionsFragmentTwo(isExistingNote, moreOptionsListener);
                break;
            default:
                fragment = new MoreOptionsFragmentOne(isExistingNote, moreOptionsListener);
                break;
        }
        return fragment;
    }



    @Override
    public int getItemCount() {
        return 2;
    }
}
