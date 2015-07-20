# Genome-wide Association Studies Pipeline

__README__


## What is GWASpi?

* A tool to perform Genome-Wide Association Studies
* Written in Java, with Apache Derby, JFreeChart and NetCDF 3 technology
* Executable locally in a GUI (Graphical User Interface) and via command line


## Getting Started

1. Obtain GWASpi
	* Get the __latest stable version__ from the [GWASpi homepage _Download_ section](http://www.gwaspi.org/?page_id=182), or
	* Get the __latest testing version__ from the [source hosting platform _Download_ section](https://bitbucket.org/gwas_combi/gwaspi/downloads), or
	* Get the __latest development version__ by [building it yourself](#building) from source
2. Obtain data
	* From the [_Tutorial_ section](http://www.gwaspi.org/?page_id=226) on the GWASpi homepage
	* From any other online source providing GWAS input data
3. Follow a tutorial
	* For general GWASpi usage, see the [_Introduction_](http://www.gwaspi.org/?page_id=213)
		and the [_Tutorial_](http://www.gwaspi.org/?page_id=226) sections on the GWASpi homepage
	* For COMBI instructions, see the (notoriously detailed) [COMBI tutorial (local)](TUTORIAL.COMBI.html)
		[(online)](https://bitbucket.org/gwas_combi/gwaspi/src/develop/src/main/resources/doc/TUTORIAL.COMBI.markdown)
		(or its [Markdown version (local)](src/main/resources/doc/TUTORIAL.COMBI.md))
	* or just try clicking around!


## Building

Maven is used as the project management system.
The project description file is `pom.xml`,
which contains everything Maven needs to know about the project, in order to:

* Download dependencies
* Compile the sources
* Pack the class files together with all the dependencies into a single,
  executable jar file

### Installing Maven

You need Maven version 2 or later.
In case you already have it installed, skip this paragraph.

* _Windows_

	Download the latest stable version (not the source)
	[here](http://maven.apache.org/download.html).
	Then extract to eg. your `C:\Program Files`,
	and make sure the bin sub-dir of the extracted folder is in your `PATH`
	environment variable.

* _Unix, Linux, BSD, OS X_

	Install the `maven2` package in your systems way of doing so.
	On Ubuntu for example, you would do this:

		> sudo apt-get install maven2

### Building the software

1.	Make sure you have Maven 2 or later installed.
	You can check that with the following command:

		> mvn --version

2.	compile, package & install to the local repository:

		> mvn install

	This may take quite some time if you are running Maven for the first time,
	as it has to download all the dependencies for the different build steps,
	plus our own dependencies.

	NOTE: If this fails because the MOAPI framework dependency can not be found,
	you first have to run 'mvn install' in the MOAPI framework project.

All the output of the build process is under the `target/` sub-dir.
This is also where you find the final jar files.


## Running

The preferred way of running the software while developing is through an IDE,
or through Maven:

	> mvn exec:java

An example with Java arguments (initial and maximum amount of RAM)
and with application arguments (run in memory mode):

	> MAVEN_OPTS="-Xms1024m -Xmx3g" mvn exec:java -Dexec.args="--memory"


## Create Distribution Package

If you want to give the software to a non-dev for trying it out/using it,
follow these instructions.

	> mvn package assembly:single

You should end up with a single file to distribute at:
`target/gwaspi-{version}-jar-with-dependencies.jar`

You can then use the file to start GWASpi like this:

	> java -jar gwaspi-*jar-with-dependencies*.jar

An example with Java arguments (initial and maximum amount of RAM)
and with application arguments (run in memory mode):

	> java -Xms1024m -Xmx3g -jar gwaspi-*jar-with-dependencies*.jar --memory


## Release a SNAPSHOT (devs only)

To release a development version to the Sonatype snapshot repository:

		mvn clean deploy


## Release (devs only)

These instructions explain how to release to the Maven central repository.

### Prepare "target/" for the release process

	mvn release:clean

### Prepare the release
* asks for the version to use
* packages
* signs with GPG
* commits
* tags
* pushes to origin

		mvn release:prepare

### Perform the release (main part)
* checks-out the release tag
* builds
* deploy into Sonatype staging repository

		mvn release:perform

### Release the site
* generates the site, and pushes it to the github gh-pages branch,
  visible under http://hoijui.github.com/GWASpi/

		git checkout <release-tag>
		mvn site
		git checkout master

### Promote it on Maven
Moves it from the Sonatype staging to the main Sonatype repo

1. using the Nexus staging plugin:

		mvn nexus:staging-close
		mvn nexus:staging-release

2. ... alternatively, using the web-interface:
	* firefox https://oss.sonatype.org
	* login
	* got to the "Staging Repositories" tab
	* select "org.jorts..."
	* "Close" it
	* select "org.jorts..." again
	* "Release" it

