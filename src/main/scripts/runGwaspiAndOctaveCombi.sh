#!/bin/bash

# change to this scripts directory
PREVIOUS_DIR="$(pwd)"
cd "$(dirname "$0")"
THIS_DIR="$(pwd)"
# NOTE This sets the bash random number generator to a fixed seed,
#      which is useful for debugging, but otherwise not required.
RANDOM=42
#RANDOM=$$

cd ../../..
GWASPI_BIN_DIR="$(pwd)"
cd ../..
GWASPI_ROOT_DIR="$(pwd)"

#INPUT_DIR="${GWASPI_ROOT_DIR}/var/data/tutorial/extractedNew/PLINK_format"
INPUT_DIR="${GWASPI_ROOT_DIR}/var/data/marius/example/extra"
#INPUT_DIR="${GWASPI_ROOT_DIR}/var/data/tutorial"
OUTPUT_DIR="/tmp/COMBI_output_${RANDOM}"
WORK_DIR="${OUTPUT_DIR}/workDir"

GWASPI_SCRIPT_TEMPLATE="${GWASPI_BIN_DIR}/src/test/resources/samples/gwaspiScript_loadPlink_hardyWeinberg_combi.txt"
# This next line will fail with spaces in the file-name.
# There is no problem with spaces in the path.
GWASPI_SCRIPT="${OUTPUT_DIR}/"$(basename "${GWASPI_SCRIPT_TEMPLATE}")
GWASPI_OUTPUT_P_VALUES="${OUTPUT_DIR}/p_values_gwaspi.txt"

OCTAVE_GIT_ROOT_DIR="${GWASPI_ROOT_DIR}/repos/GWAS_COMBI_OctaveMatlab"
OCTAVE_COMBI_DIR="${OCTAVE_GIT_ROOT_DIR}"
OCTAVE_COMBI_WORK_DIR="${OUTPUT_DIR}/"$(basename "${OCTAVE_COMBI_DIR}")
OCTAVE_RUN_SCRIPT="${OCTAVE_COMBI_WORK_DIR}/myRunScript.m"
OCTAVE_PARAMS_TEMPLATE="${OCTAVE_COMBI_DIR}/templates/combi_set_params.m"
OCTAVE_DATASET_TEMPLATE="${OCTAVE_COMBI_DIR}/templates/combi_run_with_plink_binary.m"
# This next line will fail with spaces in the file-name.
# There is no problem with spaces in the path.
OCTAVE_PARAMS="${OCTAVE_COMBI_WORK_DIR}/"$(basename "${OCTAVE_PARAMS_TEMPLATE}")
OCTAVE_DATASET="${OCTAVE_COMBI_WORK_DIR}/"$(basename "${OCTAVE_DATASET_TEMPLATE}")
OCTAVE_RUN_COMMAND=$(basename "${OCTAVE_DATASET}" ".m")
OCTAVE_OUTPUT_P_VALUES="${OUTPUT_DIR}/p_values_octave.txt"

export DATA_DIR="${WORK_DIR}"
#export INPUT_NAME="Tutorial_Matrix"
#export INPUT_NAME="Small_Tutorial_Matrix"
export INPUT_NAME="extra"
export INPUT_FILE_MAP="${INPUT_DIR}/${INPUT_NAME}.map"
export INPUT_FILE_PED="${INPUT_DIR}/${INPUT_NAME}.ped"
export INPUT_FILE_FAM="${INPUT_DIR}/${INPUT_NAME}.fam"
export INPUT_FILE_BIM="${INPUT_DIR}/${INPUT_NAME}.bim"
export INPUT_FILE_BED="${INPUT_DIR}/${INPUT_NAME}.bed"
export LOAD_SAMPLE_INFO_FILE=""
export USE_LIB_LINEAR=1
export SOLVER_LIBRARY_NAME=$(if [ ${USE_LIB_LINEAR} == 1 ]; then echo "LIB_LINEAR"; else echo "LIB_SVM"; fi)
export PER_CHROMOSOME=1
export GENOME_WIDE=$(expr ${PER_CHROMOSOME} \* -1 + 1) # invert: 0 -> 1, 1 -> 0
export GENOTYPE_ENCODING=nominal
#export GENOTYPE_ENCODING=genotypic
#export GENOTYPE_ENCODING_UC=$(echo ${GENOTYPE_ENCODING} | awk '{print toupper($0)}')
export GENOTYPE_ENCODING_P=6
export SVM_C=1
export SVM_EPS=1e-7
#export WEIGHTS_FILTER_WIDTH=35
export WEIGHTS_FILTER_WIDTH=3
export MARKERS_TO_KEEP_FRACTION=0.2

