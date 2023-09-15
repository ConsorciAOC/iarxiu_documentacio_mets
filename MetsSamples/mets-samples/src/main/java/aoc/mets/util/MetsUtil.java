package aoc.mets.util;

import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.hp.iarxiu.metsgenerator.Mets;
import com.hp.iarxiu.metsgenerator.exception.MetsException;

public class MetsUtil {
				
	public static DivType initStructMap(Mets mets, String dmdId, String label){		
		DivType rootDiv;
		rootDiv = mets.createRootDiv(dmdId, null);
		rootDiv.setLABEL(label);		
		return rootDiv;
	}
	
	/*
	 * 	El paràmetre descriptive indica si les metadades són descriptives o administratives. 
	 *  Si descriptive=true l'id que es passa s'afegeix al node com DMDID. 
	 *  Si descriptive=false l'id que es passa s'afegeix al node com AMDID. 
	 */
	public static DivType addDiv( Mets mets, DivType rootDiv, String id, boolean descriptive, String label, String fileId) throws MetsException{		
		DivType divTypeDMD_1 = mets.addDiv(rootDiv, id, label,descriptive);		
		divTypeDMD_1.setLABEL(label);
		Fptr docFPTR = divTypeDMD_1.addNewFptr();
		docFPTR.setFILEID(fileId);		
		return divTypeDMD_1;
	}
				
	public static void createZipPIT(String zipFilePath, String folderToZip) throws ZipException, IOException{
		ZipUtil zipUtil = new ZipUtil(
				zipFilePath.replaceAll("/", File.separator),
				folderToZip.replaceAll("/", File.separator));
		zipUtil.zipIt();		
	}
	
	/* Mètode creat per poder actualitzar una metadada del paquet per poder ingresar-lo sense que el control
	 * d'unicitat el detecti com a repetit i no permeti l'ingrés
	 */
	public static void updateMdDmdFile(String xmlFilePath, String tagnameNodeToModify) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		File xmlFile = new File(xmlFilePath);
		InputStream is = new FileInputStream(xmlFile);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(is);		
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String formattedDate = sdf.format(now);		
		Node dataCreacioNode = doc.getElementsByTagName(tagnameNodeToModify).item(0);
		dataCreacioNode.setTextContent(formattedDate);	
		saveXmlToFile(xmlFilePath, doc);		
	}
	
	private static void saveXmlToFile(String filePath, Document doc) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filePath));
		transformer.transform(source, result);		
	}

	public static void createIfNotExistsDirectory(String pathOutDir) throws Exception {
		File directory = new File(pathOutDir);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

}
