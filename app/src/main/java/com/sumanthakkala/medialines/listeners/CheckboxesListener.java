package com.sumanthakkala.medialines.listeners;

public interface CheckboxesListener {
    void onCheckboxEnterPressed(int position);
    void onDeleteCheckbox(int position);
    void onCheckboxTextChanged(String string, int position, boolean checkBoxVal);
    void onCheckboxValueChanged(boolean val, int position);
}
