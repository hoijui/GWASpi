#!/bin/sh

if [ ! -f ./README.markdown ]; then
	# current dir is not the project root
	# change to the project root
	cd $(dirname $0)
	cd ../../..
fi

markdownFiles="README.markdown $(ls src/main/resources/doc/*.markdown) $(ls src/main/resources/doc/*.md)"
outputDir=$(pwd)/target

for markdownFile in ${markdownFiles}; do
	htmlFile=${outputDir}/$(basename --suffix ".md" $(basename --suffix ".markdown" "${markdownFile}")).html
	echo "Converting '${markdownFile}' to '${htmlFile}' ..."
	pandoc --toc -o "${htmlFile}"  "${markdownFile}" 
done
echo "done converting."

