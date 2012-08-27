package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
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
import java.util.LinkedHashMap;
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
public class LoadGTFromPlinkBinaryFiles {

	private String bimFilePath;
	private String sampleFilePath;
	private String bedFilePath;
	private int studyId;
	private String strand;
	private String friendlyName;
	private String description;
	private Map<String, Object> sampleInfoLHM = new LinkedHashMap<String, Object>();
	private String gtCode;
	private org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding guessedGTCode = org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.O12;
	private int hyperSlabRows;

	//CONSTRUCTORS
	public LoadGTFromPlinkBinaryFiles(String _bedFilePath,
			String _sampleFilePath,
			String _bimFilePath,
			int _studyId,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoLHM) {

		bimFilePath = _bimFilePath;
		sampleFilePath = _sampleFilePath;
		bedFilePath = _bedFilePath;
		studyId = _studyId;
		strand = _strand;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		description = _description;
		sampleInfoLHM = _sampleInfoLHM;
	}

	//METHODS
	public int processData() throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderPlinkBinary markerSetLoader = new MetadataLoaderPlinkBinary(bimFilePath, strand, studyId);
		Map<String, Object> sortedMarkerSetLHM = markerSetLoader.getSortedMarkerSetWithMetaData(); //markerid, rsId, chr, pos, allele1, allele2

		hyperSlabRows = Math.round(org.gwaspi.gui.StartGWASpi.maxProcessMarkers
				/ (sampleInfoLHM.size() * 2)); //PLAYING IT SAFE WITH HALF THE maxProcessMarkers
		if (sortedMarkerSetLHM.size() < hyperSlabRows) {
			hyperSlabRows = sortedMarkerSetLHM.size();
		}


