package com.hp.iarxiu.metsgenerator;

import com.hp.iarxiu.metsgenerator.exception.MetsException;

import gov.loc.mets.AmdSecType;
import gov.loc.mets.DivType;
import gov.loc.mets.FileGrpType;
import gov.loc.mets.FileType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.StructMapType;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.FileType.CHECKSUMTYPE;
import gov.loc.mets.FileType.FContent;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.FileType.FLocat.LOCTYPE;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MdSecType.MdWrap.MDTYPE;
import gov.loc.mets.MdSecType.MdWrap.XmlData;
import gov.loc.mets.MetsType.FileSec;
import gov.loc.mets.MetsType.FileSec.FileGrp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.iarxiu.xmlschemavalidator.XMLSchemaValidator;
import com.hp.iarxiu.xmlschemavalidator.XMLSchemaValidatorException;


/**
 * This class provides some functionalities for facilitating METS creation and manipulation.
 * 
 * @author Toni Marcos Cardona
 */
public class Mets {

    private final Log log = LogFactory.getLog(getClass());

    private MetsDocument metsDocument;
    
    private int levels;
    private Collection<String> compressedFiles = new LinkedList<String>();

    /**
     * MIME type for PREMIS contents.
     */
    private static final String PREMIS_OBJECT_MIMETYPE = "text/xml";

    
    /**
     * Creates an empty Mets instance 
     */
    public Mets() {
    	init();
    }
    
    private void init(){
    	XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setValidateOnSet();
        metsDocument = MetsDocument.Factory.newInstance(xmlOptions);

        metsDocument.addNewMets();
        metsDocument.getMets().addNewStructMap();
        metsDocument.getMets().addNewFileSec();

        levels = 0;
    }
    
    
    /**
     * Creates a new Mets instance with the internal METS representation
     * parameter.
     *
     * @param mets Mets
     * @throws MetsException
     */
    public Mets(gov.loc.mets.MetsDocument.Mets mets) throws MetsException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setValidateOnSet();
        
		try
		{
			this.metsDocument = MetsDocument.Factory.parse(mets.getDomNode(), xmlOptions);	
		}
		catch (XmlException e)
		{
			throw new MetsException("Error parsing METS contents", e);
		}


