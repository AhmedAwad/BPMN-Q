package com.bpmnq;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.LogManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdfreactor.schema.rdfs.Resource;

import com.bpmnq.GraphObject.ActivityType;
import com.bpmnq.GraphObject.EventType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.QueryGraphBuilderRDF.RdfSyntax;
import com.bpmnq.rdf.ANDGateway;
import com.bpmnq.rdf.Activity;
import com.bpmnq.rdf.Edge;
import com.bpmnq.rdf.Endevent;
import com.bpmnq.rdf.Event;
import com.bpmnq.rdf.ExclusiveDatabasedGateway;
import com.bpmnq.rdf.ExclusiveEventbasedGateway;
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

public class QueryGraphBuilderRdfTest {

    File rdfTestFile;
    QueryGraphBuilderRDF testable;
    Logger logger;
    
    @Before
    public void setUp() throws Exception {
        // turn off too verbose log messages
        LogManager.getRootLogger().setLevel(Level.WARN);
        
        InputStream rdfXmlInput = getClass().getResourceAsStream("/bpmn-q-diagram_with_events_connected.rdf");
        String baseUri = "http://myhpi.de/~sryll/bpmnq.xhtml"; //erdfFile.toURI().toString();

        testable = new QueryGraphBuilderRDF();
        testable.setRdfInput(rdfXmlInput, RdfSyntax.RDF_XML, baseUri);
        
    }

    @Test
    public void testQueryGraphBuilderRdf() {
        testable = new QueryGraphBuilderRDF();
        assertNotNull("RDF model is not initialized", testable.rdfModel);
        assertTrue("RDF model is not open", testable.rdfModel.isOpen());
        assertTrue("newly created model is not empty", testable.rdfModel.isEmpty());
    }

    @Test
    public void testQueryGraphBuilderRdfString() {
        // rely on setUp() initialization
        assertNotNull("RDF model is not initialized", testable.rdfModel);
        assertTrue("RDF model is not open", testable.rdfModel.isOpen());
        assertFalse("Model is empty after import", testable.rdfModel.isEmpty());
        
//        assertEquals("Model has unexpected number of tripels", 
//                125, testable.rdfModel.size());
    }

    @Test
    public void testSetRdfFilename() throws Exception {
        URL rdfUrl = getClass().getResource("/bpmn-q-diagram_with_events_connected.rdf");
        
        testable.setRdfInput(rdfUrl.toURI()/*secondrdfTestFile.getAbsolutePath()*/, RdfSyntax.RDF_XML);
        assertFalse("Model is empty after import", testable.rdfModel.isEmpty());
//        assertEquals("Model has unexpected number of tripels", 
//                386, testable.rdfModel.size());
    }
    
    @Test
    public void testSetERdfInputStream() throws Exception {
        InputStream erdfStream = getClass().getResourceAsStream("/bpmn-q-diagram_with_events_connected.xhtml");
        if (erdfStream == null)
            throw new Exception("Test rRDF resource file not found!");
        testable.setRdfInput(erdfStream, RdfSyntax.eRDF, "http://localhost/");
        erdfStream.close();
        
        assertFalse("Model is empty after import", testable.rdfModel.isEmpty());
//        assertEquals("Model has unexpected number of tripels", 
//                386, testable.rdfModel.size());

    }
    
    @Test(expected=FileNotFoundException.class)
    public void testSetNonexistentRdfFilename() throws Exception {
        File nonexistent = File.createTempFile("delete-me", ".rdf");
        if (! nonexistent.delete())
            throw new IOException("Could not delete my own test temp file");
        
        testable.setRdfInput(nonexistent.toURI(), RdfSyntax.RDF_XML);
    }

    @Test
    public void testReset() {
        assertFalse(testable.rdfModel.isEmpty());
        
        testable.reset();
        assertTrue("reset does not make model empty", testable.rdfModel.isEmpty());
        assertNull("reset doesn not empty list of graph nodes", testable.generatedGraphObjects);
    }
    
