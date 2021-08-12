package com.bpmnq;

import java.io.BufferedInputStream;
//import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;

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
import org.ontoware.rdfreactor.schema.rdfs.Class;
import org.ontoware.rdfreactor.schema.rdfs.Resource;


import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.GraphObject.GateWayType;


// the following import depend on the nature of imported RDF -> BPMN
import de.hpi.bpmn.rdf.*;
import de.hpi.bpmn.rdf.DataObject;


public final class ProcessGraphBuilderRDF implements GraphBuilder {

    public enum RdfSyntax {
        eRDF,
        RDF_XML,
        ;
    }
    
    Model rdfModel;
    Map<org.ontoware.rdf2go.model.node.Resource, GraphObject> generatedGraphObjects;
    
    public ProcessGraphBuilderRDF() {
        super();
        ModelFactory modelFact = RDF2Go.getModelFactory();
        this.rdfModel = modelFact.createModel();
        rdfModel.open();
    }
    
    /**
     * Creates a graph builder that transforms an Oryx-generated BPMN process
     * model graph into a memory-based process graph representation for further query processing.
     * It supports eRDF and RDF/XML syntax.
     * @param fileUri URI of the file, from which the graph data shall be read in.
     * @param syntax Either "eRDF" or "RDF/XML". Identifies the serialization syntax of the RDF stream
     * @throws IOException
     */
    public ProcessGraphBuilderRDF(java.net.URI fileUri, RdfSyntax syntax) throws IOException {
        this();
        setRdfInput(fileUri, syntax);
    }
    
    /**
     * Creates a graph builder that transforms an Oryx-generated BPMN process
     * model graph into a memory-based process graph representation for further query processing.
     * It supports eRDF and RDF/XML syntax.
     * @param rdfStream A stream of RDF input, which is a serialization of the query graph.
     * @param syntax Identifies the serialization syntax of the RDF stream. 
     * @param baseURI The base URI to use for the RDF stream, i.e. where relative URI are related to.
     * @throws IOException
     */
    public ProcessGraphBuilderRDF(InputStream rdfStream, RdfSyntax syntax, String baseURI)
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
        ProcessGraph result = new ProcessGraph();
        
        List<Resource> grElems = oryxCanvases[0].getAllBpmnRender_as().asList();
//        grElems.get(0).asURI().;
        List<Resource> edgeTypeElems = new ArrayList<Resource>();
        // node elements must be processed first, as processing the edges relies on
        // a complete list of graph nodes that it can connect
        // so, in this loop all edges are copied to a separate list which is processed later
        for (Resource graphElem : grElems) {
            if (isInstanceOf(graphElem, Edge.RDFS_CLASS)) {
                edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, ANDGateway.RDFS_CLASS)) {
        	ANDGateway rdfANDGateway = (ANDGateway) graphElem.castTo(ANDGateway.class);
                GraphObject gateway = handleGateway(rdfANDGateway);
                
                generatedGraphObjects.put(rdfANDGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, ExclusiveDatabasedGateway.RDFS_CLASS)) {
        	ExclusiveDatabasedGateway rdfExclusiveDatabasedGateway = (ExclusiveDatabasedGateway) graphElem.castTo(ExclusiveDatabasedGateway.class);
                GraphObject gateway = handleGateway(rdfExclusiveDatabasedGateway);
                
                generatedGraphObjects.put(rdfExclusiveDatabasedGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, ExclusiveEventbasedGateway.RDFS_CLASS)) {
        	ExclusiveEventbasedGateway rdfExclusiveEventbasedGateway = (ExclusiveEventbasedGateway) graphElem.castTo(ExclusiveEventbasedGateway.class);
                GraphObject gateway = handleGateway(rdfExclusiveEventbasedGateway);
                
                generatedGraphObjects.put(rdfExclusiveEventbasedGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, ORGateway.RDFS_CLASS)) {
                ORGateway rdfORGateway = (ORGateway) graphElem.castTo(ORGateway.class);
                GraphObject gateway = handleGateway(rdfORGateway);
                
                generatedGraphObjects.put(rdfORGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, Task.RDFS_CLASS)) {
                Task rdfTask = (Task) graphElem.castTo(Task.class);
                GraphObject node = handleTask(rdfTask);
                
                generatedGraphObjects.put(rdfTask.asResource(), node);
                result.add(node);
                continue;
            }
            if (isInstanceOf(graphElem, Subprocess.RDFS_CLASS)) {
                Subprocess rdfSubProcess = 
                    (Subprocess) graphElem.castTo(Subprocess.class);
                GraphObject subProcess = handleSubprocess(rdfSubProcess);
                
                generatedGraphObjects.put(rdfSubProcess.asResource(), subProcess);
                result.add(subProcess);
                continue;
            }
            if (isInstanceOf(graphElem, StartTimerEvent.RDFS_CLASS)) {
                StartTimerEvent rdfStartTimerEvent = (StartTimerEvent) graphElem.castTo(StartTimerEvent.class);
                GraphObject startTimerEvent = handleEvent(rdfStartTimerEvent);

                generatedGraphObjects.put(rdfStartTimerEvent.asResource(), startTimerEvent);
                result.add(startTimerEvent);
                continue;
            }
            if (isInstanceOf(graphElem, StartSignalEvent.RDFS_CLASS)) {
                StartSignalEvent rdfStartSignalEvent = (StartSignalEvent) graphElem.castTo(StartSignalEvent.class);
                GraphObject startSignalEvent = handleEvent(rdfStartSignalEvent);

                generatedGraphObjects.put(rdfStartSignalEvent.asResource(), startSignalEvent);
                result.add(startSignalEvent);
                continue;
            }
            if (isInstanceOf(graphElem, StartMultipleEvent.RDFS_CLASS)) {
                StartMultipleEvent rdfStartMultipleEvent = (StartMultipleEvent) graphElem.castTo(StartMultipleEvent.class);
                GraphObject startMultipleEvent = handleEvent(rdfStartMultipleEvent);

                generatedGraphObjects.put(rdfStartMultipleEvent.asResource(), startMultipleEvent);
                result.add(startMultipleEvent);
                continue;
            }
            if (isInstanceOf(graphElem, StartMessageEvent.RDFS_CLASS)) {
                StartMessageEvent rdfStartMessageEvent = (StartMessageEvent) graphElem.castTo(StartMessageEvent.class);
                GraphObject startMessageEvent = handleEvent(rdfStartMessageEvent);

                generatedGraphObjects.put(rdfStartMessageEvent.asResource(), startMessageEvent);
                result.add(startMessageEvent);
                continue;
            }
            if (isInstanceOf(graphElem, Startevent.RDFS_CLASS)) {
                Startevent rdfStartevent = (Startevent) graphElem.castTo(Startevent.class);
                GraphObject startevent = handleEvent(rdfStartevent);

                generatedGraphObjects.put(rdfStartevent.asResource(), startevent);
                result.add(startevent);
                continue;
            }
            if (isInstanceOf(graphElem, StartConditionalEvent.RDFS_CLASS)) {
                StartConditionalEvent rdfStartConditionalEvent = (StartConditionalEvent) graphElem.castTo(StartConditionalEvent.class);
                GraphObject startConditionalEvent = handleEvent(rdfStartConditionalEvent);

                generatedGraphObjects.put(rdfStartConditionalEvent.asResource(), startConditionalEvent);
                result.add(startConditionalEvent);
                continue;
            }
            
