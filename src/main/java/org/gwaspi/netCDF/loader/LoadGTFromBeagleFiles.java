package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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

	private String annotationFilePath;
	private String chromosome;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromBeagleFiles(String _gtFilePath,
			String _sampleFilePath,
			String _annotationFilePath,
			int _studyId,
			String _chromosome,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoMap)
			throws IOException
	{
		super(_gtFilePath,
			_sampleFilePath,
			_studyId,
			_strand,
			cImport.ImportFormat.BEAGLE.toString(),
			_friendlyName,
			_gtCode,
			cNetCDF.Defaults.StrandType.UNKNOWN.toString(),
			0,
			-1, // disabled, else: 4,
			null, // disabled, else: cNetCDF.Variables.VAR_MARKERS_BASES_KNOWN,
			_description,
			_sampleInfoMap);

		annotationFilePath = _annotationFilePath;
		chromosome = _chromosome;
	}

	@Override
	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB) {
		super.addAdditionalBigDescriptionProperties(descSB);

		descSB.append(annotationFilePath);
		descSB.append(" (Marker file)\n");
	}

	protected MetadataLoader createMetaDataLoader(String filePath) {

		String curAnnotationFilePath = filePath;
		return new MetadataLoaderBeagle(curAnnotationFilePath, chromosome, strand, studyId);
	}

	public void loadIndividualFiles(File file,
			String currSampleId,
			Map<String, Object> wrMarkerSetMap) throws IOException, InvalidRangeException {

		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		Map<String, Object> tempMarkerIdMap = new LinkedHashMap<String, Object>();
		Map<String, Object> sampleOrderMap = new LinkedHashMap<String, Object>();

		String sampleHeader = null;
		String l;
		while ((l = inputBufferReader.readLine()) != null) {

			String[] headerFields = null;

			if (l.startsWith("I")) { //Found first marker row!
				sampleHeader = l;
				headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int i = Standard.genotypes; i < headerFields.length; i = i + 2) {
					sampleOrderMap.put(headerFields[i], i);
				}
			}
			if (l.startsWith("M")) { //Found first marker row!

				//GET ALLELES FROM MARKER ROWS
				String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
				String currMarkerId = cVals[Standard.markerId];

				Object columnNb = sampleOrderMap.get(currSampleId);
				if (columnNb != null) {
					String strAlleles = cVals[(Integer) columnNb] + cVals[((Integer) columnNb) + 1];
					byte[] tmpAlleles = new byte[]{(byte) strAlleles.toString().charAt(0),
						(byte) strAlleles.toString().charAt(1)};
					tempMarkerIdMap.put(currMarkerId, tmpAlleles);
				}
			}
		}

		wrMarkerSetMap.putAll(tempMarkerIdMap);

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(wrMarkerSetMap);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
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

		String[] beagleSamples = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleSamples.length; i++) {
			uniqueSamples.put(beagleSamples[i], "");
		}

		return uniqueSamples;
	}
	//</editor-fold>
}
