package com.bpmnq;

import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.OutputStream;
//import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;
//import org.ontoware.rdfreactor.runtime.Base;
import org.ontoware.rdfreactor.schema.rdfs.Class;
import org.ontoware.rdfreactor.schema.rdfs.Resource;

import com.bpmnq.GraphObject.ActivityType;
import com.bpmnq.GraphObject.EventType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.GraphObject.GateWayType;
import com.bpmnq.Path.TemporalType;
import com.bpmnq.rdf.ANDGateway;
//import com.bpmnq.rdf.Activity;
import com.bpmnq.rdf.BehavioralAssociation;
import com.bpmnq.rdf.Canvas;
import com.bpmnq.rdf.Edge;
import com.bpmnq.rdf.Endevent;
import com.bpmnq.rdf.Event;
import com.bpmnq.rdf.Gateway;
import com.bpmnq.rdf.Genericjoin;
import com.bpmnq.rdf.Genericshape;
import com.bpmnq.rdf.Genericsplit;
import com.bpmnq.rdf.Intermediateevent;
import com.bpmnq.rdf.NegativePath;
import com.bpmnq.rdf.NegativeSequenceflow;
import com.bpmnq.rdf.Node;
import com.bpmnq.rdf.ORGateway;
import com.bpmnq.rdf.Path;
import com.bpmnq.rdf.Sequenceflow;
import com.bpmnq.rdf.Startevent;
import com.bpmnq.rdf.Task;
import com.bpmnq.rdf.Thing;
import com.bpmnq.rdf.Variableactivity;
import com.bpmnq.rdf.XORGateway;

import com.bpmnq.rdf.DataObject;

import com.bpmnq.rdf.BidirectionalAssociation;
import com.bpmnq.rdf.UnidirectionalAssociation;

public final class QueryGraphBuilderRDF implements GraphBuilder {

    public enum RdfSyntax {
        eRDF,
        RDF_XML,
        ;
    }
    
    Model rdfModel;
    Map<org.ontoware.rdf2go.model.node.Resource, GraphObject> generatedGraphObjects;
    
    public QueryGraphBuilderRDF() {
        super();
        ModelFactory modelFact = RDF2Go.getModelFactory();
        this.rdfModel = modelFact.createModel();
        rdfModel.open();
    }
    
    /**
     * Creates a graph builder that transforms an Oryx-generated BPMN-Q query
     * graph into a memory-based query graph representation for further query processing.
     * It supports eRDF and RDF/XML syntax.
     * @param fileUri URI of the file, from which the graph data shall be read in.
     * @param syntax Either "eRDF" or "RDF/XML". Identifies the serialization syntax of the RDF stream
     * @throws IOException
     */
    public QueryGraphBuilderRDF(java.net.URI fileUri, RdfSyntax syntax) throws IOException {
        this();
        setRdfInput(fileUri, syntax);
    }
    
    /**
     * Creates a graph builder that transforms an Oryx-generated BPMN-Q query
     * graph into a memory-based query graph representation for further query processing.
     * It supports eRDF and RDF/XML syntax.
     * @param rdfStream A stream of RDF input, which is a serialization of the query graph.
     * @param syntax Identifies the serialization syntax of the RDF stream. 
     * @param baseURI The base URI to use for the RDF stream, i.e. where relative URI are related to.
     * @throws IOException
     */
    public QueryGraphBuilderRDF(InputStream rdfStream, RdfSyntax syntax, String baseURI)
            throws IOException {
        this();
        setRdfInput(rdfStream, syntax, baseURI);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        this.rdfModel.close();
        
        super.finalize();
    }

    protected void addCorrectedRdfTypes() {
        Map<org.ontoware.rdf2go.model.node.Resource, URI> subjTypes = 
            new HashMap<org.ontoware.rdf2go.model.node.Resource, URI>();
        
        ClosableIterator<Statement> resourceIter = 
            rdfModel.findStatements(Variable.ANY, Thing.TYPE, Variable.ANY);
        while (resourceIter.hasNext()) {
            Statement resourceTypeStmt = resourceIter.next();
            if (rdfModel.contains(resourceTypeStmt.getSubject(), Class.TYPE, Variable.ANY)) {
                // this resource already has a proper rdf:type set; so skip it
                continue;
            }
            
            // extract the literal type URI
            String typeUriString = resourceTypeStmt.getObject().asLiteral().getValue();
            URI typeUri = rdfModel.createURI(typeUriString);
            // we must not perform modifications to the model while the iterator is open
            // hence, store all pairs (subject, typeUri) and add later as an rdf:type
            subjTypes.put(resourceTypeStmt.getSubject(), typeUri);
        }
        resourceIter.close();
        
        for (org.ontoware.rdf2go.model.node.Resource subjRes : subjTypes.keySet()) {
            Thing.addType(rdfModel, subjRes, subjTypes.get(subjRes));            
        }
        
    }

