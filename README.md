# linkgen
Synthetic Linked Data Generator

(Code has been migrated from Bitbucket to Github)

## Please use following citation if you use this tool:
* Joshi, A.K., Hitzler, P., Dong, G.: Multi-purpose Synthetic Linked Data Generator
* https://github.com/akjoshi/linkgen/
* Web Id for the resource: http://w3id.org/linkgen
 

### What is this repository for? ###

* A synthetic linked data generator that can generate a large amount of RDF data based on certain statistical distribution. Data generation is platform independent, supports streaming mode and produces output in N-Triples and N-Quad format. Different sets of output can be generated using various configuration parameters and the outputs are reproducible. The generator accepts any vocabulary and can supplement the output with noisy and inconsistent data. The generator has an option to inter-link instances with real ones provided that the user supplies entities from real datasets.

### How do I get set up? ###

* Summary of set up

* Configuration : See config.properties file
* Dependencies: Java 1.8 or higher, Apache Jena

* How to run tests
	* Download Entire package including Jar file, config.properties, log.properties and ontologies	
```
#!java
Commandline: 
		 java -jar linkgen.jar Generator -c <file.config.properties>
	Progam:
		1) Add jar to classpath
		2) Generator gen = new Generator();
		   gen.run();

```
## Debugging
Log4J over SLF4J is used as the logging framework. Please see log4j.properties for details on debugging criteria.

### Contribution guidelines ###

* Issues? Create an issue in Github
* Feel free to fork

### License ###
GNU General Public License, version 3 (GPL-3.0)
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
