package aoc.mets.pl_doc_cades_attached;

import gov.loc.mets.DivType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import aoc.mets.util.MetsUtil;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe per generar METS d'un document TXT amb una signatura attached CAdES-T. S'envia com a document.
 * No es defineixen AMD (metadades de la signatura).
 */
public class DocumentCadesAttached {

	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replace("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_doc_cades_attached/").replace("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/src/main/resources/").replace("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String documentName = "test_cades.p7b";
		String documentPath = pathThisSample + "document" + fileSeparator + documentName;
		String dmdFullPath  = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
		String templateURN  = "urn:iarxiu:2.0:templates:catcert:PL_document";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);

		// es carreguen de disc les metadades descriptives dels vocabularis d'expedient i de document
		File dmdFile = new File(dmdFullPath);
		InputStream dmdStream = new FileInputStream(dmdFile);

		// creació de les seccions dmdSec amb les metadades carregades anteriorment
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmdStream);
														
		// creació de les estructures FileGrp dels binaris	
		InputStream document = ClassLoader.getSystemResourceAsStream(documentPath);
		mets.createFileGrp("BIN_1", mets.generateSha1File(document), "BIN_1.0", "application/pkcs7-signature", documentName);
		
		// Modify metadata to allow reupload (unicity control)
		MetsUtil.updateMdDmdFile(dmdFullPath, "voc:data_creacio");

		// creació secció structMap
		DivType rootDiv = MetsUtil.initStructMap(mets, "DMD_1", "MD_document");
		MetsUtil.addDiv(mets, rootDiv, null, true, documentName, "BIN_1");

		// validació del mets generat
		mets.validate();

		//guardem el fitxer mets.xml
		String metsFile = fullPathThisSample + "document" + fileSeparator + "mets.xml";
		OutputStream os = new FileOutputStream(metsFile);
		mets.save(os);

		// Creem el ZIP amb el mets, documents i signatures
		String zipFilePath = pathOutDir + "PIT_document_CAdES_attached.zip";
		String folderToZip = fullPathThisSample + "document";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

}
