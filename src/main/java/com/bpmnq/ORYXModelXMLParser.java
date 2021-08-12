/*
 *
 * This is a prototype implementation, to prove that an idea is working.
 * NOT TO BE USED IN A PRODUCTION ENVIRONMENT!!!!!!!!!!!!!!!!
 * 
 *
 */
package com.bpmnq;

import com.bpmnq.GraphObject.GateWayType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.DOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author nemo
 */
public class ORYXModelXMLParser {

    private PartialProcessModel partialprocessModel;
    private HashMap<String, ModelElement> model;
    private List<ModelElement> queries;
    private ModelElement diagram;

    public ORYXModelXMLParser() {
        this.partialprocessModel = new PartialProcessModel();
    }

    public PartialProcessModel getPartialProcessModel() {
        return this.partialprocessModel;
    }

    public void createModel(String location) {

        SequenceFlow sf;
        GraphObject go;
        QueryGraph qg;



        try {
            Document document = parseXMLFromInputSource(new InputSource(location));

            DOMImplementation domimpl = document.getImplementation();
            if (domimpl.hasFeature("Traversal", "2.0")) {
                //initialisation for node travers
                Node root = document.getDocumentElement();
                int whattoshow = NodeFilter.SHOW_ALL;
                NodeFilter nodefilter = null;
                boolean expandreferences = false;
                DocumentTraversal traversal = (DocumentTraversal) document;
                TreeWalker walker = traversal.createTreeWalker(root,
                        whattoshow,
                        nodefilter,
                        expandreferences);
                Node thisNode = null;

                //first node
                thisNode = walker.nextNode();


                ModelElement me = null;
                this.model = new HashMap<String, ModelElement>();

                while (thisNode != null) {

                    //get the id
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.RDF_DESCRIPTION) == 0) {

                        if (me != null && me.getId() != null && !me.getId().isEmpty()) {
                            this.model.put(me.getId(), me);
                        }

                        me = new ModelElement();
                        //System.out.print("element "+thisNode.getNodeName() + " ");
                        Element thisElement = (Element) thisNode;
                        NamedNodeMap attributes = thisElement.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
//                            System.out.println(attributes.item(i).getNodeName()+" "+attributes.item(i).getNodeValue());
                            if ((attributes.item(i).getNodeName()).compareTo(ModelElementConstants.RDF_ABOUT) == 0) {
                                me.setId(attributes.item(i).getNodeValue());
                            }
                        }

                        //System.out.println(me.getId());
                    }

                    //get the type
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.TYPE) == 0) {
                        me.setType(thisNode.getTextContent());

                    }

                    //get the name 
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.NAME) == 0) {
                        me.setName(thisNode.getTextContent());
                    }

                    //get the parent property
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.PARENT) == 0) {
                        Element thisElement = (Element) thisNode;
                        NamedNodeMap attributes = thisElement.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            if ((attributes.item(i).getNodeName()).compareTo(ModelElementConstants.RDF_RESOURCE) == 0) {
                                me.setParent(attributes.item(i).getNodeValue());
                            }
                        }
                    }

                    //ConsistencyDiagram specific
                    //get the linkedmodel
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.LINKED_MODEL) == 0) {
                        me.setLinkedModel(thisNode.getTextContent());
                    }

                    //get the similaritymatch
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.SIMILARITY_MATCH) == 0) {
                        me.setSimilarityMatch(thisNode.getTextContent());
                    }

                    //SequenceFlows and Paths
                    //get the exclude property
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.EXCLUDE) == 0) {
                        me.setExclude(thisNode.getTextContent());
                    }

                    //get the target property
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.TARGET) == 0) {
                        Element thisElement = (Element) thisNode;
                        NamedNodeMap attributes = thisElement.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            if ((attributes.item(i).getNodeName()).compareTo(ModelElementConstants.RDF_RESOURCE) == 0) {
                                me.setTarget(attributes.item(i).getNodeValue());
                            }
                        }
                    }

                    //get the outgoing property
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.OUTGOING) == 0) {
                        Element thisElement = (Element) thisNode;
                        NamedNodeMap attributes = thisElement.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            if ((attributes.item(i).getNodeName()).compareTo(ModelElementConstants.RDF_RESOURCE) == 0) {
                                me.addOutgoing(attributes.item(i).getNodeValue());
                            }
                        }
                    }

                    //get the temporalproperty
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.TEMPORAL_PROPERTY) == 0) {
                        me.setTemporalProperty(thisNode.getTextContent());
                    }


                    //get the eventtype
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.EVENT_TYPE) == 0) {
                        me.setEventtype(thisNode.getTextContent());
                    }

                    //get the gatewaytype
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.GATEWAY_TYPE) == 0) {
                        me.setGatewaytype(thisNode.getTextContent());
                    }
                    //a user defined id 
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.ID) == 0) {
                        me.setUserDefinedID(thisNode.getTextContent());
                    }
                    //split join
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.SPLIT_JOIN) == 0) {
                        me.setSplitJoin(thisNode.getTextContent());
                    }


                    //get the activitytype
                    if (thisNode.getNodeType() == thisNode.ELEMENT_NODE && (thisNode.getNodeName()).compareTo(ModelElementConstants.ACTIVITY_TYPE) == 0) {
                        me.setActivitytype(thisNode.getTextContent());
                    }