        levels = calculateLevels();
    }

    /**
     * Creates a new Mets instance with the internal METS representation
     * received as a File parameter.
     *
     * @param file File
     * @throws MetsException
     * @throws IOException
     */
    public Mets(File file) throws MetsException, IOException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setValidateOnSet();

        try {
            metsDocument = MetsDocument.Factory.parse(file, xmlOptions);
        } catch (XmlException e) {
            throw new MetsException("XML error generating Mets from file", e);
        }

        levels = calculateLevels();
    }

    /**
     * Creates a new Mets instance with the internal METS representation readed
     * from the InputStream parameter.
     * 
     * @param is
     * @throws MetsException
     * @throws IOException
     */
    public Mets(InputStream is) throws MetsException, IOException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setValidateOnSet();

        try {
            metsDocument = MetsDocument.Factory.parse(is, xmlOptions);
            
        } catch (XmlException e) {
            throw new MetsException(
                    "XML error generating Mets from input stream", e);
        }

        levels = calculateLevels();
    }

    /**
     * Creates a new Mets instance with the internal org.w3c.Document representation
     * received as parameter.
     * 
     * @param doc
     * @throws MetsException
     */
    public Mets(Document doc) throws MetsException {
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setValidateOnSet();

        try {
            metsDocument = MetsDocument.Factory.parse(doc, xmlOptions);
            
        } catch (XmlException e) {
            throw new MetsException(
                    "XML error generating Mets from input stream", e);
        }

        levels = calculateLevels();
    }

    private Collection<String> getCompressedFiles() {
        return compressedFiles;
    }

    private void setCompressedFiles(Collection<String> compressedFiles) {
        this.compressedFiles = compressedFiles;
    }

   

    /**
     * Gets the internal Mets representation.
     * 
     * @return
     */
    private gov.loc.mets.MetsDocument.Mets getInternalMets() {
        return metsDocument.getMets();
    }

   /**
    * Creates the root div in the METS StructMap section.
    * @param dmdId
    * @param label
    * @return
    */
    public DivType createRootDiv(String dmdId,String label) {
        StructMapType structMap = metsDocument.getMets().getStructMapArray(0);
        DivType rootDiv = structMap.addNewDiv();
        if(label!=null){
        	rootDiv.setLABEL(label);
        }
        setDMDID(rootDiv, dmdId);
        levels++;
        return rootDiv;
    }

    /**
     * Add a new root child division.
     * @param divType
     * @param id
     * @param label
     * @param descriptive
     * @return
     * @throws MetsException
     */
    public DivType addDiv(DivType divType, String id, String label, boolean descriptive) throws MetsException {
        if (divType == null)
            throw new MetsException(
                    "The division must exists in order to add new divisions");

        DivType newDiv = divType.addNewDiv();
        if(label!=null){
        	newDiv.setLABEL(label);
        }
        if(id!=null){
        	if(descriptive){
            	setDMDID(newDiv, id);
            }else{
            	setAMDID(newDiv, id);
            }	
        }
        
        return newDiv;
    }
   
    /**
     * Add a new file division with the division provided
     * @param divType
     * @param label
     * @param fileId
     * @return
     * @throws MetsException
     */

    public DivType addFileDiv(DivType divType, String label, String fileId) throws MetsException {
        if (divType == null)
            throw new MetsException(
                    "The division must exists in order to add new divisions");

        DivType newDiv = divType.addNewDiv();
        newDiv.setLABEL(label);
        Fptr newFptr = newDiv.addNewFptr();
        newFptr.setFILEID(fileId);

        return newDiv;
    }
    
    
    /**
     * Adds the DMD Section identifier dmdId to the div DMDID list.
     * 
     * @param div
     * @param dmdId
     */
    @SuppressWarnings("unchecked")
    private void setDMDID(DivType div, String dmdId) {
        List dmdIds = div.getDMDID();
        if (dmdIds == null) {
            dmdIds = new ArrayList();
            dmdIds.add(dmdId);
            div.setDMDID(dmdIds);
        } else {
            // Generate a ArrayList copy of the DMDID original XmlSimpleList
            // because this is immutable
            List newDmdIds = new ArrayList();
            newDmdIds.addAll(dmdIds);
            newDmdIds.add(dmdId);
            /*
             * Iterator<String> it = dmdIds.iterator(); while (it.hasNext()) {
             * newDmdIds.add(it.next()); } newDmdIds.add(dmdId);
             */
            div.setDMDID(newDmdIds);
        }

        log.debug("Finishing method setDMDID");
    }

    /**
     * Adds the DMD Section identifier dmdId to the div DMDID list.
     * 
     * @param div
     * @param amdId
     */
    @SuppressWarnings("unchecked")
    private void setAMDID(DivType div, String amdId) {
        List amdIds = div.getADMID();
        if (amdIds == null) {
        	amdIds = new ArrayList();
        	amdIds.add(amdId);
            div.setADMID(amdIds);
        } else {
            // Generate a ArrayList copy of the DMDID original XmlSimpleList
            // because this is immutable
            List newAmdIds = new ArrayList();
            newAmdIds.addAll(amdIds);
            newAmdIds.add(amdId);
            /*
             * Iterator<String> it = dmdIds.iterator(); while (it.hasNext()) {
             * newDmdIds.add(it.next()); } newDmdIds.add(dmdId);
             */
            div.setADMID(newAmdIds);
        }

        log.debug("Finishing method setDMDID");
    }
    

    /**
     * Return the depth of the METS's StructMap section.
     * 
     * @return
     * @throws MetsException
     */
    public int getLevels() throws MetsException {
        return levels;
    }

  
    /**
     * Recursive method to find a div with the DMDID provided.
     * 
     * @param div
     * @param dmdId
     * @return
     */
    private boolean findDivByDMDID(DivType div, String dmdId){
    	
    	Assert.notNull(div, "Div parameter must not be null");
    	boolean found = false;
    	if(div.getDMDID()!=null && div.getDMDID().contains(dmdId)){
    		return true;
    	}
    	DivType[] childDivs = div.getDivArray();
    	for(int divIdx = 0; !found && divIdx < childDivs.length; divIdx++){
    		DivType childDiv = childDivs[divIdx];
    		if(childDiv.getDMDID() != null && childDiv.getDMDID().contains(dmdId)){
    			return true;
    		}
    		else
    			found = findDivByDMDID(childDiv, dmdId);
    	}
    	
    	return found;
    }
    
    /**
     * Method to find a div with the DMDID provided in the StructMap section.
     * 
     * @param dmdId
     * @return
     */
    public boolean findDiv(String dmdId){
    	
    	return findDivByDMDID(getRootDiv(), dmdId);
    	
    }
    
    /**
     * Return the content of the DMD section with the specified id.
     * 
     * @param id
     * @return
     * @throws MetsException
     */
    public InputStream getDMDAsStream(String id) throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:dmdSec[@ID='" + id + "']";

        String queryText = metsNsDecl + pathText;

        XmlObject[] dmdSec = (XmlObject[]) metsDocument.selectPath(queryText);
        InputStream is = null;
        if (dmdSec.length > 0) {
            XmlData xmlData = ((MdSecType) dmdSec[0]).getMdWrap().getXmlData();
            if (xmlData != null)
                is = xmlData.newInputStream();
        } else
            throw new MetsException("DMD " + id + " not exists");

        return is;
    }

    /**
     * Return the content of the DMD section with the specified id.
     * 
     * @param id
     * @return
     * @throws MetsException
     */
    public Document getDMD(String id) throws MetsException {
        InputStream is = getDMDAsStream(id);
        Document dmd = null;
        if (is != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();

                dmd = builder.parse(is);
            } catch (ParserConfigurationException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (SAXException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (IOException e) {
                throw new MetsException("I/O Error generating DMD document", e);
            }
        }

        return dmd;
    }

    /**
     * Returns an array of the DMD identifiers in the Mets.
     * 
     * @return
     * @throws MetsException
     */
    public String[] getDMDIds() throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:dmdSec";

        String queryText = metsNsDecl + pathText;
        XmlObject[] dmdSecs = (XmlObject[]) metsDocument.selectPath(queryText);

        String[] dmsIds = new String[dmdSecs.length];
        for (int i = 0; i < dmsIds.length; i++) {
            String dmdId = ((MdSecType) dmdSecs[i]).getID();
            dmsIds[i] = dmdId;
        }

        return dmsIds;
    }

  
    /**
     * Set the content of the DMD section with the specified id. Modifies the
     * content if already exists.
     * 
     * @param id
     * @throws MetsException
     * @throws IOException
     */
    public void setDMD(String id, InputStream doc) throws MetsException,
            IOException {

        // Step 1: XPath DMD search
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:dmdSec[@ID='" + id + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] dmdSec = (XmlObject[]) metsDocument.selectPath(queryText);

        if (dmdSec.length == 0)
            throw new MetsException("DMD section " + id + " not found");

        // Step 2: Set DMD content
        try {
            MdWrap mdWrap = ((MdSecType) dmdSec[0]).getMdWrap();
            XmlData xmlData = mdWrap.getXmlData();
            if (xmlData == null) {
                xmlData = mdWrap.addNewXmlData();
                XmlObject xmlObject = XmlObject.Factory.parse(doc);
                xmlData.set(xmlObject);
            } else {
                XmlObject xmlObject = XmlObject.Factory.parse(doc);
                xmlData.set(xmlObject);
            }
        } catch (XmlException e) {
            throw new MetsException("XML error adding DMD", e);
        }
    }

    /**
     * Set the content of the DMD section with the specified id. Modifies the
     * content if already exists.
     * 
     * @param id
     * @param doc
     * @throws MetsException
     */
    public void setDMD(String id, Document doc) throws MetsException {

        // Step 1: XPath DMD search
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:dmdSec[@ID='" + id + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] dmdSec = (XmlObject[]) metsDocument.selectPath(queryText);

        if (dmdSec.length == 0)
            throw new MetsException("DMD section " + id + " not found");

        // Step 2: Set DMD content
        try {
            MdWrap mdWrap = ((MdSecType) dmdSec[0]).getMdWrap();
            XmlData xmlData = mdWrap.getXmlData();
            if (xmlData == null) {
                xmlData = mdWrap.addNewXmlData();
                XmlObject xmlObject = XmlObject.Factory.parse(doc);
                xmlData.set(xmlObject);
            } else {
                XmlObject xmlObject = XmlObject.Factory.parse(doc);
                xmlData.set(xmlObject);
            }
        } catch (XmlException e) {
            throw new MetsException("XML error adding DMD", e);
        }
    }

    
    /**
     * Return the content of the DMD section with the specified id.
     * 
     * @param id
     * @return
     * @throws MetsException
     */
    public InputStream getAMDAsStream(String id) throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:amdSec[@ID='" + id + "']";

        String queryText = metsNsDecl + pathText;

        XmlObject[] amdSec = (XmlObject[]) metsDocument.selectPath(queryText);
        InputStream is = null;
        if (amdSec.length > 0) {
            XmlData xmlData = ((MdSecType) amdSec[0]).getMdWrap().getXmlData();
            if (xmlData != null)
                is = xmlData.newInputStream();
        } else
            throw new MetsException("AMD " + id + " not exists");

        return is;
    }
    
    /**
     * Return the content of the AMD section with the specified id.
     * 
     * @param id
     * @return
     * @throws MetsException
     */
    public Document getAMD(String id) throws MetsException {
        InputStream is = getAMDAsStream(id);
        Document amd = null;
        if (is != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();

                amd = builder.parse(is);
            } catch (ParserConfigurationException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (SAXException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (IOException e) {
                throw new MetsException("I/O Error generating DMD document", e);
            }
        }

        return amd;
    }
    
    /**
     * Returns the URI of the meta-data section with the specified id.
     * 
     * @param id
     * @return
     * @throws MetsException
     */
    private String getVocabularyURI(String id) throws MetsException {

        // Step 1: Search the MD in the AMD sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:amdSec[@ID='" + id + "']";
        String queryText = metsNsDecl + pathText;
        
        String uri = null;
        
        // Search the MD in the DMD sections.
        if (uri == null) {
            pathText = "$this/METS:mets/METS:dmdSec[@ID='" + id + "']";
            queryText = metsNsDecl + pathText;
            XmlObject[] dmdSec = (XmlObject[]) metsDocument
                    .selectPath(queryText);

            if (dmdSec.length > 0)
                uri = ((MdSecType) dmdSec[0]).getMdWrap().getOTHERMDTYPE();
        }

        if (uri == null)
            throw new MetsException("Vocabulary " + id + " not found");

        return uri;
    }

  
    /**
     * Returns the content of this mets instance in a byte array
     * 
     * @return
     * @throws IOException
     * @throws MetsException
     */
    public byte[] getBytes() throws IOException, MetsException {
        InputStream is = getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        return bytes;
    }

    /**
     * Returns a input stream of this mets instance.
     * 
     * @return
     * @throws IOException
     * @throws MetsException
     */
    public InputStream getInputStream() throws IOException, MetsException {
        XmlDocumentProperties xmlDocumentProperties = metsDocument
                .documentProperties();
        String encoding = xmlDocumentProperties.getEncoding();

        log.debug("METS encoding " + encoding);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setCharacterEncoding(encoding);
        InputStream is = metsDocument.newInputStream(xmlOptions);

        return is;
    }

    /**
     * Returns an identifiers array of the binary files in the Mets.
     * 
     * @return
     */
    public String[] getB64FilesId() {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        List<String> binaryFilesId = new ArrayList<String>();
        for (int i = 0; i < file.length; i++) {
            if (((FileType) file[i]).getFContent() != null)
                binaryFilesId.add(((FileType) file[i]).getID());
        }

        return (String[]) binaryFilesId.toArray(new String[] {});
    }

    /**
     * Returns an identifiers array of the referenced files in the Mets.
     * 
     * @return
     */
    public String[] getReferencedFilesId() {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        List<String> referencedFilesId = new ArrayList<String>();
        for (int i = 0; i < file.length; i++) {
            if (((FileType) file[i]).getFLocatArray().length > 0)
                referencedFilesId.add(((FileType) file[i]).getID());
        }

        return (String[]) referencedFilesId.toArray(new String[] {});
    }

    /**
     * Returns the content type of a file.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     */
    public String getFileContentType(String fileID) throws MetsException {
        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        if (file.length == 0)
            throw new MetsException("File " + fileID + " not found");

        return ((FileType) file[0]).getMIMETYPE();
    }

    /**
     * Set the content type of a file.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     */
    public void setFileContentType(String fileID, String mimeType)
            throws MetsException {
        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        if (file.length == 0)
            throw new MetsException("File " + fileID + " not found");

        ((FileType) file[0]).setMIMETYPE(mimeType);
    }

    /**
     * Sets the package template.
     * 
     * @param templateURN
     */
    public void setTemplate(String templateURN) {
        metsDocument.getMets().setTYPE(templateURN);
    }

    /**
     * Gets the package template.
     * 
     * @return
     */
    public String getTemplate() {
        return metsDocument.getMets().getTYPE();
    }

  
    /**
     * Return the file id's that belongs to the specified file group.
     * 
     * @param fileGrpID
     * @return
     */
    public String[] getFileIDs(String fileGrpID) {
        // Step 1: Search the file group in the file sections
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp[@ID='"
                + fileGrpID + "']/METS:file";
        String queryText = metsNsDecl + pathText;
        XmlObject[] files = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Get the file id's in this file group
        String[] fileIDs = new String[files.length];
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++)
                fileIDs[i] = ((FileType) files[i]).getID();
        }

        return fileIDs;
    }

    /**
     * Return all 1st version file id's.
     * 
     * @return
     */
    public String[] getFileIDs() {
        // Step 1: Search the file group in the file sections
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp";
        String queryText = metsNsDecl + pathText;
        XmlObject[] fileGrps = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Get the file id's in this file group
        String[] fileIDs = new String[fileGrps.length];
        for (int i = 0; i < fileGrps.length; i++){
            if(((FileGrpType) fileGrps[i]).getFileArray().length > 0)
                fileIDs[i] = ((FileGrpType) fileGrps[i]).getFileArray()[0].getID();
        }
        
        return fileIDs;
    }
    
    /**
     * Return all file id's.
     * 
     * @return
     */
    public String[] getAllFileIDs() {
        // Step 1: Search the file group in the file sections
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file";
        String queryText = metsNsDecl + pathText;
        XmlObject[] files = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Get the file id's in this file group
        String[] fileIDs = new String[files.length];
        for (int i = 0; i < files.length; i++)
        	fileIDs[i] = ((FileType) files[i]).getID();
        
        return fileIDs;
    }
    
    /**
     * Returns a file content.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     * @throws IOException
     */
    public InputStream getFileContent(String fileID) throws MetsException {

        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] files = (XmlObject[]) metsDocument.selectPath(queryText);

        InputStream content;
        if (files.length > 0) {
            FileType file = (FileType) files[0];
            if (file.getFContent() != null) {
            	content = new ByteArrayInputStream(file.getFContent().getBinData());
            } else if(file.getFLocatArray() != null && file.getFLocatArray().length > 0){
                try {
                	content = new FileInputStream(file
                            .getFLocatArray(0).getHref());
                } catch (Exception e) {
                    throw new MetsException(
                            "Error while getting file content ("
                                    + file.getFLocatArray(0).getHref() + ")", e);
                }
            } else
                throw new MetsException("File content not found");

        } else
            throw new MetsException("File " + fileID + " not found");

        return content;
    }
    
    /**
     * Returns the file content bytes.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     * @throws IOException
     */
    public byte[] getFileBytes(String fileID) throws MetsException {

        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] files = (XmlObject[]) metsDocument.selectPath(queryText);

        byte[] content;
        if (files.length > 0) {
            FileType file = (FileType) files[0];
            if (file.getFContent() != null) {
            	content = file.getFContent().getBinData();
            } else if(file.getFLocatArray() != null && file.getFLocatArray().length > 0){ 
                try {
                	 InputStream is = new FileInputStream(file
                             .getFLocatArray(0).getHref());
                     try {
                    	 content = IOUtils.toByteArray(is);
                     } finally {
                         is.close();
                     }
                } catch (Exception e) {
                    throw new MetsException(
                            "Error while getting file content ("
                                    + file.getFLocatArray(0).getHref() + ")", e);
                }
            } else
                throw new MetsException("File content not found");

        } else
            throw new MetsException("File " + fileID + " not found");

        return content;
    }

    /**
     * Return the depth of the METS's StructMap section.
     * 
     * @return
     * @throws MetsException
     */
    public int calculateLevels() throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String path1LevelWithFile = "$this/METS:mets/METS:structMap/METS:div/METS:div/METS:fptr";
        String path2LevelsWithFile = "$this/METS:mets/METS:structMap/METS:div/METS:div/METS:div/METS:fptr";
        String path2Levels = "$this/METS:mets/METS:structMap/METS:div/METS:div";
        String path1Level = "$this/METS:mets/METS:structMap/METS:div";

        String queryText = metsNsDecl + path1LevelWithFile;
        XmlObject[] divs = (XmlObject[]) metsDocument.selectPath(queryText);

        if (divs.length > 0) {
            levels = 1;
        } else {
            queryText = metsNsDecl + path2LevelsWithFile;
            divs = (XmlObject[]) metsDocument.selectPath(queryText);
            if (divs.length > 0)
                levels = 2;
            else {
                queryText = metsNsDecl + path2Levels;
                divs = (XmlObject[]) metsDocument.selectPath(queryText);
                if (divs.length > 0)
                    levels = 2;
                else {
                    queryText = metsNsDecl + path1Level;
                    divs = (XmlObject[]) metsDocument.selectPath(queryText);
                    if (divs.length > 0)
                        levels = 1;
                }
            }
        }

        if (levels == 0)
            throw new MetsException("Unknown number of levels");

        return levels;
    }

    @Override
    public String toString() {
    	return toString("UTF-8");
    }
    
    /**
     * Returns XML contents for the current METS, using the specified character encoding.
     * 
     * @param characterEncoding character encoding to use.
     * @return mets content contents as string.
     */
    public String toString(final String characterEncoding) {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setCharacterEncoding(characterEncoding);
		
		MetsDocument metsDocument;
		try {
			metsDocument = MetsDocument.Factory.parse(getInternalMets().getDomNode());
		} catch(Throwable e) {
			return null;
		}
		
		return metsDocument.xmlText(xmlOptions);
    }
    
    /**
     * Save XML contents for the current mets to the given output stream, using UTF-8 as the character encoding.
     * 
     * @param outputStream output stream to write to.
     * @throws MetsException parsing METS contents.
     */
    public void save(OutputStream outputStream) throws MetsException {
    	save("utf-8", outputStream);
    }
    
    /**
     * Stores XML contents for the current mets.
     * 
     * @param characterEncoding character encoding identifier to use.
     * @param outputStream output stream to use.
     * @throws MetsException parsing METS contents.
     */
    public void save(String characterEncoding, OutputStream outputStream) throws MetsException {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setCharacterEncoding(characterEncoding);
		
    	MetsDocument metsDocument;
		try {
			metsDocument = MetsDocument.Factory.parse(getInternalMets().getDomNode());
			metsDocument.save(outputStream, xmlOptions);
		} catch(XmlException e) {
			throw new MetsException(
					"Error parsing mets contents",
					e
			);
		} catch(IOException e) {
			throw new MetsException(
					"Error writing mets contents to output stream",
					e
			);
		}
    }

    /**
     * Generates SHA-1 checksum of the data argument.
     * 
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String generateSha1File(byte[] bytes)
            throws MetsException {
    	try{
	        MessageDigest md = MessageDigest.getInstance("SHA");
	        md.update(bytes);
	        byte[] digest = md.digest();
	
	        return convertToHex(digest);
            }catch (Exception e) {
    			throw new MetsException(e);
    		}
    }
    
    /**
     * Generates SHA-1 checksum of the data argument.
     * 
     * @param fileIs
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException 
     */
    public String generateSha1File(InputStream fileIs)
            throws MetsException {
    	
    	try{
    		MessageDigest md = MessageDigest.getInstance("SHA");
            
    		byte[] buffer = new byte[1024];
    		int numRead;
    		do {
    		  numRead = fileIs.read(buffer);
    		  if (numRead > 0) {
    			  md.update(buffer, 0, numRead);
    		  }
    		} while (numRead != -1);
            
            byte[] digest = md.digest();

            return convertToHex(digest);
    	}catch (Exception e) {
			throw new MetsException(e);
		}
        
    }

  
    /**
     * Returns the StructMap root division.
     * 
     * @return
     */
    public DivType getRootDiv() {
        StructMapType structMap = metsDocument.getMets().getStructMapArray(0);
        DivType result = null;
        if (structMap.getDiv() != null) {
        	result = structMap.getDiv();
        }
        return result;
    }

    /**
     * @param fileGrpId
     * @param is
     * @param mimeType
     * @return
     * @throws MetsException
     * @throws IOException
     */
    public void addBase64FileVersion(String fileGrpId, String fileId, InputStream is,
            String mimeType) throws MetsException, IOException {

        // Step 1: File validation
        if (is == null)
            throw new MetsException("Unable to read binary file");

        // Step 2: Read the input file
        byte[] binData = IOUtils.toByteArray(is);

        // Step 3: Add the new content file to the File Section
        addBase64FileVersion(fileGrpId, fileId, binData, mimeType);

    }


    /**
     * @param fileGrpId
     * @param binData
     * @param mimeType
     * @return
     * @throws MetsException
     */
    private String addBase64FileVersion(String fileGrpId, String fileId, byte[] binData,
            String mimeType) throws MetsException {

        // Step 1: Search the file group.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp[@ID='"
                + fileGrpId + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] fileGrps = (XmlObject[]) metsDocument.selectPath(queryText);

        // // Step 2: Check the file group exists.
        if (fileGrps.length == 0)
            throw new MetsException("File group " + fileGrpId + " not found");

        FileGrpType fileGrp = (FileGrpType) fileGrps[0];

        // Step 3: SHA-1 checksum generation
        String checksum = generateSha1File(binData);
        
        FileType file = fileGrp.addNewFile();
        file.setID(fileId);
        file.setMIMETYPE(mimeType);
        file.setCHECKSUM(checksum);
        file.setCHECKSUMTYPE(CHECKSUMTYPE.SHA_1);
        file.setCREATED(Calendar.getInstance());

        FContent fContent = file.addNewFContent();
        fContent.setBinData(binData);

        log.info("Added new content file version " + fileId);

        return fileId;
    }

   

    /**
     * @param fileGrpId
     * @param fileIs
     * @param mimeType
     * @return
     * @throws MetsException
     */
    public String addReferencedFileVersion(String fileGrpId,String fileId, String href, InputStream fileIs,
            String mimeType) throws MetsException {

        // Step 1: Search the file group.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp[@ID='" + fileGrpId + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] fileGrps = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Check the file group exists.
        if (fileGrps.length == 0)
            throw new MetsException("File group " + fileGrpId + " not found");

        FileGrpType fileGrp = (FileGrpType) fileGrps[0];

        // Step 3: SHA-1 checksum generation
        String checksum = generateSha1File(fileIs);
        
        FileType file = fileGrp.addNewFile();
        file.setID(fileId);
        if (mimeType != null)
            file.setMIMETYPE(mimeType);
        file.setCHECKSUM(checksum);
        file.setCHECKSUMTYPE(CHECKSUMTYPE.SHA_1);
        file.setCREATED(Calendar.getInstance());

        FLocat fLocat = file.addNewFLocat();
        fLocat.setHref(href);
        fLocat.setLOCTYPE(LOCTYPE.URL);

        log.info("Added new referenced file version " + fileId);

        return fileId;
    }

    /**
     * Returns a file identifiers list filtered by content type.
     * 
     * @param contentType
     * @return
     */
    private String[] getFileIdsByContentType(String contentType) {
        // Step 1: Search the files by content type in the file sections
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@MIMETYPE='"
                + contentType + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] files = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Get the file id's
        String[] fileIDs = new String[files.length];
        for (int i = 0; i < files.length; i++)
            fileIDs[i] = ((FileType) files[i]).getID();

        return fileIDs;
    }

    /**
     * Returns the checksum of a file decoded.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     */
    public byte[] getFileChecksum(String fileID) throws MetsException {
        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        String checksumHex = null;
        if (file.length > 0)
            checksumHex = ((FileType) file[0]).getCHECKSUM();

        byte[] checksumBin = null;
        try {
            checksumBin = Hex.decodeHex(checksumHex.toCharArray());
        } catch (DecoderException e) {
            throw new MetsException("Error decoding file checksum", e);
        }

        return checksumBin;
    }

    /**
     * Returns the checksum of a file.
     * 
     * @param fileID
     * @return
     * @throws MetsException
     */
    public CHECKSUMTYPE.Enum getFileChecksumType(String fileID)
            throws MetsException {
        // Step 1: Search the file in the file sections.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp/METS:file[@ID='"
                + fileID + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] file = (XmlObject[]) metsDocument.selectPath(queryText);

        CHECKSUMTYPE.Enum checksumType = null;
        if (file.length > 0)
            checksumType = ((FileType) file[0]).getCHECKSUMTYPE();

        return checksumType;
    }
    
    private boolean containsFileGrpId(final String fileGrpId) {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp[@ID='" + fileGrpId + "']";
        String queryText = metsNsDecl + pathText;
        XmlObject[] fileGrps = (XmlObject[]) metsDocument.selectPath(queryText);
    	
    	return fileGrps != null && fileGrps.length > 0;
    }

    /**
     * Returns the file group identifier of a file.
     * 
     * @param fileId
     * @return
     */
    public String getFileGrpId(String fileId) {
        // Step 1: Get all file groups.
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:fileSec/METS:fileGrp";
        String queryText = metsNsDecl + pathText;
        XmlObject[] fileGrps = (XmlObject[]) metsDocument.selectPath(queryText);

        // Step 2: Search the file in all file groups.
        String fileGrpId = null;
        boolean found = (fileGrpId != null);
        for (int i = 0; i < fileGrps.length && !found; i++) {
            FileGrpType fileGrp = (FileGrpType) fileGrps[i];
            FileType[] files = fileGrp.getFileArray();
            for (int j = 0; j < files.length && !found; j++) {
                FileType file = files[j];
                if (file.getID().equals(fileId))
                    fileGrpId = fileGrp.getID();
            }
        }

        return fileGrpId;
    }

    /**
     * Returns the folder division that file group belongs.
     * 
     * @param fileGrpId
     * @return
     * @throws MetsException
     */
    public DivType getFolderDiv(String fileGrpId) throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText;

        if (getLevels() == 1)
            pathText = "$this/METS:mets/METS:structMap/METS:div";
        else if (getLevels() == 2)
            pathText = "$this/METS:mets/METS:structMap/METS:div/METS:div";
        else
            throw new MetsException("Invalid number of levels");

        String queryText = metsNsDecl + pathText;
        XmlObject[] folderDivs = (XmlObject[]) metsDocument
                .selectPath(queryText);

        DivType result = null;
        boolean found = (result != null);
        for (int i = 0; i < folderDivs.length && !found; i++) {
            DivType folderDiv = (DivType) folderDivs[i];
            DivType[] fileDivs = folderDiv.getDivArray();
            for (int j = 0; j < fileDivs.length && !found; j++) {
                DivType fileDiv = fileDivs[j];
                if (((Fptr) fileDiv.getFptrArray()[0]).getFILEID().equals(
                        fileGrpId))
                    result = fileDiv;
            }
        }

        return result;
    }

    /**
     * Returns the file division that file group belongs.
     * 
     * @param fileGrpId
     * @return
     * @throws MetsException
     */
    public DivType getFileDiv(String fileGrpId) throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText;

        if (getLevels() == 1)
            pathText = "$this/METS:mets/METS:structMap/METS:div/METS:div";
        else if (getLevels() == 2)
            pathText = "$this/METS:mets/METS:structMap/METS:div/METS:div/METS:div";
        else
            throw new MetsException("Invalid number of levels");

        String queryText = metsNsDecl + pathText;
        XmlObject[] fileDivs = (XmlObject[]) metsDocument.selectPath(queryText);

        DivType result = null;
        boolean found = (result != null);
        for (int i = 0; i < fileDivs.length && !found; i++) {
            DivType fileDiv = (DivType) fileDivs[i];
            if (((Fptr) fileDiv.getFptrArray()[0]).getFILEID()
                    .equals(fileGrpId))
                result = fileDiv;
        }

        return result;
    }

    /**
     * Return the content of the DMD section with the specified id and
     * vocabulary uri.
     * 
     * @param id
     * @param vocUri
     * @return
     */
    private InputStream getDMDAsStream(String id, String vocUri) {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText = "$this/METS:mets/METS:dmdSec[@ID='" + id
                + "']/METS:mdWrap[@OTHERMDTYPE='" + vocUri + "']";

        String queryText = metsNsDecl + pathText;

        XmlObject[] mdWrap = (XmlObject[]) metsDocument.selectPath(queryText);
        InputStream is = null;
        if (mdWrap.length > 0)
            is = ((MdWrap) mdWrap[0]).getXmlData().newInputStream();

        return is;
    }

    /**
     * Return the content of the DMD section with the specified id and
     * vocabulary uri.
     * 
     * @param id
     * @param vocUri
     * @return
     * @throws MetsException
     */
    private Document getDMD(String id, String vocUri) throws MetsException {
        InputStream is = getDMDAsStream(id, vocUri);
        Document dmd = null;
        if (is != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();

                dmd = builder.parse(is);
            } catch (ParserConfigurationException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (SAXException e) {
                throw new MetsException("Error generating DMD document", e);
            } catch (IOException e) {
                throw new MetsException("I/O Error generating DMD document", e);
            }
        }

        return dmd;
    }

    /**
     * Returns the mets encoding.
     * 
     * @return
     */
    public String getEncoding() {
        return metsDocument.documentProperties().getEncoding();
    }
    
    /**
     * Returns all file references in the struct map section.
     * 
     * @return
     * @throws MetsException
     */
    public DivType[] getAllFileDivs() throws MetsException {
        String metsNs = metsDocument.getMets().getDomNode().getNamespaceURI();
        String metsNsDecl = "declare namespace METS = '" + metsNs + "'; ";
        String pathText;

        if (getLevels() == 1)
            pathText = "$this/METS:mets/METS:structMap/METS:div/METS:div";
        else if (getLevels() == 2)
            pathText = "$this/METS:mets/METS:structMap/METS:div/METS:div/METS:div";
        else
            throw new MetsException("Invalid number of levels");

        String queryText = metsNsDecl + pathText;
        DivType[] fileDivs = (DivType[]) metsDocument.selectPath(queryText);
        	
        return fileDivs;
    }
    
    private String convertToHex(byte[] digest){
    	 // convertToHex
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            int halfbyte = (digest[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = digest[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
    
    /**
     * Creates a new DMD section with the provided parameters
     * @param dmdId
     * @param vocUri
     * @param docXmlData
     * @throws MetsException
     */
    public void createDMDSec(String dmdId, String vocUri, Document docXmlData) throws MetsException{
    	
    	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
    	
        //Add a new DMD Section
        MdSecType mdSec = internalMets.addNewDmdSec();
        mdSec.setID(dmdId);
        MdWrap mdWrap = mdSec.addNewMdWrap();
        mdWrap.setMDTYPE(MdWrap.MDTYPE.OTHER);
        if(vocUri != null)
            mdWrap.setOTHERMDTYPE(vocUri);
        mdWrap.setMIMETYPE(PREMIS_OBJECT_MIMETYPE);
        XmlData xmlData = mdWrap.addNewXmlData();
        XmlObject xmlObject;
		try {
			xmlObject = XmlObject.Factory.parse(docXmlData);
		} catch (XmlException e) {
			throw new MetsException(e);
		}
        xmlData.set(xmlObject);

    }
    
    /**
     * Creates a new DMD section with the provided parameters
     * @param dmdId
     * @param vocUri
     * @param docXmlData
     * @throws MetsException
     */
    public void createDMDSec(String dmdId, String vocUri, InputStream docXmlData) throws MetsException{
    	
    	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
    	
        //Add a new DMD Section
        MdSecType mdSec = internalMets.addNewDmdSec();
        mdSec.setID(dmdId);
        MdWrap mdWrap = mdSec.addNewMdWrap();
        mdWrap.setMDTYPE(MdWrap.MDTYPE.OTHER);
        if(vocUri != null)
            mdWrap.setOTHERMDTYPE(vocUri);
        mdWrap.setMIMETYPE(PREMIS_OBJECT_MIMETYPE);
        XmlData xmlData = mdWrap.addNewXmlData();
        XmlObject xmlObject;
		try {
			xmlObject = XmlObject.Factory.parse(docXmlData);
		} catch (Exception e) {
			throw new MetsException(e);
		}
        xmlData.set(xmlObject);

    }
    
    
  /**
   * Creates a new AMD section with the provided parameters
   * @param amdId
   * @param vocUri
   * @param techID
   * @param docXmlData
   * @throws MetsException
   */
    public void createAMDSec(String amdId, String vocUri, String techID, Document docXmlData) throws MetsException{
    	
    	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
    	
        //Add a new AMD Section
        AmdSecType amdSec = internalMets.addNewAmdSec();
        amdSec.setID(amdId);
        MdSecType newTechMD = amdSec.addNewTechMD();
        newTechMD.setID(techID);
        MdWrap mdWrap = newTechMD.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.OTHER);
        mdWrap.setOTHERMDTYPE(vocUri);
        mdWrap.setMIMETYPE(PREMIS_OBJECT_MIMETYPE);
        XmlData xmlData = mdWrap.addNewXmlData();
        
        XmlObject xmlObject;
		try {
			xmlObject = XmlObject.Factory.parse(docXmlData);
		} catch (XmlException e) {
			throw new MetsException(e);
		}
        xmlData.set(xmlObject);

        log.info("Added AMD Section " + amdId);
    }
    
    /**
     * Creates a new AMD section with the provided parameters
     * @param amdId
     * @param vocUri
     * @param techID
     * @param docXmlData
     * @throws MetsException
     */
      public void createAMDSec(String amdId, String vocUri, String techID, InputStream docXmlData) throws MetsException{
      	
      	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
      	
          //Add a new AMD Section
          AmdSecType amdSec = internalMets.addNewAmdSec();
          amdSec.setID(amdId);
          MdSecType newTechMD = amdSec.addNewTechMD();
          newTechMD.setID(techID);
          MdWrap mdWrap = newTechMD.addNewMdWrap();
          mdWrap.setMDTYPE(MDTYPE.OTHER);
          mdWrap.setOTHERMDTYPE(vocUri);
          mdWrap.setMIMETYPE(PREMIS_OBJECT_MIMETYPE);
          XmlData xmlData = mdWrap.addNewXmlData();
          
          XmlObject xmlObject;
  		try {
  			xmlObject = XmlObject.Factory.parse(docXmlData);
  		} catch (Exception e) {
  			throw new MetsException(e);
  		}
          xmlData.set(xmlObject);

          log.info("Added AMD Section " + amdId);
      }
    
    /**
     * Creates a new FileGrp section with the FLocat provided parameters (for referenced files)
     * @param fileGrpId
     * @param checksum
     * @param binId
     * @param mimeType
     * @param href
     */
    
    public void createFileGrp(String fileGrpId, String checksum, String binId, String mimeType, String href){
    	
    	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
    	FileSec fileSec = internalMets.getFileSec();
    	FileGrp fileGrp = fileSec.addNewFileGrp();
    	fileGrp.setID(fileGrpId);
    	FileType newFile = fileGrp.addNewFile();
    	newFile.setCHECKSUM(checksum);
    	newFile.setCHECKSUMTYPE(CHECKSUMTYPE.SHA_1);
    	newFile.setCREATED(Calendar.getInstance());
    	newFile.setID(binId);
    	newFile.setMIMETYPE(mimeType);
    	FLocat newFLocat = newFile.addNewFLocat();
    	newFLocat.setHref(href);
    	newFLocat.setLOCTYPE(LOCTYPE.URL);
    	log.info("New FileGrp created: "+fileGrp);
    }
    
	/**
	 * Creates a new FileGrp section with the FContent provided parameters (for binaries files)
	 * @param fileGrpId
	 * @param binData
	 * @param binId
	 * @param mimeType
	 * @throws MetsException
	 */
    public void createFileGrp(String fileGrpId, byte[] binData, String binId, String mimeType) throws MetsException{
    	
    	gov.loc.mets.MetsDocument.Mets internalMets = metsDocument.getMets();
    	FileSec fileSec = internalMets.getFileSec();
    	FileGrp fileGrp = fileSec.addNewFileGrp();
    	fileGrp.setID(fileGrpId);
    	
    	// SHA-1 checksum generation
        String checksum = generateSha1File(binData);
        
    	FileType newFile = fileGrp.addNewFile();
    	newFile.setCHECKSUM(checksum);
    	newFile.setCHECKSUMTYPE(CHECKSUMTYPE.SHA_1);
    	newFile.setCREATED(Calendar.getInstance());
    	newFile.setID(binId);
    	newFile.setMIMETYPE(mimeType);
    	FContent newFContent = newFile.addNewFContent();
    	newFContent.setBinData(binData);
    	
    	log.info("New FileGrp created: "+fileGrp);
    }
    
    
    
    
   /**
    * Validates the provided Document XMLData against the schema provided
    * @param docXMLData
    * @param schemaStream
    * @throws MetsException
    */
    public void validateXMLData(Document docXMLData, InputStream schemaStream) throws MetsException{
    	
    	if(docXMLData == null){
    		throw new MetsException("Cannot validate XMLData against the provided schema, docXMLData is null!");
    	}
    	XMLSchemaValidator xmlSchemaValidator = new XMLSchemaValidator();
		try {
			xmlSchemaValidator.addSchema(getDocumentBuilder().parse(schemaStream));
			xmlSchemaValidator.validate(docXMLData);
	        
		} catch (Exception e) {
			log.info("Cannot validate XMLData against the provided schema");
			throw new MetsException(e);
		} 
		log.info("XMLData validation process is OK");
    }
    
    /**
     * Validates the provided String XMLData against the schema provided
     * @param docXMLData
     * @param schemaStream
     * @throws MetsException
     */
    public void validateXMLData(String docXMLData, InputStream schemaStream) throws MetsException{
    	
    	if(docXMLData == null){
    		throw new MetsException("Cannot validate XMLData against the provided schema, docXMLData is null!");
    	}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(docXMLData)));
			validateXMLData(doc, schemaStream);

		} catch (Exception e) {
			throw new MetsException(e);
		}
    	
    }

    /**
     * Validates the current mets Document against the base schema provided
     * @throws MetsException
     */
    public void validate() throws MetsException{
        XMLSchemaValidator xmlSchemaValidator = new XMLSchemaValidator();

        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/xlink.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/mets.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/premis-v2-0.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/Voc_expedient.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/Voc_document_exp.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/Voc_document.xsd"));
        xmlSchemaValidator.addSchema(getResourceAsDocument("resources/xsd/Voc_signatura.xsd"));
        DOMSource metsSource = new DOMSource(getInternalMets().newDomNode());

        try {
            xmlSchemaValidator.validate(metsSource);
        } catch (XMLSchemaValidatorException e) {
            throw new MetsException("Cannot validate mets against the schemas",e);
        }
        log.info("Mets validation process is OK");
    }

    private Document getResourceAsDocument(String xsdName) throws MetsException{
    	InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdName);

		DocumentBuilder documentBuilder = this.getDocumentBuilder();
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
     * Creates a new {@link DocumentBuilder} and returns it.
     * 
     * @return an instance of DocumentBuilder.
     * @throws MetsException
     *             doing transformation
     */
    private DocumentBuilder getDocumentBuilder()
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
}
