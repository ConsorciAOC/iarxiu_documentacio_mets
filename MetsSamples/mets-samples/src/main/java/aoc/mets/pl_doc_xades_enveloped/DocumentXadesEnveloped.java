package aoc.mets.pl_doc_xades_enveloped;

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
 * Classe per generar METS d'un document TXT amb una signatura detached CAdES-T. S'envia com a document.
 */
public class DocumentXadesEnveloped {

	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replace("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_doc_xades_enveloped/").replace("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/src/main/resources/").replace("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String envelopedSignatureName     = "doc1_enveloped.xml";
		String envelopedSignatureFullPath = fullPathThisSample + "document" + fileSeparator + envelopedSignatureName;
		String dmdFullPath                = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
		String templateURN                = "urn:iarxiu:2.0:templates:catcert:PL_document";

		// creació del mets 
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);

		// creació de les seccions dmdSec amb les metadades carregades anteriorment
		File dmdFile = new File(dmdFullPath);
		InputStream dmdStream = new FileInputStream(dmdFile);
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmdStream);

		//update md to prevent incorrect duplicate ingest
		MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");

		// creació de les estructures FileGrp dels binaris
		File envelopedSignatureFile = new File(envelopedSignatureFullPath);
		InputStream envelopedSignatureIs = new FileInputStream(envelopedSignatureFile);
		mets.createFileGrp("BIN_1", mets.generateSha1File(envelopedSignatureIs), "BIN_1.0", "text/xml", envelopedSignatureName);

		// creació secció structMap
		DivType rootDiv = MetsUtil.initStructMap(mets, "DMD_1", "MD_document");
		MetsUtil.addDiv(mets, rootDiv, null, false, envelopedSignatureName, "BIN_1");

		// validació del mets generat
		mets.validate();

		//guardem el fitxer mets.xml
		String metsFile = fullPathThisSample + "document" + fileSeparator + "mets.xml";
		OutputStream os = new FileOutputStream(metsFile);
		mets.save(os);
		//System.out.println(mets);

		// Creem el ZIP amb el mets, documents i signatures
		String zipFilePath = pathOutDir + "PIT_document_XAdES_enveloped.zip";
		String folderToZip = fullPathThisSample + "document";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

}
