package aoc.mets.pl_expedient_multi_pdf;

import aoc.mets.util.MetsUtil;
import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;
import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ExpedientMultiPdf {

	public static void main(String[] args) throws Exception {
		ExpedientMultiPdf exp = new ExpedientMultiPdf();
		exp.createMets();
	}

	public void createMets() throws ParserConfigurationException, TransformerException, SAXException, IOException, MetsException {

		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);

			String pathThisSample = ("aoc/mets/pl_expedient_multi_pdf/").replace("/", fileSeparator);
			String fullPathThisSample = userDir +
					("/src/main/resources/").replace("/", fileSeparator) + pathThisSample;
			String pathOutDir = userDir + fileSeparator + "out" + fileSeparator + pathThisSample;
			MetsUtil.createIfNotExistsDirectory(pathOutDir);

			String dmdExpFullPath  = fullPathThisSample + "md" + fileSeparator + "dmdExpedient.xml";
			String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_expedient";

			// creació del mets
			Mets mets = new Mets();
			// inclou la plantilla
			mets.setTemplate(templateURN);

			File rootExpedientsFolder = new File(fullPathThisSample + "md");

			if (rootExpedientsFolder.isDirectory()) {
				//DMD EXPEDIENT
				MetsUtil.updateMdDmdFile(dmdExpFullPath, "voc:data_tancament");
				File dmdExpFile = new File(dmdExpFullPath);
				InputStream dmdExpStream = new FileInputStream(dmdExpFile);
				mets.createDMDSec("DMD_" + rootExpedientsFolder.getName(), "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient", dmdExpStream);

				//STRUCTMAP ROOT
				DivType rootDiv = mets.createRootDiv("DMD_" + rootExpedientsFolder.getName(), "expedient_amb_documents_pdf");
				rootDiv.setLABEL("MD_expedient");

				// Bucle que itera entre les N carpetes d'expedients que hi hagi
				int binPattern = 1;
				File[] expedientsFolders = rootExpedientsFolder.listFiles();
				for(File expedientFolder : expedientsFolders){

					if(expedientFolder.isDirectory()){

						String folderPattern = expedientFolder.getName();

						String documentFilename = folderPattern + ".PDF";

						String dmdFullPath = expedientFolder + fileSeparator + "dmd.xml";
						String documentFullPath = fullPathThisSample + "expedients" + fileSeparator + folderPattern + fileSeparator + documentFilename;

						// Modifiquem la md titol ja que les dates es tallen a nivell de minut
						MetsUtil.updateMdDmdFile(dmdFullPath, "voc:titol");

						File dmdFile = new File(dmdFullPath);
						InputStream dmdStream = new FileInputStream(dmdFile);

						// creacio seccions MD documents
						// guardem el DMDID del document per referenciar-lo a la secció structmap
						String DMDID_doc = "DMD_" + folderPattern;
						mets.createDMDSec(DMDID_doc, "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmdStream);

						File documentFile = new File(documentFullPath);
						InputStream documentStream = new FileInputStream(documentFile);

						// FILEGRP DOCUMENTS
						// guardem el identidicador de BIN_ dels documents per referenciar-lo al structmap
						String docBin = "BIN_" + binPattern;
						mets.createFileGrp(
								docBin,
								mets.generateSha1File(documentStream),
								"BIN_" + binPattern +".0",
								"text/xml",
								documentFullPath);
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

					}
				}
				System.out.println(mets);

				// validacio del mets generat
				mets.validate();

				//guardem el fitxer mets.xml
				String metsFile = fullPathThisSample + "expedients" + fileSeparator + "mets.xml";
				OutputStream os = new FileOutputStream(metsFile);
				mets.save(os);

				// Creem el ZIP amb el mets, documents i signatures
				String zipFilePath = pathOutDir + "PIT_expedient_multi_pdf.zip";
				String folderToZip = fullPathThisSample + "expedients";
				MetsUtil.createZipPIT(zipFilePath, folderToZip);
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
