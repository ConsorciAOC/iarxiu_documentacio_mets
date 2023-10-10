package exemples.exemple4;

import java.io.File;
import java.io.InputStream;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * Classe que s'encarrega de parsejar un mets de disc i validar-lo 
 * @author Toni Marcos
 *
 */
public class MetsTest4 {

	/**
	 * @param args
	 * @throws MetsException
	 * @throws Exception
	 */
	public static void main(String[] args) throws MetsException, Exception {
		
		//Càrrega del Mets
		String path = "exemples/exemple4/".replace("/", File.separator);
		InputStream metsStream = ClassLoader.getSystemResourceAsStream(path + "mets.xml");
		// Parseig del mets
		Mets mets = new Mets(metsStream);
		// validació del mets parsejat
		mets.validate();
	}

}
