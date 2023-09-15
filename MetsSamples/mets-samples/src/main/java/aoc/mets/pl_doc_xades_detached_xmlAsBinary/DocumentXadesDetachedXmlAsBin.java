package aoc.mets.pl_doc_xades_detached_xmlAsBinary;

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
 * Classe per generar METS d'una signatura detached feta a partir d'un xml però en comptes de tractar-lo com xml 
 * (canonicalitzat abans de calcular el hash) es tracta com binari (i el hash estaria "mal" calculat).
 *  
 */
public class DocumentXadesDetachedXmlAsBin {

	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_doc_xades_detached_xmlAsBinary/").replaceAll("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/mets-samples/src/main/resources/").replaceAll("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String documentName      = "doc.xml";
		String documentFullPath  = fullPathThisSample + "document" + fileSeparator + documentName;
		String signatureName     = "signatura.xml";
		String signatureFullPath = fullPathThisSample + "document" + fileSeparator + signatureName;
		String dmdFullPath       = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
		String amdFullPath       = fullPathThisSample + "md" + fileSeparator + "amd.xml";
		String templateURN       = "urn:iarxiu:2.0:templates:catcert:PL_document";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);											
				
		//update md to prevent incorrect duplicate ingest
		MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");
		 
		// es carreguen de disc les metadades descriptives dels vocabularis d'expedient i de document
		//InputStream dmdStream = ClassLoader.getSystemResourceAsStream(dmdPath);		
		File dmdFile = new File(dmdFullPath);
		File amdFile = new File(amdFullPath);
		InputStream dmdStream = new FileInputStream(dmdFile);
		InputStream amdStream = new FileInputStream(amdFile);
	
		// creació de les seccions dmdSec amb les metadades carregades anteriorment		
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmdStream);
		mets.createAMDSec("AMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", "AMD_1.0", amdStream);		
				
		InputStream document = new FileInputStream(new File(documentFullPath));
		InputStream xades_detached = new FileInputStream(new File(signatureFullPath));
		
		// creació de les estructures FileGrp dels binaris		
		mets.createFileGrp("BIN_1", mets.generateSha1File(document), "BIN_1.0", "text/xml", documentName);
		mets.createFileGrp("BIN_2", mets.generateSha1File(xades_detached), "BIN_2.0", "text/xml", signatureName);

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
		//System.out.println(mets);

		// Creem el ZIP amb el mets, documents i signatures
		String zipFilePath = pathOutDir + "PIT_document_XAdES_detached_xmlAsBinary.zip";
		String folderToZip = fullPathThisSample + "document";
		MetsUtil.createZipPIT(zipFilePath, folderToZip);
	}

	
}
