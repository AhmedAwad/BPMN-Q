package com.bpmnq;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.Before;

public class DatabaseQueryProcessorTest extends AbstractQueryProcessorTest
{
    DatabaseQueryProcessor testable = null;
    OutputStream wrStream;


    @Before
    public void setUp() throws Exception
    {
	super.setUp();
	
	wrStream = new ByteArrayOutputStream();
	PrintWriter wr = new PrintWriter(wrStream);
	testable = new DatabaseQueryProcessor(wr);
    }

    @Override
    public AbstractQueryProcessor getConcreteInstance()
    {
	return testable;
    }

}
