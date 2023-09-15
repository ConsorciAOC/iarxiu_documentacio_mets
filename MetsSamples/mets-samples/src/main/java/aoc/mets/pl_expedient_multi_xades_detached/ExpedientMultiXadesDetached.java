package aoc.mets.pl_expedient_multi_xades_detached;

import gov.loc.mets.DivType;

import gov.loc.mets.DivType.Fptr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import aoc.mets.util.MetsUtil;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe encarregada de crear un METS d'expedient amb 5 documents XML i la seva respectiva signatura XAdES-T detached.
 * Els documents es referencien (no s'adjunten inline en b64).
 * Per afegir més documents a l'expedient si es desitja cal seguir l'estructura següent:
 *
 * \expedients
 * 		\1
 * 			\doc1.xml (document)
 *    		\doc1_signed.xml (signatura)
 *    	\2
 *    		\...
 *    		\...
 *    	\...
 *
 * \md
 * 		\dmdExpedient.xml (metadades de l'expedient)
 * 		\1
 *    		\dmd.xml (metadades del document)
 *    		\amd.xml (metadades de la signatura)
 *    	\2
 *    		\...
 *    		\...
 *    	\...
 *
 */
public class ExpedientMultiXadesDetached {

	public static void main(String[] args) throws MetsException, Exception {

		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replaceAll("/", fileSeparator);

		String pathThisSample = ("aoc/mets/pl_expedient_multi_xades_detached/").replaceAll("/", fileSeparator);
		String fullPathThisSample = userDir +
				("/mets-samples/src/main/resources/").replaceAll("/", fileSeparator) + pathThisSample;
		String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
		MetsUtil.createIfNotExistsDirectory(pathOutDir);

		String dmdExpFullPath = fullPathThisSample + "md" + fileSeparator + "dmdExpedient.xml";
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_expedient";

		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);

		File rootExpedientsFolder = new File(fullPathThisSample + "md");

		if (rootExpedientsFolder.isDirectory()) {
			//DMD EXPEDIENT
			MetsUtil.updateMdDmdFile(dmdExpFullPath, "voc_exp:data_tancament");
			File dmdExpFile = new File(dmdExpFullPath);
			InputStream dmdExpStream = new FileInputStream(dmdExpFile);
			mets.createDMDSec("DMD_" + rootExpedientsFolder.getName(), "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient", dmdExpStream);

			//STRUCTMAP ROOT
			DivType rootDiv = mets.createRootDiv("DMD_" + rootExpedientsFolder.getName(), "expedient_amb_documents_signatures");
			rootDiv.setLABEL("MD_expedient");

			// Bucle que itera entre les N carpetes de l'expedient que hi hagi
			int binPattern = 1;
			File[] expedientsFolders = rootExpedientsFolder.listFiles();
			for (File expedientFolder : expedientsFolders) {

				if (expedientFolder.isDirectory()) {

					String folderPattern = expedientFolder.getName();

					String documentFilename =  "doc" + folderPattern + ".xml";
					String signatureFilename = "doc" + folderPattern + ".xml_signed.xml";

					String dmdFullPath       = fullPathThisSample + "md" + fileSeparator + folderPattern + fileSeparator + "dmd.xml";
					String amdFullPath       = fullPathThisSample + "md" + fileSeparator + folderPattern + fileSeparator + "amd.xml";
					String documentFullPath  = fullPathThisSample + "expedients" + fileSeparator + folderPattern + fileSeparator + documentFilename;
					String signatureFullPath = fullPathThisSample + "expedients" + fileSeparator + folderPattern + fileSeparator + signatureFilename;

					// Modifiquem la md titol ja que les dates es tallen a nivell de minut
					MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");

					File dmdFile = new File(dmdFullPath);
					File amdFile = new File(amdFullPath);
					InputStream dmdStream = new FileInputStream(dmdFile);
					InputStream amdStream = new FileInputStream(amdFile);

					// creacio seccions MD documents
					// guardem el DMDID i el AMDID del document per referenciar-lo a la secció structmap
					String DMDID_doc = "DMD_" + folderPattern;
					String AMDID_sig = "AMD_" + folderPattern;
					mets.createDMDSec(DMDID_doc, "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmdStream);
					mets.createAMDSec(AMDID_sig, "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", AMDID_sig + ".0", amdStream);

					File documentFile  = new File(documentFullPath);
					File signatureFile = new File(signatureFullPath);
					InputStream documentStream = new FileInputStream(documentFile);
					InputStream signatura_detachedStream = new FileInputStream(signatureFile);

					// FILEGRP DOCUMENTS
					// guardem el identidicador de BIN_ dels documents per referenciar-lo al structmap
					String docBin = "BIN_" + binPattern;
					mets.createFileGrp(	
							docBin, 
							mets.generateSha1File(documentStream), 
							"BIN_" + binPattern +".0", 
							"text/xml", 
							folderPattern + fileSeparator + documentFilename);
					binPattern++;

					String sigBin = "BIN_" + binPattern;
					mets.createFileGrp(
							sigBin, 
							mets.generateSha1File(signatura_detachedStream), 
							"BIN_" + binPattern + ".0", 
							"text/xml", 
							folderPattern + fileSeparator + signatureFilename);
					binPattern++;

					// STRUCTMAP DOCUMENTS
					List<String> dmdList = new ArrayList<String>();
					dmdList.add(DMDID_doc);

					// afegiment de les div inferiors
					DivType divTypeDMD = mets.addDiv(rootDiv, null, "sub_expedient", true);	
					divTypeDMD.setDMDID(dmdList);

					DivType divTypeDMDDoc = divTypeDMD.addNewDiv();
					divTypeDMDDoc.setLABEL(documentFilename);
					Fptr docFPTR = divTypeDMDDoc.addNewFptr();
					docFPTR.setFILEID(docBin);

					List<String> amdList = new ArrayList<String>();
					amdList.add(AMDID_sig);
					DivType divTypeDMDSig = divTypeDMD.addNewDiv();
					divTypeDMDSig.setADMID(amdList);
					divTypeDMDSig.setLABEL(signatureFilename);
					Fptr sigFPTR = divTypeDMDSig.addNewFptr();
					sigFPTR.setFILEID(sigBin);
				}
			}
			System.out.println(mets);

			// validació del mets generat
			mets.validate();

			//guardem el fitxer mets.xml
			String metsFile = fullPathThisSample + "expedients" + fileSeparator + "mets.xml";
			OutputStream os = new FileOutputStream(metsFile);
			mets.save(os);

			// Creem el ZIP amb el mets, documents i signatures
			String zipFilePath = pathOutDir + "PIT_expedient_multi_XAdES_detached.zip";
			String folderToZip = fullPathThisSample + "expedients";
			MetsUtil.createZipPIT(zipFilePath, folderToZip);
		}
	}

}
