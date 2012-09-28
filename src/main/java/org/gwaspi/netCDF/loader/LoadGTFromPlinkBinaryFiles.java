package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadGTFromPlinkBinaryFiles implements GenotypesLoader {

	public LoadGTFromPlinkBinaryFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.PLINK_Binary;
	}

	@Override
	public StrandType getMatrixStrand() {
		return null;
	}

	@Override
	public boolean isHasDictionary() {
		return true;
	}

	@Override
	public int getMarkersD2ItemNb() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public String getMarkersD2Variables() {
		return cNetCDF.Variables.VAR_MARKERS_BASES_DICT;
	}

	@Override
	public int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderPlinkBinary markerSetLoader = new MetadataLoaderPlinkBinary(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getStrand(),
				loadDescription.getStudyId());
		Map<String, Object> sortedMarkerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData(); //markerid, rsId, chr, pos, allele1, allele2

		int hyperSlabRows = Math.round(org.gwaspi.gui.StartGWASpi.maxProcessMarkers
				/ (sampleInfo.size() * 2)); //PLAYING IT SAFE WITH HALF THE maxProcessMarkers
		if (sortedMarkerSetMap.size() < hyperSlabRows) {
			hyperSlabRows = sortedMarkerSetMap.size();
		}

		System.out.println("Done initializing sorted MarkerSetMap at " + startTime);

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!loadDescription.getDescription().isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(loadDescription.getDescription());
			descSB.append("\n");
		}
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
		descSB.append("\n");
		descSB.append("Markers: ").append(sortedMarkerSetMap.size()).append(", Samples: ").append(sampleInfo.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(cImport.ImportFormat.PLINK_Binary.toString());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (BIM file)\n");
		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (BED file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath());
			descSB.append(" (FAM/Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(sortedMarkerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
				isHasDictionary(),
				sampleInfo.size(),
				sortedMarkerSetMap.size(),
				chrSetMap.size(),
				loadDescription.getAnnotationFilePath());

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(sampleInfo, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		samplesD2 = null;
		System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(ncfile, chrSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(sortedMarkerSetMap, 3);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//WRITE ALLELE DICTIONARY METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 4, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(getMarkersD2Variables(), markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing forward alleles to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[]{0, 0};
		String strandFlag;
		switch (loadDescription.getStrand()) {
			case PLUS:
				strandFlag = cImport.StrandFlags.strandPLS;
				break;
			case MINUS:
				strandFlag = cImport.StrandFlags.strandMIN;
				break;
			case FWD:
				strandFlag = cImport.StrandFlags.strandFWD;
				break;
			case REV:
				strandFlag = cImport.StrandFlags.strandREV;
				break;
			default:
				strandFlag = cImport.StrandFlags.strandUNK;
				break;
		}
		for (Map.Entry<String, Object> entry : sortedMarkerSetMap.entrySet()) {
			entry.setValue(strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(sortedMarkerSetMap, cNetCDF.Strides.STRIDE_STRAND);

		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = null;
		System.out.println("Done writing strand info to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">
		GenotypeEncoding guessedGTCode = GenotypeEncoding.O12;
		System.out.println(Text.All.processing);
		Map<String, Object> bimMarkerSetMap = markerSetLoader.parseOrigBimFile(loadDescription.getAnnotationFilePath()); //key = markerId, values{allele1 (minor), allele2 (major)}
		loadBedGenotypes(
				new File(loadDescription.getGtDirPath()),
				ncfile,
				sortedMarkerSetMap,
				bimMarkerSetMap,
				sampleInfo,
				guessedGTCode,
				hyperSlabRows);

		System.out.println("Done writing genotypes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		// </editor-fold>

		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("Genotype encoding: ");
			descSB.append(guessedGTCode);
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			db.updateTable(cDBGWASpi.SCH_MATRICES,
					cDBMatrix.T_MATRICES,
					new String[]{cDBMatrix.f_DESCRIPTION},
					new Object[]{descSB.toString()},
					new String[]{cDBMatrix.f_ID},
					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}


		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	private static void loadBedGenotypes(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> wrMarkerIdSetMap,
			Map<String, Object> bimMarkerSetMap,
			Map<String, Object> sampleSetMap,
			GenotypeEncoding guessedGTCode,
			int hyperSlabRows)
			throws IOException, InvalidRangeException
	{
		FileInputStream bedFS = new FileInputStream(file);
		DataInputStream bedIS = new DataInputStream(bedFS);

		int sampleNb = sampleSetMap.size();
		int markerNb = bimMarkerSetMap.size();
		int bytesPerSNP = 0;
		if (sampleNb % 4 == 0) {  //Nb OF BYTES IN EACH ROW
			bytesPerSNP = sampleNb / 4;
		} else {
			bytesPerSNP = (int) (Math.floor(sampleNb / 4) + 1);
		}

		Iterator<String> itMarkerSet = bimMarkerSetMap.keySet().iterator();
		Iterator<String> itSampleSet = sampleSetMap.keySet().iterator();

		//SKIP HEADER
		bedIS.readByte();
		bedIS.readByte();
		byte mode = bedIS.readByte();


		//INIT VARS
		String[] alleles;
		byte[] rowBytes = new byte[bytesPerSNP];
		byte byteData;
		boolean allele1;
		boolean allele2;
		String sampleId;
		if (mode == 1) {
			//GET GENOTYPES
			int rowCounter = 1;
			List<byte[]> genotypesAL = new ArrayList<byte[]>();
			while (itMarkerSet.hasNext()) {
				try {
					////////////////// NEW SNP //////////////
					//PURGE Map
					while (itSampleSet.hasNext()) {
						String key = itSampleSet.next();
						sampleSetMap.put(key, cNetCDF.Defaults.DEFAULT_GT);
					}

					String markerId = itMarkerSet.next();
					alleles = (String[]) bimMarkerSetMap.get(markerId); //key = markerId, values{allele1 (minor), allele2 (major)}

					//READ ALL SAMPLE GTs FOR CURRENT SNP
					int check = bedIS.read(rowBytes, 0, bytesPerSNP);
					int bitsPerRow = 1;
					itSampleSet = sampleSetMap.keySet().iterator();
					for (int j = 0; j < bytesPerSNP; j++) { //ITERATE THROUGH ROWS (READING BYTES PER ROW)
						byteData = rowBytes[j];
						for (int i = 0; i < 8; i = i + 2) {   //FOR EACH BYTE IN ROW, EAT 2 BITS AT A TIME
							if (bitsPerRow <= (sampleNb * 2)) {    //SKIP EXCESS BITS
								allele1 = getBit(byteData, i);
								allele2 = getBit(byteData, i + 1);
								sampleId = itSampleSet.next();
								if (!allele1 && !allele2) {     //  00  Homozygote "1"/"1" - Minor allele
									sampleSetMap.put(sampleId, new byte[]{(byte) alleles[0].charAt(0), (byte) alleles[0].charAt(0)});
								} else if (allele1 && allele2) {     //  11  Homozygote "2"/"2" - Major allele
									sampleSetMap.put(sampleId, new byte[]{(byte) alleles[1].charAt(0), (byte) alleles[1].charAt(0)});
								} else if (!allele1 && allele2) {     //  01  Heterozygote
									sampleSetMap.put(sampleId, new byte[]{(byte) alleles[0].charAt(0), (byte) alleles[1].charAt(0)});
								} else if (allele1 && !allele2) {     //  10  Missing genotype
									sampleSetMap.put(sampleId, cNetCDF.Defaults.DEFAULT_GT);
								}

								bitsPerRow = bitsPerRow + 2;
							}
						}
					}

					/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
					if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
						guessedGTCode = Utils.detectGTEncoding(sampleSetMap);
					} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
						guessedGTCode = Utils.detectGTEncoding(sampleSetMap);
					}


					//WRITING HYPERSLABS AT A TIME
					for (Iterator it = sampleSetMap.keySet().iterator(); it.hasNext();) {
						Object key = it.next();
						byte[] value = (byte[]) sampleSetMap.get(key);
						genotypesAL.add(value);
					}

					if (rowCounter != 1 && rowCounter % (hyperSlabRows) == 0) {
						ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeALValuesToSamplesHyperSlabArrayByteD3(genotypesAL, sampleSetMap.size(), cNetCDF.Strides.STRIDE_GT);
						int[] origin = new int[]{0, (rowCounter - hyperSlabRows), 0}; //0,0,0 for 1st marker ; 0,1,0 for 2nd marker....
//                        System.out.println("Origin at rowCount "+rowCounter+": "+origin[0]+"|"+origin[1]+"|"+origin[2]);
						try {
							ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
						} catch (IOException e) {
							System.err.println("ERROR writing file");
						} catch (InvalidRangeException e) {
							System.out.println("Error in origin at rowCount " + rowCounter + ": " + origin[0] + "|" + origin[1] + "|" + origin[2]);
							e.printStackTrace();
						}

						genotypesAL = new ArrayList();
					}

				} catch (EOFException eof) {
					System.out.println("End of File");
					break;
				}
				if (rowCounter % 10000 == 0) {
					System.out.println("Processed markers: " + rowCounter);
				}
				rowCounter++;
			}

			//WRITING LAST HYPERSLAB
			int lastHyperSlabRows = markerNb - (genotypesAL.size() / sampleNb);
			ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeALValuesToSamplesHyperSlabArrayByteD3(genotypesAL, sampleSetMap.size(), cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[]{0, lastHyperSlabRows, 0}; //0,0,0 for 1st marker ; 0,1,0 for 2nd marker....
//            System.out.println("Last origin at rowCount "+rowCounter+": "+origin[0]+"|"+origin[1]+"|"+origin[2]);
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
				System.out.println("Processed markers: " + rowCounter);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				System.out.println("Error in origin at rowCount " + rowCounter + ": " + origin[0] + "|" + origin[1] + "|" + origin[2]);
				e.printStackTrace();
			}

		} else {
			System.out.println("Binary PLINK file must be in SNP-major mode!");
		}


		bedIS.close();
		bedFS.close();

	}

	private static int translateBitSet(byte b, int bit) {
		int result = 0;
		boolean by = (b & (1 << bit)) != 0;
		if (by) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}

	private static boolean getBit(byte b, int pos) {
		boolean by = (b & (1 << pos)) != 0;
		return by;
	}
}
