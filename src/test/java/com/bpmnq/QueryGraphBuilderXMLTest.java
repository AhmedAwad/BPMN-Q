package com.bpmnq;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class QueryGraphBuilderXMLTest {

    File queryGraphXMLFile;
    QueryGraphBuilderXML testable;

    final String graphXMLunderTest = "<QueryGraph>\n" +
        "<GraphObject id='3' type1='Activity' type2='' name='Record Customer Info' />\n" +
        "<GraphObject id='4' type1='Activity' type2='' name='Open Account' />\n" +
        "<GraphObject id='5' type1='Activity' type2='' name='Submit Deposit' />\n" +
        "<GraphObject id='6' type1='Gateway' type2='XOR SPLIT' name='' />\n" +
        "<GraphObject id='7' type1='Gateway' type2='XOR JOIN' name='' />\n" +
        "<Path from='3' to='4' execlude='' />\n" +
        "<NegativePath from='5' to='3' />\n" +
        "<SequenceFlow from='7' to='5' />\n" +
        "<SequenceFlow from='5' to='6' />\n" +
        "<NegativeSequenceFlow from='5' to='4' />\n" +
        "</QueryGraph>";

    @Before
    public void setUp() throws Exception {
        queryGraphXMLFile = File.createTempFile("bpmnq", ".xml");
        queryGraphXMLFile.deleteOnExit();
        
        // write test data to file
        BufferedWriter xmlWriter = new BufferedWriter(new FileWriter(queryGraphXMLFile));
        xmlWriter.write(graphXMLunderTest);
        xmlWriter.close();
        
        testable = new QueryGraphBuilderXML(queryGraphXMLFile.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testQueryGraphBuilderXML() {
        QueryGraphBuilderXML qGrBuilder = 
            new QueryGraphBuilderXML(queryGraphXMLFile.getAbsolutePath());
        
//        assertNull(qGrBuilder.getNodeWithID("0"));
//        assertNull(qGrBuilder.getNodeWithID(Integer.MAX_VALUE));
        
        assertNotNull(qGrBuilder.nodeMap);

    }

    @Test
    public void testBuildQueryGraph() {
        QueryGraph qGraph = testable.buildQueryGraph();
        assertNotNull(qGraph);
        assertNotNull(testable.nodeMap);
        assertFalse(testable.nodeMap.isEmpty());
        
        // following depends on test data fed into the graph builder
        assertEquals(5, qGraph.nodes.size());
        assertEquals(1, qGraph.paths.size());
        assertEquals(2, qGraph.edges.size());
        assertEquals(1, qGraph.negativeEdges.size());
        assertEquals(1, qGraph.negativePaths.size());
        
    }

    @Test
    public void testParseFile() {
        Document doc = testable.parseFile(queryGraphXMLFile.getAbsolutePath());
        assertNotNull(doc);
        // don't want to test the parser code here, just check that it creates a document object
    }

    @Test
    public void testSaveXMLDocument() {
        File outFile;
        String outFilePath;
        try {
            outFile = File.createTempFile("bpmnq", ".xml");
            outFilePath = outFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not create temp file for writing. But it's not bpmnq's fault.");
            return;
        } 

        try {
            Document doc = testable.parseFile(queryGraphXMLFile.getAbsolutePath());

            assertTrue(testable.saveXMLDocument(outFilePath, doc));
            assertTrue(outFile.exists());
            assertTrue(outFile.length() > 0);

        } finally {
            assertTrue(outFile.delete());
        }
        
    }

}