    @Test
    public void testBuildGraph() throws Exception {
        QueryGraph out = testable.buildGraph();
        assertNotNull(out);
        assertTrue("buildGraph() returns the wrong specific type", 
                (out instanceof QueryGraph));
        
        assertEquals("query graph has wrong number of edges", 7, out.edges.size());
        assertEquals("query graph has wrong number of neg. edges", 2, out.negativeEdges.size());
        assertEquals("query graph has wrong number of neg. paths", 1, out.negativePaths.size());
        assertEquals("query graph has wrong number of paths", 2, out.paths.size());
        assertEquals("query graph has wrong number of nodes", 12, out.nodes.size());
        assertEquals("forbidden activity IDs", 1, out.forbiddenActivityIDs.length());
        assertEquals("forbidden activity IDs", 1, out.forbiddenEventIDs.length());
        assertEquals("forbidden activity IDs", 1, out.forbiddenGatewayIDs.length());
        
        for (com.bpmnq.Path path : out.paths) {
            assertNotNull(path);
            assertNotNull(path.exclude);
        }
        for (GraphObject node : out.nodes) {
            assertNotNull(node.type2);
            assertNotNull(node.getName());
            assertNotNull(node.getTemporalExpressionName());
        }
    }
    
    @Test
    public void testAddCorrectedRdfType() throws Exception {
        testable.addCorrectedRdfTypes();

        TriplePattern pattern = 
            testable.rdfModel.createTriplePattern(Variable.ANY, Thing.TYPE, Variable.ANY);
        ClosableIterator<Statement> iter = testable.rdfModel.findStatements(pattern);
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            org.ontoware.rdf2go.model.node.Resource subjRes = stmt.getSubject().asResource();
            Thing subjThing = Thing.getInstance(testable.rdfModel, subjRes);
            
            assertTrue("oryx-typed resource did not get rdf:type statement",
                    subjThing.getAllType_as().count() > 0);
            
       }
        iter.close();
    }
    
    @Test
    public void testIsSubclassOf() throws Exception {
        Task task = new Task(testable.rdfModel, true);
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(task, Task.RDFS_CLASS));
        
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(task, Activity.RDFS_CLASS));
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(task, com.bpmnq.rdf.Node.RDFS_CLASS));
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(task, Resource.RDFS_CLASS));
        
        assertFalse(QueryGraphBuilderRDF.isSubClassOf(task, Variableactivity.RDFS_CLASS));
        assertFalse(QueryGraphBuilderRDF.isSubClassOf(task, Edge.RDFS_CLASS));
        
        Gateway gw = new Gateway(testable.rdfModel, true);
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(gw, com.bpmnq.rdf.Node.RDFS_CLASS));
        assertTrue(QueryGraphBuilderRDF.isSubClassOf(gw, Gateway.RDFS_CLASS));
        assertFalse(QueryGraphBuilderRDF.isSubClassOf(gw, Event.RDFS_CLASS));
        assertFalse(QueryGraphBuilderRDF.isSubClassOf(gw, XORGateway.RDFS_CLASS));
        assertFalse(QueryGraphBuilderRDF.isSubClassOf(gw, ExclusiveEventbasedGateway.RDFS_CLASS));
    }
    
    @Test
    public void testHandleGateway() throws Exception {
        List<Gateway> allRdfGateways = new ArrayList<Gateway>();
        allRdfGateways.addAll(
                ANDGateway.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                ORGateway.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                Genericjoin.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                Genericsplit.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                ExclusiveDatabasedGateway.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                ExclusiveEventbasedGateway.getAllInstances_as(testable.rdfModel).asList());
        
        for (Gateway rdfGateway : allRdfGateways) {
            GraphObject output = testable.handleGateway(rdfGateway);
            assertNotNull("handleGateway returned null", output);
            assertEquals("handleGateway produced no gateway node", 
                    GraphObjectType.GATEWAY, output.type);

            assertTrue(rdfGateway.hasBpmnqGatewaytype());
            assertTrue("handleGateway did not set the proper gateway type, expected: " + rdfGateway.getAllBpmnqGatewaytype_as().firstValue() +", actual: " + output.type2,
                    output.type2.startsWith(rdfGateway.getAllBpmnqGatewaytype_as().firstValue()));

            if (rdfGateway.hasBpmnqId()) {
                String gwID = rdfGateway.getAllBpmnqId_as().firstValue();
                if (!gwID.equals("")) {
                    assertEquals("wrong gateway ID set", gwID, output.getID());
                }
            }

            // property is not yet present in RDF Schema
//            if (rdfGateway.hasBpmnqName()) {
//                assertEquals("gateway name is wrong", 
//                        rdfGateway.getAllBpmnqName_as().firstValue, output.getName());
//            }
        }
        
        
    }
    
    @Test
    public void testHandleEvent() throws Exception {
        List<Event> allRdfEvents = new ArrayList<Event>();
        allRdfEvents.addAll(
                Startevent.getAllInstances_as(testable.rdfModel).asList());
        allRdfEvents.addAll(
                Intermediateevent.getAllInstances_as(testable.rdfModel).asList());
        allRdfEvents.addAll(
                Endevent.getAllInstances_as(testable.rdfModel).asList());
        for (Event rdfEvent : allRdfEvents) {
            GraphObject output = testable.handleEvent(rdfEvent);
            assertNotNull("event must not be null", output);
            assertEquals("no event node was returned", 
                    GraphObjectType.EVENT, output.type);
            assertNotNull("Event type (type2) must not be null", output.type2);
            if (QueryGraphBuilderRDF.isInstanceOf(rdfEvent, Startevent.RDFS_CLASS)) {
                assertEquals("event type should be a start event", 
                        EventType.START.asType2String(), output.type2);
            } else if (QueryGraphBuilderRDF.isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS)) {
                assertEquals("event type should be a Intermediate event", 
                        EventType.INTERMEDIATE.asType2String(), output.type2);
            } else if (QueryGraphBuilderRDF.isInstanceOf(rdfEvent, Endevent.RDFS_CLASS)) {
                assertEquals("event type should be a end event", 
                        EventType.END.asType2String(), output.type2);
            }
            
            assertTrue(rdfEvent.hasBpmnqEventtype());
            assertEquals("did not set proper event type",
                    EventType.valueOf(rdfEvent.getAllBpmnqEventtype_as().firstValue().toUpperCase()).asType2String(), output.type2);

            if (rdfEvent.hasBpmnqId()) {
                String evID = rdfEvent.getAllBpmnqId_as().firstValue();
                if (!evID.equals("")) {
                    assertEquals("wrong event ID set", evID, output.getID());
                }
            }

        }
    }
    
    @Test @Ignore
    public void printQueryGraph() throws Exception {
        QueryGraph out = testable.buildGraph();
        out.print(System.out);
    }
    
    @Test
    public void testHandleGenericShape() throws Exception {
        for (Genericshape rdfGenShape : Genericshape.getAllInstances_as(testable.rdfModel).asArray()) {
            GraphObject output = testable.handleGenericShape(rdfGenShape);
            assertNotNull("must not return a null value", output);
            assertEquals("wrong node type", GraphObjectType.ACTIVITY, output.type);
            assertEquals("wrong sub-type", ActivityType.GENERIC_SHAPE.asType2String(), output.type2);
            // TODO test for a reasonable ID
        }
    }
    
    @Test
    public void testHandleTask() throws Exception {
        for (Task rdfTask : Task.getAllInstances_as(testable.rdfModel).asArray()) {
            GraphObject output = testable.handleTask(rdfTask);
            assertNotNull("must not return a null value", output);
            assertEquals("wrong node type", GraphObjectType.ACTIVITY, output.type);
            assertEquals("wrong sub-type", ActivityType.TASK.asType2String(), output.type2);
            if (rdfTask.hasBpmnqName())
                assertEquals("wrong name in node", rdfTask.getAllBpmnqName_as()
                        .firstValue(), output.getName());
            // TODO test for a reasonable ID
            
        }
    }
    
    @Test
    public void testHandleVariableActivity() throws Exception {
        for (Variableactivity rdfVarAct : Variableactivity.getAllInstances_as(testable.rdfModel).asArray()) {
            GraphObject output = testable.handleVariableActivity(rdfVarAct);
            assertNotNull("must not return a null value", output);
            assertEquals("wrong node type", GraphObjectType.ACTIVITY, output.type);
            assertEquals("wrong sub-type", ActivityType.VARIABLE_ACTIVITY.asType2String(), output.type2);
            assertEquals("wrong name in node", 
                    rdfVarAct.getAllBpmnqName_as().firstValue(), output.getName());
            assertTrue("Variable activity name doesn't start with a @ sign", 
                    output.getName().startsWith("@"));
            // TODO test for a reasonable ID
        }
    }
    
    @Test
    public void testHandlePath() throws Exception {
        // all nodes must be processed already, so let it build the graph
        testable.buildGraph();
        
        for (Path rdfPath : Path.getAllInstances_as(testable.rdfModel).asArray()) {
            com.bpmnq.Path output = testable.handlePath(rdfPath);
            assertNotNull("must not return a null value", output);

            Resource endsAt = rdfPath.getAllBpmnqOutgoing_as().firstValue();
            Resource startsAt = testable.findAllBpmnqIncoming(rdfPath)[0];
            
            checkSeqFlowPlausibility(output, startsAt, endsAt);
            
            if (rdfPath.hasBpmnqExclude()) {
                assertEquals("path doesn't have same exclude value", 
                        rdfPath.getAllBpmnqExclude_as().firstValue(), output.exclude);
            } else
                assertEquals("", output.exclude);
            
            if (rdfPath.hasBpmnqName()) {
        	assertEquals("path doesn't have specified label", 
                        rdfPath.getAllBpmnqName_as().firstValue(), output.label);
            } else 
        	assertEquals("", output.label);
            
        }
    }
    
    @Test
    public void testHandleNegativePath() throws Exception {
        // all nodes must be processed already, so let it build the graph
        testable.buildGraph();
        
        for (NegativePath rdfNegPath : NegativePath.getAllInstances_as(testable.rdfModel).asArray()) {
            SequenceFlow output = testable.handleNegativePath(rdfNegPath);
            assertNotNull("handleNegativePath must not return a null value", output);

            Resource endsAt = rdfNegPath.getAllBpmnqOutgoing_as().firstValue();
            Resource startsAt = testable.findAllBpmnqIncoming(rdfNegPath)[0];
            
            checkSeqFlowPlausibility(output, startsAt, endsAt);
        }
    }
    
    @Test
    public void testHandleNegativeSequenceFlow() throws Exception {
        // all nodes must be processed already, so let it build the graph
        testable.buildGraph();
        
        for (NegativeSequenceflow rdfNegSeqFlow : NegativeSequenceflow.getAllInstances_as(testable.rdfModel).asArray()) {
            SequenceFlow output = testable.handleNegativeSequenceflow(rdfNegSeqFlow);
            assertNotNull("must not return a null value", output);

            Resource endsAt = rdfNegSeqFlow.getAllBpmnqOutgoing_as().firstValue();
            Resource startsAt = testable.findAllBpmnqIncoming(rdfNegSeqFlow)[0];
            
            checkSeqFlowPlausibility(output, startsAt, endsAt);
        }
    }
    
    @Test
    public void testHandleSequenceFlow() throws Exception {
        // all nodes must be processed already, so let it build the graph
        testable.buildGraph();
        
        for (Sequenceflow rdfSeqFlow : Sequenceflow.getAllInstances_as(testable.rdfModel).asArray()) {
            SequenceFlow output = testable.handleSequenceflow(rdfSeqFlow);
            assertNotNull("must not return a null value", output);
            
            Resource endsAt = rdfSeqFlow.getAllBpmnqOutgoing_as().firstValue();
            Resource startsAt = testable.findAllBpmnqIncoming(rdfSeqFlow)[0];
            
            checkSeqFlowPlausibility(output, startsAt, endsAt);
        }
    }

    private void checkSeqFlowPlausibility(SequenceFlow output,
            Resource startsAt, Resource endsAt) {
        
        if (QueryGraphBuilderRDF.isInstanceOf(startsAt, Activity.RDFS_CLASS)) {
            assertNotNull("should actually start at an activity", output.frmActivity);
            assertNull(output.frmEvent);
            assertNull(output.frmGateWay);
            Activity startAct = (Activity) startsAt.castTo(Activity.class);
            if (startAct.hasBpmnqName()) {
                assertEquals("seems to start at a different activity", 
                        startAct.getAllBpmnqName_as().firstValue(),
                        output.frmActivity.name);
            }
        } else if (QueryGraphBuilderRDF.isInstanceOf(startsAt, Gateway.RDFS_CLASS)) {
            assertNotNull("should actually start at a gateway", output.frmGateWay);
            assertNull(output.frmEvent);
            assertNull(output.frmActivity);
        } else if (QueryGraphBuilderRDF.isInstanceOf(startsAt, Event.RDFS_CLASS)) {
            assertNotNull("should actually start at an event", output.frmEvent);
            assertNull(output.frmActivity);
            assertNull(output.frmGateWay);
        }
        
        if (QueryGraphBuilderRDF.isInstanceOf(endsAt, Activity.RDFS_CLASS)) {
            assertNotNull("should actually end at an activity", output.toActivity);
            assertNull(output.toEvent);
            assertNull(output.toGateWay);
            Activity endAct = (Activity) endsAt.castTo(Activity.class);
            if (endAct.hasBpmnqName()) {
                assertEquals("seems to end at a different activity",
                        endAct.getAllBpmnqName_as().firstValue(),
                        output.toActivity.name);
            }
        } else if (QueryGraphBuilderRDF.isInstanceOf(endsAt, Gateway.RDFS_CLASS)) {
            assertNotNull("should actually end at a gateway", output.toGateWay);
            assertNull(output.toEvent);
            assertNull(output.toActivity);
        } else if (QueryGraphBuilderRDF.isInstanceOf(endsAt, Event.RDFS_CLASS)) {
            assertNotNull("should actually end at an event", output.toEvent);
            assertNull(output.toActivity);
            assertNull(output.toGateWay);
        }
    }
    
    @Test
    public void testFindAllBpmnqIncomingNode() throws Exception {
        
    }
    
    @Test
    public void testFindAllBpmnqIncomingEdge() throws Exception {
        for (Sequenceflow rdfSeqFlow : Sequenceflow.getAllInstances_as(testable.rdfModel).asArray()) {
            Resource[] incomings = testable.findAllBpmnqIncoming(rdfSeqFlow);
            assertNotNull(incomings);
            
            for (Resource incoming : incomings) {
                if (QueryGraphBuilderRDF.isInstanceOf(incoming, Node.RDFS_CLASS)) {
                    Node incomingNode = (Node) incoming.castTo(Node.class);
                    assertTrue("alledge incoming resource has no corresponding outgoing",
                            incomingNode.getAllBpmnqOutgoing_as().asList().contains(rdfSeqFlow));
                } else if (QueryGraphBuilderRDF.isInstanceOf(incoming, Edge.RDFS_CLASS)) {
                    Edge incomingEdge = (Edge) incoming.castTo(Edge.class);
                    assertTrue("alledge incoming resource has no corresponding outgoing", 
                            incomingEdge.getAllBpmnqOutgoing_as().asList().contains(rdfSeqFlow));
                }
            }
        }

    }

    @Test @Ignore
    public void testXsltInputTransform() throws Exception {
        File erdfFile = new File("c:/Container/HPI/Master-Arbeit/bpmn-q-diagram_with_events_connected.xhtml");
        File styleFile = new File("c:/Container/HPI/Master-Arbeit/extract-rdf.xsl");
        ByteArrayOutputStream rdfXmlMemStore = new ByteArrayOutputStream(); 
        OutputStream rdfXmlOutput = new BufferedOutputStream(rdfXmlMemStore);
        
        Source erdfInput = new StreamSource(erdfFile);
        Source styleInput = new StreamSource(styleFile);
        Result rdfXml = new StreamResult(rdfXmlOutput);
        
        TransformerFactory transFac = TransformerFactory.newInstance();
        Transformer trans = transFac.newTransformer(styleInput);
        trans.transform(erdfInput, rdfXml);
        rdfXmlOutput.close();
        //System.out.println(rdfXmlMemStore.toString());
        
        ModelFactory modelFact = RDF2Go.getModelFactory();
        Model erdfModel = modelFact.createModel();
        erdfModel.open();
        
        InputStream rdfXmlInput = new ByteArrayInputStream(rdfXmlMemStore.toByteArray());
        String baseUri = "http://myhpi.de/~sryll/bpmnq.xhtml"; //erdfFile.toURI().toString();
        erdfModel.readFrom(rdfXmlInput, Syntax.RdfXml, baseUri);
        
        TriplePattern emptyStatementPattern = 
            erdfModel.createTriplePattern(Variable.ANY, Variable.ANY, erdfModel.createPlainLiteral(""));
        erdfModel.removeStatements(emptyStatementPattern);
        erdfModel.removeStatement(erdfModel.createURI(baseUri), 
                erdfModel.createURI("http://webns.net/mvcb/generatorAgent"), 
                erdfModel.createURI("http://purl.org/NET/erdf/extract"));

        erdfModel.dump();
        System.out.println("Dumped remaining " + erdfModel.size() + " statements.");
        System.out.println("Model comparison: test model has " + testable.rdfModel.size() + " statements.");
        
        assertTrue("XSLT-transformed eRDF model is not isomorphic with test model.", 
                erdfModel.isIsomorphicWith(testable.rdfModel));

    }
    }
