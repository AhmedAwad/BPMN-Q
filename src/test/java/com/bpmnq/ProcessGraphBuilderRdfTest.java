package com.bpmnq;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdfreactor.schema.rdfs.Resource;

import com.bpmnq.GraphObject.ActivityType;
import com.bpmnq.GraphObject.EventType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.ProcessGraphBuilderRDF.RdfSyntax;

import de.hpi.bpmn.rdf.*;

public class ProcessGraphBuilderRdfTest {

    File rdfTestFile;
    ProcessGraphBuilderRDF testable;
    Logger logger;
    
    @Before
    public void setUp() throws Exception {
        // turn off too verbose log messages
        LogManager.getRootLogger().setLevel(Level.WARN);
        
        InputStream rdfXmlInput = getClass().getResourceAsStream("/oryx-1149.rdf");
        String baseUri = "http://myhpi.de/~sryll/bpmnq.xhtml"; //erdfFile.toURI().toString();
        
        testable = new ProcessGraphBuilderRDF();
        String processURI = "http://oryx-editor.org/backend/poem/model/950/rdf";
        URL processuri = new URL(processURI);
        Reader in = new BufferedReader(new InputStreamReader(processuri.openStream()));
	
	baseUri = processURI; //"http://myhpi.de/~sryll/bpmnq.xhtml"; //erdfFile.toURI().toString();
	    
	testable.setRdfInput(in, RdfSyntax.RDF_XML, baseUri);
    }
    @Test 
    public void testname() throws Exception
    {
	ProcessGraph result;
	result = testable.buildGraph();
//	testable.rdfModel.writeTo(System.out);
	
	System.out.println("Result:");
	result.print(System.out);
    }
    
    @Test @Ignore
    public void testLoadFromOryx() throws Exception
    {
	ProcessGraph p = new ProcessGraph();
	p.loadFromOryx("http://oryx-editor.org/backend/poem/model/950/rdf");
	p.print(System.out);
    }
    
    
    @Test @Ignore
    public void testinstanceOf() throws Exception
    {
//	testable.rdfModel.writeTo(System.out);
	
	Task task = new Task(testable.rdfModel, true);
	
	UnidirectionalAssociation assoc = new UnidirectionalAssociation(testable.rdfModel, true);
	org.ontoware.rdfreactor.schema.rdfs.Class[] types = task.getAllSubClassOf_as().asArray();
	
//	org.ontoware.rdfreactor.schema.rdfs.Class[] typestype = types[0].getAllSubClassOf_as().asArray();
	
	System.out.println("Types:");
	for (org.ontoware.rdfreactor.schema.rdfs.Class class1 : types)
	{
	    System.out.println(class1.toString());
	}
    }
    
    @Test
    public void testProcessGraphBuilderRdf() {
        testable = new ProcessGraphBuilderRDF();
        assertNotNull("RDF model is not initialized", testable.rdfModel);
        assertTrue("RDF model is not open", testable.rdfModel.isOpen());
        assertTrue("newly created model is not empty", testable.rdfModel.isEmpty());
    }

    @Test
    public void testProcessGraphBuilderRdfString() {
        // rely on setUp() initialization
        assertNotNull("RDF model is not initialized", testable.rdfModel);
        assertTrue("RDF model is not open", testable.rdfModel.isOpen());
        assertFalse("Model is empty after import", testable.rdfModel.isEmpty());
        
//        assertEquals("Model has unexpected number of tripels", 
//                125, testable.rdfModel.size());
    }