//                    if (thisNode.getNodeType() == thisNode.TEXT_NODE) {
//                        //  System.out.print(thisNode.getNodeValue());
//                    }

                    thisNode = walker.nextNode();
                }
                //at the end add also the last element
                if (me != null && me.getId() != null && !me.getId().isEmpty() && me.getType().compareTo(ModelElementConstants.DIAGRAM_TYPE) != 0) {
                    this.model.put(me.getId(), me);
                } else if (me != null && me.getId() != null && !me.getId().isEmpty() && me.getType().compareTo(ModelElementConstants.DIAGRAM_TYPE) == 0) {

                    this.diagram = me;
                }


                createPartialProcessModel();


                //==================for testing purposes
//                System.out.println("nr of model elements " + this.model.size());
//                int i;
//                for(i=0;i<model.size();i++){
//                    System.out.println(model.get(i).toString());
//                }
                //==================for testing purposes
            }



        } catch (RuntimeException re) {
            System.out.println(re);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * load a Document from InputSource
     * 
     * @param is
     * @return
     * @throws XSLTransformerException
     */
    public Document parseXMLFromInputSource(InputSource is)
            throws
            Exception {
        Document doc;

        try {
            DOMParser parser = new DOMParser();
            parser.parse(is);
            doc = parser.getDocument();

//            System.out.println("we have parsed the document");

        } catch (IOException ioe) {

            throw new Exception("Unable to read from source string", ioe);
        } catch (SAXException saxe) {

            throw new Exception("Unable to parse the given string", saxe);
        }
        return doc;
    }

    private void createPartialProcessModel() throws InvalidConsistencyQuery, FileFormatException {
        try {
            //1. find all consitencyQueries
            List<ModelElement> cq = findConsistencyQueryElements();
            //2. add any existing QueryGraphs
            Iterator itcq = cq.iterator();
            while (itcq.hasNext()) {
                ModelElement modeleElementConsistencyQuery = (ModelElement) itcq.next();
                //for every cq i find the elements that are inside it
                this.partialprocessModel.addQueryGraph(createQueryGraph(modeleElementConsistencyQuery));
            }

            //the consistency queries have been created deal withe rest of the elements

            Collection c = this.model.values();

            Iterator it = c.iterator();
            do {
                ModelElement from = (ModelElement) it.next();
                handleElement(from);
            } while (it.hasNext());


        } catch (NullPointerException npe) {
            // npe.printStackTrace();
        }

    }

    private List<ModelElement> findConsistencyQueryElements() throws InvalidConsistencyQuery {
        List<ModelElement> cq = null;

        Collection c = this.model.values();

        Iterator it = c.iterator();
        while (it.hasNext()) {

            ModelElement me = (ModelElement) it.next();
            if (me.getType().compareTo(ModelElementConstants.CONSISTENCY_QUERY_TYPE) == 0) {
                if (cq == null) {

                    if (me.getLinkedModel().isEmpty() || me.getLinkedModel() == null) {
                        throw new InvalidConsistencyQuery();

                    }
                    cq = new ArrayList<ModelElement>();

                    cq.add(me);
                    it.remove();

//                    System.out.println("new size is "+this.model.size());

                    // System.out.println(this.model.size());
                } else {
                    if (me.getLinkedModel().isEmpty() || me.getLinkedModel() == null) {
                        throw new InvalidConsistencyQuery();

                    }
                    cq.add(me);
                }
            }
        }

        this.queries = cq;
        return cq;
    }

    private QueryGraph createQueryGraph(ModelElement modelElementConsistencyQuery) throws InvalidConsistencyQuery, FileFormatException {

        QueryGraph qg = new QueryGraph();

        Collection c = this.model.values();
        Iterator it = c.iterator();
        do {
            ModelElement from = (ModelElement) it.next();

            handleQueryGraphElement(from, qg, modelElementConsistencyQuery);

        } while (it.hasNext());
        // Added by Ahmed Awad
        qg.modelURI = modelElementConsistencyQuery.getLinkedModel();
        qg.setQueryNodeID(modelElementConsistencyQuery.getId());
        qg.setQueryNodeName(modelElementConsistencyQuery.getName());
        return qg;
    }

    private GraphObject handleElement(ModelElement fromME) throws FileFormatException {
        //we deal here only with nodes. No edges are taken into accoutn here
        GraphObject fromGO = null;
        boolean belongsToTheMainDiagram = false;
        if (fromME.getParent() != null && fromME.getParent().compareTo(this.diagram.getId()) == 0) {
            //it means that we have an element that belongs to the query
            belongsToTheMainDiagram = true;
        }
        //activities
        if (fromME.getActivitytype() != null ) {
            fromGO = handleActivitytype(fromME);

            if (belongsToTheMainDiagram && !fromME.isVisited()) {

                System.out.println("an activity has been added to the partial model " + fromME.getId());
                
                this.partialprocessModel.addNode(fromGO);

                handleOutgoingEdges(fromME, fromGO);
            }
        }

        //gateways
        if (fromME.getGatewaytype() != null ) {
            fromGO = handleGatewaytype(fromME);
            if (belongsToTheMainDiagram && !fromME.isVisited()) {
                System.out.println("a gateway has been added to the partial model " + fromME.getId());
                this.partialprocessModel.addNode(fromGO);

                handleOutgoingEdges(fromME, fromGO);
            }
        }

        //events
        if (fromME.getEventtype() != null ) {
            fromGO = handleEventtype(fromME);
            if (belongsToTheMainDiagram && !fromME.isVisited()) {
                System.out.println("an event has been added to the partial model " + fromME.getId());
                this.partialprocessModel.addNode(fromGO);

                handleOutgoingEdges(fromME, fromGO);
            }
        }

        if (belongsToTheMainDiagram) {
            fromME.setVisited(true);
        }

        return fromGO;
    }

    private GraphObject handleQueryGraphElement(ModelElement fromME, QueryGraph qg, ModelElement modelElementConsistencyQuery) throws FileFormatException {
        //we deal here only with nodes. No edges are taken into accoutn here

        System.out.println("*5*  handleQueryGraphElement ");
        System.out.println(fromME.toString());
       
        System.out.println("\n\n isVisited "+fromME.isVisited());
        System.out.println("\n\n getActivityType "+fromME.getActivitytype());
        System.out.println("\n\n getGatewayType "+fromME.getGatewaytype());
        System.out.println("\n\n getEventType "+fromME.getEventtype());

        GraphObject fromGO = null;

        boolean belongsToThisQuery = false;
        if (fromME.getParent() != null && fromME.getParent().compareTo(modelElementConsistencyQuery.getId()) == 0) {
            //it means that we have an element that belongs to the query

            belongsToThisQuery = true;
        }

        System.out.println();

        //activities
        if (fromME.getActivitytype() != null ) {
            System.out.println("*5* Activity");
            fromGO = handleActivitytype(fromME);

            if (belongsToThisQuery && !fromME.isVisited()) {
                System.out.println("an activity has been added to a query graph " + fromME.getId());
               
                qg.add(fromGO);
                
                handleOutgoingEdgesOfANodeInConsistencyQuery(fromME, fromGO, qg, modelElementConsistencyQuery);
               
            }
        }

        //gateways
        if (fromME.getGatewaytype() != null ) {
            fromGO = handleGatewaytype(fromME);

            if (belongsToThisQuery && !fromME.isVisited()) {

                System.out.println("a gateway has been added to a query graph " + fromME.getId());
                
                qg.add(fromGO);
                
                handleOutgoingEdgesOfANodeInConsistencyQuery(fromME, fromGO, qg, modelElementConsistencyQuery);
                
            }
        }

        //events
        if (fromME.getEventtype() != null ) {
             System.out.println("*5* Event");
            fromGO = handleEventtype(fromME);

            System.out.println(fromGO);

            if (belongsToThisQuery && !fromME.isVisited()) {
                System.out.println("an event has been added to a query graph " + fromME.getId());
                
                qg.add(fromGO);
                
                handleOutgoingEdgesOfANodeInConsistencyQuery(fromME, fromGO, qg, modelElementConsistencyQuery);
                
            }
        }

        if (belongsToThisQuery) {

            fromME.setVisited(true);

        }

         System.out.println("*5* END");

        return fromGO;

    }

    private void handleOutgoingEdges(ModelElement fromME, GraphObject fromGO) throws FileFormatException {


        //deal with the outgoing edges for this particular node
        //1. retrieve outgoing edges
        List<String> outgoingEdges = fromME.getOutgoing();
        //if this element has outgoing edges then iterate over them
        if (outgoingEdges != null) {
            Iterator io = outgoingEdges.iterator();
            do {
                String edgeID = (String) io.next();
                ModelElement edge = this.model.get(edgeID);

                //we set visited here to avoid cyclic tests
                edge.setVisited(true);

                String targetID = edge.getTarget();
                ModelElement to = null;
                if (targetID != null && !targetID.isEmpty()) {
                    to = this.model.get(targetID);

                    GraphObject toGO = handleElement(to);

                    String targetParentID = to.getParent();
                    if (targetParentID != null && !targetParentID.isEmpty()) {
                        //2. if the target node is in this query then this query has to be added to this query
                        if (targetParentID.compareTo(this.diagram.getId()) == 0) {
                            //this is an internal edge
                            // System.out.println("a common partial model edge");
                            handleEdgeNotBelongingToQueryGraph(edge, fromGO, toGO);

                        } else {
                            //if not then we have a special edge
                            //this edge points from inside of a consistency diagram towards exterior
                            System.out.println("a special edge has been added " + edge.getId());
                            //this one can be only a sequenceflow
                            //at least for the moment
                            this.partialprocessModel.addSpecialEdge(createSequenceFlow(fromGO, toGO));
                        }

                    }

                }

            } while (io.hasNext());
            /////////


        }
    }

    private void handleOutgoingEdgesOfANodeInConsistencyQuery(ModelElement fromME, GraphObject fromGO, QueryGraph qg, ModelElement modeleElementConsistencyQuery) throws FileFormatException {


        //deal with the outgoing edges for this particular node
        //1. retrieve outgoing edges
        List<String> outgoingEdges = fromME.getOutgoing();
        //if this element has outgoing edges then iterate over them
        if (outgoingEdges != null) {
            Iterator io = outgoingEdges.iterator();
            do {
                String edgeID = (String) io.next();
                ModelElement edge = this.model.get(edgeID);
                System.out.println("-------------------------1");
                System.out.println(edge.toString());
                System.out.println("-------------------------1_end");
                //we set visited here to avoid cyclic tests
                edge.setVisited(true);

                String targetID = edge.getTarget();

                System.out.println(" *2* EDGE targetID  "+targetID);

                ModelElement to = null;
                if (targetID != null && !targetID.isEmpty()) {
                    to = this.model.get(targetID);

                    System.out.println(" *3* EDGE target element FOUND");
                    System.out.println(to.toString());
                    System.out.println(" *3* EDGE target element FOUND END");


                    GraphObject toGO = handleQueryGraphElement(to, qg, modeleElementConsistencyQuery);
                    //toGo comes back as a null
                    //why?????
                    //to is not null

                    System.out.println("*4*    toGO ");
                    System.out.println(toGO.toString());
                    System.out.println("*4*    toGO END");

                    String targetParentID = to.getParent();
                    if (targetParentID != null && !targetParentID.isEmpty()) {
                        //2. if the target node is in this query then this query has to be added to this query
                        if (targetParentID.compareTo(modeleElementConsistencyQuery.getId()) == 0) {
                            //this is an internal edge
                            // System.out.println("an internal edge");
//                            toGO =
                            //edge si added only here if I have a toGO


                            handleEdgeBelongingToQueryGraph(edge, fromGO, toGO, qg);

                        } else {
                            //if not then we have a special edge
                            //this edge points from inside of a consistency diagram towards exterior
                            System.out.println("a special edge has been added " + edge.getId());
                            //this one can be only a sequenceflow
                            //at least for the moment
//                            if (to.getActivitytype() != null)
//                            
//                            for (QueryGraph qgg : partialprocessModel.getQueryGraphs())
//                            {
//                        	toGO = qgg.getNodeByID("-"+to.getId());
//                        	if (toGO != null)
//                        	    break;
//                            }
//                            if (toGO == null)
//                        	toGO = partialprocessModel.getNodeByID(to.getId());

                            this.partialprocessModel.addSpecialEdge(createSequenceFlow(fromGO, toGO));
                        }

                    }

                }

            } while (io.hasNext());
            /////////


        }
    }

    private Path createPath(GraphObject from, GraphObject to)
            throws ClassCastException {
        Path result;
        if (from == null || to == null) {
            result = new Path();
        } else {
            result = new Path(from, to);
        }
        return result;
    }

    private SequenceFlow createSequenceFlow(GraphObject from, GraphObject to) {
        SequenceFlow sq = null;
        if (from == null || to == null) {
            sq = new SequenceFlow();
        } else {
            sq = new SequenceFlow(from, to);
        }
        return sq;

    }

    private void handleEdgeNotBelongingToQueryGraph(ModelElement edge, GraphObject from, GraphObject to) {
        //different types of edges: sequenceFlow, negativeSequenceFlow, path, negativePath

        //deal with sequeceFlow
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.SEQUENCE_FLOW_TYPE) == 0) {
            System.out.println("edge not belonging to a query has been added " + edge.getId());
            this.partialprocessModel.addEdge(createSequenceFlow(from, to));
        }


        //deal with NegativeSequenceFlow
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.NEGATIVE_SEQUENCE_FLOW_TYPE) == 0) {
            System.out.println("edge not belonging to a query has been added " + edge.getId());
            this.partialprocessModel.addEdge(createSequenceFlow(from, to));
        }


        //deal with path
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.PATH_TYPE) == 0) {

            Path memPath = createPath(from, to);
            if (edge.getExclude() != null) {
                memPath.exclude = edge.getExclude();
            }

            if (edge.getName() != null) {
                memPath.label = edge.getName();
            }

            if (edge.getUserDefinedID() != null) {
                memPath.label = edge.getUserDefinedID();
            }

            if (edge.getTemporalProperty() != null) {
                // lookup whether there is a TemporalProperty type for the specified value.
                // if not, NONE will be used.
                String tagText = edge.getTemporalProperty().trim().replace(" ", "_");
                Path.TemporalType tag = Path.TemporalType.NONE;
                for (Path.TemporalType tagType : Path.TemporalType.values()) {
                    if (tagType.toString().equalsIgnoreCase(tagText)) {
                        tag = tagType;
                        break;
                    }
                }
                memPath.setTemporalTag(tag);
            }
            System.out.println("edge not belonging to a query has been added " + edge.getId());
            this.partialprocessModel.addEdge(memPath);
        }


        //deal with negative path
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.NEGATIVE_PATH_TYPE) == 0) {
            System.out.println("edge not belonging to a query has been added " + edge.getId());
            this.partialprocessModel.addEdge(createSequenceFlow(from, to));
        }


    }

    /**
     * this method deals with all the edges that do not belong to 
     * @param edge
     * @param from
     * @param to
     * @param qg
     */
    private void handleEdgeBelongingToQueryGraph(ModelElement edge, GraphObject from, GraphObject to, QueryGraph qg) {
        //different types of edges: sequenceFlow, negativeSequenceFlow, path, negativePath


        //deal with sequeceFlow
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.SEQUENCE_FLOW_TYPE) == 0) {
            System.out.println("edge belonging to a query has been added " + edge.getId());
            qg.add(createSequenceFlow(from, to));
        }

        //deal with NegativeSequenceFlow     
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.NEGATIVE_SEQUENCE_FLOW_TYPE) == 0) {
            System.out.println("edge belonging to a query has been added " + edge.getId());
            qg.addNegativeEdge(createSequenceFlow(from, to));
        }


        //deal with path
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.PATH_TYPE) == 0) {

            Path memPath = createPath(from, to);
            if (edge.getExclude() != null) {
                memPath.exclude = edge.getExclude();
            }

            if (edge.getName() != null) {
                memPath.label = edge.getName();
            }

            if (edge.getUserDefinedID() != null) {
                memPath.label = edge.getUserDefinedID();
            }

            if (edge.getTemporalProperty() != null) {
                // lookup whether there is a TemporalProperty type for the specified value.
                // if not, NONE will be used.
                String tagText = edge.getTemporalProperty().trim().replace(" ", "_");
                Path.TemporalType tag = Path.TemporalType.NONE;
                for (Path.TemporalType tagType : Path.TemporalType.values()) {
                    if (tagType.toString().equalsIgnoreCase(tagText)) {
                        tag = tagType;
                        break;
                    }
                }
                memPath.setTemporalTag(tag);
            }
            System.out.println("edge belonging to a query has been added " + edge.getId());
            qg.add(memPath);
        }


        //deal with negative path        
        if (edge.getType() != null && edge.getType().compareTo(ModelElementConstants.NEGATIVE_PATH_TYPE) == 0) {
            System.out.println("edge belonging to a query has been added " + edge.getId());
            qg.addNegativePath(createSequenceFlow(from, to));
        }
    }

    private GraphObject handleActivitytype(ModelElement activity) {
        //this method will create both task and variable activity 
        GraphObject node = null;
        //This deals both with Task and Variable
        if (activity.getActivitytype().compareTo(ModelElementConstants.TASK_TYPE) == 0) {
            node = initializeActivity();
            node.setName(activity.getName());
            node.setID("-" + activity.getId());
            //System.out.println("task was set " + node);

        }
        return node;
    }

    private GraphObject initializeActivity() {
        GraphObject node = new GraphObject();
        node.type = GraphObjectType.ACTIVITY;
        node.type2 = "";
        return node;
    }

    private GraphObject handleGatewaytype(ModelElement gateway) throws FileFormatException {
        //this method deals with gateways 

//        System.out.println("handling gatway");

        GraphObject node = null;
        //GENERIC SHAPE
        if (gateway.getGatewaytype().compareTo(ModelElementConstants.GENERIC_SHAPE_TYPE) == 0) {
            node = initializeActivity();
            // added on 26.08.2011 to fix a small bug
            if (gateway.getName() != null)
        	node.setName(gateway.getName());
            
            node.setID("-" + gateway.getId());
            //I am usign the existing code to set up type2. But this property has been set as public!!!!!!! This is bad
            node.type2 = GraphObject.ActivityType.GENERIC_SHAPE.asType2String();
            System.out.println("generic shape" + node);
            return node;
        }


        //Gateways
        node = new GraphObject();
        node.type = GraphObjectType.GATEWAY;

        node.setID("-" + gateway.getId());

        if (gateway.getUserDefinedID() != null && gateway.getUserDefinedID().startsWith("?")) {
            node.setName(gateway.getUserDefinedID());
        }

        // is it a split or a join node?
        /** true means split, false means join */
        boolean isSplitStructured = true;
        boolean splitJoinStructureIsKnown = false;
        int numOutgoings = 0;
        if (gateway.getOutgoing() != null) {
            numOutgoings = gateway.getOutgoing().size();
        }

        int numIncomings = countGatewayIncomingEdges(gateway);

        if (numIncomings <= 1 && numOutgoings > 1) {
            isSplitStructured = true; // the graph structure suggests it's a split node
            splitJoinStructureIsKnown = true;
        } else if (numIncomings > 1 && numOutgoings <= 1) {
            isSplitStructured = false; // the graph structure suggests it's a join node
            splitJoinStructureIsKnown = true;
        }


        //dealing with AND gateways

        if (gateway.getGatewaytype() != null && gateway.getGatewaytype().compareTo(ModelElementConstants.AND) == 0) {

            if (gateway.getSplitJoin().equals(ModelElementConstants.AND_JOIN)) {
                if (splitJoinStructureIsKnown && isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a split, but says it was a join.");
                }

                node.type2 = GateWayType.AND_JOIN.asType2String();
            } else if (gateway.getSplitJoin().equals(ModelElementConstants.AND_SPLIT)) {
                if (splitJoinStructureIsKnown && !isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a join, but says it was a spli.");
                }

                node.type2 = GateWayType.AND_SPLIT.asType2String();
            } else {
                throw new FileFormatException("And Gateway resource " + gateway.getId()
                        + "doesn't specifiy whether it's a join or a split.");
            }

        }

        //dealing with OR gateways

        if (gateway.getGatewaytype() != null && gateway.getGatewaytype().compareTo(ModelElementConstants.OR) == 0) {

            if (gateway.getSplitJoin().equals(ModelElementConstants.OR_JOIN)) {
                if (splitJoinStructureIsKnown && isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a split, but says it was a join.");
                }

                node.type2 = GateWayType.OR_JOIN.asType2String();
            } else if (gateway.getSplitJoin().equals(ModelElementConstants.OR_SPLIT)) {
                if (splitJoinStructureIsKnown && !isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a join, but says it was a spli.");
                }

                node.type2 = GateWayType.OR_SPLIT.asType2String();
            } else {
                throw new FileFormatException("And Gateway resource " + gateway.getId()
                        + "doesn't specifiy whether it's a join or a split.");
            }

        }

        //dealing with XOR gateways

        if (gateway.getGatewaytype() != null && gateway.getGatewaytype().compareTo(ModelElementConstants.XOR) == 0) {

            if (gateway.getSplitJoin().equals(ModelElementConstants.XOR_JOIN)) {
                if (splitJoinStructureIsKnown && isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a split, but says it was a join.");
                }

                node.type2 = GateWayType.XOR_JOIN.asType2String();
            } else if (gateway.getSplitJoin().equals(ModelElementConstants.XOR_SPLIT)) {
                if (splitJoinStructureIsKnown && !isSplitStructured) {
                    throw new FileFormatException("And Gateway resource " + gateway.getId()
                            + " looks like a join, but says it was a spli.");
                }

                node.type2 = GateWayType.XOR_SPLIT.asType2String();
            } else {
                throw new FileFormatException("And Gateway resource " + gateway.getId()
                        + "doesn't specifiy whether it's a join or a split.");
            }

        }

        if (gateway.getGatewaytype() != null && gateway.getGatewaytype().compareTo(ModelElementConstants.GENERIC_SPLIT) == 0) {
            if (splitJoinStructureIsKnown && !isSplitStructured) {
                throw new FileFormatException("Generic Split Gateway resource " + gateway.getId() + " looks more like a join.");
            }
            node.type2 = GateWayType.GENERIC_SPLIT.asType2String();
        }
        if (gateway.getGatewaytype() != null && gateway.getGatewaytype().compareTo(ModelElementConstants.GENERIC_JOIN) == 0) {
            if (splitJoinStructureIsKnown && isSplitStructured) {
                throw new FileFormatException("Generic Join Gateway resource " + gateway.getId() + " looks more like a join.");
            }
            node.type2 = GateWayType.GENERIC_JOIN.asType2String();
        }

        return node;
    }

    private GraphObject handleEventtype(ModelElement me) {
        GraphObject node = new GraphObject();
        
        if (me.getName() != null) {
           // node.setName("$#");
            node.setName(me.getName());
        }
        node.setID("-" + me.getId());
        // Added by Ahmed Awad on 9.8.2010
        node.type = GraphObjectType.EVENT;

        if (me.getEventtype() != null) {

            if (me.getEventtype().compareTo(ModelElementConstants.ENDEVENT) == 0) {
                node.type2 = GraphObject.EventType.END.asType2String();
            }
            if (me.getEventtype().compareTo(ModelElementConstants.STARTEVENT) == 0) {
                node.type2 = GraphObject.EventType.START.asType2String();
            }
            if (me.getEventtype().compareTo(ModelElementConstants.INTERMEDIATEEVENT) == 0) {
                node.type2 = GraphObject.EventType.INTERMEDIATE.asType2String();
            }

        }

        System.out.println(node);
        return node;
    }

    private int countGatewayIncomingEdges(ModelElement gateway) {
        int no = 0;
        Collection c = this.model.values();
        Iterator it = c.iterator();
        do {
            ModelElement currentME = (ModelElement) it.next();
            if (currentME.getId().compareTo(gateway.getId()) != 0) {
                if (currentME.getTarget() != null && currentME.getTarget().compareTo(gateway.getId()) == 0) {
                    no++;
                }
            }
        } while (it.hasNext());

//        System.out.println("no of incoming edges for gateway "+gateway.getId()+" "+no);
        return no;

    }
}
