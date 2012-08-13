/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF.exporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import samples.SampleManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

    public static HashMap getCurrentSampleFormattedInfo(String sampleId, Object poolId) throws IOException{
        HashMap sampleInfo = new HashMap();

        List<Map<String, Object>> rs = SampleManager.getCurrentSampleInfoFromDB(sampleId, poolId);

        //PREVENT PHANTOM-DB READS EXCEPTIONS
        if (!rs.isEmpty() && rs.get(0).size()==constants.cDBSamples.T_CREATE_SAMPLES_INFO.length) {
            Object familyId = rs.get(0).get(constants.cDBSamples.f_FAMILY_ID);
            if (familyId == null) {
                familyId = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_FAMILY_ID, familyId);


            Object fatherId = rs.get(0).get(constants.cDBSamples.f_FATHER_ID);
            if (fatherId == null) {
                fatherId = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_FATHER_ID, fatherId);

            Object motherId = rs.get(0).get(constants.cDBSamples.f_MOTHER_ID);
            if (motherId == null) {
                motherId = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_MOTHER_ID, motherId);

            Object sex = rs.get(0).get(constants.cDBSamples.f_SEX);
            if (sex == null) {
                sex = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_SEX, sex);

            Object affection = rs.get(0).get(constants.cDBSamples.f_AFFECTION);
            if (affection == null) {
                affection = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_AFFECTION, affection);

            Object disease = rs.get(0).get(constants.cDBSamples.f_DISEASE);
            if (disease == null) {
                disease = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_DISEASE, disease);

            Object category = rs.get(0).get(constants.cDBSamples.f_CATEGORY);
            if (category == null) {
                category = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_CATEGORY, category);

            Object population = rs.get(0).get(constants.cDBSamples.f_POPULATION);
            if (population == null) {
                population = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_POPULATION, population);

            Object age = rs.get(0).get(constants.cDBSamples.f_AGE);
            if (age == null || age.equals("-1")) {
                age = "0";
            }
            sampleInfo.put(constants.cDBSamples.f_AGE, age);
        }

        return sampleInfo;
    }

}