    @SuppressWarnings("unchecked")
    public <GraphT extends ProcessGraph> GraphT buildGraph() throws FileFormatException {
        Canvas[] oryxCanvases = Canvas.getAllInstances_as(this.rdfModel).asArray();
        if (oryxCanvases.length > 1)
            throw new FileFormatException("There must not be more than one Oryx canvas.");
        QueryGraph result = new QueryGraph();
        
        List<Resource> grElems = oryxCanvases[0].getAllBpmnqRender_as().asList();
        List<Resource> edgeTypeElems = new ArrayList<Resource>();
        // node elements must be processed first, as processing the edges relies on
        // a complete list of graph nodes that it can connect
        // so, in this loop all edges are copied to a separate list which is processed later
        for (Resource graphElem : grElems) {
            if (isInstanceOf(graphElem, Edge.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, Task.RDFS_CLASS)) {
                Task rdfTask = (Task) graphElem.castTo(Task.class);
                GraphObject node = handleTask(rdfTask);
                
                generatedGraphObjects.put(rdfTask.asResource(), node);
                result.add(node);
                continue;
            }
            if (isInstanceOf(graphElem, Variableactivity.RDFS_CLASS)) {
                Variableactivity rdfVarAct = 
                    (Variableactivity) graphElem.castTo(Variableactivity.class);
                GraphObject varAct = handleVariableActivity(rdfVarAct);
                
                generatedGraphObjects.put(rdfVarAct.asResource(), varAct);
                result.add(varAct);
                continue;
            }
            if (isInstanceOf(graphElem, Genericshape.RDFS_CLASS)) {
                Genericshape rdfGenShape = (Genericshape) graphElem.castTo(Genericshape.class);
                GraphObject genShape = handleGenericShape(rdfGenShape);

                generatedGraphObjects.put(rdfGenShape.asResource(), genShape);
                result.add(genShape);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq.rdf.ANDGateway.RDFS_CLASS)) {
                com.bpmnq.rdf.ANDGateway rdfGateway = (com.bpmnq.rdf.ANDGateway) graphElem.castTo(com.bpmnq.rdf.ANDGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq.rdf.ORGateway.RDFS_CLASS)) {
                com.bpmnq.rdf.ORGateway rdfGateway = (com.bpmnq.rdf.ORGateway) graphElem.castTo(com.bpmnq.rdf.ORGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq.rdf.ExclusiveDatabasedGateway.RDFS_CLASS)) {
                com.bpmnq.rdf.ExclusiveDatabasedGateway rdfGateway = (com.bpmnq.rdf.ExclusiveDatabasedGateway) graphElem.castTo(com.bpmnq.rdf.ExclusiveDatabasedGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq.rdf.ExclusiveEventbasedGateway.RDFS_CLASS)) {
                com.bpmnq.rdf.ExclusiveEventbasedGateway rdfGateway = (com.bpmnq.rdf.ExclusiveEventbasedGateway) graphElem.castTo(com.bpmnq.rdf.ExclusiveEventbasedGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, Event.RDFS_CLASS)) {
                Event rdfEvent = (Event) graphElem.castTo(Event.class);
                GraphObject event = handleEvent(rdfEvent);
                
                generatedGraphObjects.put(rdfEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, DataObject.RDFS_CLASS)) {
        	DataObject rdfDataObject = (DataObject) graphElem.castTo(DataObject.class);
                com.bpmnq.DataObject dob = handleDataObject(rdfDataObject);
                
                generatedGraphObjects.put(rdfDataObject.asResource(), dob.originalNode());
                result.add(dob);
                continue;
            }
            // repeat for the new typing scheme by Oryx
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Sequenceflow.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.NegativeSequenceflow.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Path.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.NegativePath.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.BehavioralAssociation.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.BidirectionalAssociation.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.UndirectedAssociation.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.UnidirectionalAssociation.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Task.RDFS_CLASS)) {
        	com.bpmnq2.rdf.Task rdfTask = (com.bpmnq2.rdf.Task) graphElem.castTo(com.bpmnq2.rdf.Task.class);
                GraphObject node = handleTask(rdfTask);
                
                generatedGraphObjects.put(rdfTask.asResource(), node);
                result.add(node);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Variableactivity.RDFS_CLASS)) {
        	com.bpmnq2.rdf.Variableactivity rdfVarAct = 
                    (com.bpmnq2.rdf.Variableactivity) graphElem.castTo(com.bpmnq2.rdf.Variableactivity.class);
                GraphObject varAct = handleVariableActivity(rdfVarAct);
                
                generatedGraphObjects.put(rdfVarAct.asResource(), varAct);
                result.add(varAct);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Genericshape.RDFS_CLASS)) {
        	com.bpmnq2.rdf.Genericshape rdfGenShape = (com.bpmnq2.rdf.Genericshape) graphElem.castTo(com.bpmnq2.rdf.Genericshape.class);
                GraphObject genShape = handleGenericShape(rdfGenShape);

                generatedGraphObjects.put(rdfGenShape.asResource(), genShape);
                result.add(genShape);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.ANDGateway.RDFS_CLASS)) {
                com.bpmnq2.rdf.ANDGateway rdfGateway = (com.bpmnq2.rdf.ANDGateway) graphElem.castTo(com.bpmnq2.rdf.ANDGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.ORGateway.RDFS_CLASS)) {
                com.bpmnq2.rdf.ORGateway rdfGateway = (com.bpmnq2.rdf.ORGateway) graphElem.castTo(com.bpmnq2.rdf.ORGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.ExclusiveDatabasedGateway.RDFS_CLASS)) {
                com.bpmnq2.rdf.ExclusiveDatabasedGateway rdfGateway = (com.bpmnq2.rdf.ExclusiveDatabasedGateway) graphElem.castTo(com.bpmnq2.rdf.ExclusiveDatabasedGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.ExclusiveEventbasedGateway.RDFS_CLASS)) {
                com.bpmnq2.rdf.ExclusiveEventbasedGateway rdfGateway = (com.bpmnq2.rdf.ExclusiveEventbasedGateway) graphElem.castTo(com.bpmnq2.rdf.ExclusiveEventbasedGateway.class);
                GraphObject gateway = handleGateway(rdfGateway);
                
                generatedGraphObjects.put(rdfGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Startevent.RDFS_CLASS)) {
                com.bpmnq2.rdf.Startevent rdfEvent = (com.bpmnq2.rdf.Startevent) graphElem.castTo(com.bpmnq2.rdf.Startevent.class);
                GraphObject event = handleEvent(rdfEvent);
                
                generatedGraphObjects.put(rdfEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Intermediateevent.RDFS_CLASS)) {
                com.bpmnq2.rdf.Intermediateevent rdfEvent = (com.bpmnq2.rdf.Intermediateevent) graphElem.castTo(com.bpmnq2.rdf.Intermediateevent.class);
                GraphObject event = handleEvent(rdfEvent);
                
                generatedGraphObjects.put(rdfEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Endevent.RDFS_CLASS)) {
                com.bpmnq2.rdf.Endevent rdfEvent = (com.bpmnq2.rdf.Endevent) graphElem.castTo(com.bpmnq2.rdf.Endevent.class);
                GraphObject event = handleEvent(rdfEvent);
                
                generatedGraphObjects.put(rdfEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.DataObject.RDFS_CLASS)) {
        	com.bpmnq2.rdf.DataObject rdfDataObject = (com.bpmnq2.rdf.DataObject) graphElem.castTo(com.bpmnq2.rdf.DataObject.class);
                com.bpmnq.DataObject dob = handleDataObject(rdfDataObject);
                
                generatedGraphObjects.put(rdfDataObject.asResource(), dob.originalNode());
                result.add(dob);
                continue;
            }
        }
        
        // now, process all edges
        for (Resource graphElem : edgeTypeElems) {
            if (isInstanceOf(graphElem, Sequenceflow.RDFS_CLASS)) {
                Sequenceflow rdfSeqFlow = (Sequenceflow) graphElem.castTo(Sequenceflow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, NegativeSequenceflow.RDFS_CLASS)) {
                NegativeSequenceflow rdfNegSeqFlow =
                    (NegativeSequenceflow) graphElem.castTo(NegativeSequenceflow.class);
                SequenceFlow negSeqFlow = handleNegativeSequenceflow(rdfNegSeqFlow);
                
                result.addNegativeEdge(negSeqFlow);
                continue;
            }
            if (graphElem.isInstanceof(Path.RDFS_CLASS)) {
                Path rdfPath = (Path) graphElem.castTo(Path.class);
                com.bpmnq.Path memPath = handlePath(rdfPath);
                
                result.add(memPath);
                continue;
            }
            if (isInstanceOf(graphElem, NegativePath.RDFS_CLASS)) {
                NegativePath rdfNegPath = (NegativePath) graphElem.castTo(NegativePath.class);
                SequenceFlow negPath = handleNegativePath(rdfNegPath);
                
                result.addNegativePath(negPath);
                continue;
            }
            if (isInstanceOf(graphElem, UnidirectionalAssociation.RDFS_CLASS)) {
        	UnidirectionalAssociation rdfUnidirectionalAssociation = (UnidirectionalAssociation) graphElem.castTo(UnidirectionalAssociation.class);
                Association ass = handleAssociation(rdfUnidirectionalAssociation);
                
                result.add(ass);
                continue;
            }
            if (isInstanceOf(graphElem, BehavioralAssociation.RDFS_CLASS)) {
        	BehavioralAssociation rdfBehavioralAssociation = (BehavioralAssociation) graphElem.castTo(BehavioralAssociation.class);
                Association ass = handleAssociation(rdfBehavioralAssociation);
                ass.assType = com.bpmnq.Association.AssociaitonType.Behavioral;
                result.add(ass);
                continue;
            }
            if (isInstanceOf(graphElem, BidirectionalAssociation.RDFS_CLASS)) {
        	BidirectionalAssociation rdfBidirectionalAssociation = (BidirectionalAssociation) graphElem.castTo(BidirectionalAssociation.class);
                List<Association> ass = handleBidirectionalAssociation(rdfBidirectionalAssociation);
                for(Association a : ass)
                    result.add(a);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.Sequenceflow.RDFS_CLASS)) {
        	com.bpmnq2.rdf.Sequenceflow rdfSeqFlow = (com.bpmnq2.rdf.Sequenceflow) graphElem.castTo(com.bpmnq2.rdf.Sequenceflow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.NegativeSequenceflow.RDFS_CLASS)) {
        	com.bpmnq2.rdf.NegativeSequenceflow rdfNegSeqFlow =
                    (com.bpmnq2.rdf.NegativeSequenceflow) graphElem.castTo(com.bpmnq2.rdf.NegativeSequenceflow.class);
                SequenceFlow negSeqFlow = handleNegativeSequenceflow(rdfNegSeqFlow);
                
                result.addNegativeEdge(negSeqFlow);
                continue;
            }
            if (graphElem.isInstanceof(com.bpmnq2.rdf.Path.RDFS_CLASS)) {
        	com.bpmnq2.rdf.Path rdfPath = (com.bpmnq2.rdf.Path) graphElem.castTo(com.bpmnq2.rdf.Path.class);
                com.bpmnq.Path memPath = handlePath(rdfPath);
                
                result.add(memPath);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.NegativePath.RDFS_CLASS)) {
        	com.bpmnq2.rdf.NegativePath rdfNegPath = (com.bpmnq2.rdf.NegativePath) graphElem.castTo(com.bpmnq2.rdf.NegativePath.class);
                SequenceFlow negPath = handleNegativePath(rdfNegPath);
                
                result.addNegativePath(negPath);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.UnidirectionalAssociation.RDFS_CLASS)) {
        	com.bpmnq2.rdf.UnidirectionalAssociation rdfUnidirectionalAssociation = (com.bpmnq2.rdf.UnidirectionalAssociation) graphElem.castTo(com.bpmnq2.rdf.UnidirectionalAssociation.class);
                Association ass = handleAssociation(rdfUnidirectionalAssociation);
                
                result.add(ass);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.BehavioralAssociation.RDFS_CLASS)) {
        	com.bpmnq2.rdf.BehavioralAssociation rdfBehavioralAssociation = (com.bpmnq2.rdf.BehavioralAssociation) graphElem.castTo(com.bpmnq2.rdf.BehavioralAssociation.class);
                Association ass = handleAssociation(rdfBehavioralAssociation);
                ass.assType = com.bpmnq.Association.AssociaitonType.Behavioral;
                result.add(ass);
                continue;
            }
            if (isInstanceOf(graphElem, com.bpmnq2.rdf.BidirectionalAssociation.RDFS_CLASS)) {
        	com.bpmnq2.rdf.BidirectionalAssociation rdfBidirectionalAssociation = (com.bpmnq2.rdf.BidirectionalAssociation) graphElem.castTo(com.bpmnq2.rdf.BidirectionalAssociation.class);
                List<Association> ass = handleBidirectionalAssociation(rdfBidirectionalAssociation);
                for(Association a : ass)
                    result.add(a);
                continue;
            }
        }
        for (com.bpmnq.Path p : result.paths)
        {
           if (p.exclude != null)
               p.exclude = p.exclude.replace("Exclude(", "").replace(")","");
        }
        return (GraphT) result;
    }

    /**
     * TODO
     * @param clazz
     * @param superClassUri
     * @return
     */
    public static boolean isSubClassOf(Class clazz, URI superClassUri) {
        final URI clazzURI = clazz.asURI();
        if (clazzURI.equals(superClassUri))
            return true;
        
        // terminate recursion 
        if (clazzURI.equals(Resource.RDFS_CLASS))
            return false;
        
        // because instances are also subtype of Class, such objects might be 
        // passed in as well; thus, recurse inheritance tree upwards, 
        // differentiating between objects and class objects
        boolean isSuperclass = false;
        Class[] superclasses;
        if (clazz.isInstanceof(Class.RDFS_CLASS)) {
            superclasses = clazz.getAllSubClassOf_as().asArray();
        } 
        else {
            superclasses = clazz.getAllType_as().asArray();
        }
        
        for (Class superclass : superclasses) {
            isSuperclass = isSubClassOf(superclass, superClassUri);
            if (isSuperclass)
                return true;
        }

        
        return false;
    }
    
    /**
     * TODO
     * @param res
     * @param superClassUri
     * @return
     */
    public static boolean isInstanceOf(Resource res, URI superClassUri) {
        for (Class resourceType : res.getAllType_as().asArray()) {
            boolean isInstance = isSubClassOf(resourceType, superClassUri);
            if (isInstance)
                return true;
        }
        
        return false;
    }
    
    /**
     * 
     * @param rdfEvent
     * @return
     */
    protected GraphObject handleEvent(Event rdfEvent) {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.EVENT;
        if (rdfEvent.hasBpmnqId()) {
//            String evId = rdfEvent.getAllBpmnqId_as().firstValue();
             result.setID("-"+Utilities.getNextVal());
            
        }

        if (isInstanceOf(rdfEvent, Startevent.RDFS_CLASS)) {
            result.type2 = EventType.START.asType2String();
        }
        if (isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS)) {
            result.type2 = EventType.INTERMEDIATE.asType2String();
        }
        if (isInstanceOf(rdfEvent, Endevent.RDFS_CLASS)) {
            result.type2 = EventType.END.asType2String();
        }
        String rdfName = rdfEvent.getAllBpmnqId_as().firstValue();
        rdfName = normalizeRdfLiteral(rdfName);
        if (rdfName.startsWith("?") || rdfName.startsWith("#oryx"))
            result.setName(rdfName);
        return result;
    }

    /**
     * Takes a Gateway RDF node and transforms it to a bpmnq GraphObject with type GATEWAY 
     * @param rdfGateway
     * @return
     * @throws FileFormatException thrown if a gateway cannot be clearly identified as
     *      a split or join node
     */
    protected GraphObject handleGateway(Gateway rdfGateway) throws FileFormatException {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.GATEWAY;
        
        result.setID("-"+Utilities.getNextVal());
        if (rdfGateway.hasBpmnqId()) {
//            String gwId = rdfGateway.getAllBpmnqId_as().firstValue();
            String id = rdfGateway.getAllBpmnqId_as().firstValue();
            id = normalizeRdfLiteral(id);
            if (id.startsWith("?") || id.startsWith("#oryx"))
        	result.setName(id);
        }
        
        // is it a split or a join node?
        /** true means split, false means join */
        boolean isSplitStructured = true;
        boolean splitJoinStructureIsKnown = false;
        long numOutgoings = rdfGateway.getAllBpmnqOutgoing_as().count();
        List<org.ontoware.rdf2go.model.node.Resource> incomings = new ArrayList<org.ontoware.rdf2go.model.node.Resource>();
        ClosableIterator<Statement> iter = 
            rdfModel.findStatements(Variable.ANY, Gateway.OUTGOING, rdfGateway.asResource());
        while (iter.hasNext()) {
            Statement tripel = iter.next();
            incomings.add(tripel.getSubject());
        }
        iter.close();
        final int numIncomings = incomings.size();
        if (numIncomings <= 1 && numOutgoings > 1) {
            isSplitStructured = true; // the graph structure suggests it's a split node
            splitJoinStructureIsKnown = true;
        } else if (numIncomings > 1 && numOutgoings <= 1) {
            isSplitStructured = false; // the graph structure suggests it's a join node
            splitJoinStructureIsKnown = true;
        }

        if (rdfGateway instanceof ANDGateway) {
            ANDGateway rdfAndGateway = (ANDGateway) rdfGateway.castTo(ANDGateway.class);
            String rdfSplitOrJoin = rdfAndGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
            if (rdfSplitOrJoin.equals("AND Join")) {
                if (splitJoinStructureIsKnown && isSplitStructured)
                    throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a split, but says it was a join.");
                
                result.type2 = GateWayType.AND_JOIN.asType2String();
            } else if (rdfSplitOrJoin.equals("AND Split")) {
                if (splitJoinStructureIsKnown && !isSplitStructured)
                    throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a join, but says it was a spli.");

                result.type2 = GateWayType.AND_SPLIT.asType2String();
            } else throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
                    + "doesn't specifiy whether it's a join or a split.");
            
        }
        if (rdfGateway instanceof ORGateway) {
            ORGateway rdfOrGateway = (ORGateway) rdfGateway.castTo(ORGateway.class);
            String rdfSplitOrJoin = rdfOrGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
            if (rdfSplitOrJoin.equals("OR Join")) {
                if (splitJoinStructureIsKnown && isSplitStructured)
                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a split, but says it was a join.");
                
                result.type2 = GateWayType.OR_JOIN.asType2String();
            } else if (rdfSplitOrJoin.equals("OR Split")) {
                if (splitJoinStructureIsKnown && !isSplitStructured)
                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a join, but says it was a spli.");

                result.type2 = GateWayType.OR_SPLIT.asType2String();
            } else throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
                    + "doesn't specifiy whether it's a join or a split.");            
        }
        if (rdfGateway instanceof XORGateway) {
            XORGateway rdfXorGateway = (XORGateway) rdfGateway.castTo(XORGateway.class);
            String rdfSplitOrJoin = rdfXorGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
            if (rdfSplitOrJoin.equals("XOR Join")) {
                if (splitJoinStructureIsKnown && isSplitStructured)
                    throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a split, but says it was a join.");
                
                result.type2 = GateWayType.XOR_JOIN.asType2String();
            } else if (rdfSplitOrJoin.equals("XOR Split")) {
                if (splitJoinStructureIsKnown && !isSplitStructured)
                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
                            + " looks like a join, but says it was a spli.");

                result.type2 = GateWayType.XOR_SPLIT.asType2String();
            } else throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
                    + "doesn't specifiy whether it's a join or a split."); 
        }
        if (rdfGateway instanceof Genericsplit) {
            if (splitJoinStructureIsKnown && !isSplitStructured)
                throw new FileFormatException("Generic Split Gateway resource " + rdfGateway.getResource().toString() + " looks more like a join.");
            result.type2 = GateWayType.GENERIC_SPLIT.asType2String();
        }
        if (rdfGateway instanceof Genericjoin) {
            if (splitJoinStructureIsKnown && isSplitStructured)
                throw new FileFormatException("Generic Join Gateway resource " + rdfGateway.getResource().toString() + " looks more like a join.");
            result.type2 = GateWayType.GENERIC_JOIN.asType2String();
        }
        
        return result;
    }

    /**
     * 
     * @param rdfGenShape
     * @return
     */
    protected GraphObject handleGenericShape(Genericshape rdfGenShape) {
        GraphObject node = initializeActivity();
        node.type2 = ActivityType.GENERIC_SHAPE.asType2String();
        node.setID("-"+Utilities.getNextVal());
        if (rdfGenShape.hasBpmnqName()) {
            String rdfName = rdfGenShape.getAllBpmnqName_as().firstValue();
            rdfName = rdfGenShape.getAllBpmnqName_as().firstValue();
            rdfName = normalizeRdfLiteral(rdfName);
            if (rdfName.startsWith("?") || rdfName.startsWith("#oryx"))
        	node.setName(rdfName);
            
        }
        else if (rdfGenShape.hasBpmnqId())
        {
            String rdfName ;
            rdfName = rdfGenShape.getAllBpmnqId_as().firstValue();
            rdfName = normalizeRdfLiteral(rdfName);
            if (rdfName.startsWith("?") || rdfName.startsWith("#oryx"))
        	node.setName(rdfName);
            
        }
        
        // TODO set the node ID in a meaningful way
        return node;

    }

    /**
     * 
     * @param rdfNegPath
     * @return
     */
    protected SequenceFlow handleNegativePath(NegativePath rdfNegPath) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfNegPath);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfNegPath.getAllBpmnqOutgoing_as().firstValue(); // might return null

        return createSequenceFlow(startsAt, endsAt);

    }

    /**
     * 
     * @param rdfNegSeqFlow
     * @return
     */
    protected SequenceFlow handleNegativeSequenceflow(
            NegativeSequenceflow rdfNegSeqFlow) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfNegSeqFlow);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfNegSeqFlow.getAllBpmnqOutgoing_as().firstValue(); // might return null

        return createSequenceFlow(startsAt, endsAt);
    }

    /**
     * @param rdfPath
     * @return
     */
    protected com.bpmnq.Path handlePath(Path rdfPath) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfPath);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfPath.getAllBpmnqOutgoing_as().firstValue(); // might return null

        com.bpmnq.Path memPath = createPath(startsAt, endsAt);
        if (rdfPath.hasBpmnqExclude()) {
            memPath.exclude = rdfPath.getAllBpmnqExclude_as().firstValue();
        }
        if (rdfPath.hasBpmnqName()) {
            memPath.label = rdfPath.getAllBpmnqName_as().firstValue();
            memPath.label = rdfPath.getAllBpmnqId_as().firstValue();
        }
        if (rdfPath.hasBpmnqTemporalproperty()) {
            // lookup whether there is a TemporalProperty type for the specified value.
            // if not, NONE will be used.
            String tagText = rdfPath.getAllBpmnqTemporalproperty_as().firstValue().trim().replace(" ", "_");
            TemporalType tag = TemporalType.NONE;
            for (TemporalType tagType : com.bpmnq.Path.TemporalType.values()) {
        	if (tagType.toString().equalsIgnoreCase(tagText)) {
        	    tag = tagType;
        	    break;
        	}
            }
            memPath.setTemporalTag(tag);
        }
        return memPath;
    }

    /**
     * 
     * @param rdfSeqFlow
     * @return
     */
    protected SequenceFlow handleSequenceflow(Sequenceflow rdfSeqFlow) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfSeqFlow);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfSeqFlow.getAllBpmnqOutgoing_as().firstValue(); // might return null

        return createSequenceFlow(startsAt, endsAt);
    }
    protected Association handleAssociation(com.bpmnq.rdf.Association rdfAss) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnqOutgoing_as().firstValue(); // might return null
        
        return createAssociation(startsAt, endsAt);
    }
    private Association createAssociation(Resource startsAt, Resource endsAt)
    throws ClassCastException {
	Association result;
	if (startsAt == null || endsAt == null) {
	    result = new Association();
	} else {
	    if (generatedGraphObjects.keySet().contains(startsAt.asResource()) && generatedGraphObjects.keySet().contains(endsAt.asResource()) )
		result = new Association(generatedGraphObjects.get(startsAt.asResource()),
		    generatedGraphObjects.get(endsAt.asResource()));
	    else
		result = new Association();
	}

	return result;
    }
    protected Association handleAssociation(com.bpmnq2.rdf.Association rdfAss) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnqOutgoing_as().firstValue(); // might return null

        return createAssociation(startsAt, endsAt);
    }
    private SequenceFlow createSequenceFlow(Resource startsAt, Resource endsAt)
            throws ClassCastException {
        SequenceFlow result;
        if (startsAt == null || endsAt == null) {
            result = new SequenceFlow();
        } else {
            result = new SequenceFlow(generatedGraphObjects.get(startsAt.asResource()),
                    generatedGraphObjects.get(endsAt.asResource()));
        }
        
        return result;
    }
    
    private com.bpmnq.Path createPath(Resource startsAt, Resource endsAt)
    throws ClassCastException {
	com.bpmnq.Path result;
	if (startsAt == null || endsAt == null) {
	    result = new com.bpmnq.Path();
	} else {
	    result = new com.bpmnq.Path(generatedGraphObjects.get(startsAt.asResource()),
		    generatedGraphObjects.get(endsAt.asResource()));
	}

	return result;
    }

    private String normalizeRdfLiteral(String rdfName) {
        rdfName = rdfName.replace('\n', ' ');
        rdfName = rdfName.replace('\r', ' ');
        rdfName = rdfName.replace('\t', ' ');
        rdfName = rdfName.replace("  ", " ");
        rdfName = rdfName.replace("  ", " ");
        rdfName = rdfName.trim();
        return rdfName;
    }

    /**
     * @param rdfTask
     * @return
     */
    protected GraphObject handleTask(Task rdfTask) {
        GraphObject node = initializeActivity();
        
        if (rdfTask.hasBpmnqName()) {
            String rdfName = rdfTask.getAllBpmnqName_as().firstValue(); 
            node.setName(normalizeRdfLiteral(rdfName));
            node.setID("-"+Utilities.getNextVal());
        }
        // TODO set the node ID in a meaningful way
        return node;
    }
    
    /**
     * 
     * @param rdfVarAct
     * @return
     */
    protected GraphObject handleVariableActivity(Variableactivity rdfVarAct) {
        GraphObject node = initializeActivity();
        
        if (rdfVarAct.hasBpmnqName()) {
            String rdfName = rdfVarAct.getAllBpmnqName_as().firstValue();
            node.setName(normalizeRdfLiteral(rdfName));
            node.setID("-"+Utilities.getNextVal());
        }
        // TODO set the node ID in a meaningful way
        return node;
    }
    
    /**
     * creates an empty GraphObject instance and sets its type info
     * @return
     */
    private GraphObject initializeActivity() {
        GraphObject node = new GraphObject();
        node.type = GraphObjectType.ACTIVITY;
        node.type2 = "";
        return node;
    }
    
    protected Resource[] findAllBpmnqIncoming(Node node) {
        return findAllBpmnqIncoming(node.asResource());
    }
    
    protected Resource[] findAllBpmnqIncoming(Edge edge) {
        return findAllBpmnqIncoming(edge.asResource());
    }
    
    private Resource[] findAllBpmnqIncoming(org.ontoware.rdf2go.model.node.Resource rdfResource) {
        ClosableIterator<Statement> iter = this.rdfModel.findStatements(Variable.ANY, 
                Edge.OUTGOING, rdfResource);
        List<Resource> resultList = new ArrayList<Resource>();
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            Resource res = new Resource(this.rdfModel, stmt.getSubject(), false);
            resultList.add(res);
        }
        iter.close();
        
        return resultList.toArray(new Resource[0]);
    }
    
    /**
     * Removes all statements from the graph model. Breaks the connection to the 
     * underlying rdf file.
     * <p>After <code>reset</code>, <code>setRdfFilename</code> must be called 
     * before calling other methods. </p> 
     */
    public void reset() {
        this.rdfModel.removeAll();
        this.generatedGraphObjects = null;
    }

    /**
     * TODO
     * @param rdfFilename the rdfFilename to set
     * @throws FileNotFoundException 
     */
    public void setRdfInput(java.net.URI rdfFileUri, RdfSyntax syntax) throws IOException {
        File rdfStoreFile = new File(rdfFileUri);
        if (! rdfStoreFile.exists()) {
            throw new FileNotFoundException("Cannot open the specified file " + rdfFileUri.toString());
        }
            
        String baseURI = rdfStoreFile.toURI().toString();
        InputStream storeStream = new BufferedInputStream(
                new FileInputStream(rdfStoreFile));
        setRdfInput(storeStream, syntax, baseURI);
        storeStream.close();
    }
    
    /**
     * TODO
     * @param rdfStream
     * @param baseURI
     * @throws IOException
     */
    public void setRdfInput(InputStream rdfStream, RdfSyntax syntax,
            String baseURI) throws IOException {
        // empty the model first
        reset();
        // load the RDFS bpmnq into the model, so that subclass tripels are present 
        InputStream schemaStream = this.getClass().getResourceAsStream("/bpmnq-schema.rdf");
        schemaStream = new BufferedInputStream(schemaStream);
        this.rdfModel.readFrom(schemaStream);
        
        switch(syntax) {
        case RDF_XML:
            break;
        case eRDF:
            rdfStream = transformERdf2RdfXml(rdfStream);
            break;
        }
        this.rdfModel.readFrom(rdfStream, Syntax.RdfXml, baseURI);

        this.generatedGraphObjects = 
            new HashMap<org.ontoware.rdf2go.model.node.Resource, GraphObject>();
        
        // fix Oryx's broken resource type information
        addCorrectedRdfTypes();

    }
    protected com.bpmnq.DataObject handleDataObject(DataObject rdfDataObject){
	com.bpmnq.DataObject node= new com.bpmnq.DataObject();
	node.doID = "-1";//rdfDataObject.asURI().toString();
	node.name = rdfDataObject.getAllBpmnqName_as().firstValue();
	node.setState(rdfDataObject.getAllBpmnqState_as().firstValue().replace('[', ' ').replace(']',' ').trim());
	return node;
    }
    protected com.bpmnq.DataObject handleDataObject(com.bpmnq2.rdf.DataObject rdfDataObject){
	com.bpmnq.DataObject node= new com.bpmnq.DataObject();
	node.doID = "-1";//rdfDataObject.asURI().toString();
	node.name = rdfDataObject.getAllBpmnqName_as().firstValue();
	node.setState(rdfDataObject.getAllBpmnqState_as().firstValue().replace('[', ' ').replace(']',' ').trim());
	return node;
    }
    protected List<Association> handleBidirectionalAssociation(com.bpmnq.rdf.BidirectionalAssociation rdfAss) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnqOutgoing_as().firstValue(); // might return null
        List<Association> result = new ArrayList<Association>();
        result.add(createAssociation(startsAt, endsAt));
        result.add(createAssociation(endsAt, startsAt));
        return result;
    }
    protected List<Association> handleBidirectionalAssociation(com.bpmnq2.rdf.BidirectionalAssociation rdfAss) {
        Resource[] startsAtArr = findAllBpmnqIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnqOutgoing_as().firstValue(); // might return null
        List<Association> result = new ArrayList<Association>();
        result.add(createAssociation(startsAt, endsAt));
        result.add(createAssociation(endsAt, startsAt));
        return result;
    }
    protected InputStream transformERdf2RdfXml(InputStream eRdfStream) throws IOException {
        
        InputStream styleStream = new BufferedInputStream(
                getClass().getResourceAsStream("/extract-rdf.xsl"));
        ByteArrayOutputStream rdfXmlMemStore = new ByteArrayOutputStream(); 
        
        Source erdfInput = new StreamSource(eRdfStream);
        Source styleInput = new StreamSource(styleStream);
        Result rdfXml = new StreamResult(rdfXmlMemStore);
        
        TransformerFactory transFac = TransformerFactory.newInstance();
        try {
            Transformer trans = transFac.newTransformer(styleInput);
            trans.transform(erdfInput, rdfXml);
        } catch (TransformerException e) {
            throw new IOException("Invalid eRDF file format", e);
        } finally {
            rdfXmlMemStore.close();
            styleStream.close();
        }

        eRdfStream.close();
        return new ByteArrayInputStream(rdfXmlMemStore.toByteArray());
    }

}
