package com.bpmnq;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.bpmnq.GraphObject.GraphObjectType;

/**
 *
 * @author Ahmed Awad, Steffen Ryll
 */
public final class ProcessGraphBuilderXML extends AbstractGraphBuilderXML {

    public ProcessGraphBuilderXML(String fileName) {
        super(fileName);
    }
    
    public ProcessGraph buildProcessGraph() {
        return buildGraph();
    }
    
    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleGraphObjectNode(org.w3c.dom.Node)
     */
    @Override
    protected GraphObject handleGraphObjectNode(Node node) {
        GraphObject result = new GraphObject();
        NamedNodeMap attributes = node.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            String currentAttributeName = currentAttribute.getNodeName();
            
            if ("id".equals(currentAttributeName))
            {
                result.setID(currentAttribute.getNodeValue());
//                result.setResolved(false);
            }
            if ("type1".equals(currentAttributeName)) {
                
                // set node type according to xml node value
                for (GraphObjectType type : GraphObjectType.values()) {
                    if (type.xmlEncodedName().equals(
                            currentAttribute.getNodeValue())) {
                        result.type = type;
                        
                        // it can be only one type
                        break;
                    }
                }
            }
            if ("type2".equals(currentAttributeName)) {
                result.type2 = currentAttribute.getNodeValue();
            }
            if ("name".equals(currentAttributeName)) {
                if (currentAttribute.getNodeValue().length() > 0)
                    result.setName(currentAttribute.getNodeValue().replace('"', ' ').trim());
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleSequenceFlowNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleSequenceFlowNode(Node node) {
        String sourceId = "0", destId = "0";
        NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            
            if ("from".equals(currentAttribute.getNodeName())) {
                sourceId = currentAttribute.getNodeValue();
            }
            if ("to".equals(currentAttribute.getNodeName())) {
                destId = currentAttribute.getNodeValue();
            }
        }
        
        return new SequenceFlow(getNodeWithID(sourceId), getNodeWithID(destId));
    }

    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleNegativePathNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleNegativePathNode(Node node) {
        // do nothing for a process graph
        return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleNegativeSequenceFlowNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleNegativeSequenceFlowNode(Node node) {
        // do nothing for a process graph
        return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handlePathNode(org.w3c.dom.Node)
     */
    @Override
    protected Path handlePathNode(Node node) {
        // do nothing for a process graph
        return null;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleAssociationNode(org.w3c.dom.Node)
     */
    @Override
    protected Association handleAssociationNode(Node node) {
        String sourceId = "0", destId = "0";
        NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            
            if ("from".equals(currentAttribute.getNodeName())) {
                sourceId = currentAttribute.getNodeValue();
            }
            if ("to".equals(currentAttribute.getNodeName())) {
                destId = currentAttribute.getNodeValue();
            }
        }
        
        return new Association(getNodeWithID(sourceId), getNodeWithID(destId));
    }
    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleAssociationNode(org.w3c.dom.Node)
     */

    @Override
    protected UndirectedAssociation handleUndirectedAssociationNode(Node node) {
	// TODO Auto-generated method stub
	return null;
    }


}
