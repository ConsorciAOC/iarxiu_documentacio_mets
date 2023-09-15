package aoc.mets.pl_doc_cades_detached;

import gov.loc.mets.DivType;

import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import aoc.mets.util.MetsUtil;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe per generar METS d'un document TXT amb una signatura detached CAdES-T. S'envia com a document.
 */
public class DocumentCadesDetached {

	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_doc_cades_detached/").replaceAll("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/mets-samples/src/main/resources/").replaceAll("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String documentName  = "test.txt";
		String signatureName = "test.txt_cades.p7s";
		String documentPath  = pathThisSample + "document" + fileSeparator + documentName;
		String signaturePath = pathThisSample + "document" + fileSeparator + signatureName;
		String dmdPath       = pathThisSample + "md" + fileSeparator + "dmd.xml";
		String dmdFullPath   = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
		String amdPath       = pathThisSample + "md" + fileSeparator + "amd.xml";
		String templateURN   = "urn:iarxiu:2.0:templates:catcert:PL_document";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);

		// es carreguen de disc les metadades descriptives dels vocabularis d'expedient i de document
		InputStream dmd1Stream = ClassLoader.getSystemResourceAsStream(dmdPath);
		InputStream amd1Stream = ClassLoader.getSystemResourceAsStream(amdPath);
	
		// creació de les seccions dmdSec amb les metadades carregades anteriorment
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmd1Stream);
		mets.createAMDSec("AMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", "AMD_1.0", amd1Stream);		

		// creació de les estructures FileGrp dels binaris	
		InputStream document = ClassLoader.getSystemResourceAsStream(documentPath);
		InputStream cades_detached = ClassLoader.getSystemResourceAsStream(signaturePath);
		mets.createFileGrp("BIN_1", mets.generateSha1File(document), "BIN_1.0", "text/plain", documentName);
		mets.createFileGrp("BIN_2", mets.generateSha1File(cades_detached), "BIN_2.0", "application/pkcs7-signature", signatureName);

		// Modify metadata to allow reupload (unicity control)
		MetsUtil.updateMdDmdFile(dmdFullPath, "voc:data_creacio");
		
		// creació secció structMap				
		DivType rootDiv = MetsUtil.initStructMap(mets, "DMD_1", "MD_document");
		MetsUtil.addDiv(mets, rootDiv, null, true, documentName, "BIN_1");
		MetsUtil.addDiv(mets, rootDiv,"AMD_1", false, signatureName, "BIN_2");

		// validació del mets generat
		mets.validate();

		//guardem el fitxer mets.xml
		String metsFile = fullPathThisSample + "document" + fileSeparator + "mets.xml";
		OutputStream os = new FileOutputStream(metsFile);
		mets.save(os);

		// Creem el ZIP amb el mets, documents i signatures
		String zipFilePath = pathOutDir + "PIT_document_CAdES_detached.zip";
		String folderToZip = fullPathThisSample + "document";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

}
