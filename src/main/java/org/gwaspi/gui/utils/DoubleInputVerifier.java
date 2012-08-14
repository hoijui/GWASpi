/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.gui.utils;

import java.awt.Toolkit;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

class DoubleInputVerifier extends InputVerifier{

    private int lengthBeforeDot = 1;
    private int lengthAfterDot = 10;

    public DoubleInputVerifier(){

    }

    public DoubleInputVerifier(int lengthBeforeDot, int lengthAfterDot){
        this.lengthAfterDot = lengthAfterDot;
        this.lengthBeforeDot = lengthBeforeDot;
    }

    public boolean verify(JComponent input) {
        boolean correct = true;
        try {
            JTextField tField = (JTextField) input;
            String text = tField.getText();
            if(text.length() == 0){
                return true;
            }

            if ((correct = isDoubleOrFloat(text))) {
                //correct = isFormatCorrect(text);
            }

        } finally {
            if(!correct){
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Must be numeric!");
            }
        }
        return correct;
    }

    private boolean isDoubleOrFloat(String text){
        try{
            Double.parseDouble(text);
        }catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }

}

