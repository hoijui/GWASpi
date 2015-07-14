# GWASpi tutorial for the COMBI method

A tutorial listing the minimal steps for a GWASpi GUI workflow ending in the use of the COMBI method.

* Download latest GWASpi testing version from
	[the bitbucket repo](https://bitbucket.org/gwas_combi/gwaspi/downloads)
* Run GWASpi in in-memory mode with lots of RAM (2GB initial, 3GB maximum).

		java -Xms2g -Xmx3g -jar gwaspi-*-jar-with-dependencies*.jar --memory

* Click _[OK]_ in the "Information" dialog.
* Click on "Study Management" in the application tree on the left.
* In the "Study Name" field, write "sss".
* Click on the _[Add Study]_ button.
* Click on the _[Load Genotype Data]_ button.
* In the "New Matrix Name" field, write "mmm".
* In the "Format" field, choose "PLINK".
* Behind the "MAP File" field, click on the [Browse] button, and select the "Tutorial\_Matrix.map" file.
* Click on the _[Go!]_ button.
* Click on the _[No, just load data]_ button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* In the application tree, click on "MX: 1 - mmm".
* Click on the _[Analyse Data]_ button.
* Click on the _[Genotype freq. & Hardy-Weinberg QA]_ button.
* Click on the _[Current Case/Control Affection from DB]_ button.
* Click on the _[Go!]_ button.
* In the text field, write "ggg".
* Click on the _[OK]_ button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* Click on the _[Analyse Data]_ button.
* Click on the _[COMBI Association Test]_ button.
* Click on the _[OK]_ button.
* Click on the _[OK]_ button.
* Click on the _[OK]_ button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* In the application tree, under "OP: 10 - Cochr...",
	click on the different reports ("RP: ...") to see the result.

