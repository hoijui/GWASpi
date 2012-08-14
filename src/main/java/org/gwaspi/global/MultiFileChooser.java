/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

package org.gwaspi.global;

import java.awt.Container;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JList;

public class MultiFileChooser extends JFileChooser {     
  public MultiFileChooser() {
    }     
    public File[] getSelectedFiles() {
        Container c1 = (Container)getComponent(3);
        JList list = null;
        while (c1 != null) {
            Container c = (Container)c1.getComponent(0);
            if (c instanceof JList) {
                list = (JList)c;
                break;
            }
            c1 = c;
        }
        Object[] entries = list.getSelectedValues();
        File[] files = new File[entries.length];
        for (int k=0; k < entries.length; k++) {
            if (entries[k] instanceof File)
                files[k] = (File)entries[k];
        }
        return files;
    }
}