            if (isInstanceOf(graphElem, IntermediateTimerEvent.RDFS_CLASS)) {
        	IntermediateTimerEvent rdfIntermediateTimerEvent = (IntermediateTimerEvent) graphElem.castTo(IntermediateTimerEvent.class);
                GraphObject intermediateTimerEvent = handleEvent(rdfIntermediateTimerEvent);

                generatedGraphObjects.put(rdfIntermediateTimerEvent.asResource(), intermediateTimerEvent);
                result.add(intermediateTimerEvent);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateSignalEventThrowing.RDFS_CLASS)) {
        	IntermediateSignalEventThrowing rdfIntermediateSignalEventThrowing = (IntermediateSignalEventThrowing) graphElem.castTo(IntermediateSignalEventThrowing.class);
                GraphObject intermediateSignalEventThrowing = handleEvent(rdfIntermediateSignalEventThrowing);

                generatedGraphObjects.put(rdfIntermediateSignalEventThrowing.asResource(), intermediateSignalEventThrowing);
                result.add(intermediateSignalEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateSignalEventCatching.RDFS_CLASS)) {
        	IntermediateSignalEventCatching rdfIntermediateSignalEventCatching = (IntermediateSignalEventCatching) graphElem.castTo(IntermediateSignalEventCatching.class);
                GraphObject intermediateSignalEventCatching = handleEvent(rdfIntermediateSignalEventCatching);

                generatedGraphObjects.put(rdfIntermediateSignalEventCatching.asResource(), intermediateSignalEventCatching);
                result.add(intermediateSignalEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateMultipleEventThrowing.RDFS_CLASS)) {
        	IntermediateMultipleEventThrowing rdfIntermediateMultipleEventThrowing = (IntermediateMultipleEventThrowing) graphElem.castTo(IntermediateMultipleEventThrowing.class);
                GraphObject intermediateMultipleEventThrowing = handleEvent(rdfIntermediateMultipleEventThrowing);

                generatedGraphObjects.put(rdfIntermediateMultipleEventThrowing.asResource(), intermediateMultipleEventThrowing);
                result.add(intermediateMultipleEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateMultipleEventCatching.RDFS_CLASS)) {
        	IntermediateMultipleEventCatching rdfIntermediateMultipleEventCatching = (IntermediateMultipleEventCatching) graphElem.castTo(IntermediateMultipleEventCatching.class);
                GraphObject intermediateMultipleEventCatching = handleEvent(rdfIntermediateMultipleEventCatching);

                generatedGraphObjects.put(rdfIntermediateMultipleEventCatching.asResource(), intermediateMultipleEventCatching);
                result.add(intermediateMultipleEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateMessageEventThrowing.RDFS_CLASS)) {
        	IntermediateMessageEventThrowing rdfIntermediateMessageEventThrowing = (IntermediateMessageEventThrowing) graphElem.castTo(IntermediateMessageEventThrowing.class);
                GraphObject intermediateMessageEventThrowing = handleEvent(rdfIntermediateMessageEventThrowing);

                generatedGraphObjects.put(rdfIntermediateMessageEventThrowing.asResource(), intermediateMessageEventThrowing);
                result.add(intermediateMessageEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateMessageEventCatching.RDFS_CLASS)) {
        	IntermediateMessageEventCatching rdfIntermediateMessageEventCatching = (IntermediateMessageEventCatching) graphElem.castTo(IntermediateMessageEventCatching.class);
                GraphObject intermediateMessageEventCatching = handleEvent(rdfIntermediateMessageEventCatching);

                generatedGraphObjects.put(rdfIntermediateMessageEventCatching.asResource(), intermediateMessageEventCatching);
                result.add(intermediateMessageEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateLinkEventThrowing.RDFS_CLASS)) {
        	IntermediateLinkEventThrowing rdfIntermediateLinkEventThrowing = (IntermediateLinkEventThrowing) graphElem.castTo(IntermediateLinkEventThrowing.class);
                GraphObject intermediateLinkEventThrowing = handleEvent(rdfIntermediateLinkEventThrowing);

                generatedGraphObjects.put(rdfIntermediateLinkEventThrowing.asResource(), intermediateLinkEventThrowing);
                result.add(intermediateLinkEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateLinkEventCatching.RDFS_CLASS)) {
        	IntermediateLinkEventCatching rdfIntermediateLinkEventCatching = (IntermediateLinkEventCatching) graphElem.castTo(IntermediateLinkEventCatching.class);
                GraphObject intermediateLinkEventCatching = handleEvent(rdfIntermediateLinkEventCatching);

                generatedGraphObjects.put(rdfIntermediateLinkEventCatching.asResource(), intermediateLinkEventCatching);
                result.add(intermediateLinkEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, Intermediateevent.RDFS_CLASS)) {
        	Intermediateevent rdfIntermediateevent = (Intermediateevent) graphElem.castTo(Intermediateevent.class);
                GraphObject event = handleEvent(rdfIntermediateevent);
                
                generatedGraphObjects.put(rdfIntermediateevent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateErrorEvent.RDFS_CLASS)) {
        	IntermediateErrorEvent rdfIntermediateErrorEvent = (IntermediateErrorEvent) graphElem.castTo(IntermediateErrorEvent.class);
                GraphObject event = handleEvent(rdfIntermediateErrorEvent);
                
                generatedGraphObjects.put(rdfIntermediateErrorEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateConditionalEvent.RDFS_CLASS)) {
        	IntermediateConditionalEvent rdfIntermediateConditionalEvent = (IntermediateConditionalEvent) graphElem.castTo(IntermediateConditionalEvent.class);
                GraphObject event = handleEvent(rdfIntermediateConditionalEvent);
                
                generatedGraphObjects.put(rdfIntermediateConditionalEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateCompensationEventThrowing.RDFS_CLASS)) {
        	IntermediateCompensationEventThrowing rdfIntermediateCompensationEventThrowing = (IntermediateCompensationEventThrowing) graphElem.castTo(IntermediateCompensationEventThrowing.class);
                GraphObject intermediateCompensationEventThrowing = handleEvent(rdfIntermediateCompensationEventThrowing);

                generatedGraphObjects.put(rdfIntermediateCompensationEventThrowing.asResource(), intermediateCompensationEventThrowing);
                result.add(intermediateCompensationEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateCompensationEventCatching.RDFS_CLASS)) {
        	IntermediateCompensationEventCatching rdfIntermediateCompensationEventCatching = (IntermediateCompensationEventCatching) graphElem.castTo(IntermediateCompensationEventCatching.class);
                GraphObject intermediateCompensationEventCatching = handleEvent(rdfIntermediateCompensationEventCatching);

                generatedGraphObjects.put(rdfIntermediateCompensationEventCatching.asResource(), intermediateCompensationEventCatching);
                result.add(intermediateCompensationEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, IntermediateCancelEvent.RDFS_CLASS)) {
        	IntermediateCancelEvent rdfIntermediateCancelEvent = (IntermediateCancelEvent) graphElem.castTo(IntermediateCancelEvent.class);
                GraphObject event = handleEvent(rdfIntermediateCancelEvent);
                
                generatedGraphObjects.put(rdfIntermediateCancelEvent.asResource(), event);
                result.add(event);
                continue;
            }
