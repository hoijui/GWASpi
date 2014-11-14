# GWASpi tutorial for the COMBI method

A tutorial listing the minimal steps for a GWASpi GUI workflow ending in the use of the COMBI method.

* Run GWASpi in in-memory mode with lots of RAM (2GB initial, 3GB maximum).

		java -Xms2g -Xmx3g -jar gwaspi-*-jar-with-dependencies*.jar --memory

* Click [OK] in the "Information" dialog.
* Click on "Study Management" in the application tree on the left.
* In the "Study Name" field, write "sss".
* Click on the [Add Study] button.
* Click on the [Load Genotype Data] button.
* In the "New Matrix Name" field, write "mmm".
* In the "Format" field, choose "PLINK".
* Behind the "MAP File" field, click on the [Browse] button, and select the "Tutorial\_Matrix.map" file.
* Click on the [Go!] button.
* Click on the [No, just load data] button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* In the application tree, click on "MX: 1 - mmm".
* Click on the [Analyse Data] button.
* Click on the [Genotype freq. & Hardy-Weinberg QA] button.
* Click on the [Current Case/Control Affection from DB] button.
* Click on the [Go!] button.
* In the text field, write "ggg".
* Click on the [OK] button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* Click on the [Analyse Data] button.
* Click on the [COMBI Association Test] button.
* Click on the [OK] button.
* Click on the [OK] button.
* Click on the [OK] button.
* Wait till the process is finished ("Activity" == "DONE")
* Click on the "GWASpi Management" tab.
* In the application tree, under "OP: 10 - Cochr..." click on the different reports ("RP: ...") to see the result.

