package com.bpmnq;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.Test;

public class ProcessGraphBuilderXMLTest {
    
    File queryGraphXMLFile;
    ProcessGraphBuilderXML testable;

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
        
        testable = new ProcessGraphBuilderXML(queryGraphXMLFile.getAbsolutePath());
    }

    @Test
    public void testBuildProcessGraph() {
        ProcessGraph pGraph = testable.buildProcessGraph();
        assertNotNull(pGraph);
        assertNotNull(testable.nodeMap);
        assertFalse(testable.nodeMap.isEmpty());
        
        // following depends on test data fed into the graph builder
        assertEquals(5, pGraph.nodes.size());
        assertEquals(2, pGraph.edges.size());
        
    }
    
    // remaining relevant methods are tested in QueryGraphBuilderXMLTest 

}