//            if (isInstanceOf(graphElem, Event.RDFS_CLASS)) {
//                Event rdfEvent = (Event) graphElem.castTo(Event.class);
//                GraphObject event = handleEvent(rdfEvent);
//                
//                generatedGraphObjects.put(rdfEvent.asResource(), event);
//                result.add(event);
//                continue;
//            }
            if (isInstanceOf(graphElem, EndTerminateEvent.RDFS_CLASS)) {
        	EndTerminateEvent rdfEndTerminateEvent = (EndTerminateEvent) graphElem.castTo(EndTerminateEvent.class);
                GraphObject event = handleEvent(rdfEndTerminateEvent);
                
                generatedGraphObjects.put(rdfEndTerminateEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndSignalEvent.RDFS_CLASS)) {
        	EndSignalEvent rdfEndSignalEvent = (EndSignalEvent) graphElem.castTo(EndSignalEvent.class);
                GraphObject event = handleEvent(rdfEndSignalEvent);
                
                generatedGraphObjects.put(rdfEndSignalEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndMultipleEvent.RDFS_CLASS)) {
        	EndMultipleEvent rdfEndMultipleEvent = (EndMultipleEvent) graphElem.castTo(EndMultipleEvent.class);
                GraphObject event = handleEvent(rdfEndMultipleEvent);
                
                generatedGraphObjects.put(rdfEndMultipleEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndMessageEvent.RDFS_CLASS)) {
        	EndMessageEvent rdfEndMessageEvent = (EndMessageEvent) graphElem.castTo(EndMessageEvent.class);
                GraphObject event = handleEvent(rdfEndMessageEvent);
                
                generatedGraphObjects.put(rdfEndMessageEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, Endevent.RDFS_CLASS)) {
        	Endevent rdfEndevent = (Endevent) graphElem.castTo(Endevent.class);
                GraphObject event = handleEvent(rdfEndevent);
                
                generatedGraphObjects.put(rdfEndevent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndErrorEvent.RDFS_CLASS)) {
        	EndErrorEvent rdfEndErrorEvent = (EndErrorEvent) graphElem.castTo(EndErrorEvent.class);
                GraphObject event = handleEvent(rdfEndErrorEvent);
                
                generatedGraphObjects.put(rdfEndErrorEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndCompensationEvent.RDFS_CLASS)) {
        	EndCompensationEvent rdfEndCompensationEvent = (EndCompensationEvent) graphElem.castTo(EndCompensationEvent.class);
                GraphObject event = handleEvent(rdfEndCompensationEvent);
                
                generatedGraphObjects.put(rdfEndCompensationEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, EndCancelEvent.RDFS_CLASS)) {
        	EndCancelEvent rdfEndCancelEvent = (EndCancelEvent) graphElem.castTo(EndCancelEvent.class);
                GraphObject event = handleEvent(rdfEndCancelEvent);
                
                generatedGraphObjects.put(rdfEndCancelEvent.asResource(), event);
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
            // we repeat them for type 2 classes to handle broken types in oryx rdf definitons
            // Ahmed Awad 17.06.09
            // Since inheritance is not working
//            if (isInstanceOf(graphElem, Edge2.RDFS_CLASS)) {
//                edgeTypeElems.add(graphElem);
//                continue;
//            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.DefaultFlow.RDFS_CLASS)) {
        	edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ConditionalFlow.RDFS_CLASS)) {
        	edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Sequenceflow.RDFS_CLASS)) {
        	edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.UnidirectionalAssociation.RDFS_CLASS)) {
        	edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.BidirectionalAssociation.RDFS_CLASS)) {
        	edgeTypeElems.add(graphElem);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ANDGateway.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.ANDGateway rdfANDGateway = (de.hpi.bpmn2.rdf.ANDGateway) graphElem.castTo(de.hpi.bpmn2.rdf.ANDGateway.class);
                GraphObject gateway = handleGateway(rdfANDGateway);
                
                generatedGraphObjects.put(rdfANDGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ExclusiveDatabasedGateway.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.ExclusiveDatabasedGateway rdfExclusiveDatabasedGateway = (de.hpi.bpmn2.rdf.ExclusiveDatabasedGateway) graphElem.castTo(de.hpi.bpmn2.rdf.ExclusiveDatabasedGateway.class);
                GraphObject gateway = handleGateway(rdfExclusiveDatabasedGateway);
                
                generatedGraphObjects.put(rdfExclusiveDatabasedGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ExclusiveEventbasedGateway.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.ExclusiveEventbasedGateway rdfExclusiveEventbasedGateway = (de.hpi.bpmn2.rdf.ExclusiveEventbasedGateway) graphElem.castTo(de.hpi.bpmn2.rdf.ExclusiveEventbasedGateway.class);
                GraphObject gateway = handleGateway(rdfExclusiveEventbasedGateway);
                
                generatedGraphObjects.put(rdfExclusiveEventbasedGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ORGateway.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.ORGateway rdfORGateway = (de.hpi.bpmn2.rdf.ORGateway) graphElem.castTo(de.hpi.bpmn2.rdf.ORGateway.class);
                GraphObject gateway = handleGateway(rdfORGateway);
                
                generatedGraphObjects.put(rdfORGateway.asResource(), gateway);
                result.add(gateway);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Task.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Task rdfTask = (de.hpi.bpmn2.rdf.Task) graphElem.castTo(de.hpi.bpmn2.rdf.Task.class);
                GraphObject node = handleTask(rdfTask);
                
                generatedGraphObjects.put(rdfTask.asResource(), node);
                result.add(node);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Subprocess.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Subprocess rdfSubProcess = 
                    (de.hpi.bpmn2.rdf.Subprocess) graphElem.castTo(de.hpi.bpmn2.rdf.Subprocess.class);
                GraphObject subProcess = handleSubprocess(rdfSubProcess);
                
                generatedGraphObjects.put(rdfSubProcess.asResource(), subProcess);
                result.add(subProcess);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.StartTimerEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.StartTimerEvent rdfStartTimerEvent = (de.hpi.bpmn2.rdf.StartTimerEvent) graphElem.castTo(de.hpi.bpmn2.rdf.StartTimerEvent.class);
                GraphObject startTimerEvent = handleEvent2(rdfStartTimerEvent);

                generatedGraphObjects.put(rdfStartTimerEvent.asResource(), startTimerEvent);
                result.add(startTimerEvent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.StartSignalEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.StartSignalEvent rdfStartSignalEvent = (de.hpi.bpmn2.rdf.StartSignalEvent) graphElem.castTo(de.hpi.bpmn2.rdf.StartSignalEvent.class);
                GraphObject startSignalEvent = handleEvent2(rdfStartSignalEvent);

                generatedGraphObjects.put(rdfStartSignalEvent.asResource(), startSignalEvent);
                result.add(startSignalEvent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.StartMultipleEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.StartMultipleEvent rdfStartMultipleEvent = (de.hpi.bpmn2.rdf.StartMultipleEvent) graphElem.castTo(de.hpi.bpmn2.rdf.StartMultipleEvent.class);
                GraphObject startMultipleEvent = handleEvent2(rdfStartMultipleEvent);

                generatedGraphObjects.put(rdfStartMultipleEvent.asResource(), startMultipleEvent);
                result.add(startMultipleEvent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.StartMessageEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.StartMessageEvent rdfStartMessageEvent = (de.hpi.bpmn2.rdf.StartMessageEvent) graphElem.castTo(de.hpi.bpmn2.rdf.StartMessageEvent.class);
                GraphObject startMessageEvent = handleEvent2(rdfStartMessageEvent);

                generatedGraphObjects.put(rdfStartMessageEvent.asResource(), startMessageEvent);
                result.add(startMessageEvent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Startevent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Startevent rdfStartevent = (de.hpi.bpmn2.rdf.Startevent) graphElem.castTo(de.hpi.bpmn2.rdf.Startevent.class);
                GraphObject startevent = handleEvent2(rdfStartevent);

                generatedGraphObjects.put(rdfStartevent.asResource(), startevent);
                result.add(startevent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.StartConditionalEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.StartConditionalEvent rdfStartConditionalEvent = (de.hpi.bpmn2.rdf.StartConditionalEvent) graphElem.castTo(de.hpi.bpmn2.rdf.StartConditionalEvent.class);
                GraphObject startConditionalEvent = handleEvent2(rdfStartConditionalEvent);

                generatedGraphObjects.put(rdfStartConditionalEvent.asResource(), startConditionalEvent);
                result.add(startConditionalEvent);
                continue;
            }
            
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateTimerEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateTimerEvent rdfIntermediateTimerEvent = (de.hpi.bpmn2.rdf.IntermediateTimerEvent) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateTimerEvent.class);
                GraphObject intermediateTimerEvent = handleEvent2(rdfIntermediateTimerEvent);

                generatedGraphObjects.put(rdfIntermediateTimerEvent.asResource(), intermediateTimerEvent);
                result.add(intermediateTimerEvent);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateSignalEventThrowing.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateSignalEventThrowing rdfIntermediateSignalEventThrowing = (de.hpi.bpmn2.rdf.IntermediateSignalEventThrowing) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateSignalEventThrowing.class);
                GraphObject intermediateSignalEventThrowing = handleEvent2(rdfIntermediateSignalEventThrowing);

                generatedGraphObjects.put(rdfIntermediateSignalEventThrowing.asResource(), intermediateSignalEventThrowing);
                result.add(intermediateSignalEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateSignalEventCatching.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateSignalEventCatching rdfIntermediateSignalEventCatching = (de.hpi.bpmn2.rdf.IntermediateSignalEventCatching) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateSignalEventCatching.class);
                GraphObject intermediateSignalEventCatching = handleEvent2(rdfIntermediateSignalEventCatching);

                generatedGraphObjects.put(rdfIntermediateSignalEventCatching.asResource(), intermediateSignalEventCatching);
                result.add(intermediateSignalEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateMultipleEventThrowing.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateMultipleEventThrowing rdfIntermediateMultipleEventThrowing = (de.hpi.bpmn2.rdf.IntermediateMultipleEventThrowing) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateMultipleEventThrowing.class);
                GraphObject intermediateMultipleEventThrowing = handleEvent2(rdfIntermediateMultipleEventThrowing);

                generatedGraphObjects.put(rdfIntermediateMultipleEventThrowing.asResource(), intermediateMultipleEventThrowing);
                result.add(intermediateMultipleEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateMultipleEventCatching.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateMultipleEventCatching rdfIntermediateMultipleEventCatching = (de.hpi.bpmn2.rdf.IntermediateMultipleEventCatching) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateMultipleEventCatching.class);
                GraphObject intermediateMultipleEventCatching = handleEvent2(rdfIntermediateMultipleEventCatching);

                generatedGraphObjects.put(rdfIntermediateMultipleEventCatching.asResource(), intermediateMultipleEventCatching);
                result.add(intermediateMultipleEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateMessageEventThrowing.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateMessageEventThrowing rdfIntermediateMessageEventThrowing = (de.hpi.bpmn2.rdf.IntermediateMessageEventThrowing) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateMessageEventThrowing.class);
                GraphObject intermediateMessageEventThrowing = handleEvent2(rdfIntermediateMessageEventThrowing);

                generatedGraphObjects.put(rdfIntermediateMessageEventThrowing.asResource(), intermediateMessageEventThrowing);
                result.add(intermediateMessageEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateMessageEventCatching.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateMessageEventCatching rdfIntermediateMessageEventCatching = (de.hpi.bpmn2.rdf.IntermediateMessageEventCatching) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateMessageEventCatching.class);
                GraphObject intermediateMessageEventCatching = handleEvent2(rdfIntermediateMessageEventCatching);

                generatedGraphObjects.put(rdfIntermediateMessageEventCatching.asResource(), intermediateMessageEventCatching);
                result.add(intermediateMessageEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateLinkEventThrowing.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateLinkEventThrowing rdfIntermediateLinkEventThrowing = (de.hpi.bpmn2.rdf.IntermediateLinkEventThrowing) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateLinkEventThrowing.class);
                GraphObject intermediateLinkEventThrowing = handleEvent2(rdfIntermediateLinkEventThrowing);

                generatedGraphObjects.put(rdfIntermediateLinkEventThrowing.asResource(), intermediateLinkEventThrowing);
                result.add(intermediateLinkEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateLinkEventCatching.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateLinkEventCatching rdfIntermediateLinkEventCatching = (de.hpi.bpmn2.rdf.IntermediateLinkEventCatching) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateLinkEventCatching.class);
                GraphObject intermediateLinkEventCatching = handleEvent2(rdfIntermediateLinkEventCatching);

                generatedGraphObjects.put(rdfIntermediateLinkEventCatching.asResource(), intermediateLinkEventCatching);
                result.add(intermediateLinkEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Intermediateevent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Intermediateevent rdfIntermediateevent = (de.hpi.bpmn2.rdf.Intermediateevent) graphElem.castTo(de.hpi.bpmn2.rdf.Intermediateevent.class);
                GraphObject event = handleEvent2(rdfIntermediateevent);
                
                generatedGraphObjects.put(rdfIntermediateevent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateErrorEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateErrorEvent rdfIntermediateErrorEvent = (de.hpi.bpmn2.rdf.IntermediateErrorEvent) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateErrorEvent.class);
                GraphObject event = handleEvent2(rdfIntermediateErrorEvent);
                
                generatedGraphObjects.put(rdfIntermediateErrorEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateConditionalEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateConditionalEvent rdfIntermediateConditionalEvent = (de.hpi.bpmn2.rdf.IntermediateConditionalEvent) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateConditionalEvent.class);
                GraphObject event = handleEvent2(rdfIntermediateConditionalEvent);
                
                generatedGraphObjects.put(rdfIntermediateConditionalEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateCompensationEventThrowing.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateCompensationEventThrowing rdfIntermediateCompensationEventThrowing = (de.hpi.bpmn2.rdf.IntermediateCompensationEventThrowing) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateCompensationEventThrowing.class);
                GraphObject intermediateCompensationEventThrowing = handleEvent2(rdfIntermediateCompensationEventThrowing);

                generatedGraphObjects.put(rdfIntermediateCompensationEventThrowing.asResource(), intermediateCompensationEventThrowing);
                result.add(intermediateCompensationEventThrowing);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateCompensationEventCatching.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateCompensationEventCatching rdfIntermediateCompensationEventCatching = (de.hpi.bpmn2.rdf.IntermediateCompensationEventCatching) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateCompensationEventCatching.class);
                GraphObject intermediateCompensationEventCatching = handleEvent2(rdfIntermediateCompensationEventCatching);

                generatedGraphObjects.put(rdfIntermediateCompensationEventCatching.asResource(), intermediateCompensationEventCatching);
                result.add(intermediateCompensationEventCatching);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.IntermediateCancelEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.IntermediateCancelEvent rdfIntermediateCancelEvent = (de.hpi.bpmn2.rdf.IntermediateCancelEvent) graphElem.castTo(de.hpi.bpmn2.rdf.IntermediateCancelEvent.class);
                GraphObject event = handleEvent2(rdfIntermediateCancelEvent);
                
                generatedGraphObjects.put(rdfIntermediateCancelEvent.asResource(), event);
                result.add(event);
                continue;
            }
//            if (isInstanceOf(graphElem, Event.RDFS_CLASS)) {
//                Event rdfEvent = (Event) graphElem.castTo(Event.class);
//                GraphObject event = handleEvent(rdfEvent);
//                
//                generatedGraphObjects.put(rdfEvent.asResource(), event);
//                result.add(event);
//                continue;
//            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndTerminateEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndTerminateEvent rdfEndTerminateEvent = (de.hpi.bpmn2.rdf.EndTerminateEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndTerminateEvent.class);
                GraphObject event = handleEvent2(rdfEndTerminateEvent);
                
                generatedGraphObjects.put(rdfEndTerminateEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndSignalEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndSignalEvent rdfEndSignalEvent = (de.hpi.bpmn2.rdf.EndSignalEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndSignalEvent.class);
                GraphObject event = handleEvent2(rdfEndSignalEvent);
                
                generatedGraphObjects.put(rdfEndSignalEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndMultipleEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndMultipleEvent rdfEndMultipleEvent = (de.hpi.bpmn2.rdf.EndMultipleEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndMultipleEvent.class);
                GraphObject event = handleEvent2(rdfEndMultipleEvent);
                
                generatedGraphObjects.put(rdfEndMultipleEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndMessageEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndMessageEvent rdfEndMessageEvent = (de.hpi.bpmn2.rdf.EndMessageEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndMessageEvent.class);
                GraphObject event = handleEvent2(rdfEndMessageEvent);
                
                generatedGraphObjects.put(rdfEndMessageEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Endevent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Endevent rdfEndevent = (de.hpi.bpmn2.rdf.Endevent) graphElem.castTo(de.hpi.bpmn2.rdf.Endevent.class);
                GraphObject event = handleEvent2(rdfEndevent);
                
                generatedGraphObjects.put(rdfEndevent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndErrorEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndErrorEvent rdfEndErrorEvent = (de.hpi.bpmn2.rdf.EndErrorEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndErrorEvent.class);
                GraphObject event = handleEvent2(rdfEndErrorEvent);
                
                generatedGraphObjects.put(rdfEndErrorEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndCompensationEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndCompensationEvent rdfEndCompensationEvent = (de.hpi.bpmn2.rdf.EndCompensationEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndCompensationEvent.class);
                GraphObject event = handleEvent2(rdfEndCompensationEvent);
                
                generatedGraphObjects.put(rdfEndCompensationEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.EndCancelEvent.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.EndCancelEvent rdfEndCancelEvent = (de.hpi.bpmn2.rdf.EndCancelEvent) graphElem.castTo(de.hpi.bpmn2.rdf.EndCancelEvent.class);
                GraphObject event = handleEvent2(rdfEndCancelEvent);
                
                generatedGraphObjects.put(rdfEndCancelEvent.asResource(), event);
                result.add(event);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.DataObject.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.DataObject rdfDataObject = (de.hpi.bpmn2.rdf.DataObject) graphElem.castTo(de.hpi.bpmn2.rdf.DataObject.class);
                com.bpmnq.DataObject dob = handleDataObject(rdfDataObject);
                
                generatedGraphObjects.put(rdfDataObject.asResource(), dob.originalNode());
                result.add(dob);
                continue;
            }

            
        }
        
        
        // now, process all edges
        for (Resource graphElem : edgeTypeElems) {
            // sequence flow stuff
            if (isInstanceOf(graphElem, DefaultFlow.RDFS_CLASS)) {
                DefaultFlow rdfSeqFlow = (DefaultFlow) graphElem.castTo(DefaultFlow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, ConditionalFlow.RDFS_CLASS)) {
                ConditionalFlow rdfSeqFlow = (ConditionalFlow) graphElem.castTo(ConditionalFlow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, Sequenceflow.RDFS_CLASS)) {
                Sequenceflow rdfSeqFlow = (Sequenceflow) graphElem.castTo(Sequenceflow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, UnidirectionalAssociation.RDFS_CLASS)) {
        	UnidirectionalAssociation rdfUnidirectionalAssociation = (UnidirectionalAssociation) graphElem.castTo(UnidirectionalAssociation.class);
                Association ass = handleAssociation(rdfUnidirectionalAssociation);
                
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
            // repeated to handle broken types in oryx rdf
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.DefaultFlow.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.DefaultFlow rdfSeqFlow = (de.hpi.bpmn2.rdf.DefaultFlow) graphElem.castTo(de.hpi.bpmn2.rdf.DefaultFlow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.ConditionalFlow.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.ConditionalFlow rdfSeqFlow = (de.hpi.bpmn2.rdf.ConditionalFlow) graphElem.castTo(de.hpi.bpmn2.rdf.ConditionalFlow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.Sequenceflow.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.Sequenceflow rdfSeqFlow = (de.hpi.bpmn2.rdf.Sequenceflow) graphElem.castTo(de.hpi.bpmn2.rdf.Sequenceflow.class);
                SequenceFlow seqFlow = handleSequenceflow(rdfSeqFlow);
                
                result.add(seqFlow);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.UnidirectionalAssociation.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.UnidirectionalAssociation rdfUnidirectionalAssociation = (de.hpi.bpmn2.rdf.UnidirectionalAssociation) graphElem.castTo(de.hpi.bpmn2.rdf.UnidirectionalAssociation.class);
                Association ass = handleAssociation(rdfUnidirectionalAssociation);
                
                result.add(ass);
                continue;
            }
            if (isInstanceOf(graphElem, de.hpi.bpmn2.rdf.BidirectionalAssociation.RDFS_CLASS)) {
        	de.hpi.bpmn2.rdf.BidirectionalAssociation rdfBidirectionalAssociation = (de.hpi.bpmn2.rdf.BidirectionalAssociation) graphElem.castTo(de.hpi.bpmn2.rdf.BidirectionalAssociation.class);
                List<Association> ass = handleBidirectionalAssociation(rdfBidirectionalAssociation);
                for(Association a : ass)
                    result.add(a);
                continue;
            }
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
//    private org.ontoware.rdfreactor.schema.rdfs.Class reinstantiate(Resource classDef)
//    {
//	de.hpi.bpmn.rdf.Thing th;
//	if (classDef.equals("XOR_Gateway"))
//	{
//	    th = new de.hpi.bpmn.rdf.XORGateway(rdfModel,);
//	    
//	}
//	    return 
//	else if (classDef.equals("Association_Unidirectional"))
//	    return new UnidirectionalAssociation;
//    }
    public static boolean isInstanceOf(Resource res, URI superClassUri) {
        for (Class resourceType : res.getAllType_as().asArray()) {
//            if (!resourceType.toString().startsWith("http://b3mn.org/stencilset/bpmn1.1#"))
        	// A stupid trick to overcome the limitaiton of oryx import...
        	// a resource type as 
//        	resourceType= new Class("http://b3mn.org/stencilset/bpmn1.1#"+resourceType.toString());
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
    protected GraphObject handleEvent(de.hpi.bpmn.rdf.Event rdfEvent) {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.EVENT;
        result.setID(rdfEvent.asURI().toString());

        // long list
        
        if (isInstanceOf(rdfEvent, StartTimerEvent.RDFS_CLASS))
            result.type2 = "Timer1";
        else if (isInstanceOf(rdfEvent, StartSignalEvent.RDFS_CLASS))
            result.type2 = "Signal1";
        else if (isInstanceOf(rdfEvent, StartMultipleEvent.RDFS_CLASS))
            result.type2 = "Multiple1";
        else if (isInstanceOf(rdfEvent, StartMessageEvent.RDFS_CLASS))
            result.type2 = "Message1";
        else if (isInstanceOf(rdfEvent, StartConditionalEvent.RDFS_CLASS))
            result.type2 = "Conditional1";
        else if (isInstanceOf(rdfEvent, Startevent.RDFS_CLASS))
            result.type2 = "1";
        else if (isInstanceOf(rdfEvent, IntermediateTimerEvent.RDFS_CLASS))
            result.type2 = "Timer2";
        else if (isInstanceOf(rdfEvent, IntermediateSignalEventThrowing.RDFS_CLASS))
            result.type2 = "SignalThrowing2";
        else if (isInstanceOf(rdfEvent, IntermediateSignalEventCatching.RDFS_CLASS))
            result.type2 = "SignalCatching2";
        else if (isInstanceOf(rdfEvent, IntermediateMultipleEventThrowing.RDFS_CLASS))
            result.type2 = "MultipleThrowing2";
        else if (isInstanceOf(rdfEvent, IntermediateMultipleEventCatching.RDFS_CLASS))
            result.type2 = "MultipleCatching2";
        else if (isInstanceOf(rdfEvent, IntermediateMessageEventThrowing.RDFS_CLASS))
            result.type2 = "MessageThrowing2";
        else if (isInstanceOf(rdfEvent, IntermediateMessageEventCatching.RDFS_CLASS))
            result.type2 = "MessageCatching2";
        else if (isInstanceOf(rdfEvent, IntermediateLinkEventThrowing.RDFS_CLASS))
            result.type2 = "LinkThrowing2";
        else if (isInstanceOf(rdfEvent, IntermediateLinkEventCatching.RDFS_CLASS))
            result.type2 = "LinkCatching2";
        else if (isInstanceOf(rdfEvent, IntermediateConditionalEvent.RDFS_CLASS))
            result.type2 = "Conditional2";
        else if (isInstanceOf(rdfEvent, IntermediateErrorEvent.RDFS_CLASS))
            result.type2 = "Error2";
        else if (isInstanceOf(rdfEvent, IntermediateCompensationEventThrowing.RDFS_CLASS))
            result.type2 = "CompensationThrowing2";
        else if (isInstanceOf(rdfEvent, IntermediateCompensationEventCatching.RDFS_CLASS))
            result.type2 = "CompensationCatching2";
        else if (isInstanceOf(rdfEvent, IntermediateCancelEvent.RDFS_CLASS))
            result.type2 = "Cancel2";
        else if (isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS))
            result.type2 = "2";
        else if (isInstanceOf(rdfEvent, EndTerminateEvent.RDFS_CLASS))
            result.type2 = "Terminate3";
        else if (isInstanceOf(rdfEvent, EndSignalEvent.RDFS_CLASS))
            result.type2 = "Signal3";
        else if (isInstanceOf(rdfEvent, EndMultipleEvent.RDFS_CLASS))
            result.type2 = "Multiple3";
        else if (isInstanceOf(rdfEvent, EndMessageEvent.RDFS_CLASS))
            result.type2 = "Message3";
        else if (isInstanceOf(rdfEvent, EndErrorEvent.RDFS_CLASS))
            result.type2 = "Error3";
        else if (isInstanceOf(rdfEvent, EndCompensationEvent.RDFS_CLASS))
            result.type2 = "Compensation3";
        else if (isInstanceOf(rdfEvent, EndCancelEvent.RDFS_CLASS))
            result.type2 = "Cancel3";
        else if (isInstanceOf(rdfEvent, Endevent.RDFS_CLASS))
            result.type2 = "3";
//        if (isInstanceOf(rdfEvent, Startevent.RDFS_CLASS)) {
//            result.type2 = EventType.START.asType2String();
//        }
//        if (isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS)) {
//            result.type2 = EventType.INTERMEDIATE.asType2String();
//        }
//        if (isInstanceOf(rdfEvent, Endevent.RDFS_CLASS)) {
//            result.type2 = EventType.END.asType2String();
//        }

        return result;
    }

    protected GraphObject handleEvent2(de.hpi.bpmn2.rdf.Event rdfEvent) {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.EVENT;
        result.setID(rdfEvent.asURI().toString());

        // long list
        
        if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.StartTimerEvent.RDFS_CLASS))
            result.type2 = "Timer1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.StartSignalEvent.RDFS_CLASS))
            result.type2 = "Signal1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.StartMultipleEvent.RDFS_CLASS))
            result.type2 = "Multiple1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.StartMessageEvent.RDFS_CLASS))
            result.type2 = "Message1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.StartConditionalEvent.RDFS_CLASS))
            result.type2 = "Conditional1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.Startevent.RDFS_CLASS))
            result.type2 = "1";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateTimerEvent.RDFS_CLASS))
            result.type2 = "Timer2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateSignalEventThrowing.RDFS_CLASS))
            result.type2 = "SignalThrowing2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateSignalEventCatching.RDFS_CLASS))
            result.type2 = "SignalCatching2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateMultipleEventThrowing.RDFS_CLASS))
            result.type2 = "MultipleThrowing2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateMultipleEventCatching.RDFS_CLASS))
            result.type2 = "MultipleCatching2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateMessageEventThrowing.RDFS_CLASS))
            result.type2 = "MessageThrowing2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateMessageEventCatching.RDFS_CLASS))
            result.type2 = "MessageCatching2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateLinkEventThrowing.RDFS_CLASS))
            result.type2 = "LinkThrowing2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateLinkEventCatching.RDFS_CLASS))
            result.type2 = "LinkCatching2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateConditionalEvent.RDFS_CLASS))
            result.type2 = "Conditional2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateErrorEvent.RDFS_CLASS))
            result.type2 = "Error2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateCompensationEventThrowing.RDFS_CLASS))
            result.type2 = "CompensationThrowing2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateCompensationEventCatching.RDFS_CLASS))
            result.type2 = "CompensationCatching2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.IntermediateCancelEvent.RDFS_CLASS))
            result.type2 = "Cancel2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.Intermediateevent.RDFS_CLASS))
            result.type2 = "2";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndTerminateEvent.RDFS_CLASS))
            result.type2 = "Terminate3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndSignalEvent.RDFS_CLASS))
            result.type2 = "Signal3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndMultipleEvent.RDFS_CLASS))
            result.type2 = "Multiple3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndMessageEvent.RDFS_CLASS))
            result.type2 = "Message3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndErrorEvent.RDFS_CLASS))
            result.type2 = "Error3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndCompensationEvent.RDFS_CLASS))
            result.type2 = "Compensation3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.EndCancelEvent.RDFS_CLASS))
            result.type2 = "Cancel3";
        else if (isInstanceOf(rdfEvent, de.hpi.bpmn2.rdf.Endevent.RDFS_CLASS))
            result.type2 = "3";
//        if (isInstanceOf(rdfEvent, Startevent.RDFS_CLASS)) {
//            result.type2 = EventType.START.asType2String();
//        }
//        if (isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS)) {
//            result.type2 = EventType.INTERMEDIATE.asType2String();
//        }
//        if (isInstanceOf(rdfEvent, Endevent.RDFS_CLASS)) {
//            result.type2 = EventType.END.asType2String();
//        }

        return result;
    }
    /**
     * Takes a Gateway RDF node and transforms it to a bpmn GraphObject with type GATEWAY 
     * @param rdfGateway
     * @return
     * @throws FileFormatException thrown if a gateway cannot be clearly identified as
     *      a split or join node
     */
    
    protected GraphObject handleGateway(de.hpi.bpmn.rdf.Gateway rdfGateway) throws FileFormatException {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.GATEWAY;
        result.setID(rdfGateway.asURI().toString());
        
        
        // is it a split or a join node?
        /** true means split, false means join */
        boolean isSplitStructured = true;
        //boolean splitJoinStructureIsKnown = false;
        long numOutgoings = rdfGateway.getAllBpmnOutgoing_as().count();
        List<org.ontoware.rdf2go.model.node.Resource> incomings = new ArrayList<org.ontoware.rdf2go.model.node.Resource>();
        ClosableIterator<Statement> iter = 
            rdfModel.findStatements(Variable.ANY, de.hpi.bpmn.rdf.Gateway.OUTGOING, rdfGateway.asResource());
        while (iter.hasNext()) {
            Statement tripel = iter.next();
            incomings.add(tripel.getSubject());
        }
        iter.close();
        final int numIncomings = incomings.size();
        if (numIncomings <= 1 && numOutgoings > 1) {
            isSplitStructured = true; // the graph structure suggests it's a split node
          //  splitJoinStructureIsKnown = true;
        } else if (numIncomings > 1 && numOutgoings <= 1) {
            isSplitStructured = false; // the graph structure suggests it's a join node
          //  splitJoinStructureIsKnown = true;
        }

        if (rdfGateway instanceof de.hpi.bpmn.rdf.ANDGateway) {
            //ANDGateway rdfAndGateway = (ANDGateway) rdfGateway.castTo(ANDGateway.class);
            //String rdfSplitOrJoin = rdfAndGateway.getallbpmn getAllBpmnSplitjoin_as().firstValue().trim(); 
            //if (rdfSplitOrJoin.equals("AND Join")) {
            //    if (splitJoinStructureIsKnown && isSplitStructured)
            //        throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
            //                + " looks like a split, but says it was a join.");
                if (isSplitStructured)
                    result.type2 = GateWayType.AND_SPLIT.asType2String();
                else
                    result.type2 = GateWayType.AND_JOIN.asType2String();
                
//            } else if (rdfSplitOrJoin.equals("AND Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.AND_SPLIT.asType2String();
//            } else throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            
        }
        if (rdfGateway instanceof de.hpi.bpmn.rdf.ORGateway) {
//            ORGateway rdfOrGateway = (ORGateway) rdfGateway.castTo(ORGateway.class);
//            String rdfSplitOrJoin = rdfOrGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
//            if (rdfSplitOrJoin.equals("OR Join")) {
//                if (splitJoinStructureIsKnown && isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a split, but says it was a join.");
//                
//                result.type2 = GateWayType.OR_JOIN.asType2String();
//            } else if (rdfSplitOrJoin.equals("OR Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.OR_SPLIT.asType2String();
//            } else throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            if (isSplitStructured)
                result.type2 = GateWayType.OR_SPLIT.asType2String();
            else
                result.type2 = GateWayType.OR_JOIN.asType2String();
            
        }
        if (rdfGateway instanceof de.hpi.bpmn.rdf.XORGateway) {
//            XORGateway rdfXorGateway = (XORGateway) rdfGateway.castTo(XORGateway.class);
//            String rdfSplitOrJoin = rdfXorGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
//            if (rdfSplitOrJoin.equals("XOR Join")) {
//                if (splitJoinStructureIsKnown && isSplitStructured)
//                    throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a split, but says it was a join.");
//                
//                result.type2 = GateWayType.XOR_JOIN.asType2String();
//            } else if (rdfSplitOrJoin.equals("XOR Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.XOR_SPLIT.asType2String();
//            } else throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            if (isSplitStructured)
                result.type2 = GateWayType.XOR_SPLIT.asType2String();
            else
                result.type2 = GateWayType.XOR_JOIN.asType2String();
        }
        // I dont need to distinguish the exclusive data based and event based
        
        return result;
    }

    protected GraphObject handleGateway(de.hpi.bpmn2.rdf.Gateway rdfGateway) throws FileFormatException {
        GraphObject result = new GraphObject();
        result.type = GraphObjectType.GATEWAY;
        result.setID(rdfGateway.asURI().toString());
        
        
        // is it a split or a join node?
        /** true means split, false means join */
        boolean isSplitStructured = true;
        //boolean splitJoinStructureIsKnown = false;
        long numOutgoings = rdfGateway.getAllBpmnOutgoing_as().count();
        List<org.ontoware.rdf2go.model.node.Resource> incomings = new ArrayList<org.ontoware.rdf2go.model.node.Resource>();
        ClosableIterator<Statement> iter = 
            rdfModel.findStatements(Variable.ANY, de.hpi.bpmn2.rdf.Gateway.OUTGOING, rdfGateway.asResource());
        while (iter.hasNext()) {
            Statement tripel = iter.next();
            incomings.add(tripel.getSubject());
        }
        iter.close();
        final int numIncomings = incomings.size();
        if (numIncomings <= 1 && numOutgoings > 1) {
            isSplitStructured = true; // the graph structure suggests it's a split node
          //  splitJoinStructureIsKnown = true;
        } else if (numIncomings > 1 && numOutgoings <= 1) {
            isSplitStructured = false; // the graph structure suggests it's a join node
          //  splitJoinStructureIsKnown = true;
        }

        if (rdfGateway instanceof de.hpi.bpmn2.rdf.ANDGateway) {
            //ANDGateway rdfAndGateway = (ANDGateway) rdfGateway.castTo(ANDGateway.class);
            //String rdfSplitOrJoin = rdfAndGateway.getallbpmn getAllBpmnSplitjoin_as().firstValue().trim(); 
            //if (rdfSplitOrJoin.equals("AND Join")) {
            //    if (splitJoinStructureIsKnown && isSplitStructured)
            //        throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
            //                + " looks like a split, but says it was a join.");
                if (isSplitStructured)
                    result.type2 = GateWayType.AND_SPLIT.asType2String();
                else
                    result.type2 = GateWayType.AND_JOIN.asType2String();
                
//            } else if (rdfSplitOrJoin.equals("AND Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.AND_SPLIT.asType2String();
//            } else throw new FileFormatException("And Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            
        }
        if (rdfGateway instanceof de.hpi.bpmn2.rdf.ORGateway) {
//            ORGateway rdfOrGateway = (ORGateway) rdfGateway.castTo(ORGateway.class);
//            String rdfSplitOrJoin = rdfOrGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
//            if (rdfSplitOrJoin.equals("OR Join")) {
//                if (splitJoinStructureIsKnown && isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a split, but says it was a join.");
//                
//                result.type2 = GateWayType.OR_JOIN.asType2String();
//            } else if (rdfSplitOrJoin.equals("OR Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.OR_SPLIT.asType2String();
//            } else throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            if (isSplitStructured)
                result.type2 = GateWayType.OR_SPLIT.asType2String();
            else
                result.type2 = GateWayType.OR_JOIN.asType2String();
            
        }
        if (rdfGateway instanceof de.hpi.bpmn2.rdf.XORGateway) {
//            XORGateway rdfXorGateway = (XORGateway) rdfGateway.castTo(XORGateway.class);
//            String rdfSplitOrJoin = rdfXorGateway.getAllBpmnqSplitjoin_as().firstValue().trim(); 
//            if (rdfSplitOrJoin.equals("XOR Join")) {
//                if (splitJoinStructureIsKnown && isSplitStructured)
//                    throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a split, but says it was a join.");
//                
//                result.type2 = GateWayType.XOR_JOIN.asType2String();
//            } else if (rdfSplitOrJoin.equals("XOR Split")) {
//                if (splitJoinStructureIsKnown && !isSplitStructured)
//                    throw new FileFormatException("Or Gateway resource " + rdfGateway.getResource().toString() 
//                            + " looks like a join, but says it was a spli.");
//
//                result.type2 = GateWayType.XOR_SPLIT.asType2String();
//            } else throw new FileFormatException("XOr Gateway resource " + rdfGateway.getResource().toString() 
//                    + "doesn't specifiy whether it's a join or a split.");
            if (isSplitStructured)
                result.type2 = GateWayType.XOR_SPLIT.asType2String();
            else
                result.type2 = GateWayType.XOR_JOIN.asType2String();
        }
        // I dont need to distinguish the exclusive data based and event based
        
        return result;
    }
    /**
     * 
     * @param rdfSeqFlow
     * @return
     */
    protected SequenceFlow handleSequenceflow(Sequenceflow rdfSeqFlow) {
        Resource[] startsAtArr = findAllBpmnIncoming(rdfSeqFlow);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
	Resource endsAt = rdfSeqFlow.getAllBpmnOutgoing_as().firstValue(); // might return null
        String cond = rdfSeqFlow.getAllBpmnConditionexpression_as().firstValue();
        SequenceFlow sq =createSequenceFlow(startsAt, endsAt);
        sq.arcCondition = cond;
        return sq;
    }
//    protected SequenceFlow handleSequenceflow(de.hpi.bpmn2.rdf.Sequenceflow rdfSeqFlow) {
//        Resource[] startsAtArr = findAllBpmnIncoming2(rdfSeqFlow);
//        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
//	Resource endsAt = rdfSeqFlow.getAllBpmnOutgoing_as().firstValue(); // might return null
//        // Added by Ahmed Awad on 16.11.2009
//        String cond = rdfSeqFlow.getAllBpmnConditionexpression_as().firstValue();
//        
//        SequenceFlow sq =createSequenceFlow(startsAt, endsAt);
//        sq.arcCondition = cond;
//        return sq;
//        
//    }
    protected Association handleAssociation(de.hpi.bpmn.rdf.Association rdfAss) {
        Resource[] startsAtArr = findAllBpmnIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
	Resource endsAt = rdfAss.getAllBpmnOutgoing_as().firstValue(); // might return null

        return createAssociation(startsAt, endsAt);
    }
//    protected Association handleAssociation(de.hpi.bpmn2.rdf.Association rdfAss) {
//        Resource[] startsAtArr = findAllBpmnIncoming2(rdfAss);
//        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
//        Resource endsAt = rdfAss.getAllBpmnOutgoing_as().firstValue(); // might return null
//
//        return createAssociation(startsAt, endsAt);
//    }
    protected List<Association> handleBidirectionalAssociation(de.hpi.bpmn.rdf.BidirectionalAssociation rdfAss) {
        Resource[] startsAtArr = findAllBpmnIncoming(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnOutgoing_as().firstValue(); // might return null
        List<Association> result = new ArrayList<Association>();
        result.add(createAssociation(startsAt, endsAt));
        result.add(createAssociation(endsAt, startsAt));
        return result;
    }
    protected List<Association> handleBidirectionalAssociation(de.hpi.bpmn2.rdf.BidirectionalAssociation rdfAss) {
        Resource[] startsAtArr = findAllBpmnIncoming2(rdfAss);
        Resource startsAt = (startsAtArr.length > 0 ? startsAtArr[0] : null);
        Resource endsAt = rdfAss.getAllBpmnOutgoing_as().firstValue(); // might return null
        List<Association> result = new ArrayList<Association>();
        result.add(createAssociation(startsAt, endsAt));
        result.add(createAssociation(endsAt, startsAt));
        return result;
    }

   private SequenceFlow createSequenceFlow(Resource startsAt, Resource endsAt)
            throws ClassCastException {
        SequenceFlow result;
        if (startsAt == null || endsAt == null) {
            result = new SequenceFlow();
        } else {
            if (generatedGraphObjects.keySet().contains(startsAt.asResource()) && generatedGraphObjects.keySet().contains(endsAt.asResource()) )
        	result = new SequenceFlow(generatedGraphObjects.get(startsAt.asResource()),
                    generatedGraphObjects.get(endsAt.asResource()));
            else
        	result = new SequenceFlow();
        }
        
        return result;
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
        
        if (rdfTask.hasBpmnName()) {
            String rdfName = rdfTask.getAllBpmnName_as().firstValue(); 
            node.setName(normalizeRdfLiteral(rdfName));
        }
        // TODO set the node ID in a meaningful way
        node.setID(rdfTask.asURI().toString());
        return node;
    }
    protected GraphObject handleTask(de.hpi.bpmn2.rdf.Task rdfTask) {
        GraphObject node = initializeActivity();
        
        if (rdfTask.hasBpmnName()) {
            String rdfName = rdfTask.getAllBpmnName_as().firstValue(); 
            node.setName(normalizeRdfLiteral(rdfName));
        }
        // TODO set the node ID in a meaningful way
        node.setID(rdfTask.asURI().toString());
        return node;
    }
    protected GraphObject handleSubprocess(Subprocess rdfSubprocess) {
        GraphObject node = initializeActivity();
        
        if (rdfSubprocess.hasBpmnName()) {
            String rdfName = rdfSubprocess.getAllBpmnName_as().firstValue(); 
            node.setName(normalizeRdfLiteral(rdfName));
        }
        // TODO set the node ID in a meaningful way
        node.setID(rdfSubprocess.asURI().toString());
        return node;
    }
    protected GraphObject handleSubprocess(de.hpi.bpmn2.rdf.Subprocess rdfSubprocess) {
        GraphObject node = initializeActivity();
        
        if (rdfSubprocess.hasBpmnName()) {
            String rdfName = rdfSubprocess.getAllBpmnName_as().firstValue(); 
            node.setName(normalizeRdfLiteral(rdfName));
        }
        // TODO set the node ID in a meaningful way
        node.setID(rdfSubprocess.asURI().toString());
        return node;
    }
    /**
     * 
     * @param rdfVarAct
     * @return
     */
    protected com.bpmnq.DataObject handleDataObject(DataObject rdfDataObject){
	com.bpmnq.DataObject node= new com.bpmnq.DataObject();
	node.doID = rdfDataObject.asURI().toString();
	node.name = rdfDataObject.getAllBpmnName_as().firstValue();
	node.setState(rdfDataObject.getAllBpmnState_as().firstValue().replace('[', ' ').replace(']',' ').trim());
	return node;
    }
    protected com.bpmnq.DataObject handleDataObject(de.hpi.bpmn2.rdf.DataObject rdfDataObject){
	com.bpmnq.DataObject node= new com.bpmnq.DataObject();
	node.doID = rdfDataObject.asURI().toString();
	node.name = rdfDataObject.getAllBpmnName_as().firstValue();
	node.setState(rdfDataObject.getAllBpmnState_as().firstValue().replace('[', ' ').replace(']',' ').trim());
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
    
    protected Resource[] findAllBpmnIncoming(Node node) {
        return findAllBpmnIncoming(node.asResource());
    }
    
    protected Resource[] findAllBpmnIncoming2(de.hpi.bpmn2.rdf.Node node) {
        return findAllBpmnIncoming2(node.asResource());
    }
    protected Resource[] findAllBpmnIncoming(Edge edge) {
        return findAllBpmnIncoming(edge.asResource());
    }
    protected Resource[] findAllBpmnIncoming2(de.hpi.bpmn2.rdf.Edge edge) {
        return findAllBpmnIncoming2(edge.asResource());
    }
    private Resource[] findAllBpmnIncoming(org.ontoware.rdf2go.model.node.Resource rdfResource) {
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
    
    private Resource[] findAllBpmnIncoming2(org.ontoware.rdf2go.model.node.Resource rdfResource) {
        ClosableIterator<Statement> iter = this.rdfModel.findStatements(Variable.ANY, 
                de.hpi.bpmn2.rdf.Edge.OUTGOING, rdfResource);
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
        InputStream schemaStream = this.getClass().getResourceAsStream("/bpmn-schema.rdf");
        if (schemaStream == null)
            throw new IOException("Cannot read in schema file!");
        
        schemaStream = new BufferedInputStream(schemaStream);
//        BufferedReader schem = new BufferedReader(new InputStreamReader(schemaStream));
        
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
    public void setRdfInput(Reader rdfStream, RdfSyntax syntax,
            String baseURI) throws IOException {
        // empty the model first
        reset();
        // load the RDFS bpmnq into the model, so that subclass tripels are present 
        
        InputStream schemaStream = this.getClass().getResourceAsStream("/bpmn-schema.rdf");
        if (schemaStream == null)
            throw new IOException("Cannot read in schema file!");
        
        schemaStream = new BufferedInputStream(schemaStream);
        
        
        this.rdfModel.readFrom(schemaStream);
        

        this.rdfModel.readFrom(rdfStream, Syntax.RdfXml, baseURI);

        this.generatedGraphObjects = 
            new HashMap<org.ontoware.rdf2go.model.node.Resource, GraphObject>();
        
        // fix Oryx's broken resource type information
        addCorrectedRdfTypes();

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
