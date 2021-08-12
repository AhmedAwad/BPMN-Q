package com.bpmnq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bpmnq.Association.AssociaitonType;
import com.bpmnq.GraphObject.GraphObjectType;

public abstract class AbstractGraphBuilderXML implements GraphBuilder {

    protected String xmlFileName = "";
    protected Map<String, GraphObject> nodeMap;
    
    private Logger log = Logger.getLogger(AbstractGraphBuilderXML.class);

    /**
     * Creates a new instance of ParseXMLFile
     * @param fileName 
     *          Path and name of the xml file containing a query graph description
     */
    public AbstractGraphBuilderXML(String fileName) {
        this.xmlFileName = fileName;
        this.nodeMap = new HashMap<String, GraphObject>();
    }
    
    /**
     * Returns element value
     * 
     * @param elem
     *            element (it is an XML tag)
     * @return Element value, otherwise empty String
     */
    public final String getElementValue(Node elem) {
        Node kid;
        if (elem != null)
            return "";
        
        if (elem.hasChildNodes()) {
            for (kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
                if (kid.getNodeType() == Node.TEXT_NODE) {
                    return kid.getNodeValue();
                }
            }
        }
    
        return "";
    }

    /**
     * Finds the node with given ID and returns it, if it exists.
     * @param id 
     *          Node ID
     * @return The node with ID==<code>id</code>, or <code>null</code> if it does not exist.
     */
    protected GraphObject getNodeWithID(String id) {
	return nodeMap.get(id);
    }

    /** 
     * Parses XML file and returns XML document.
     * @param fileName 
     *          Path and name of the XML file to parse
     * @return XML document or <code>null</code> if error occurred
     */
    protected Document parseFile(String fileName) {
        //log.debug("Parsing XML file... " + fileName);
        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Wrong XML parser configuration.", e);
            return null;
        }
        File sourceFile = new File(fileName);
        try {
            doc = docBuilder.parse(sourceFile);
        } catch (SAXException e) {
            log.error("Wrong XML file structure.", e);
            return null;
        } catch (IOException e) {
            log.error("Could not read source file.", e);
            return null;
        }
        //log.debug("XML file parsed");
        return doc;
    }
 
    protected abstract GraphObject handleGraphObjectNode(Node node);

    protected abstract SequenceFlow handleNegativePathNode(Node node);
    
    protected abstract Path handlePathNode(Node node);
    
    protected abstract SequenceFlow handleNegativeSequenceFlowNode(Node node);
    
    protected abstract SequenceFlow handleSequenceFlowNode(Node node);

    protected abstract Association handleAssociationNode(Node node);
    
    protected abstract UndirectedAssociation handleUndirectedAssociationNode(Node node);
    /* (non-Javadoc)
     * @see com.bpmnq.GraphBuilder#buildGraph()
     */
    @SuppressWarnings("unchecked")
    public <GraphT extends ProcessGraph> GraphT buildGraph() {
        // parse XML file -> XML document will be built
        QueryGraph graph = new QueryGraph();

        Document doc = parseFile(xmlFileName);
        // get root node of xml tree structure
        Node root = doc.getDocumentElement();

        nodeMap.clear();

        // write all child nodes recursively
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ("GraphObject".equals(child.getNodeName())) {
                // a serious caveat is that this approach relies on a specific xml format,
                // meaning that it requires all graphObjects to be mentioned first, then sequenceFlow-like types
                GraphObject node = handleGraphObjectNode(child);
                if (node != null) {
                    if (node.type != GraphObjectType.DATAOBJECT)
                	graph.add(node);
                    else if (node.type == GraphObjectType.DATAOBJECT)
                    {
                	DataObject d = new DataObject();
                	d.doID = node.getID();
                	if (node.getName().contains("["))
                	{
                	    d.name = node.getName().substring(0, node.getName().indexOf("[")-1);
                	    d.setState(node.type2.replace("\"", ""));
                	    if (d.getState().length() == 0)
                	    {
                		d.setState(node.getName().substring(node.getName().indexOf("[")+1, node.getName().indexOf("]")-1));
                		d.setState(d.getState().replace("\"", "").trim());
                	    }
                	}
                	else
                	{
                	    d.name = node.getName();
                	    d.setState(node.type2.replace("\"", ""));
                	}
                	graph.add(d);
                    }

                    // only deal with positive Ids. Negative Ids are attached with implementation-specific meaning
                    nodeMap.put(node.getID(), node);
                }

            } else if ("SequenceFlow".equals(child.getNodeName())) {
                SequenceFlow seqFlow = handleSequenceFlowNode(child);

                if (seqFlow != null) {
                    graph.add(seqFlow);
                }

            } else if ("Path".equals(child.getNodeName())) {
                Path pathflow = handlePathNode(child);

                if (pathflow != null) {
                    graph.add(pathflow);
                }

            } else if ("NegativeSequenceFlow".equals(child.getNodeName())) {
                SequenceFlow negSeqFlow = handleNegativeSequenceFlowNode(child);

                if (negSeqFlow != null) {
                    graph.addNegativeEdge(negSeqFlow);
                }

            } else if ("NegativePath".equals(child.getNodeName())) {
                SequenceFlow negPath = handleNegativePathNode(child);

                if (negPath != null) {
                    graph.addNegativePath(negPath);
                }
            } else if ("UndirectAssociation".equals(child.getNodeName())) {
                UndirectedAssociation unia = handleUndirectedAssociationNode(child);
                
                if (unia != null) {
                    graph.addDataPathAssociation(unia);
                }
            }
            else if ("Association".equals(child.getNodeName())){
            	Association assoc = handleAssociationNode(child);
            	
            	if (assoc != null)
            	{
            	    assoc.assType = AssociaitonType.Structural;
            	    graph.add(assoc);
            	}
            }
            else if ("BehavioralAssociation".equals(child.getNodeName())){
            	Association assoc = handleAssociationNode(child);
            	
            	if (assoc != null)
            	{
            	    assoc.assType = AssociaitonType.Behavioral;
            	    graph.add(assoc);
            	}
            }
            
        }
        for (DataObject a: graph.dataObjs)
            a.normalize();
        return (GraphT) graph;
    }

    /** 
     * Saves XML Document into XML file.
     * @param fileName XML file name
     * @param doc XML document to save
     * @return <code>true</code> if method success <code>false</code> otherwise
     * 
     * TODO what is the purpose of this method? Write out the same things that 
     * were read in before?! What is more, it's never used.
     */
    public boolean saveXMLDocument(String fileName, Document doc) {
        // log.debug("Saving XML file... " + fileName);
        // open output stream where XML Document will be saved
        File xmlOutputFile = new File(fileName);
        FileOutputStream fos;
        Transformer transformer;
        try {
            fos = new FileOutputStream(xmlOutputFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot open file for writing XML.", e);
            return false;
        }
        
        // Use a Transformer for output
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            log.error("XML Transformer configuration error.", e);
            return false;
        }
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fos);
        // transform source into result will do save
        try {
            transformer.transform(source, result);
            fos.close();
        } catch (TransformerException e) {
            log.error("Error during XML transformation.", e);
        } catch (IOException e) {
            log.error("I/O Error occured during XML transformation.", e);
            return false;
        }
        // log.debug("XML file saved.");
        return true;
    }

}