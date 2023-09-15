package exemples.exemple1;

import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;

import java.io.File;
import java.io.InputStream;

import utils.Utils;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe encaregada de generar un mets amb dues signatures incrustades
 * Les metadades descriptives han estat generades prèviament i es carreguen des de disc.
 * 
 * @author Toni Marcos
 */
public class MetsTest {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MetsException 
	 */
	public static void main(String[] args) throws MetsException, Exception {
		 
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_expedient";
		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);
		
		// es carreguen de disc les metadades descriptives dels vocabularis d'expedient i de document
		String path = "exemples/exemple1/".replaceAll("/", File.separator);
		InputStream dmd1Stream = ClassLoader.getSystemResourceAsStream(path  + "dmd1.xml");
		InputStream dmd2Stream = ClassLoader.getSystemResourceAsStream(path  + "dmd2.xml");
		InputStream dmd3Stream = ClassLoader.getSystemResourceAsStream(path  + "dmd3.xml");
	
		// creació de les seccions dmdSec amb les metadades carregades anteriorment
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient", dmd1Stream);
		mets.createDMDSec("DMD_2", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmd2Stream);
		mets.createDMDSec("DMD_3", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", dmd3Stream);

		InputStream cmsAttachedStream = ClassLoader.getSystemResourceAsStream(path  + "CMS-attached.p7s");
		InputStream cadesBESStream = ClassLoader.getSystemResourceAsStream(path  + "CAdES-BES-attached.p7s");
		
		// creació de les estructures FileGrp dels binaris
		mets.createFileGrp("BIN_1", Utils.getBytes(cmsAttachedStream), "BIN_1.0", "text/plain");
		mets.createFileGrp("BIN_2", Utils.getBytes(cadesBESStream), "BIN_2.0", "text/plain");

		// creació secció structMap
		// es crea la rootDiv
		DivType rootDiv = mets.createRootDiv("DMD_1", "exp_nototesav");
		
		// afegiment de les div inferiors
		DivType divTypeDMD_2 = mets.addDiv(rootDiv, "DMD_2", "CMS-attached",true);
		DivType divTypeCMS = divTypeDMD_2.addNewDiv();
		divTypeCMS.setLABEL("CMS-attached.p7s");
		Fptr cmsFPTR = divTypeCMS.addNewFptr();
		cmsFPTR.setFILEID("BIN_1");
		
		DivType divTypeDMD_3 = mets.addDiv(rootDiv, "DMD_3", "CAdES-BES-attached",true);
		DivType divTypeCADES = divTypeDMD_3.addNewDiv();
		divTypeCADES.setLABEL("CAdES-BES-attached.p7s");
		Fptr cadesFPTR = divTypeCADES.addNewFptr();
		cadesFPTR.setFILEID("BIN_2");

		// validació del mets generat
		mets.validate();
	}


}