    @Test
    public void testSetRdfFilename() throws Exception {
        URL rdfUrl = getClass().getResource("/oryx-1149.rdf");
        
        testable.setRdfInput(rdfUrl.toURI(), RdfSyntax.RDF_XML);
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
        ProcessGraph out = testable.buildGraph();
        assertNotNull(out);
        assertTrue("buildGraph() returns the wrong specific type", 
                (out instanceof ProcessGraph));
        
        assertNotSame("query graph has wrong number of edges", 0, out.edges.size());
        assertNotSame("query graph has wrong number of nodes", 0, out.nodes.size());
        
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
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(task, Task.RDFS_CLASS));
        
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(task, de.hpi.bpmn.rdf.Activity.RDFS_CLASS));
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(task, Node.RDFS_CLASS));
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(task, Resource.RDFS_CLASS));
        
        
        assertFalse(ProcessGraphBuilderRDF.isSubClassOf(task, Edge.RDFS_CLASS));
        
        Gateway gw = new Gateway(testable.rdfModel, true);
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(gw, Node.RDFS_CLASS));
        assertTrue(ProcessGraphBuilderRDF.isSubClassOf(gw, Gateway.RDFS_CLASS));
        assertFalse(ProcessGraphBuilderRDF.isSubClassOf(gw, de.hpi.bpmn.rdf.Event.RDFS_CLASS));
        assertFalse(ProcessGraphBuilderRDF.isSubClassOf(gw, XORGateway.RDFS_CLASS));
        assertFalse(ProcessGraphBuilderRDF.isSubClassOf(gw, ExclusiveEventbasedGateway.RDFS_CLASS));
    }
    
    @Test
    public void testHandleGateway() throws Exception {
        List<Gateway> allRdfGateways = new ArrayList<Gateway>();
        allRdfGateways.addAll(
                ANDGateway.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                ORGateway.getAllInstances_as(testable.rdfModel).asList());
        
        allRdfGateways.addAll(
                ExclusiveDatabasedGateway.getAllInstances_as(testable.rdfModel).asList());
        allRdfGateways.addAll(
                ExclusiveEventbasedGateway.getAllInstances_as(testable.rdfModel).asList());
        
        for (Gateway rdfGateway : allRdfGateways) {
            GraphObject output = testable.handleGateway(rdfGateway);
            assertNotNull("handleGateway returned null", output);
            assertEquals("handleGateway produced no gateway node", 
                    GraphObjectType.GATEWAY, output.type);

//            assertTrue(rdfGateway.hasBpmnGatewaytype());
//            assertTrue("handleGateway did not set the proper gateway type, expected: " + rdfGateway.getAllBpmnqGatewaytype_as().firstValue() +", actual: " + output.type2,
//                    output.type2.startsWith(rdfGateway.getAllBpmnqGatewaytype_as().firstValue()));

            if (rdfGateway.hasBpmnId()) {
                String gwID = rdfGateway.getAllBpmnId_as().firstValue();
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
        List<de.hpi.bpmn.rdf.Event> allRdfEvents = new ArrayList<de.hpi.bpmn.rdf.Event>();
        allRdfEvents.addAll(
                Startevent.getAllInstances_as(testable.rdfModel).asList());
        allRdfEvents.addAll(
                Intermediateevent.getAllInstances_as(testable.rdfModel).asList());
        allRdfEvents.addAll(
                Endevent.getAllInstances_as(testable.rdfModel).asList());
        for (de.hpi.bpmn.rdf.Event rdfEvent : allRdfEvents) {
            GraphObject output = testable.handleEvent(rdfEvent);
            assertNotNull("event must not be null", output);
            assertEquals("no event node was returned", 
                    GraphObjectType.EVENT, output.type);
            assertNotNull("Event type (type2) must not be null", output.type2);
            if (ProcessGraphBuilderRDF.isInstanceOf(rdfEvent, Startevent.RDFS_CLASS)) {
                assertEquals("event type should be a start event", 
                        EventType.START.asType2String(), output.type2);
            } else if (ProcessGraphBuilderRDF.isInstanceOf(rdfEvent, Intermediateevent.RDFS_CLASS)) {
                assertEquals("event type should be a Intermediate event", 
                        EventType.INTERMEDIATE.asType2String(), output.type2);
            } else if (ProcessGraphBuilderRDF.isInstanceOf(rdfEvent, Endevent.RDFS_CLASS)) {
                assertEquals("event type should be a end event", 
                        EventType.END.asType2String(), output.type2);
            }
            
            assertTrue(rdfEvent.hasBpmnEventtype());
            assertEquals("did not set proper event type",
                    EventType.valueOf(rdfEvent.getAllBpmnEventtype_as().firstValue().toUpperCase()).asType2String(), output.type2);

            if (rdfEvent.hasBpmnId()) {
                String evID = rdfEvent.getAllBpmnId_as().firstValue();
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
    public void testHandleTask() throws Exception {
        for (Task rdfTask : Task.getAllInstances_as(testable.rdfModel).asArray()) {
            GraphObject output = testable.handleTask(rdfTask);
            assertNotNull("must not return a null value", output);
            assertEquals("wrong node type", GraphObjectType.ACTIVITY, output.type);
            assertEquals("wrong sub-type", ActivityType.TASK.asType2String(), output.type2);
            if (rdfTask.hasBpmnName())
                assertEquals("wrong name in node", rdfTask.getAllBpmnName_as()
                        .firstValue().replace("\n", " "), output.getName());
            // TODO test for a reasonable ID
            
        }
    }
    
    @Test
    public void testHandleSequenceFlow() throws Exception {
        // all nodes must be processed already, so let it build the graph
        testable.buildGraph();
        
        for (Sequenceflow rdfSeqFlow : Sequenceflow.getAllInstances_as(testable.rdfModel).asArray()) {
            SequenceFlow output = testable.handleSequenceflow(rdfSeqFlow);
            assertNotNull("must not return a null value", output);
            
            Resource endsAt = rdfSeqFlow.getAllBpmnOutgoing_as().firstValue();
            Resource startsAt = testable.findAllBpmnIncoming(rdfSeqFlow)[0];
            
            checkSeqFlowPlausibility(output, startsAt, endsAt);
        }
    }

    private void checkSeqFlowPlausibility(SequenceFlow output,
            Resource startsAt, Resource endsAt) {
        
        if (ProcessGraphBuilderRDF.isInstanceOf(startsAt, de.hpi.bpmn.rdf.Activity.RDFS_CLASS)) {
            assertNotNull("should actually start at an activity", output.frmActivity);
            assertNull(output.frmEvent);
            assertNull(output.frmGateWay);
            de.hpi.bpmn.rdf.Activity startAct = (de.hpi.bpmn.rdf.Activity) startsAt.castTo(de.hpi.bpmn.rdf.Activity.class);
            if (startAct.hasBpmnName()) {
                assertEquals("seems to start at a different activity", 
                        startAct.getAllBpmnName_as().firstValue().replace("\n", " "),
                        output.frmActivity.name);
            }
        } else if (ProcessGraphBuilderRDF.isInstanceOf(startsAt, Gateway.RDFS_CLASS)) {
            assertNotNull("should actually start at a gateway", output.frmGateWay);
            assertNull(output.frmEvent);
            assertNull(output.frmActivity);
        } else if (ProcessGraphBuilderRDF.isInstanceOf(startsAt, de.hpi.bpmn.rdf.Event.RDFS_CLASS)) {
            assertNotNull("should actually start at an event", output.frmEvent);
            assertNull(output.frmActivity);
            assertNull(output.frmGateWay);
        }
        
        if (ProcessGraphBuilderRDF.isInstanceOf(endsAt, de.hpi.bpmn.rdf.Activity.RDFS_CLASS)) {
            assertNotNull("should actually end at an activity", output.toActivity);
            assertNull(output.toEvent);
            assertNull(output.toGateWay);
            de.hpi.bpmn.rdf.Activity endAct = (de.hpi.bpmn.rdf.Activity) 
            		endsAt.castTo(de.hpi.bpmn.rdf.Activity.class);
            if (endAct.hasBpmnName()) {
                assertEquals("seems to end at a different activity",
                        endAct.getAllBpmnName_as().firstValue().replace("\n", " "),
                        output.toActivity.name);
            }
        } else if (ProcessGraphBuilderRDF.isInstanceOf(endsAt, Gateway.RDFS_CLASS)) {
            assertNotNull("should actually end at a gateway", output.toGateWay);
            assertNull(output.toEvent);
            assertNull(output.toActivity);
        } else if (ProcessGraphBuilderRDF.isInstanceOf(endsAt, de.hpi.bpmn.rdf.Event.RDFS_CLASS)) {
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
            Resource[] incomings = testable.findAllBpmnIncoming(rdfSeqFlow);
            assertNotNull(incomings);
            
            for (Resource incoming : incomings) {
                if (ProcessGraphBuilderRDF.isInstanceOf(incoming, Node.RDFS_CLASS)) {
                    Node incomingNode = (Node) incoming.castTo(Node.class);
                    assertTrue("alledge incoming resource has no corresponding outgoing",
                            incomingNode.getAllBpmnOutgoing_as().asList().contains(rdfSeqFlow));
                } else if (ProcessGraphBuilderRDF.isInstanceOf(incoming, Edge.RDFS_CLASS)) {
                    Edge incomingEdge = (Edge) incoming.castTo(Edge.class);
                    assertTrue("alledge incoming resource has no corresponding outgoing", 
                            incomingEdge.getAllBpmnOutgoing_as().asList().contains(rdfSeqFlow));
                }
            }
        }

    }
}
