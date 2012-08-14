/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.reports;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class ComparatorPvalAsc  implements Comparator {

    public int compare(Object o1, Object o2) {
       return compare((Map.Entry)o1, (Map.Entry)o2);
    }
    public int compare(Map.Entry e1, Map.Entry e2) {
       int cf = ((Comparable)e1.getValue()).compareTo(e2.getValue());
       if (cf == 0) {
          cf = ((Comparable)e1.getKey()).compareTo(e2.getKey());
       }
       return cf;
    }

}

