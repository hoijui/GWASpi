package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.CensusMethod.CensusDecision;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OP_MarkerCensus_opt {

	public static int processMatrix(int _rdMatrixId,
			String censusName,
			Operation sampleQAOP,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			Operation markerQAOP,
			boolean discardMismatches,
			double markerMissingRatio,
			File phenoFile) throws IOException, InvalidRangeException {

		int resultOpId = Integer.MIN_VALUE;

//        LinkedHashMap wrMarkerSetCensusLHM = new LinkedHashMap();
//        LinkedHashMap wrMarkerSetKnownAllelesLHM = new LinkedHashMap();

		//<editor-fold defaultstate="collapsed" desc="PICKING CLEAN MARKERS AND SAMPLES FROM QA">

		OperationMetadata markerQAMetadata = new OperationMetadata(markerQAOP.getOperationId());
		NetcdfFile rdMarkerQANcFile = NetcdfFile.open(markerQAMetadata.getPathToMatrix());

		OperationMetadata sampleQAMetadata = new OperationMetadata(sampleQAOP.getOperationId());
		NetcdfFile rdSampleQANcFile = NetcdfFile.open(sampleQAMetadata.getPathToMatrix());

		OperationSet rdQAMarkerSet = new OperationSet(markerQAMetadata.getStudyId(), markerQAMetadata.getOPId());
		OperationSet rdQASampleSet = new OperationSet(sampleQAMetadata.getStudyId(), sampleQAMetadata.getOPId());
		LinkedHashMap rdQAMarkerSetLHM = rdQAMarkerSet.getOpSetLHM();
		LinkedHashMap rdQASampleSetLHM = rdQASampleSet.getOpSetLHM();
		LinkedHashMap excludeMarkerSetLHM = new LinkedHashMap();
		LinkedHashMap excludeSampleSetLHM = new LinkedHashMap();

		int totalSampleNb = rdQASampleSetLHM.size();
		int totalMarkerNb = rdQAMarkerSetLHM.size();

		//EXCLUDE MARKER BY MISMATCH STATE
		if (discardMismatches) {
			rdQAMarkerSetLHM = rdQAMarkerSet.fillOpSetLHMWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

			for (Iterator it = rdQAMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = rdQAMarkerSetLHM.get(key);
				if (value.equals(cNetCDF.Defaults.DEFAULT_MISMATCH_YES)) {
					excludeMarkerSetLHM.put(key, value);
				}
			}
		}

		//EXCLUDE MARKER BY MISSING RATIO
		rdQAMarkerSetLHM = rdQAMarkerSet.fillOpSetLHMWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

		for (Iterator it = rdQAMarkerSetLHM.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			double value = (Double) rdQAMarkerSetLHM.get(key);
			if (value > markerMissingRatio) {
				excludeMarkerSetLHM.put(key, value);
			}
		}

		//EXCLUDE SAMPLE BY MISSING RATIO
		rdQASampleSetLHM = rdQASampleSet.fillOpSetLHMWithVariable(rdSampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

		if (rdQASampleSetLHM != null) {
			int brgl = 0;
			for (Iterator it = rdQASampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				double value = (Double) rdQASampleSetLHM.get(key);
				if (value > sampleMissingRatio) {
					excludeSampleSetLHM.put(key, value);
				}
				brgl++;
			}
		}

		//EXCLUDE SAMPLE BY HETEROZYGOSITY RATIO
		rdQASampleSetLHM = rdQASampleSet.fillOpSetLHMWithVariable(rdSampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);

		if (rdQASampleSetLHM != null) {
			int brgl = 0;
			for (Iterator it = rdQASampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				double value = (Double) rdQASampleSetLHM.get(key);
				if (value > sampleHetzygRatio) {
					excludeSampleSetLHM.put(key, value);
				}
				brgl++;
			}
		}

		if (rdQAMarkerSetLHM != null) {
			rdQAMarkerSetLHM.clear();
		}
		if (rdQASampleSetLHM != null) {
			rdQASampleSetLHM.clear();
		}
		rdSampleQANcFile.close();
		rdMarkerQANcFile.close();

		//</editor-fold>

		if (excludeSampleSetLHM.size() < totalSampleNb
				&& excludeMarkerSetLHM.size() < totalMarkerNb) { //CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING

			//<editor-fold defaultstate="collapsed" desc="PURGE LHMs">
			int rdMatrixId = _rdMatrixId;
			MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdMatrixId);

			NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

			MarkerSet_opt rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
			rdMarkerSet.initFullMarkerIdSetLHM();
			rdMarkerSet.fillInitLHMWithMyValue(cNetCDF.Defaults.DEFAULT_GT);

			LinkedHashMap wrMarkerSetLHM = new LinkedHashMap();
			wrMarkerSetLHM.putAll(rdMarkerSet.getMarkerIdSetLHM());


			SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
			LinkedHashMap rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();
			LinkedHashMap wrSampleSetLHM = new LinkedHashMap();
			for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				if (!excludeSampleSetLHM.containsKey(key)) {
					wrSampleSetLHM.put(key, cNetCDF.Defaults.DEFAULT_GT);
				}
			}
			//</editor-fold>

			NetcdfFileWriteable wrNcFile = null;
			try {
				///////////// CREATE netCDF-3 FILE ////////////
				cNetCDF.Defaults.OPType opType = cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION;

				String description = "Genotype frequency count -" + censusName + "- on " + rdMatrixMetadata.getMatrixFriendlyName();
				if (phenoFile != null) {
					description += "\nCase/Control status read from file: " + phenoFile.getPath();
					opType = cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE;
				}
				OperationFactory wrOPHandler = new OperationFactory(rdMatrixMetadata.getStudyId(),
						"Genotypes freq. - " + censusName, //friendly name
						description + "\nSample missing ratio threshold: " + sampleMissingRatio + "\nSample heterozygosity ratio threshold: " + sampleHetzygRatio + "\nMarker missing ratio threshold: " + markerMissingRatio + "\nDiscard mismatching Markers: " + discardMismatches + "\nMarkers: " + wrMarkerSetLHM.size() + "\nSamples: " + wrSampleSetLHM.size(), //description
						wrMarkerSetLHM.size(),
						wrSampleSetLHM.size(),
						0,
						opType.toString(),
						rdMatrixMetadata.getMatrixId(), //Parent matrixId
						-1);       //Parent operationId

				wrNcFile = wrOPHandler.getNetCDFHandler();
				try {
					wrNcFile.create();
				} catch (IOException e) {
					System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
				}

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
				//MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
				} catch (IOException e) {
					System.err.println("ERROR writing file");
				} catch (InvalidRangeException e) {
					e.printStackTrace();
				}

				//MARKERSET RSID
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
				for (Iterator it = wrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object value = rdMarkerSet.getMarkerIdSetLHM().get(key);
					wrMarkerSetLHM.put(key, value);
				}
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);


				//WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
				ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(wrSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
				} catch (IOException e) {
					System.err.println("ERROR writing file");
				} catch (InvalidRangeException e) {
					e.printStackTrace();
				}
				samplesD2 = null;
				System.out.println("Done writing Sample Set to operation at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//</editor-fold>


				//<editor-fold defaultstate="collapsed" desc="PROCESSOR">

				//<editor-fold defaultstate="collapsed" desc="GET SAMPLES INFO">
				LinkedHashMap samplesInfoLHM;
				List<Map<String, Object>> rsSamplesInfo = org.gwaspi.samples.SampleManager.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyId());
				if (phenoFile == null) {
					samplesInfoLHM = new LinkedHashMap();
					int count = 0;
					while (count < rsSamplesInfo.size()) {
						//PREVENT PHANTOM-DB READS EXCEPTIONS
						if (!rsSamplesInfo.isEmpty() && rsSamplesInfo.get(count).size() == org.gwaspi.constants.cDBSamples.T_CREATE_SAMPLES_INFO.length) {
							String tempSampleId = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_SAMPLE_ID).toString();
							if (wrSampleSetLHM.containsKey(tempSampleId)) {
								String sex = "0";
								String affection = "0";
								Object tmpSex = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_SEX);
								Object tmpAffection = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_AFFECTION);
								if (tmpSex != null) {
									sex = tmpSex.toString();
								}
								if (tmpAffection != null) {
									affection = tmpAffection.toString();
								}
								String[] info = new String[]{sex, affection};
								samplesInfoLHM.put(tempSampleId, info);

								//samplesInfoLHM.put(tempSampleId, affection);
							}
						}
						count++;
					}
				} else {
					FileReader phenotypeFR = new FileReader(phenoFile); //Pheno file has SampleInfo format!
					BufferedReader phenotypeBR = new BufferedReader(phenotypeFR);
					samplesInfoLHM = new LinkedHashMap();

					String header = phenotypeBR.readLine(); //ignore header block
					String l;
					while ((l = phenotypeBR.readLine()) != null) {

						String[] cVals = l.split(org.gwaspi.constants.cImport.Separators.separators_CommaSpaceTab_rgxp);
						String[] info = new String[]{cVals[GWASpi.sex], cVals[GWASpi.affection]};
						samplesInfoLHM.put(cVals[1], info);

						//samplesInfoLHM.put(cVals[0], cVals[1]);
					}
					//CHECK IF THERE ARE MISSING SAMPLES IN THE PHENO PHILE
					for (Iterator it = wrSampleSetLHM.keySet().iterator(); it.hasNext();) {
						Object sampleId = it.next();
						if (!samplesInfoLHM.containsKey(sampleId)) {
							String sex = "0";
							String affection = "0";
							int count = 0;
							boolean seeking = true;
							while (count < rsSamplesInfo.size() && seeking) {
								Object tmpSampleId = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_SAMPLE_ID);
								if (tmpSampleId.equals(sampleId)) {
									Object tmpSex = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_SEX);
									Object tmpAffection = rsSamplesInfo.get(count).get(org.gwaspi.constants.cDBSamples.f_AFFECTION);
									if (tmpSex != null) {
										sex = tmpSex.toString();
									}
									if (tmpAffection != null) {
										affection = tmpAffection.toString();
									}
									seeking = false;
								}
								count++;
							}
							String[] info = new String[]{sex, affection};
							samplesInfoLHM.put(sampleId, info);
						}
					}

				}
				//</editor-fold>


				//Iterate through markerset, take it marker by marker
				rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				//INIT wrSampleSetLHM with indexing order and chromosome info
				int idx = 0;
				for (Iterator it = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					if (wrMarkerSetLHM.containsKey(key)) {
						String chr = rdMarkerSet.getMarkerIdSetLHM().get(key).toString();
						Object[] markerInfo = new Object[]{idx, chr};
						wrMarkerSetLHM.put(key, markerInfo);
					}
					//rdMarkerIdSetIndex.put(key, idx);
					idx++;
				}
				if (rdMarkerSet.getMarkerIdSetLHM() != null) {
					rdMarkerSet.getMarkerIdSetLHM().clear();
				}

				System.out.println(org.gwaspi.global.Text.All.processing);

				int countMarkers = 0;
				int chunkSize = Math.round(org.gwaspi.gui.StartGWASpi.maxProcessMarkers / 4);
				if (chunkSize > 500000) {
					chunkSize = 500000; //We want to keep things manageable for RAM
				}
				if (chunkSize < 10000 && org.gwaspi.gui.StartGWASpi.maxProcessMarkers > 10000) {
					chunkSize = 10000; //But keep LHM size sensible
				}
				int countChunks = 0;

				LinkedHashMap wrChunkedMarkerCensusLHM = new LinkedHashMap();
				LinkedHashMap wrChunkedKnownAllelesLHM = new LinkedHashMap();
				for (Iterator it = wrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object markerId = it.next();

					if (countMarkers % chunkSize == 0) {

						if (countMarkers > 0) {

							//<editor-fold defaultstate="collapsed" desc="CENSUS DATA WRITER">

							//KNOWN ALLELES
							Utils.saveCharChunkedLHMToWrMatrix(wrNcFile,
									wrChunkedKnownAllelesLHM,
									cNetCDF.Variables.VAR_ALLELES,
									cNetCDF.Strides.STRIDE_GT,
									countChunks * chunkSize);


							//ALL CENSUS
							int[] columns = new int[]{0, 1, 2, 3};
							Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
									wrChunkedMarkerCensusLHM,
									columns,
									cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL,
									countChunks * chunkSize);


							//CASE CENSUS
							columns = new int[]{4, 5, 6};
							Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
									wrChunkedMarkerCensusLHM,
									columns,
									cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE,
									countChunks * chunkSize);


							//CONTROL CENSUS
							columns = new int[]{7, 8, 9};
							Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
									wrChunkedMarkerCensusLHM,
									columns,
									cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL,
									countChunks * chunkSize);

							//ALTERNATE HW CENSUS
							columns = new int[]{10, 11, 12};
							Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
									wrChunkedMarkerCensusLHM,
									columns,
									cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW,
									countChunks * chunkSize);
							//</editor-fold>

							countChunks++;
						}
						wrChunkedMarkerCensusLHM = new LinkedHashMap();
						wrChunkedKnownAllelesLHM = new LinkedHashMap();
						System.gc(); //Try to garbage collect here

					}
					wrChunkedMarkerCensusLHM.put(markerId, "");
					countMarkers++;


					LinkedHashMap knownAlleles = new LinkedHashMap();
					LinkedHashMap allSamplesGTsTable = new LinkedHashMap();
					LinkedHashMap caseSamplesGTsTable = new LinkedHashMap();
					LinkedHashMap ctrlSamplesGTsTable = new LinkedHashMap();
					LinkedHashMap hwSamplesGTsTable = new LinkedHashMap();
					LinkedHashMap allSamplesContingencyTable = new LinkedHashMap();
					LinkedHashMap caseSamplesContingencyTable = new LinkedHashMap();
					LinkedHashMap ctrlSamplesContingencyTable = new LinkedHashMap();
					LinkedHashMap hwSamplesContingencyTable = new LinkedHashMap();
					Integer missingCount = 0;

					//Get a sampleset-full of GTs
					//int markerNb = (Integer) rdMarkerIdSetIndex.get(markerId);
					Object[] markerInfo = (Object[]) wrMarkerSetLHM.get(markerId);
					int markerNb = Integer.parseInt(markerInfo[0].toString());
					String markerChr = markerInfo[1].toString();

					rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerNb);
					for (Iterator it2 = wrSampleSetLHM.keySet().iterator(); it2.hasNext();) {

						Object sampleId = it2.next();
						String[] sampleInfo = (String[]) samplesInfoLHM.get(sampleId);

						//<editor-fold defaultstate="collapsed" desc="THE DECIDER">
						CensusDecision decision = CensusDecision.getDecisionByChrAndSex(markerChr, sampleInfo[0]);

						float counter = 1;
//                    if(decision == CensusDecision.CountMalesNonAutosomally){
//                        counter = 0.5f;
//                    }
						//</editor-fold>


						//<editor-fold defaultstate="collapsed" desc="SUMMING SAMPLESET GENOTYPES">
						byte[] tempGT = (byte[]) rdSampleSetLHM.get(sampleId);
						//Gather alleles different from 0 into a list of known alleles and count the number of appearences
						if (tempGT[0] != AlleleBytes._0) {
							float tempCount = 0;
							if (knownAlleles.containsKey(tempGT[0])) {
								tempCount = (Float) knownAlleles.get(tempGT[0]);
							}
							knownAlleles.put(tempGT[0], tempCount + counter);
						}
						if (tempGT[1] != AlleleBytes._0) {
							float tempCount = 0;
							if (knownAlleles.containsKey(tempGT[1])) {
								tempCount = (Float) knownAlleles.get(tempGT[1]);
							}
							knownAlleles.put(tempGT[1], tempCount + counter);
						}
						if (tempGT[0] == AlleleBytes._0 && tempGT[1] == AlleleBytes._0) {
							missingCount++;
						}

						int intAllele1 = tempGT[0];
						int intAllele2 = tempGT[1];
						Integer intAlleleSum = intAllele1 + intAllele2; //2 alleles per GT



						//CASE/CONTROL CENSUS
						float tempCount = 0;
						if (allSamplesGTsTable.containsKey(intAlleleSum)) {
							tempCount = (Float) allSamplesGTsTable.get(intAlleleSum);
						}
						allSamplesGTsTable.put(intAlleleSum, tempCount + counter);

						//if(affection.equals("2")){ //CASE
						if (sampleInfo[1].equals("2")) { //CASE
							tempCount = 0;
							if (caseSamplesGTsTable.containsKey(intAlleleSum)) {
								tempCount = (Float) caseSamplesGTsTable.get(intAlleleSum);
							}
							caseSamplesGTsTable.put(intAlleleSum, tempCount + counter);
						} else if (sampleInfo[1].equals("1")) { //CONTROL
							tempCount = 0;
							if (ctrlSamplesGTsTable.containsKey(intAlleleSum)) {
								tempCount = (Float) ctrlSamplesGTsTable.get(intAlleleSum);
							}
							ctrlSamplesGTsTable.put(intAlleleSum, tempCount + counter);

							//HARDY WEINBERG COUNTER
							if (hwSamplesGTsTable.containsKey(intAlleleSum)) {
								tempCount = (Float) hwSamplesGTsTable.get(intAlleleSum);
							}
							if (decision == CensusDecision.CountMalesNonAutosomally) {
								hwSamplesGTsTable.put(intAlleleSum, tempCount);
							}
							if (decision == CensusDecision.CountFemalesNonAutosomally) {
								hwSamplesGTsTable.put(intAlleleSum, tempCount);
							}
							if (decision == CensusDecision.CountAutosomally) {
								hwSamplesGTsTable.put(intAlleleSum, tempCount + counter);
							}

						}
						//</editor-fold>

					}

					//AFFECTION ALLELE CENSUS + MISMATCH STATE + MISSINGNESS
					if (knownAlleles.size() <= 2) { //Check if there are mismatches in alleles

						//<editor-fold defaultstate="collapsed" desc="KNOW YOUR ALLELES">
						ArrayList AAnumValsAL = new ArrayList();
						ArrayList AanumValsAL = new ArrayList();
						ArrayList aanumValsAL = new ArrayList();

						Iterator itKnAll = knownAlleles.keySet().iterator();
						if (knownAlleles.size() == 1) { //Homozygote (AA or aa)
							byte key = (Byte) itKnAll.next();
							int intAllele1 = (int) key;
							AAnumValsAL.add(intAllele1); //Single A
							AAnumValsAL.add(intAllele1 * 2); //Double AA
						}
						if (knownAlleles.size() == 2) { //Heterezygote (AA, Aa or aa)
							byte key = (Byte) itKnAll.next();
							int countA = Math.round((Float) knownAlleles.get(key));
							int intAllele1 = (int) key;
							key = (Byte) itKnAll.next();
							int countB = Math.round((Float) knownAlleles.get(key));
							int intAllele2 = (int) key;

							if (countA >= countB) { //Finding out what allele is major and minor
								AAnumValsAL.add(intAllele1);
								AAnumValsAL.add(intAllele1 * 2);

								aanumValsAL.add(intAllele2);
								aanumValsAL.add(intAllele2 * 2);

								AanumValsAL.add(intAllele1 + intAllele2);
							} else {
								AAnumValsAL.add(intAllele2);
								AAnumValsAL.add(intAllele2 * 2);

								aanumValsAL.add(intAllele1);
								aanumValsAL.add(intAllele1 * 2);

								AanumValsAL.add(intAllele1 + intAllele2);
							}
						}
						//</editor-fold>

						//<editor-fold defaultstate="collapsed" desc="CONTINGENCY ALL SAMPLES">
						for (Iterator itUnqGT = allSamplesGTsTable.keySet().iterator(); itUnqGT.hasNext();) {
							Object key = itUnqGT.next();
							Integer value = Math.round((Float) allSamplesGTsTable.get(key));

							if (AAnumValsAL.contains(key)) { //compare to all possible character values of AA
								//ALL CENSUS
								int tempCount = 0;
								if (allSamplesContingencyTable.containsKey("AA")) {
									tempCount = (Integer) allSamplesContingencyTable.get("AA");
								}
								allSamplesContingencyTable.put("AA", tempCount + value);
							}
							if (AanumValsAL.contains(key)) { //compare to all possible character values of Aa
								//ALL CENSUS
								int tempCount = 0;
								if (allSamplesContingencyTable.containsKey("Aa")) {
									tempCount = (Integer) allSamplesContingencyTable.get("Aa");
								}
								allSamplesContingencyTable.put("Aa", tempCount + value);
							}
							if (aanumValsAL.contains(key)) { //compare to all possible character values of aa
								//ALL CENSUS
								int tempCount = 0;
								if (allSamplesContingencyTable.containsKey("aa")) {
									tempCount = (Integer) allSamplesContingencyTable.get("aa");
								}
								allSamplesContingencyTable.put("aa", tempCount + value);
							}
						}
						//</editor-fold>

						//<editor-fold defaultstate="collapsed" desc="CONTINGENCY CASE SAMPLES">
						for (Iterator itUnqGT = caseSamplesGTsTable.keySet().iterator(); itUnqGT.hasNext();) {
							Object key = itUnqGT.next();
							Integer value = Math.round((Float) caseSamplesGTsTable.get(key));

							if (AAnumValsAL.contains(key)) { //compare to all possible character values of AA
								//ALL CENSUS
								int tempCount = 0;
								if (caseSamplesContingencyTable.containsKey("AA")) {
									tempCount = (Integer) caseSamplesContingencyTable.get("AA");
								}
								caseSamplesContingencyTable.put("AA", tempCount + value);
							}
							if (AanumValsAL.contains(key)) { //compare to all possible character values of Aa
								//ALL CENSUS
								int tempCount = 0;
								if (caseSamplesContingencyTable.containsKey("Aa")) {
									tempCount = (Integer) caseSamplesContingencyTable.get("Aa");
								}
								caseSamplesContingencyTable.put("Aa", tempCount + value);
							}
							if (aanumValsAL.contains(key)) { //compare to all possible character values of aa
								//ALL CENSUS
								int tempCount = 0;
								if (caseSamplesContingencyTable.containsKey("aa")) {
									tempCount = (Integer) caseSamplesContingencyTable.get("aa");
								}
								caseSamplesContingencyTable.put("aa", tempCount + value);
							}
						}
						//</editor-fold>

						//<editor-fold defaultstate="collapsed" desc="CONTINGENCY CTRL SAMPLES">
						for (Iterator itUnqGT = ctrlSamplesGTsTable.keySet().iterator(); itUnqGT.hasNext();) {
							Object key = itUnqGT.next();
							Integer value = Math.round((Float) ctrlSamplesGTsTable.get(key));

							if (AAnumValsAL.contains(key)) { //compare to all possible character values of AA
								//ALL CENSUS
								int tempCount = 0;
								if (ctrlSamplesContingencyTable.containsKey("AA")) {
									tempCount = (Integer) ctrlSamplesContingencyTable.get("AA");
								}
								ctrlSamplesContingencyTable.put("AA", tempCount + value);
							}
							if (AanumValsAL.contains(key)) { //compare to all possible character values of Aa
								//ALL CENSUS
								int tempCount = 0;
								if (ctrlSamplesContingencyTable.containsKey("Aa")) {
									tempCount = (Integer) ctrlSamplesContingencyTable.get("Aa");
								}
								ctrlSamplesContingencyTable.put("Aa", tempCount + value);
							}
							if (aanumValsAL.contains(key)) { //compare to all possible character values of aa
								//ALL CENSUS
								int tempCount = 0;
								if (ctrlSamplesContingencyTable.containsKey("aa")) {
									tempCount = (Integer) ctrlSamplesContingencyTable.get("aa");
								}
								ctrlSamplesContingencyTable.put("aa", tempCount + value);
							}
						}
						//</editor-fold>

						//<editor-fold defaultstate="collapsed" desc="CONTINGENCY HW SAMPLES">
						for (Iterator itUnqGT = hwSamplesGTsTable.keySet().iterator(); itUnqGT.hasNext();) {
							Object key = itUnqGT.next();
							Integer value = Math.round((Float) hwSamplesGTsTable.get(key));

							if (AAnumValsAL.contains(key)) { //compare to all possible character values of AA
								//HW CENSUS
								int tempCount = 0;
								if (hwSamplesContingencyTable.containsKey("AA")) {
									tempCount = (Integer) hwSamplesContingencyTable.get("AA");
								}
								hwSamplesContingencyTable.put("AA", tempCount + value);
							}
							if (AanumValsAL.contains(key)) { //compare to all possible character values of Aa
								//HW CENSUS
								int tempCount = 0;
								if (hwSamplesContingencyTable.containsKey("Aa")) {
									tempCount = (Integer) hwSamplesContingencyTable.get("Aa");
								}
								hwSamplesContingencyTable.put("Aa", tempCount + value);
							}
							if (aanumValsAL.contains(key)) { //compare to all possible character values of aa
								//HW CENSUS
								int tempCount = 0;
								if (hwSamplesContingencyTable.containsKey("aa")) {
									tempCount = (Integer) hwSamplesContingencyTable.get("aa");
								}
								hwSamplesContingencyTable.put("aa", tempCount + value);
							}
						}
						//</editor-fold>


						//CENSUS
						int obsAllAA = 0;
						int obsAllAa = 0;
						int obsAllaa = 0;
						int obsCaseAA = 0;
						int obsCaseAa = 0;
						int obsCaseaa = 0;
						int obsCntrlAA = 0;
						int obsCntrlAa = 0;
						int obsCntrlaa = 0;
						int obsHwAA = 0;
						int obsHwAa = 0;
						int obsHwaa = 0;
						if (allSamplesContingencyTable.containsKey("AA")) {
							obsAllAA = (Integer) allSamplesContingencyTable.get("AA");
						}
						if (allSamplesContingencyTable.containsKey("Aa")) {
							obsAllAa = (Integer) allSamplesContingencyTable.get("Aa");
						}
						if (allSamplesContingencyTable.containsKey("aa")) {
							obsAllaa = (Integer) allSamplesContingencyTable.get("aa");
						}
						if (caseSamplesContingencyTable.containsKey("AA")) {
							obsCaseAA = (Integer) caseSamplesContingencyTable.get("AA");
						}
						if (caseSamplesContingencyTable.containsKey("Aa")) {
							obsCaseAa = (Integer) caseSamplesContingencyTable.get("Aa");
						}
						if (caseSamplesContingencyTable.containsKey("aa")) {
							obsCaseaa = (Integer) caseSamplesContingencyTable.get("aa");
						}
						if (ctrlSamplesContingencyTable.containsKey("AA")) {
							obsCntrlAA = (Integer) ctrlSamplesContingencyTable.get("AA");
						}
						if (ctrlSamplesContingencyTable.containsKey("Aa")) {
							obsCntrlAa = (Integer) ctrlSamplesContingencyTable.get("Aa");
						}
						if (ctrlSamplesContingencyTable.containsKey("aa")) {
							obsCntrlaa = (Integer) ctrlSamplesContingencyTable.get("aa");
						}
						if (hwSamplesContingencyTable.containsKey("AA")) {
							obsHwAA = (Integer) hwSamplesContingencyTable.get("AA");
						}
						if (hwSamplesContingencyTable.containsKey("Aa")) {
							obsHwAa = (Integer) hwSamplesContingencyTable.get("Aa");
						}
						if (hwSamplesContingencyTable.containsKey("aa")) {
							obsHwaa = (Integer) hwSamplesContingencyTable.get("aa");
						}

						int[] census = new int[13];

						census[0] = obsAllAA; //all
						census[1] = obsAllAa; //all
						census[2] = obsAllaa; //all
						census[3] = missingCount; //all
						census[4] = obsCaseAA; //case
						census[5] = obsCaseAa; //case
						census[6] = obsCaseaa; //case
						census[7] = obsCntrlAA; //control
						census[8] = obsCntrlAa; //control
						census[9] = obsCntrlaa; //control
						census[10] = obsHwAA; //HW samples
						census[11] = obsHwAa; //HW samples
						census[12] = obsHwaa; //HW samples

						wrChunkedMarkerCensusLHM.put(markerId, census);

						StringBuilder sb = new StringBuilder();
						byte[] alleles = org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT;
						Iterator knit = knownAlleles.keySet().iterator();
						if (knownAlleles.size() == 2) {
							Byte allele1 = (Byte) knit.next();
							Byte allele2 = (Byte) knit.next();
							alleles = new byte[]{allele1, allele2};
						}
						if (knownAlleles.size() == 1) {
							Byte allele1 = (Byte) knit.next();
							alleles = new byte[]{allele1, allele1};
						}
						sb.append(new String(alleles));




						wrChunkedKnownAllelesLHM.put(markerId, sb.toString());
					} else {
						//MISMATCHES FOUND
						int[] census = new int[10];
						wrChunkedMarkerCensusLHM.put(markerId, census);
						wrChunkedKnownAllelesLHM.put(markerId, "00");
					}

					if (markerNb != 0 && markerNb % 100000 == 0) {
						System.out.println("Processed markers: " + markerNb + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
					}

				}
				//</editor-fold>


				//<editor-fold defaultstate="collapsed" desc="LAST CENSUS DATA WRITER">

				//KNOWN ALLELES
				Utils.saveCharChunkedLHMToWrMatrix(wrNcFile,
						wrChunkedKnownAllelesLHM,
						cNetCDF.Variables.VAR_ALLELES,
						cNetCDF.Strides.STRIDE_GT,
						countChunks * chunkSize);


				//ALL CENSUS
				int[] columns = new int[]{0, 1, 2, 3};
				Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
						wrChunkedMarkerCensusLHM,
						columns,
						cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL,
						countChunks * chunkSize);


				//CASE CENSUS
				columns = new int[]{4, 5, 6};
				Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
						wrChunkedMarkerCensusLHM,
						columns,
						cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE,
						countChunks * chunkSize);


				//CONTROL CENSUS
				columns = new int[]{7, 8, 9};
				Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
						wrChunkedMarkerCensusLHM,
						columns,
						cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL,
						countChunks * chunkSize);

				//ALTERNATE HW CENSUS
				columns = new int[]{10, 11, 12};
				Utils.saveIntChunkedLHMD2ToWrMatrix(wrNcFile,
						wrChunkedMarkerCensusLHM,
						columns,
						cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW,
						countChunks * chunkSize);
				//</editor-fold>


				resultOpId = wrOPHandler.getResultOPId();
			} catch (InvalidRangeException invalidRangeException) {
			} catch (IOException iOException) {
			} finally {
				if (null != rdNcFile) {
					try {
						rdNcFile.close();
						wrNcFile.close();
					} catch (IOException ioe) {
						System.err.println("Cannot close file: " + ioe);
					}
				}

				org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
			}
		} else {    //NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			System.out.println(org.gwaspi.global.Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultOpId;
	}

	protected void writeLHMToMatrix() {
	}
}
