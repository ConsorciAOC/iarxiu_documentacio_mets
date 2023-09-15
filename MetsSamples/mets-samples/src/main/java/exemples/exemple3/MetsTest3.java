package exemples.exemple3;

import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;

import utils.Utils;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientDocument;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientType;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientType.ClassificacioSeguretatAcces;

/**
 * Classe encarregada de generar un mets amb 
 * metadades descriptives d'expedient generades amb la llibreria METS
 * metadades administratives d'una signatura detached incrustada, carregades des de disc (creades prèviament)
 * @author Toni Marcos
 *
 */
public class MetsTest3 {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MetsException 
	 */
	public static void main(String[] args) throws MetsException, Exception {
		 
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_document";
		
		// creació del mets
		Mets mets = new Mets();
		
		// inclou la plantilla
		mets.setTemplate(templateURN);
		
		// creació de les metadades descriptives d'expedient
		Document expedient_DMD1 = createExpedient_DMD1();
	
		// càrrega de disc de les metadades administratives de signatura
		String path = "exemples/exemple3/".replaceAll("/", File.separator);
		InputStream amd1Stream = ClassLoader.getSystemResourceAsStream(path + "amd1.xml");
		
		// creació de la estructura de les metadades descriptives
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document", expedient_DMD1);
		
		// creació de la estructura de les metadades administratives
		mets.createAMDSec("AMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", "AMD_1.0", amd1Stream);
		InputStream documentHashStream = ClassLoader.getSystemResourceAsStream(path + "document.txt");
		InputStream signaturaStream = ClassLoader.getSystemResourceAsStream(path + "signatura.p7b");
		
		// creació de les estructures FileGrp dels binaris
		mets.createFileGrp("BIN_1", Base64.decode(Utils.getBytes(documentHashStream)), "BIN_1.0", "text/plain");
		mets.createFileGrp("BIN_2", Base64.decode(Utils.getBytes(signaturaStream)), "BIN_2.0", "application/pkcs7-mime");

		// creació secció structMap
		// es crea la rootDiv
		DivType rootDiv = mets.createRootDiv("DMD_1", null);
		
		// afegiment de les div inferiors
		DivType divTypeDMD_1 = mets.addDiv(rootDiv, null, "documento.doc",true);
		Fptr cmsFPTR = divTypeDMD_1.addNewFptr();
		cmsFPTR.setFILEID("BIN_1");
		
		DivType divTypeAMD_1 = mets.addDiv(rootDiv, "AMD_1", "firma.p7b",false);
		Fptr newFptr = divTypeAMD_1.addNewFptr();
		newFptr.setFILEID("BIN_2");
		mets.validate();
		
	}

	/**
	 *
	 * @return Document amb les metadades d'expedient
	 * @throws Exception
	 */
	public static Document createExpedient_DMD1() throws Exception{
		
		ExpedientDocument expedientDocument = ExpedientDocument.Factory.newInstance();
		ExpedientType expedient = expedientDocument.addNewExpedient();
		expedient.setCodiReferencia("a");
		expedient.setNumeroExpedient("a");
		expedient.setCodiClassificacio("a");
		expedient.setTitolSerieDocumental("s1");
		expedient.setNivellDescripcio("Unitat documental composta");
		expedient.setTitol("a");
		expedient.setDataObertura(Calendar.getInstance());
		expedient.setDataTancament(Calendar.getInstance());
		expedient.setNomProductor("a");
		expedient.setClassificacioSeguretatAcces(ClassificacioSeguretatAcces.ACCÉS_PÚBLIC);
		
		return Utils.getResourceAsDocument(expedientDocument.newInputStream());
	}
	
}
