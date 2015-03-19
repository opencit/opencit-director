/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;


import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 *
 * @author admkrushnakant
 */
public class Directories implements Comparable<Directories>{
    
    private CheckBox checkBox;
    private TextField textField;
    
    public Directories(CheckBox cbox, TextField tfield) {
        this.checkBox = cbox;
        this.textField = tfield;
    }
    
    public CheckBox getCbox() {
        return checkBox;
    }
    
    public void setCbox(CheckBox cbox) {
        this.checkBox = cbox;
    }
    
    public TextField getTfield() {
        return textField;
    }
    
    public void setTfield(TextField tfield) {
        tfield.setEditable(false);
        this.textField = tfield;
    }

    @Override
    public int compareTo(Directories anotherInstance) {
        return this.checkBox.getText().compareTo(anotherInstance.checkBox.getText());
    }
    
}
