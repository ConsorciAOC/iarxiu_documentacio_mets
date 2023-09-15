package exemples.exemple6;

import aoc.mets.util.MetsUtil;
import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe encaregada de generar un mets amb binaris referenciats
 * Les metadades, prèviament creades, es carreguen de disc 
 * @author Toni Marcos
 *
 */
public class MetsTest6 {

	/**
	 * @param args
	 * @throws Exception
	 * @throws MetsException
	 */
	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);
		String pathOutDir = userDir + "/out/exemples/exemple6/".replace("/", fileSeparator);
		MetsUtil.createIfNotExistsDirectory(pathOutDir);
		 
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_expedient";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);
		
		// carrega de les metadades descriptives
		String path = "exemples/exemple6/".replaceAll("/", File.separator);
		InputStream dmd1Stream = ClassLoader.getSystemResourceAsStream(path + "dmd_TS_753526.xml");
		InputStream dmd2Stream = ClassLoader.getSystemResourceAsStream(path + "dmd_CN_753535.xml");
		InputStream dmd3Stream = ClassLoader.getSystemResourceAsStream(path + "dmd_CN_753535_PDF.xml");
		
		// creació de la estructura de les metadades descriptives
		mets.createDMDSec("TS_753526", "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient", dmd1Stream);
		mets.createDMDSec("CN_753535", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmd2Stream);
		mets.createDMDSec("CN_753535_PDF", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmd3Stream);

		// càrrega dels binaris referenciats
		InputStream binari1 = ClassLoader.getSystemResourceAsStream(path + "CN_753535.xml");
		InputStream binari2 = ClassLoader.getSystemResourceAsStream(path + "CN_753535.pdf");
		// càlcul de checksums per als binaris
		String checksumBinari1 = mets.generateSha1File(binari1);
		System.out.println(checksumBinari1);
		String checksumBinari2 = mets.generateSha1File(binari2);
		System.out.println(checksumBinari2);
		// creació de les estructures FileGrp per a binaris referenciats
		mets.createFileGrp("BIN_753535", checksumBinari1, "BIN_753535.0", "text/xml",
				"753526/CN/753535/CN_753535.xml".replaceAll("/", fileSeparator));
		mets.createFileGrp("BIN_753535_PDF", checksumBinari2, "BIN_753535_PDF.0", "application/pdf",
				"753526/CN/753535/CN_753535.pdf".replaceAll("/", fileSeparator));
		// creació secció structMap
		// es crea la rootDiv
		DivType rootDiv = mets.createRootDiv("TS_753526", "expedient_1AL_1AF");
		
		// afegiment de les div inferiors
		DivType divTypeDMD_1 = mets.addDiv(rootDiv, "CN_753535", null,true);
		
		DivType divType = divTypeDMD_1.addNewDiv();
		divType.setLABEL("CN_753535.xml");
		Fptr newFptr = divType.addNewFptr();
		newFptr.setFILEID("BIN_753535");
		
		DivType divType2 = mets.addDiv(divTypeDMD_1, "CN_753535_PDF", null, true);
		Fptr newFptr2 = divType2.addNewFptr();
		newFptr2.setFILEID("BIN_753535_PDF");
		
		mets.validate();

		//guardem el fitxer mets.xml
		String metsFile = userDir +
				("/mets-samples/src/main/resources/exemples/exemple6/mets.xml").replaceAll("/", fileSeparator);
		OutputStream os = new FileOutputStream(metsFile);
		mets.save(os);

		// Creem el ZIP amb el mets, documents i signatures
		String zipFilePath = pathOutDir + "exemple6.zip";
		String folderToZip = userDir + "/mets-samples/src/main/resources/exemples/exemple6";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

}
