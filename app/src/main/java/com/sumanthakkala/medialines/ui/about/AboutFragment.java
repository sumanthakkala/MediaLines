package com.sumanthakkala.medialines.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sumanthakkala.medialines.BuildConfig;
import com.sumanthakkala.medialines.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_about, container, false);
        setHasOptionsMenu(true);
        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                remove();
                NavController navController = Navigation.findNavController(root);
                navController.popBackStack(R.id.nav_home, false);
            }
        });
        TextView textView =(TextView)root.findViewById(R.id.developedBy);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "Developed by <a href='https://sumanthakkala.work'>Sumanth Akkala</a>!";
        textView.setText(Html.fromHtml(text));

        TextView version = root.findViewById(R.id.aboutVersion);
        version.setText("V " + BuildConfig.VERSION_NAME);

        return root;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search_view).setVisible(false);
    }
}