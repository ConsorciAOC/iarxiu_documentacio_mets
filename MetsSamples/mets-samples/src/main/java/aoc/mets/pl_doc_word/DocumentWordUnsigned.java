package aoc.mets.pl_doc_word;

import gov.loc.mets.DivType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import aoc.mets.util.MetsUtil;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

public class DocumentWordUnsigned {
	
	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_doc_word/").replaceAll("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/mets-samples/src/main/resources/").replaceAll("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String documentName = "doc.docx";
		String documentFullPath = fullPathThisSample + "document" + fileSeparator + documentName;
		String dmdFullPath = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_document";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);

		//update md to prevent incorrect duplicate ingest
		MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");

		// creació de les seccions dmdSec amb les metadades carregades anteriorment
		File dmdFile = new File(dmdFullPath);
		InputStream dmd1Stream = new FileInputStream(dmdFile);
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmd1Stream);

		// creació de les estructures FileGrp dels binaris		
		File documentFile = new File(documentFullPath);
		InputStream document = new FileInputStream(documentFile);
		mets.createFileGrp("BIN_1", mets.generateSha1File(document), "BIN_1.0", "application/msword", documentName);

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
		String zipFilePath = pathOutDir + "PIT_document_word.zip";
		String folderToZip = fullPathThisSample + "document";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

}
