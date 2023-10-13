iArxiuMets
============

Este proyecto genera ejemplos de PIT (paquetes de información de transferencia). Es decir, ejemplos 
de documentación electrónica que transfiere el productor de la documentación electrónica a la plataforma 
iArxiu, ya sea ésta una unidad documental compuesta o una unidad documental simple, de acuerdo con el 
protocolo de transferencia establecido por el Consorcio AOC.

Los pasos para su utilización, tanto en Windows como en Linux, son:

1. Se descomprime la distribucion ZIP de Java 8 en:

	C:\java\jdk1.8.0_382
	ó
	$HOME/jdk1.8.0_382

2. Se descomprime la distribución ZIP de Maven 3.0.5 en:

	C:\java\apache-maven-3.0.5
	ó
	$HOME/apache-maven-3.0.5

3. Configurar el siguiente mirror:

	 <mirror>
		<id>central-secure</id>
		<url>https://repo.maven.apache.org/maven2</url>
		<mirrorOf>central</mirrorOf>
	 </mirror>

	en el archivo settings.xml ubicado en el directorio del repositorio M2 de Maven, 
	que suele ser:
	
	En Windows:
	%USERPROFILE%\.m2
	
	En Linux:
	$HOME/.m2
	
	o si no existiese dicho archivo, configurar el mirror en el setting.xml del subdirectorio 
	conf donde se ha descomprimido Maven.

4. Se clona repositorio GIT de iarxiu_mets en:

	C:\java\iarxiu_mets
	ó
	$HOME/iarxiu_mets

5. Desde una consola del sistema, se ejecutan los siguientes comandos:

	En Windows:

	SET JAVA_HOME=C:\java\jdk1.8.0_382
	SET PATH=%JAVA_HOME%\bin;%PATH%
	SET MAVEN_HOME=C:\java\apache-maven-3.0.5
	SET PATH=%MAVEN_HOME%\bin\;%PATH%

	En Linux:

	export JAVA_HOME=$HOME/jdk1.8.0_382
	export PATH=$JAVA_HOME/bin:$PATH
	export M2_HOME=$HOME/apache-maven-3.0.5
	export MAVEN_HOME=$HOME/apache-maven-3.0.5
	export PATH=$M2_HOME/bin:$PATH

6. Desde dicha consola del sistema, se ejecutan los siguientes comandos, situándose antes en el directorio 
	"MetsCreator/lib":

	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=mets-schema-2.23.0.jar
	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=xmlschema-validator-2.23.0.jar

7. Ejecutar "mvn clean install" situados en el directorio del POM de MetsCreator.

8. Desde dicha consola del sistema, se ejecuta el siguiente comando, situándose antes en el directorio 
	"MetsSamples/mets-samples/lib":

	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=xlink-schema-2.23.0.jar

9. Ejecutar "mvn clean install" situados en el directorio del POM de MetsSamples.

10. Cargar el proyecto mets-samples en el IDE que se prefiera (Eclipse / IIJ), y configurarlo para que use Java 8.

	En Eclipse:

		- File -> Import... -> Maven -> Existing Maven Projects -> Root Directory:

		C:\java\iarxiu_mets\MetsSamples\mets-samples
		ó
		$HOME/iarxiu_mets/MetsSamples/mets-samples

		- Desde Window -> Preferences en Eclipse -> Installed JREs, añadir como Standard VM la jdk1.8.0_382 del paso 1 
		y marcarla como la de defecto.

	En IIJ:

		- File -> Open... (Open File or Project) abrir el pom.xml de mets-samples e indicar "Open as Project".
	
		- Desde File -> Project Structure -> Project, cambiar "SDK" a la ruta donde se descomprimió Java 8 en el punto 1.

11. Ejecutar o debuggear cada clase java, método main.

	Nota: tras cada ejecución se modifican los ficheros mets.xml y otros XML, y luego aparecen como cambiados para Git.

	Nota: En vocabularies hay unos vocabularios de ejemplo, pero el desarrollador puede colocar allí los suyos propios.