mkdir -p "${OUTPUT_DIR}"
mkdir -p "${WORK_DIR}"

if [ 1 == 1 ]; then # HACK
	envsubst < "${GWASPI_SCRIPT_TEMPLATE}" > "${GWASPI_SCRIPT}"

	cd "${GWASPI_BIN_DIR}"

	echo "COMBI GWASpi run in \"${OUTPUT_DIR}\"..."
	mvn exec:java \
		-Dexec.args="--memory --array-genotypes-lists --script \"${GWASPI_SCRIPT}\" " #\
	# TODO use the next 2 lines
	#	>  "${OUTPUT_DIR}/GWASpi_log_stdout.txt" \
	#	2> "${OUTPUT_DIR}/GWASpi_log_stderr.txt"
	GWASPI_EXIT_STATE=${?}
	echo "${GWASPI_EXIT_STATE}" > "${OUTPUT_DIR}/GWASpi_log_exit_status.txt"

	if [ ${GWASPI_EXIT_STATE} -eq 0 ]; then
	#	EXPORT_DIR="${WORK_DIR}/export/STUDY_1"
	#	EXPORTED_FILE_MAP="${EXPORTS_DIR}/myTestMatrixName.map"
	#	EXPORTED_FILE_PED="${EXPORTS_DIR}/myTestMatrixName.ped"
		REPORTS_DIR="${WORK_DIR}/reports/STUDY_1"
		REPORT_FILE_TREND_TEST="${REPORTS_DIR}/mx-1_TRENDTEST-10.txt"
		cat "${REPORT_FILE_TREND_TEST}" | awk '{if (NR != 1) {print $1 "\t" $8;}}' \
			> "${GWASPI_OUTPUT_P_VALUES}"
		echo "GWASpi output written to '${GWASPI_OUTPUT_P_VALUES}'"
	fi
	echo "done. (COMBI GWASpi run)"
	#exit 88
fi



cp -r "${OCTAVE_COMBI_DIR}" "${OCTAVE_COMBI_WORK_DIR}"

envsubst < "${OCTAVE_PARAMS_TEMPLATE}" > "${OCTAVE_PARAMS}"
envsubst < "${OCTAVE_DATASET_TEMPLATE}" > "${OCTAVE_DATASET}"

cd "${OCTAVE_COMBI_WORK_DIR}"

echo "${OCTAVE_RUN_COMMAND};" > "${OCTAVE_RUN_SCRIPT}"

echo "COMBI Octave run in \"${OUTPUT_DIR}\"..."
octave -f "${OCTAVE_RUN_SCRIPT}"
# TODO use the next 2 lines
#	>  "${OUTPUT_DIR}/octave_log_stdout.txt" \
#	2> "${OUTPUT_DIR}/octave_log_stderr.txt"
OCTAVE_EXIT_STATE=${?}
echo "${OCTAVE_EXIT_STATE}" > "${OUTPUT_DIR}/octave_log_exit_status.txt"

#if [ ${OCTAVE_EXIT_STATE} -eq 0 ]; then
	OCTAVE_OUTPUT_DIR="${OCTAVE_COMBI_WORK_DIR}/data/octave"
	OCTAVE_OUTPUT_FILE="${OCTAVE_OUTPUT_DIR}/result_combi_complete.csv"
	cat "${OCTAVE_OUTPUT_FILE}" | awk -F ',' '{if (NR != 1) {print $1 "\t" $2;}}' \
		> "${OCTAVE_OUTPUT_P_VALUES}"
	echo "Octave output written to '${OCTAVE_OUTPUT_P_VALUES}'"
#fi
echo "done. (COMBI Octave run)"

