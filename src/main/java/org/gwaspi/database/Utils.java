package org.gwaspi.database;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	public static String arrayToString(String[] a, String separator) {
		StringBuilder result = new StringBuilder();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	public static String generateMatrixNetCDFNameByDate() {
		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName();
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");

//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name
		return matrixName;
	}
}
