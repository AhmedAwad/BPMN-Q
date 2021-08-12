package com.bpmnq;

import org.w3c.dom.*;

import com.bpmnq.GraphObject.GraphObjectType;

public final class QueryGraphBuilderXML extends AbstractGraphBuilderXML {

    /**
     * Creates a new instance of QueryGraphBuilderXML
     * @param fileName 
     *          Path and name of the xml file containing a query graph description
     */
    public QueryGraphBuilderXML(String fileName) {
        super(fileName);
    }

    public QueryGraph buildQueryGraph() {
        return buildGraph();
    }

    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleGraphObjectNode(org.w3c.dom.Node, int)
     */
    @Override
    protected GraphObject handleGraphObjectNode(Node node) {
        GraphObject result = new GraphObject();
        NamedNodeMap attributes = node.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            if ("id".equals(currentAttribute.getNodeName())) {
            	result.setID(currentAttribute.getNodeValue());
//            	result.setResolved(false);
            }
            if ("type1".equals(currentAttribute.getNodeName())) {
                // set node type according to xml node value
                for (GraphObjectType type : GraphObjectType.values()) {
                    if (type.xmlEncodedName().equals(
                            currentAttribute.getNodeValue())) {
                        result.type = type;
                        break;
                    }
                }
                // if
                // (currentAttribute.getNodeValue().equals("GateWay"))
                // id++;
                // else if
                // (currentAttribute.getNodeValue().equals("Generic"))

            }
            if ("type2".equals(currentAttribute.getNodeName())) {
                result.type2 = currentAttribute.getNodeValue();
                // if (node.type2.equals("GENERIC SHAPE"))
                // id+=3;
            }
            if ("name".equals(currentAttribute.getNodeName())
                    && currentAttribute.getNodeValue().length() > 0)
                result.setName(currentAttribute.getNodeValue().replace('"', ' ').trim());
        }

        return result;
    }


    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleNegativePathNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleNegativePathNode(Node node) {
        // xml format for both node types is identical
        return handleSequenceFlowNode(node);
    }


    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleNegativeSequenceFlowNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleNegativeSequenceFlowNode(Node node) {
        // xml format for both node types is identical
        return handleSequenceFlowNode(node);
    }


    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handlePathNode(org.w3c.dom.Node)
     */
    @Override
    protected Path handlePathNode(Node node) {
        String sourceId = "0", destId = "0";
        String exclude = "",name="";
        NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            
            if (currentAttribute.getNodeName().equals("from")) {
                sourceId = currentAttribute.getNodeValue();
            }
            if (currentAttribute.getNodeName().equals("to")) {
                destId = currentAttribute.getNodeValue();
            }
            if (currentAttribute.getNodeName().equals("execlude")) {
                exclude = currentAttribute.getNodeValue().replace('"', ' ').trim();
            }
            if (currentAttribute.getNodeName().equals("name")) {
                name = currentAttribute.getNodeValue().replace('"', ' ').trim();
            }
        }
        Path result = new Path(getNodeWithID(sourceId), getNodeWithID(destId));
        result.exclude = exclude;
        result.label = name;
        return result;
    }

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
        Association result = new Association(getNodeWithID(sourceId), getNodeWithID(destId));
        
        return result;
    }
    
    protected UndirectedAssociation handleUndirectedAssociationNode(Node node) {
        String dataObjId = "0";
	String pathSrcId = "0", pathDstId = "0";
	NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) 
        {
            Node currentAttribute = attributes.item(j);
            if ("dataobject".equals(currentAttribute.getNodeName())) {
                dataObjId = currentAttribute.getNodeValue();
            }
            if ("path".equals(currentAttribute.getNodeName())) {
                
                String list[] = null;
                list = currentAttribute.getNodeValue().split("->");
                pathSrcId = list[0];
                pathDstId = list[1];
            }
        }
        
        return new UndirectedAssociation(getNodeWithID(dataObjId), 
        	new Path(getNodeWithID(pathSrcId), getNodeWithID(pathDstId)));
    }
    
    /* (non-Javadoc)
     * @see com.bpmnq.AbstractGraphBuilderXML#handleSequenceFlowNode(org.w3c.dom.Node)
     */
    @Override
    protected SequenceFlow handleSequenceFlowNode(Node node) {
        String sourceId = "0", destId = "0";
        NamedNodeMap attributes = node.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) 
        {
            Node currentAttribute = attributes.item(j);
            if ("from".equals(attributes.item(j).getNodeName())) {
                sourceId = currentAttribute.getNodeValue();
            }
            if ("to".equals(attributes.item(j).getNodeName())) {
                destId = currentAttribute.getNodeValue();
            }
        }
        
        return new SequenceFlow(getNodeWithID(sourceId), getNodeWithID(destId));
    }

}
