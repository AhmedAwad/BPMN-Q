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

public class OryxMemoryQueryProcessorTest
{
    private Logger log = Logger.getLogger(OryxMemoryQueryProcessorTest.class);
    OryxMemoryQueryProcessor testable = null;
    ByteArrayOutputStream wrStream;
    PrintWriter wr;

    @Before
    public void setUp() throws Exception
    {
	Utilities util = Utilities.getInstance(); // initial setup
	
	wrStream = new ByteArrayOutputStream();
	wr = new PrintWriter(wrStream);
	testable = new OryxMemoryQueryProcessor(wr);
    }
    
    @After
    public void tearDown() throws Exception
    {
	wr.close();
//	if (wrStream.size() > 0)
//	{
//	    log.trace("Queryprocessor outstream contents: " + wrStream.toString());
//	}
    }
    
    @Test
    public void findRelevantProcesses() throws Exception
    {
	List<String> result;
	result = testable.findRelevantProcessModels(null);
	for (String s : result)
	{
	    System.out.println(s);
	}
    }
    
    @Test
    public void simpleQuery() throws Exception
    {
	QueryGraph q = new QueryGraph();
	
	GraphObject src,dst;
	Path p;
	src = new GraphObject();
	src.type =GraphObjectType.EVENT;
	src.type2 = "1";
	
	dst = new GraphObject();
	dst.setID("-1");
	dst.type = GraphObjectType.ACTIVITY;
	dst.setName("Vervollst. und sende Angebot");
	p = new Path(src,dst,"");
	q.add(src);
	q.add(dst);
	q.add(p);
	ProcessGraph result=new ProcessGraph();
//	testable.testQueryAgainstModel(q, "http://oryx-editor.org/backend/poem/model/1476/rdf",result);
//	System.out.println("------------------------------------------------PRINTING-----------------------------------------");
//	result.print(System.out);
	
	testable.processQuery(q);
	this.wr.close();
	System.out.println(this.wrStream.toString());
	
    }
    
    @Test
    public void deadlockQuery() throws Exception
    {
	long startTime, endTime;
	startTime = System.currentTimeMillis();
	QueryGraphBuilderXML qgbx = new QueryGraphBuilderXML("C:/Temp/query.xml");
	QueryGraph q = new QueryGraph();
	q = qgbx.buildQueryGraph();
//	GraphObject src,dst;
//	Path p;
//	src = new GraphObject();
//	src.type =GraphObjectType.EVENT;
//	src.type2 = "1";
//	
//	dst = new GraphObject();
//	dst.setID("-1");
//	dst.type = GraphObjectType.ACTIVITY;
//	dst.setName("Vervollst. und sende Angebot");
//	p = new Path(src,dst,"");
//	q.add(src);
//	q.add(dst);
//	q.add(p);
//	ProcessGraph result=new ProcessGraph();
//	testable.testQueryAgainstModel(q, "http://oryx-editor.org/backend/poem/model/950/rdf",result);
//	System.out.println("------------------------------------------------PRINTING-----------------------------------------");
//	result.print(System.out);
	testable.stopAtFirstMatch = false;
	testable.processQuery(q);
	endTime = System.currentTimeMillis();
	System.out.println("Total time = "+ (endTime - startTime) );
	System.out.println("Done...");
//	this.wr.close();
//	System.out.println(this.wrStream.toString());
	
    }
    
}
