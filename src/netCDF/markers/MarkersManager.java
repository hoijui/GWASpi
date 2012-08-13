package netCDF.markers;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

public class MarkersManager {
    

    
    public static String generateMarkerSetTableNameByDate(){
        String setName = "";
        setName += global.Utils.getShortDateTimeForFileName();
        setName = setName.replace(":", "");
        setName = setName.replace(" ", "");
        setName = setName.replace("/", "");
        return setName;
    }
    
}
