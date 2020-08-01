package com.sumanthakkala.medialines.adapters;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.listeners.CheckboxesListener;
import java.util.List;

public class CheckboxesAdapter extends RecyclerView.Adapter<CheckboxesAdapter.CheckboxItemViewHolder> {



    private List<String> checkboxesList;
    private CheckboxesListener checkboxesListener;
    private static Context context = null;
    private int focusRequestedPosition;
    private List<String> rawCheckboxText;


    public CheckboxesAdapter(List<String> list, CheckboxesListener listener) {
        this.checkboxesList = list;
        this.checkboxesListener = listener;
    }

    @NonNull
    @Override
    public CheckboxItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CheckboxItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.checkbox_container_layout,
                        parent,
                        false
                ), checkboxesListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckboxItemViewHolder holder, final int position) {
        holder.checkboxText.setTag(position);
        holder.setCheckboxViewData(checkboxesList.get(position), position, rawCheckboxText.get(position));
//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                checkboxValueCHanged(b, position);
//            }
//        });

//        holder.deleteCheckBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkboxesListener.onDeleteCheckbox(position);
//            }
//        });
        holder.checkboxText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) || i == EditorInfo.IME_ACTION_NEXT || textView.getImeActionLabel().equals("—>|")){
                    checkboxesListener.onCheckboxEnterPressed(position);
                }
                return true;
            }
        });
        holder.checkboxText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    holder.deleteCheckBox.setVisibility(View.VISIBLE);
                }
                else {
                    holder.deleteCheckBox.setVisibility(View.GONE);
                }
            }
        });

        if(position == focusRequestedPosition){
            holder.checkboxText.requestFocus();
        }
        else {
            holder.checkboxText.clearFocus();
        }
    }


    @Override
    public int getItemCount() {
        return checkboxesList.size();
    }

    public void setRequestFocusPosition(int pos){
        this.focusRequestedPosition = pos;
    }

    public static class CheckboxItemViewHolder extends RecyclerView.ViewHolder{

        public CheckBox checkBox;
        public EditText checkboxText;
        public ImageView deleteCheckBox;
        private CheckboxesListener checkboxesListener;
        private int position;
        public CheckboxItemViewHolder(@NonNull View itemView, CheckboxesListener listener) {
            super(itemView);

            checkboxesListener = listener;

            checkBox = itemView.findViewById(R.id.checkbox_CB);
            checkboxText = itemView.findViewById(R.id.checkboxText_ET);
            deleteCheckBox = itemView.findViewById(R.id.deleteCheckBox_IV);
            TextWatcher textWatcher = new MyTextWatcher(checkboxText, checkBox, checkboxesListener);
            checkboxText.addTextChangedListener(textWatcher);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    checkboxesListener.onCheckboxValueChanged(isChecked, position);
                }
            });

            deleteCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkboxesListener.onDeleteCheckbox(position);
                }
            });
        }


        void setCheckboxViewData(String text, int pos, String rawData){
            this.position = pos;
            if(!rawData.isEmpty()){
                if(rawData.contains(Constants.CHECKBOX_VALUE_CHECKED.substring(2))){
                    checkBox.setChecked(true);
                }
                else {
                    checkBox.setChecked(false);
                }
            }
            else {
                checkBox.setChecked(false);
            }
            if(!text.isEmpty()){
                checkboxText.setText(text);
            }
            else {
                checkboxText.setText(null);
            }
        }
    }


    private void checkboxValueCHanged(boolean isChecked, int position){
        checkboxesListener.onCheckboxValueChanged(isChecked, position);
    }

    public void setRawCheckboxStrs(List<String> str){
        this.rawCheckboxText = str;
    }
}

class MyTextWatcher implements TextWatcher {
    private EditText editText;
    private CheckboxesListener checkboxesListener;
    private CheckBox checkBox;
    public MyTextWatcher(EditText editText, CheckBox cb, CheckboxesListener listener) {
        this.editText = editText;
        checkboxesListener = listener;
        checkBox = cb;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int position = (int) editText.getTag();
        // Do whatever you want with position
        checkboxesListener.onCheckboxTextChanged(editText.getText().toString(), position, checkBox.isChecked());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}