		System.out.println("Done initializing sorted MarkerSetLHM at " + startTime);

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//        descSB.append("\nStrand: ");
//        descSB.append(strand);
//        descSB.append("\n");
		descSB.append("\n");
		descSB.append("Markers: ").append(sortedMarkerSetLHM.size()).append(", Samples: ").append(sampleInfoLHM.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(cImport.ImportFormat.PLINK_Binary.toString());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(bimFilePath);
		descSB.append(" (BIM file)\n");
		descSB.append(bedFilePath);
		descSB.append(" (BED file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (FAM/Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(sortedMarkerSetLHM, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				cImport.ImportFormat.PLINK.toString(),
				friendlyName,
				descSB.toString(), //description
				gtCode,
				strand,
				1,
				sampleInfoLHM.size(),
				sortedMarkerSetLHM.size(),
				chrSetLHM.size(),
				bimFilePath);

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		// <editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">

		//WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(sampleInfoLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//        ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeALToD2ArrayChar(samplesAL, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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


		//WRITE RSID & MARKERID METADATA FROM METADATALHM
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(sortedMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(sortedMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(sortedMarkerSetLHM, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(ncfile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		//Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(ncfile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);



		//WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(sortedMarkerSetLHM, 3);
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
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(sortedMarkerSetLHM, 4, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing forward alleles to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//WRITE GT STRAND FROM ANNOTATION FILE
		String strandFlag;
		switch (cNetCDF.Defaults.StrandType.compareTo(strand)) {
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
		for (Iterator<String> it = sortedMarkerSetLHM.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			sortedMarkerSetLHM.put(key, strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(sortedMarkerSetLHM, cNetCDF.Strides.STRIDE_STRAND);
		int[] gtOrig = new int[]{0, 0};
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
		System.out.println(org.gwaspi.global.Text.All.processing);
		Map<String, Object> bimMarkerSetLHM = markerSetLoader.parseOrigBimFile(bimFilePath); //key = markerId, values{allele1 (minor), allele2 (major)}
		loadBedGenotypes(new File(bedFilePath),
				ncfile,
				sortedMarkerSetLHM,
				bimMarkerSetLHM,
				sampleInfoLHM);

		System.out.println("Done writing genotypes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		// </editor-fold>

		// WRAP-UP FILE AND BY THIS, MAKE IT READABLE-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("Genotype encoding: ");
			descSB.append(guessedGTCode);
			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
					org.gwaspi.constants.cDBMatrix.T_MATRICES,
					new String[]{constants.cDBMatrix.f_DESCRIPTION},
					new Object[]{descSB.toString()},
					new String[]{constants.cDBMatrix.f_ID},
					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
			e.printStackTrace();
		}


		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public void loadBedGenotypes(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> wrMarkerIdSetLHM,
			Map<String, Object> bimMarkerSetLHM,
			Map<String, Object> sampleSetLHM) throws IOException, InvalidRangeException {

		FileInputStream bedFS = new FileInputStream(file);
		DataInputStream bedIS = new DataInputStream(bedFS);

		int sampleNb = sampleSetLHM.size();
		int markerNb = bimMarkerSetLHM.size();
		int bytesPerSNP = 0;
		if (sampleNb % 4 == 0) {  //Nb OF BYTES IN EACH ROW
			bytesPerSNP = sampleNb / 4;
		} else {
			bytesPerSNP = (int) (Math.floor(sampleNb / 4) + 1);
		}

		Iterator itMarkerSet = bimMarkerSetLHM.keySet().iterator();
		Iterator<String> itSampleSet = sampleSetLHM.keySet().iterator();

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
			ArrayList<byte[]> genotypesAL = new ArrayList();
			while (itMarkerSet.hasNext()) {
				try {
					////////////////// NEW SNP //////////////
					//PURGE LHM
					while (itSampleSet.hasNext()) {
						String key = itSampleSet.next();
						sampleSetLHM.put(key, cNetCDF.Defaults.DEFAULT_GT);
					}

					Object markerId = itMarkerSet.next();
					alleles = (String[]) bimMarkerSetLHM.get(markerId); //key = markerId, values{allele1 (minor), allele2 (major)}

					//READ ALL SAMPLE GTs FOR CURRENT SNP
					int check = bedIS.read(rowBytes, 0, bytesPerSNP);
					int bitsPerRow = 1;
					itSampleSet = sampleSetLHM.keySet().iterator();
					for (int j = 0; j < bytesPerSNP; j++) { //ITERATE THROUGH ROWS (READING BYTES PER ROW)
						byteData = rowBytes[j];
						for (int i = 0; i < 8; i = i + 2) {   //FOR EACH BYTE IN ROW, EAT 2 BITS AT A TIME
							if (bitsPerRow <= (sampleNb * 2)) {    //SKIP EXCESS BITS
								allele1 = getBit(byteData, i);
								allele2 = getBit(byteData, i + 1);
								sampleId = itSampleSet.next();
								if (!allele1 && !allele2) {     //  00  Homozygote "1"/"1" - Minor allele
									sampleSetLHM.put(sampleId, new byte[]{(byte) alleles[0].charAt(0), (byte) alleles[0].charAt(0)});
								} else if (allele1 && allele2) {     //  11  Homozygote "2"/"2" - Major allele
									sampleSetLHM.put(sampleId, new byte[]{(byte) alleles[1].charAt(0), (byte) alleles[1].charAt(0)});
								} else if (!allele1 && allele2) {     //  01  Heterozygote
									sampleSetLHM.put(sampleId, new byte[]{(byte) alleles[0].charAt(0), (byte) alleles[1].charAt(0)});
								} else if (allele1 && !allele2) {     //  10  Missing genotype
									sampleSetLHM.put(sampleId, cNetCDF.Defaults.DEFAULT_GT);
								}

								bitsPerRow = bitsPerRow + 2;
							}
						}
					}

					/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
					if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
						guessedGTCode = Utils.detectGTEncoding(sampleSetLHM);
					} else if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.O12)) {
						guessedGTCode = Utils.detectGTEncoding(sampleSetLHM);
					}


					//WRITING HYPERSLABS AT A TIME
					for (Iterator it = sampleSetLHM.keySet().iterator(); it.hasNext();) {
						Object key = it.next();
						byte[] value = (byte[]) sampleSetLHM.get(key);
						genotypesAL.add(value);
					}

					if (rowCounter != 1 && rowCounter % (hyperSlabRows) == 0) {
						ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeALValuesToSamplesHyperSlabArrayByteD3(genotypesAL, sampleSetLHM.size(), cNetCDF.Strides.STRIDE_GT);
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
			ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeALValuesToSamplesHyperSlabArrayByteD3(genotypesAL, sampleSetLHM.size(), cNetCDF.Strides.STRIDE_GT);
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
