package exemples.exemple5;

import java.io.File;
import java.io.InputStream;

import org.w3c.dom.Document;

import utils.Utils;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe que s'encarregada de parsejar metadades descriptives de disc i validar-les contra el vocabulari d'expedient 
 * @author Toni Marcos
 *
 */
public class MetsTest5 {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MetsException 
	 */
	public static void main(String[] args) throws MetsException, Exception {
		
		//instancia de la llibreria Mets
		Mets mets = new Mets();
		//Càrrega de les metadades descriptives
		String path = "exemples/exemple5/".replaceAll("/", File.separator);
		InputStream dmd1_ok = ClassLoader.getSystemResourceAsStream(path + "dmd1_ok.xml");
		
		Document docXMLData_ok = Utils.getResourceAsDocument(dmd1_ok);
		// Càrrega del schema de vocabulari d'expedient mitjançant la classe d'utilitats de la llibreria
		InputStream expedientSchema = Utils.getExpedientSchema();
		mets.validateXMLData(docXMLData_ok, expedientSchema);
	}

}
