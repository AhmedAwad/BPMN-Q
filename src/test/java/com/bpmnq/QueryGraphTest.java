package com.bpmnq;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.bpmnq.GraphObject.GraphObjectType;

public class QueryGraphTest
{
    QueryGraph testable;
    GraphObject node1;
    GraphObject node2;

    @Before
    public void setUp() throws Exception
    {
	testable = new QueryGraph();
	node1 = new GraphObject("-1", "foo", GraphObjectType.ACTIVITY, "");
	node2 = new GraphObject("-2", "bar", GraphObjectType.ACTIVITY, "");
    }

    @Test
    public void testAddPath() throws Exception
    {
	Path path = addNodesAndConnectWithPath(node1, node2);
	assertEquals(1, testable.paths.size());
	assertEquals(2, testable.nodes.size());
	
	Path insertedPath = testable.paths.get(0);
	assertEquals("Inserting a path should not change it", path, insertedPath);

	testable.add(path);
	assertEquals("addPath should not insert identical duplicates", 1, testable.paths.size());
	Path path2 = new Path(node1, node2);
	testable.add(path2);
	assertEquals("addPath should not insert equal duplicates", 1, testable.paths.size());
	
	
    }

    @Test
    public void testRemovePathsWithDestination()
    {
	Path path = addNodesAndConnectWithPath(node1, node2);
	int beforeSize = testable.paths.size();
	testable.removePathsWithDestination(node1);
	int afterSize = testable.paths.size();
	assertEquals("removePathWithDestination removed too much", beforeSize, afterSize);
	
	testable.removePathsWithDestination(node2);
	afterSize = testable.paths.size();
	assertEquals("removePathWithDestination removed too much", beforeSize-1, afterSize);
	assertFalse("path was not removed", testable.paths.contains(path));

    }

    @Test
    public void testRemovePathsWithSource()
    {
	Path path = addNodesAndConnectWithPath(node1, node2);
	int beforeSize = testable.paths.size();
	testable.removePathsWithSource(node2);
	int afterSize = testable.paths.size();
	assertEquals("removePathWithDestination removed too much", beforeSize, afterSize);
	
	testable.removePathsWithSource(node1);
	afterSize = testable.paths.size();
	assertEquals("removePathWithDestination removed too much", beforeSize-1, afterSize);
	assertFalse("path was not removed", testable.paths.contains(path));

    }
    
    protected Path addNodesAndConnectWithPath(GraphObject node1, GraphObject node2)
    {
	testable.add(node1);
	testable.add(node2);
	Path path = new Path(node1, node2);
	testable.add(path);
	return path;
    }
    
    @Test
    public void testAddDataObject() throws Exception
    {
	DataObject dObj = new DataObject("21", "", "");
	int dObjCount = testable.dataObjs.size();
	testable.add(dObj);
	assertEquals("Too few data objects in graph", dObjCount+1, testable.dataObjs.size());
	assertTrue("Data object is not in graph", testable.dataObjs.contains(dObj));
	
	testable.add(dObj);
	assertEquals("Must not add duplicates", dObjCount+1, testable.dataObjs.size());
	
	testable.remove(dObj);
	assertEquals("Too many data objects in graph", dObjCount, testable.dataObjs.size());
	assertFalse("Data object is still in graph", testable.dataObjs.contains(dObj));
	
	testable.remove(dObj);
	assertEquals("Removed too many data objects from graph", dObjCount, testable.dataObjs.size());
    }
    
    @Test
    public void testRemoveDataPathAssociation() throws Exception
    {
	Path path = addNodesAndConnectWithPath(node1, node2);
//	GraphObject dObj = new GraphObject(20, "", GraphObjectType.DATAOBJECT, "");
	// XXX what if we add a GraphObject with type==DATAOBJECT -- will it have the same effect as adding a DataObject?? 
	DataObject dObj = new DataObject("21", "", "");
	testable.add(dObj);
	
	UndirectedAssociation undAssoc = new UndirectedAssociation(dObj.originalNode(), path);
	int assocCount = testable.dataPathAssociations.size();
	testable.addDataPathAssociation(undAssoc);
	assertTrue("Data-path association should be in graph", testable.dataPathAssociations.contains(undAssoc));
	assertEquals("Expecting one more data-path association", assocCount+1, testable.dataPathAssociations.size());
	
	testable.addDataPathAssociation(undAssoc);
	assertEquals("Must not add duplicates", assocCount+1, testable.dataPathAssociations.size());
	
	// Why can't we remove undAssoc directly?? Cause of the failure??
	testable.removeDataPathAssociation(dObj.originalNode(), path);
	assertFalse("Data-path association is still in graph", testable.dataPathAssociations.contains(undAssoc));
	assertEquals("Too many data-path associations in graph", assocCount, testable.dataPathAssociations.size());
	
	testable.removeDataPathAssociation(dObj.originalNode(), path);
	assertEquals("Removed too many data-path associations from graph", assocCount, testable.dataPathAssociations.size());
    }

}
