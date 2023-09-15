package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.hp.iarxiu.metsgenerator.exception.MetsException;

/**
 * 
 * @author Toni Marcos
 *
 */
public class Utils {

	
	public static final String IARXIU_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	/**
	 * Prints an XMLObject.
	 * @param xmlObj XMLObject to print
	 */
	public static void printXmlObject(XmlObject xmlObj){	
		//print request message
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
		System.out.println(xmlObj);
	}
	
	
	/** 
	 * Returns the contents of the file in a byte array.
	 */
    public static byte[] getBytesFromFile(String path) throws IOException {
    	
    	InputStream is = ClassLoader.getSystemResourceAsStream(path);
        byte[] bytes = getBytes(is);
        is.close();

        return bytes;
    }
    
    
    /**
     * Gets a byte array from an InputStream.
     * @param is InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] getBytes(InputStream is) throws IOException {
    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	int bytee;
    	while (-1!=(bytee=is.read()))
    	{
    	   baos.write(bytee);
    	}
    	baos.close();
    	byte[] bytes = baos.toByteArray();

        is.close();
        return bytes;
    }
	
    /**
     * Returns a Document from an InputStream
     * @param inputStream
     * @return
     * @throws MetsException
     */
    public static Document getResourceAsDocument(InputStream inputStream) throws MetsException{
    	
    	DocumentBuilder documentBuilder = getDocumentBuilder();
		Document document;
		try {
			document = documentBuilder.parse(inputStream);
		} catch (SAXException e) {
			throw new MetsException("Cannot parse the provided content",e);
		} catch (IOException e) {
			throw new MetsException("Cannot parse the provided content",e);
		}
		return document;
		
    }
    
    /**
     * Gets the document builder
     * @return
     * @throws MetsException
     */
    private static DocumentBuilder getDocumentBuilder()
            throws MetsException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new MetsException(e);
        }
            
        return documentBuilder;
    }
    
    /**
     * Gets the schema of a Expedient
     * @return
     */
	
    public static InputStream getExpedientSchema(){
    	
    	InputStream schema = ClassLoader.getSystemResourceAsStream("resources/xsd/Voc_expedient.xsd");
    	return schema;
    }
    
    /**
     * Gets the schema of a single Document
     * @return
     */
    public static InputStream getDocumentSchema(){
    	
    	InputStream schema = ClassLoader.getSystemResourceAsStream("resources/xsd/Voc_document.xsd");
    	return schema;
    }
    
    /**
     * Gets the schema of a Document Expedient
     * @return
     */
    public static InputStream getDocumentExpedientSchema(){
    	
    	InputStream schema = ClassLoader.getSystemResourceAsStream("resources/xsd/Voc_document_exp.xsd");
    	return schema;
    }
    
    /**
     * Gets the schema of a Signatura
     * @return
     */
    public static InputStream getDocumentSignaturaSchema(){
    	
    	InputStream schema = ClassLoader.getSystemResourceAsStream("resources/xsd/Voc_signatura.xsd");
    	return schema;
    }
    
    /**
     * Format the provided date to iArxiu format date
     * @param date
     * @return
     */
    public static String formatDate(Date date){
    	
	    SimpleDateFormat sdf = new SimpleDateFormat(IARXIU_DATE_FORMAT);
	    return sdf.format(date);
    }
    
    public static String formatCalendar(Calendar calendar){
    	return formatDate(calendar.getTime());
    }
}
