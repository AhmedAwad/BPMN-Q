package com.bpmnq;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bpmnq.GraphObject.GraphObjectType;

public class MemoryQueryProcessorTest extends AbstractQueryProcessorTest
{
    private Logger log = Logger.getLogger(MemoryQueryProcessorTest.class);
    MemoryQueryProcessor testable = null;
    ByteArrayOutputStream wrStream;
    PrintWriter wr;

    @Before
    public void setUp() throws Exception
    {
	super.setUp();
	
	wrStream = new ByteArrayOutputStream();
	wr = new PrintWriter(wrStream);
	testable = new MemoryQueryProcessor(wr);
    }
    
    @After
    public void tearDown() throws Exception
    {
	wr.close();
//	if (wrStream.size() > 0)
//	{
//	    log.trace("Queryprocessor outstream contents: " + wrStream.toString());
//	}
	super.tearDown();
    }
    
    @Override
    public AbstractQueryProcessor getConcreteInstance()
    {
	return testable;
    }

    @Test
    public void testResolveGateWayNode()
    {
	// create a small partially resolved query graph
	GraphObject dest = new GraphObject("305", "Open Account", GraphObjectType.ACTIVITY, "");
	// this gateway should be resolved to ID 229
	GraphObject gw  = new GraphObject("-2", "", GraphObjectType.GATEWAY, "AND JOIN");
	QueryGraph gwToActivityGraph = new QueryGraph();
	gwToActivityGraph.add(dest);
	gwToActivityGraph.add(gw);
	gwToActivityGraph.add(new SequenceFlow(gw, dest));
	
	testable.intermediateRefinements.add(gwToActivityGraph);
	testable.finalRefinements.clear();

	testable.resolveGateWayNode("40");

	assertEquals("intermediate refinements were not removed", 0, testable.intermediateRefinements.size());
	assertEquals("final refinements too small", 1, testable.finalRefinements.size());
	
	// test another query graph, where the gateway is at the end 
	QueryGraph actToGwGraph = new QueryGraph();
	GraphObject act1 = new GraphObject("309", "Propose Account Opening", GraphObjectType.ACTIVITY, "");
	GraphObject act2 = new GraphObject("306", "Verify Customer ID", GraphObjectType.ACTIVITY, "");
	GraphObject act3 = new GraphObject("307", "Open Acc Status Review", GraphObjectType.ACTIVITY, "");
	actToGwGraph.add(gw);
	actToGwGraph.add(act1);
	actToGwGraph.add(act2);
	actToGwGraph.add(act3);
	actToGwGraph.add(new SequenceFlow(act1, gw));
	actToGwGraph.add(new SequenceFlow(act2, gw));
	actToGwGraph.add(new SequenceFlow(act3, gw));
	
	testable.intermediateRefinements.clear();
	testable.finalRefinements.clear();
	testable.intermediateRefinements.add(actToGwGraph);
	
	testable.resolveGateWayNode("40");
	
	QueryGraph refined2 = testable.finalRefinements.get(0);
	assertEquals("final refinements too small", 4, refined2.nodes.size());
//	assertNotSame("gateway was not changed/ resolved", gw, refined2.nodes.get(0));
	assertEquals("gateway resolved to wrong ID", "229", refined2.nodes.get(0).getID());
	assertEquals(act1, refined2.nodes.get(1));
	assertEquals(act2, refined2.nodes.get(2));
	assertEquals(act3, refined2.nodes.get(3));

    }

}
