package exemples.exemple7;

import java.io.File;
import java.io.InputStream;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe que s'encarregada 
 * d'obtenir el hash d'un document word
 * d'obtenir la codificació d'un mets, prèviament carregat 
 * @author Toni Marcos
 *
 */
public class MetsTest7 {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws MetsException 
	 */
	public static void main(String[] args) throws MetsException, Exception {

		String path = "exemples/exemple7/".replace("/", File.separator);
		InputStream metsStream = ClassLoader.getSystemResourceAsStream(path + "mets.xml");
		//parseig del Mets
		Mets mets = new Mets(metsStream);
		String encoding = mets.getEncoding();
		System.out.println("Encoding: "+ encoding);
		//Càrrega del document per generar el checksum
		InputStream documentStream = ClassLoader.getSystemResourceAsStream(path + "documento.doc");
		String sha1File = mets.generateSha1File(documentStream);
		System.out.println("Checksum: " + sha1File);
	}

}
