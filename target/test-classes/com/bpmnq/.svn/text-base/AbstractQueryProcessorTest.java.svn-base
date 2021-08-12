package com.bpmnq;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bpmnq.GraphObject.GraphObjectType;

import static com.bpmnq.GraphObject.GraphObjectType.*;

public abstract class AbstractQueryProcessorTest
{
    private Logger log = Logger.getLogger(AbstractQueryProcessorTest.class);

    public void setUp() throws Exception
    {
	Utilities util = Utilities.getInstance();
	util.openConnection();
	
//	AbstractQueryProcessor.intermediateRefinements.clear();
//	AbstractQueryProcessor.finalRefinements.clear();
    }
    
    @After
    public void tearDown() throws Exception
    {
	Utilities.closeConnection();
    }
    
    public abstract AbstractQueryProcessor getConcreteInstance();

    @Test @Ignore
    public void testFindRelevantProcessModels()
    {
	fail("Not yet implemented"); // TODO
    }

    @Test
    public void testTestQueryAgainstModel()
    {
	AbstractQueryProcessor testable = getConcreteInstance();
	QueryGraph testGraph = buildMyStdTestgraph();
	ProcessGraph match = null;
	boolean res = testable.testQueryAgainstModel(testGraph, "40", match);
	assertTrue("query should be tested successfully on model 40", res);
	if (true == res)
	{
	    assertNotNull("Shouldn't return a null process graph", match);
	}
    }

    @Test
    public void testProcessQuery()
    {
	AbstractQueryProcessor testable = getConcreteInstance();
	QueryGraph emptyGraph = new QueryGraph();
	log.debug("--- running new query --");
	List<String> matchingModels = testable.processQuery(emptyGraph);
	assertNotNull(matchingModels);
	//FIXME inconsistency between MemoryQP and DatabaseQP. MQP finds all, DbQP finds none
	assertEquals("Querying with an empty graph should find all models", 61, matchingModels.size());
	
	QueryGraph testGraph = new QueryGraph();
	GraphObject node1 = new GraphObject("-1", "Submit Deposit", ACTIVITY, "");
	GraphObject node2 = new GraphObject("-2", "Record Customer Info", ACTIVITY, "");
	testGraph.add(node1);
	testGraph.add(node2);
	testGraph.addEdge(node1, node2);
	log.debug("--- running new query --");
	matchingModels = testable.processQuery(testGraph);
	assertTrue("Query should at least return model 40", matchingModels.contains(40));
	
	testGraph.removeEdge(node1, node2);
	testGraph.add(new Path(node1, node2));
	log.debug("--- running new query --");
	matchingModels = testable.processQuery(testGraph);
	assertTrue("Query should at least return model 40", matchingModels.contains(40));
	
	testGraph.removePathsWithSource(node1);
	testGraph.add(new Path(node1, node2, ""));
	log.debug("--- running new query --");
	List<String> matchingModels4Comp = testable.processQuery(testGraph);
	assertTrue("Query should at least return model 40", matchingModels4Comp.contains(40));
	assertEquals("Queryies should be identical when one path is exclude-less, the other with empty exclude", matchingModels, matchingModels4Comp);
	

	testGraph = buildMyStdTestgraph();
	matchingModels = testable.processQuery(testGraph);
	assertTrue("Query should at least return model 40", matchingModels.contains(40));

    }

    private QueryGraph buildMyStdTestgraph()
    {
	QueryGraph testGraph;
	// build one of my test graphs i used in file-form
	testGraph = new QueryGraph();
	GraphObject act3 = new GraphObject("-3", "Record Customer Info", GraphObjectType.ACTIVITY, "");
	GraphObject act4 = new GraphObject("-4", "Open Account", GraphObjectType.ACTIVITY, "");
	GraphObject act5 = new GraphObject("-5", "Submit Deposit", GraphObjectType.ACTIVITY, "");
	GraphObject gw2  = new GraphObject("-2", "", GraphObjectType.GATEWAY, "AND JOIN");
	testGraph.add(act3);
	testGraph.add(act4);
	testGraph.add(act5);
	testGraph.add(gw2);
	testGraph.add(new Path(act3, gw2, ""));
	testGraph.add(new Path(act5, act3, ""));
	testGraph.add(new SequenceFlow(gw2, act4));
	return testGraph;
    }

}
