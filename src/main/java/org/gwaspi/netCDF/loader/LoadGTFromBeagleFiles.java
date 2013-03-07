package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadGTFromBeagleFiles extends AbstractLoadGTFromFiles {

	private static interface Standard {

		public static final int markerId = 1;
		public static final int genotypes = 2;
		public static final String missing = "0";
	}

	//<editor-fold defaultstate="expanded" desc="CONSTRUCTORS">
	public LoadGTFromBeagleFiles()
	{
		super(
				ImportFormat.BEAGLE,
				StrandType.UNKNOWN,
				false,
				-1, // disabled, else: 4
				null); // disabled, else: cNetCDF.Variables.VAR_MARKERS_BASES_KNOWN
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
		super.addAdditionalBigDescriptionProperties(descSB, loadDescription);

		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Marker file)\n");
	}

	@Override
	protected MetadataLoader createMetaDataLoader(String filePath, GenotypesLoadDescription loadDescription) {

		String curAnnotationFilePath = filePath;
		return new MetadataLoaderBeagle(
				curAnnotationFilePath,
				loadDescription.getChromosome(),
				loadDescription.getStrand(),
				loadDescription.getStudyId());
	}

	@Override
	public void loadIndividualFiles(
			File file,
			SampleKey sampleKey,
			Map<MarkerKey, Object> wrMarkerSetMap)
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		Map<MarkerKey, Object> tempMarkerIdMap = new LinkedHashMap<MarkerKey, Object>();
		Map<SampleKey, Object> sampleOrderMap = new LinkedHashMap<SampleKey, Object>();

		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (l.startsWith("I")) { // Found first marker row!
				String sampleHeader = l;
				String[] headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int i = Standard.genotypes; i < headerFields.length; i = i + 2) {
					String sampleId = headerFields[i];
					// NOTE The Beagle format does not have a family-ID
					sampleOrderMap.put(new SampleKey(sampleId, SampleKey.FAMILY_ID_NONE), i);
				}
			}
			if (l.startsWith("M")) { // Found first marker row!
				// GET ALLELES FROM MARKER ROWS
				String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
				MarkerKey markerKey = MarkerKey.valueOf(cVals[Standard.markerId]);

				Object columnNb = sampleOrderMap.get(sampleKey);
				if (columnNb != null) {
					String strAlleles = cVals[(Integer) columnNb] + cVals[((Integer) columnNb) + 1];
					byte[] tmpAlleles = new byte[]{
						(byte) strAlleles.toString().charAt(0),
						(byte) strAlleles.toString().charAt(1)};
					tempMarkerIdMap.put(markerKey, tmpAlleles);
				}
			}
		}
		inputBufferReader.close();

		wrMarkerSetMap.putAll(tempMarkerIdMap);

		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private Map<String, Object> getBeagleSampleIds(File hapmapGTFile) throws IOException {

		Map<String, Object> uniqueSamples = new LinkedHashMap<String, Object>();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputBeagleBr = new BufferedReader(fr);

		String sampleHeader = "";
		boolean gotSamples = false;
		while (!gotSamples) {
			String l = inputBeagleBr.readLine();
			if (l == null) {
				break;
			}
			if (l.startsWith("I")) {
				sampleHeader = l;
				gotSamples = true;
			}
		}
		inputBeagleBr.close();

		String[] beagleSamples = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleSamples.length; i++) {
			uniqueSamples.put(beagleSamples[i], "");
		}

		return uniqueSamples;
	}
	//</editor-fold>
}
