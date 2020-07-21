package com.sumanthakkala.medialines.ui.settings;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.sumanthakkala.medialines.R;

public class SettingsFragmentContainer extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_settings_container, container, false);
        setHasOptionsMenu(true);
//        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                setEnabled(false);
//                remove();
//                NavController navController = Navigation.findNavController(root);
//                navController.popBackStack(R.id.nav_home, false);
//            }
//        });
        return root;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search_view).setVisible(false);
    }
}