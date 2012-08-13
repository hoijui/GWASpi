/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package statistics;

import java.math.BigInteger;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

    public static double calculatePunnettFrequency(int obsAA, int obsAa, int sampleNb){
        double freq = (double) ((obsAA*2)+obsAa)/(sampleNb*2);
        return freq;

    }


    public static BigInteger factorial(BigInteger n)
    {
        if( n.compareTo(BigInteger.ONE) <= 0 )     // base case
            return BigInteger.ONE;
        else
            return factorial(n.subtract(BigInteger.ONE)).multiply(n);
    }

}

