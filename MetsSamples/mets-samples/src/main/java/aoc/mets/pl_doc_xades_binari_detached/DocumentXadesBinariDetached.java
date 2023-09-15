package aoc.mets.pl_doc_xades_binari_detached;

import aoc.mets.util.MetsUtil;
import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;
import gov.loc.mets.DivType;

import java.io.*;

/**
 * Classe per generar METS d'un document xml amb una signatura detached XAdES-T tractant el document com un binari
 */
public class DocumentXadesBinariDetached {

	public static void main(String[] args) throws Exception {
		 
		try{

			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);

			String pathThisSample = ("aoc/mets/pl_doc_xades_binari_detached/").replaceAll("/", fileSeparator);
			String fullPathThisSample = userDir +
					("/mets-samples/src/main/resources/").replaceAll("/", fileSeparator) + pathThisSample;
			String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
			MetsUtil.createIfNotExistsDirectory(pathOutDir);

			String documentName = "AQ-2016-3101G01007-ER-J.xml";
			String documentFullPath  = fullPathThisSample + "document" + fileSeparator + documentName;
			String signatureName = "AQ-2016-3101G01007-ER-J_signatura_1.xml";
			String signatureFullPath = fullPathThisSample + "document" + fileSeparator + signatureName;
			String dmdFullPath       = fullPathThisSample + "md" + fileSeparator + "dmd.xml";
			String amdFullPath       = fullPathThisSample + "md" + fileSeparator + "amd.xml";
			String templateURN       = "urn:iarxiu:2.0:templates:catcert:PL_document";

			// creacio del mets
			Mets mets = new Mets();
			// inclou la plantilla
			mets.setTemplate(templateURN);

			//update md to prevent incorrect duplicate ingest
			MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");
			 
			// es carreguen de disc les metadades descriptives dels vocabularis d'expedient i de document
			File dmdFile = new File(dmdFullPath);
			File amdFile = new File(amdFullPath);
			InputStream dmdStream = new FileInputStream(dmdFile);
			InputStream amdStream = new FileInputStream(amdFile);

			// creacio de les seccions dmdSec amb les metadades carregades anteriorment
			mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", dmdStream );
			mets.createAMDSec("AMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", "AMD_1.0", amdStream);

			// creació de les estructures FileGrp dels binaris
			InputStream document = new FileInputStream(new File(documentFullPath));
			InputStream xades_detached = new FileInputStream(new File(signatureFullPath));
			mets.createFileGrp("BIN_1", mets.generateSha1File(document), "BIN_1.0", "text/xml", documentName);
			mets.createFileGrp("BIN_2", mets.generateSha1File(xades_detached), "BIN_2.0", "text/xml", signatureName);

			// creacio seccio structMap
			DivType rootDiv = MetsUtil.initStructMap(mets, "DMD_1", "MD_document");
			MetsUtil.addDiv(mets, rootDiv, null, true, documentName, "BIN_1");
			MetsUtil.addDiv(mets, rootDiv,"AMD_1", false, signatureName, "BIN_2");	
					
			// validacio del mets generat
			mets.validate();

			//guardem el fitxer mets.xml
			String metsFile = fullPathThisSample + "document" + fileSeparator + "mets.xml";
			OutputStream os = new FileOutputStream(metsFile);
			mets.save(os);

			// Creem el ZIP amb el mets, documents i signatures
			String zipFilePath = pathOutDir + "PIT_document_XAdES_binari_detached.zip";
			String folderToZip = fullPathThisSample + "document";
			MetsUtil.createZipPIT(zipFilePath, folderToZip);
		}
		catch(Exception e){
			System.out.println(e);
		}		
	}

	
}
