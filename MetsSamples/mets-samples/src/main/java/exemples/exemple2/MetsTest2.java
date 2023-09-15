package exemples.exemple2;

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
import com.hp.iarxiu.user.schemas.x20.vocDocumentExp.DocumentDocument;
import com.hp.iarxiu.user.schemas.x20.vocDocumentExp.DocumentType;
import com.hp.iarxiu.user.schemas.x20.vocDocumentExp.DocumentType.DocumentEssencial;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientDocument;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientType;
import com.hp.iarxiu.user.schemas.x20.vocExpedient.ExpedientType.ClassificacioSeguretatAcces;
import com.hp.iarxiu.user.schemas.x20.vocSignatura.SignaturaDocument;
import com.hp.iarxiu.user.schemas.x20.vocSignatura.SignaturaType;
import com.hp.iarxiu.user.schemas.x20.vocSignatura.TipusSignaturaType;



/**
 * Classe encarregada de 
 * crear de metadades d'expedient, document d'expedient i signatura mitjançant les classes generades de la compilació
 * dels esquemes bàsics d'iArxiu
 * 
 * Les metadades descriptives i administratives creades mitjançant les classes anteriors, es validen contra els seus 
 * esquemes respectius abans d'incloure-les al METS
 * 
 * Finalment es valida el mets generat
 * @author Toni Marcos
 *
 */
public class MetsTest2 {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MetsException 
	 */
	public static void main(String[] args) throws MetsException, Exception {
		 
		String templateURN = "urn:iarxiu:2.0:templates:catcert:PL_expedient_document_generica";
		// creació del mets
		Mets mets = new Mets();
		// inclou la plantilla
		mets.setTemplate(templateURN);
		
		// es crea l'expedient, DMD1
		Document expedient = createExpedient_DMD1();
		mets.validateXMLData(expedient, Utils.getExpedientSchema());
		
		// Un cop validades les metadades d'expedient es procedeix a la creació de la estructura DMD amb aquestes metadades
		mets.createDMDSec("DMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient", expedient);
		
		// es creen les medadates del document d'expedient, DMD2
		Document documentExpedient = createDocumentExpedient_DMD2();
		mets.validateXMLData(documentExpedient, Utils.getDocumentExpedientSchema());
		// es crea al mets l'estructura de metadades de document d'expedient amb les metadades anteriors
		mets.createDMDSec("DMD_2", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", documentExpedient);
		
		// es creen les medadates del document d'expedient, DMD3
		Document documentExpedient2 = createDocumentExpedient_DMD3();
		mets.validateXMLData(documentExpedient2, Utils.getDocumentExpedientSchema());
		// es crea al mets l'estructura de metadades de document d'expedient amb les metadades anteriors
		mets.createDMDSec("DMD_3", "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp", documentExpedient2);
		
		// es creen les medades adminstratives de signatura, AMD1
		Document signatura = createSignatura_AMD1();
		
		mets.createAMDSec("AMD_1", "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura", "AMD_1.0", signatura);
		mets.validateXMLData(signatura, Utils.getDocumentSignaturaSchema());
		
		// creació de les estructures FileGrp dels binaris
		String path = "exemples/exemple2/".replaceAll("/", File.separator);
		InputStream documentHashStream = ClassLoader.getSystemResourceAsStream(path + "documentHash.txt");
		mets.createFileGrp("BIN_1", Base64.decode(Utils.getBytes(documentHashStream)), "BIN_1.0", "text/xml");
		
		InputStream bin2Stream = ClassLoader.getSystemResourceAsStream(path + "bin2.txt");
		mets.createFileGrp("BIN_2", Base64.decode(Utils.getBytes(bin2Stream)), "BIN_2.0", "text/xml");
		
		InputStream bin3Stream = ClassLoader.getSystemResourceAsStream(path + "pdf_B64_BIN3.txt");
		mets.createFileGrp("BIN_3", Base64.decode(Utils.getBytes(bin3Stream)), "BIN_3.0", "application/pdf");
		
		// creació de la secció structMap
		// es crea la rootDiv
		DivType rootDiv = mets.createRootDiv("DMD_1", "Expedient_personal");
		// afegiment de les div inferiors
		DivType divTypeDMD_2 = mets.addDiv(rootDiv, "DMD_2", "Resolucio",true);
		
		DivType subDivTypeDMD_2 = divTypeDMD_2.addNewDiv();
		subDivTypeDMD_2.setLABEL("doc_xmldsig-detached.xml");
		Fptr cmsFPTR = subDivTypeDMD_2.addNewFptr();
		cmsFPTR.setFILEID("BIN_1");
		
		DivType divTypeAMD_1 = mets.addDiv(subDivTypeDMD_2, "AMD_1", "xmldsig-detached.xml",false);
		Fptr fptr_AMD_1 = divTypeAMD_1.addNewFptr();
		fptr_AMD_1.setFILEID("BIN_2");
		
		DivType divTypeDMD_3 = mets.addDiv(rootDiv, "DMD_3", "Solicitud",true);
		DivType subDivTypeDMD_3 = divTypeDMD_3.addNewDiv();
		subDivTypeDMD_3.setLABEL("0561-1363-2009-ENT-0.pdf_signat.pdf");
		Fptr cmsFPTR_DMD3 = subDivTypeDMD_3.addNewFptr();
		cmsFPTR_DMD3.setFILEID("BIN_3");
		
		// validació del mets generat
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
	
	/**
	 * 
	 * @return Document amb les metadades d'un document d'expedient
	 * @throws Exception
	 */
	public static Document createDocumentExpedient_DMD2() throws Exception{
		
		DocumentDocument documentDocument = DocumentDocument.Factory.newInstance();
		DocumentType documentExp = documentDocument.addNewDocument();
		documentExp.setCodiReferencia("a");
		documentExp.setTitol("a");
		documentExp.setDataCreacio(Calendar.getInstance());
		documentExp.setNivellDescripcio("Unitat documental simple");
		documentExp.setSuport("a");
		documentExp.setDocumentEssencial(DocumentEssencial.NO);
		
		return Utils.getResourceAsDocument(documentDocument.newInputStream());
	}
	
	/**
	 * 
	 * @return Document amb les metadades d'un document d'expedient
	 * @throws Exception
	 */
	public static Document createDocumentExpedient_DMD3() throws Exception{
		
		DocumentDocument documentDocument = DocumentDocument.Factory.newInstance();
		DocumentType documentExp = documentDocument.addNewDocument();
		documentExp.setCodiReferencia("a");
		documentExp.setTitol("a");
		documentExp.setDataCreacio(Calendar.getInstance());
		documentExp.setNivellDescripcio("Unitat documental simple");
		documentExp.setSuport("a");
		documentExp.setDocumentEssencial(DocumentEssencial.SÍ);
		
		return Utils.getResourceAsDocument(documentDocument.newInputStream());
	}
	
	/**
	 * 
	 * @return Document amb les metadades d'una signatura
	 * @throws Exception
	 */
	public static Document createSignatura_AMD1() throws Exception{
		
		SignaturaDocument signaturaDocument = SignaturaDocument.Factory.newInstance();
		SignaturaType signatura = signaturaDocument.addNewSignatura();
		signatura.setIdentificador("BIN_1.0_SIG");
		signatura.setIdentificadorDocument("BIN_1.0");
		signatura.setTipusSignatura(TipusSignaturaType.INDEPENDENT_DETACHED);
		signatura.setDataSignatura(Calendar.getInstance());
		
		return Utils.getResourceAsDocument(signaturaDocument.newInputStream());
	}
	
}
