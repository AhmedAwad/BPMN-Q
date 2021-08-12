package com.pst;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.pst.Node.NodeType;

public final class ProcessStructureTree {
    private com.pst.Node root;
    private List<com.pst.Node> nodes;
    private static int id = 0;
    private static int getNextID()
    {
	return ++id;
    }
    public com.pst.Node getRoot()
    {
	return root;
    }
    public ProcessStructureTree()
    {
	nodes = new ArrayList<com.pst.Node>();
    }
    public ProcessStructureTree(com.pst.Node r)
    {
	root = r;
	root.setID(getNextID());
	root.setLevel(0);
	nodes = new ArrayList<com.pst.Node>();
	nodes.add(r);
    }
    public void insertNodeBasic(com.pst.Node child, com.pst.Node parent)
    {
	if (nodes.contains(parent))
	{
	    child.setID(getNextID());
	    child.setLevel(parent.getLevel()+1);
	    parent.addChild(child);
	    
	    nodes.add(child);
	}
    }
    public void insertNode(com.pst.Node child,com.pst.Node parent)
    {
	insertNodeBasic(child, parent);
	if (nodes.contains(child))
	    processChilNodes(child);

    }
    private void processChilNodes(com.pst.Node p)
    {
	List<com.pst.Node> moreNodes = new ArrayList<com.pst.Node>();
	com.pst.Node currentNode = p;
	moreNodes.addAll(currentNode.getChildren());
	do
	{
	    List<com.pst.Node> children = currentNode.copyChildren();
	    currentNode.clearChildren();
	    for(com.pst.Node nd : children)
	    {
		moreNodes.addAll(nd.getChildren());
		insertNodeBasic(nd, currentNode);
	    }
	    if (moreNodes.size() > 0)
		currentNode = moreNodes.remove(0);
	}while(moreNodes.size() > 0);
    }
    public void insertNode(com.pst.Node child,com.pst.Node parent, int pos)
    {
	if (nodes.contains(parent))
	{
	    child.setID(getNextID());
	    parent.addChild(child,pos);
	    nodes.add(child);
	    processChilNodes(child);
	}
    }
    public List<com.pst.Node> findPath(com.pst.Node ch,com.pst.Node pa)
    {
	List<com.pst.Node> result = new ArrayList<com.pst.Node>();
	result.add(0,ch);
	com.pst.Node tmp = ch.getParent();
	if (tmp == null)
	    return null;
	while (!tmp.equals(pa))
	{
	    result.add(0,tmp);
	    tmp = tmp.getParent();
	    if (tmp == null)
		return null;
	}
	result.add(0,pa);
	return result;
    }
    public int getOrder(com.pst.Node ch, com.pst.Node pa)
    {
	if (ch.equals(pa))
	    return -1;
	if (!nodes.contains(ch) || !nodes.contains(pa))
	    return -1;
	if (pa.getNodeType() != NodeType.Sequence)
	    return 0;
	List<com.pst.Node> result = findPath(ch, pa);
	if (result == null)
	    return -1;
	else
	{
	    // the node just before the top level sequence = pa is the one having the order
	    int cnt = 1;
	    for (int i = 0; i< pa.getChildren().size(); i++)
	    {
		if (pa.getChildren().get(i).equals(result.get(1)))
		    return cnt;
		cnt++;
	    }
	}
	return 0;
    }
    public List<com.pst.Node> findPathToRoot(com.pst.Node someChild)
    {
	List<com.pst.Node> result = new ArrayList<com.pst.Node>();
	result.add(0,someChild);
	com.pst.Node tmp = someChild.getParent();
	while (tmp != null)
	{
	    result.add(0,tmp);
	    tmp = tmp.getParent();
	}
	return result;
    }
    public com.pst.Node leastCommonParent(com.pst.Node a, com.pst.Node b) //lcp
    {
	if (!nodes.contains(a) || !nodes.contains(b))
	    return null;

	List<com.pst.Node> path1, path2;
	path1 = findPathToRoot(a);
	path2 = findPathToRoot(b);
	int s1,s2;
	s1 = path1.size();
	s2 = path2.size();
	if (s1 < s2)
	{
	    // node a is on a higher level. Thus, we shoud search its parents for the lcp
	    for (int i = s1 -1; i>= 0;i-- )
	    {
		if (path2.contains(path1.get(i)))
		    return path1.get(i);
	    }
	}
	else
	{
	    for (int i = s2 -1; i>= 0;i-- )
	    {
		if (path1.contains(path2.get(i)))
		    return path2.get(i);
	    }
	}
	return root;
    }
    public List<com.pst.Node> getOccurrencesOfLabel(String l)
    {
	List<com.pst.Node> result = new ArrayList<com.pst.Node>();
	for(com.pst.Node n: nodes)
	{
	    if (n.getLabel().equalsIgnoreCase(l))
		result.add(n);
	}
	return result;
    }
    private void completeCloning(com.pst.Node n,com.pst.Node parent)
    {
	//n.setID(getNextID());
	n.setParent(parent);
	if (!nodes.contains(n))
	    nodes.add(n);
	for(com.pst.Node nd : n.getChildren())
	{
	    completeCloning(nd,n);
	}
    }
    public ProcessStructureTree clone()
    {
	com.pst.Node newRoot = this.root.clone();
	ProcessStructureTree newTree = new ProcessStructureTree();
	newTree.root = newRoot;
	newTree.root.setID(this.root.getID());
	newTree.completeCloning(newTree.root,null);
//	com.pst.Node currentNode = newRoot;
//	List<com.pst.Node> moreNodes = new ArrayList<com.pst.Node>();
//
//	moreNodes.addAll(currentNode.getChildren());
//	do
//	{
//	    List<com.pst.Node> children = currentNode.getChildren();
//	    currentNode.clearChildren();
//	    for(com.pst.Node nd : children)
//	    {
//		moreNodes.addAll(nd.getChildren());
//		newTree.insertNodeBasic(nd, currentNode);
//	    }
//	    if (moreNodes.size() > 0)
//		currentNode = moreNodes.remove(0);
//	}while(moreNodes.size() > 0);

	return newTree;

    }
    public boolean alwaysExecuted(com.pst.Node block,String label)
    {
	if (block.getNodeType() == NodeType.Activity)
	    return block.getLabel().equalsIgnoreCase(label);
	else if (block.getNodeType() == NodeType.Sequence)
	{
	    for (com.pst.Node nd : block.getChildren())
	    {
		if (alwaysExecuted(nd, label))
		    return true;
	    }
	    return false;
	}
	else if (block.getNodeType() == NodeType.Parallel)
	{
	    for (com.pst.Node nd : block.getChildren())
	    {
		if (alwaysExecuted(nd, label))
		    return true;
	    }
	    return false;
	}
	else if (block.getNodeType() == NodeType.XChoice || block.getNodeType() == NodeType.IChoice)
	{
	    for (com.pst.Node nd : block.getChildren())
	    {
		if (!alwaysExecuted(nd, label))
		    return false;
	    }
	    return true;
	}
	else // this is the loop
	{
	    if (block.getChildren().get(0).getNodeType() == NodeType.Mandatory)
		return alwaysExecuted(block.getChildren().get(0).getChildren().get(0), label);
	    else
		return alwaysExecuted(block.getChildren().get(1).getChildren().get(0), label);
	}
    }

    public boolean sometimeExecuted(com.pst.Node block,String label)
    {
	if (block.getNodeType() == NodeType.Activity)
	    return block.getLabel().equalsIgnoreCase(label);
	else if (block.getNodeType() == NodeType.Sequence || block.getNodeType() == NodeType.XChoice 
		|| block.getNodeType() == NodeType.IChoice || block.getNodeType() == NodeType.Parallel)
	{
	    for (com.pst.Node nd : block.getChildren())
	    {
		if (sometimeExecuted(nd, label))
		    return true;
	    }
//	    System.out.println("Activity "+label +" does not execute whithin block "+block.toString());
	    return false;
	}

	else // this is the loop
	{
	    if (block.getChildren().get(0).getNodeType() == NodeType.Mandatory)
		return sometimeExecuted(block.getChildren().get(0).getChildren().get(0), label);
	    else
		return sometimeExecuted(block.getChildren().get(1).getChildren().get(0), label);
	}
    }
    public com.pst.Node getSubSequenceAfter(com.pst.Node n)
    {
	com.pst.Node subseq = new com.pst.Node();
	subseq = n.getParent().clone();
	List<com.pst.Node> toDelete = new ArrayList<com.pst.Node>();
	for (com.pst.Node m : subseq.getChildren())
	{
	    if (getOrder(m, subseq) <= getOrder(n, n.getParent()))
		toDelete.add(m);
	}
	for (com.pst.Node d : toDelete)
	    subseq.deleteChild(d);
	return subseq;
    }
    public void deleteNode(com.pst.Node ch,com.pst.Node parent)
    {
	if (nodes.contains(parent))
	{
	    parent.deleteChild(ch);
	    //this.nodes.remove(ch);
	    List<com.pst.Node> moreNodes = new ArrayList<com.pst.Node>();
	    com.pst.Node currentNode = ch;
	    moreNodes.addAll(currentNode.getChildren());
	    do
	    {
		nodes.remove(currentNode);
		
		for(com.pst.Node nd : currentNode.getChildren())
		{
		    moreNodes.addAll(nd.getChildren());
		    
		}
		if (moreNodes.size() > 0)
		    currentNode = moreNodes.remove(0);
	    }while(moreNodes.size() > 0);
	}
	
    }
    private String depthFirst(com.pst.Node nd, int depth)
    {
	String result = "";
	for (int d= 1; d <= depth;d++)
	    result+="\t";
	result+=nd.toString();
	result+="\n";
	for (com.pst.Node nds :nd.getChildren())
	{
	    String tmp = depthFirst(nds, depth+1);
	    result += tmp;
	}
	return result;
	
    }
    public void print(PrintStream os)
    {
	os.print(depthFirst(root, 0));
	
	// depth first print
	
//	int currentLevel = 1;
//	List<com.pst.Node> currentLevelNodes, nextLevelNodes;
//	com.pst.Node dummy = new com.pst.Node();
//	dummy.setLabel("Dummy");
//	dummy.setID(-110);
//	
//	currentLevelNodes = root.getChildren();
//	do
//	{
//	    
//	}while (currentLevelNodes.size() > 0);
	
    }
    public List<com.pst.Node> getActivities()
    {
	List<com.pst.Node> acts = new ArrayList<com.pst.Node>();
	for (com.pst.Node n : nodes)
	{
	    if (n.getNodeType() == NodeType.Activity)
		acts.add(n);
	}
	return acts;
    }
    public com.pst.Node getNextNode(com.pst.Node n)
    {
	if (n.getParent().getNodeType() != NodeType.Sequence)
	    return null;
	for (com.pst.Node nn : n.getParent().getChildren())
	{
	    if (getOrder(nn, n.getParent()) > getOrder(n, n.getParent()))
		return nn;
	}
	return null;
    }
    public com.pst.Node getPreviousNode(com.pst.Node n)
    {
	if (n.getParent().getNodeType() != NodeType.Sequence)
	    return null;
	com.pst.Node candidate=null;
	for (com.pst.Node nn : n.getParent().getChildren())
	{
	    if (getOrder(nn, n.getParent()) < getOrder(n, n.getParent()))
		candidate = nn;
	}
	return candidate;
    }
    
    public com.pst.Node getPreviousActivityNodeStar(com.pst.Node n)
    {
	com.pst.Node prev = getPreviousNode(n);
	if (prev != null)
	    return prev;
	
	// here get the previous 
	if (n.getParent().getNodeType() != NodeType.Sequence)
	    return null;
	com.pst.Node candidate=null;
	for (com.pst.Node nn : n.getParent().getChildren())
	{
	    if (getOrder(nn, n.getParent()) < getOrder(n, n.getParent()))
		candidate = nn;
	}
	return candidate;
    }
    public com.pst.Node getEnclosingBlock(com.pst.Node n, NodeType nt)
    {
	com.pst.Node parent = n.getParent();
	while (parent != null)
	{
	    if (parent.getNodeType() == nt)
		return parent;
	    parent = parent.getParent();
	}
	return null;
    }
    public com.pst.Node getRepresentativeNodeAtLevel(com.pst.Node ch, int level)
    {

	if (ch.getLevel() < level)
	    return null;
	if (ch.getLevel() == level)
	    return ch;
	return getRepresentativeNodeAtLevel(ch.getParent(), level);
    }
    public List<com.pst.Node> getSuperSequenceOf(com.pst.Node seq)
    {
	// this method is used to return a sequence node in the tree
	// in which the parameter seq represents a subsequence
	List<com.pst.Node> result = new ArrayList<com.pst.Node>();
	for (com.pst.Node n : nodes)
	{
	    if (n.getNodeType() != NodeType.Sequence)
		continue;
	    if (n.getChildren().toString().toLowerCase().contains(seq.getChildren().toString().toLowerCase()))
		result.add(n);
	}
	return result;
    }
    public com.pst.Node getSubSequenceBefore(com.pst.Node n)
    {
	com.pst.Node subseq = new com.pst.Node();
	subseq = n.getParent().clone();
	List<com.pst.Node> toDelete = new ArrayList<com.pst.Node>();
	for (com.pst.Node m : subseq.getChildren())
	{
	    if (getOrder(m, subseq) >= getOrder(n, n.getParent()))
		toDelete.add(m);
	}
	for (com.pst.Node d : toDelete)
	    subseq.deleteChild(d);
	return subseq;
    }
    private void handleSubtree(com.pst.Node currentParent, org.w3c.dom.Node xmlNode)
    
    {
	NodeList children = xmlNode.getChildNodes();
	com.pst.Node pChild;
	if ("SEQUENCE".equals(xmlNode.getNodeName()))
        {
	    pChild = handleSequenceNode(xmlNode);
	    insertNode(pChild, currentParent);
	    for (int i = 0; i < children.getLength(); i++) 
	    {
		org.w3c.dom.Node child = children.item(i);
		handleSubtree(pChild, child);
	    }
        }
        else if ("ACTIVITY".equals(xmlNode.getNodeName()))
        {
            pChild = handleActivityNode(xmlNode);
            insertNode(pChild, currentParent);
        }
        else if ("XCHOICE".equals(xmlNode.getNodeName()))
        {
            pChild = handleXChoiceNode(xmlNode);
            insertNode(pChild, currentParent);
            for (int i = 0; i < children.getLength(); i++) 
            {
        	org.w3c.dom.Node child = children.item(i);
        	handleSubtree(pChild, child);
            }
        }
        else if ("ICHOICE".equals(xmlNode.getNodeName()))
        {
            pChild = handleIChoiceNode(xmlNode);
            insertNode(pChild, currentParent);
            for (int i = 0; i < children.getLength(); i++) 
            {
        	org.w3c.dom.Node child = children.item(i);
        	handleSubtree(pChild, child);
            }
        }
        else if ("PARALLEL".equals(xmlNode.getNodeName()))
        {
            pChild = handleParallelNode(xmlNode);
            insertNode(pChild, currentParent);
            for (int i = 0; i < children.getLength(); i++) 
            {
        	org.w3c.dom.Node child = children.item(i);
        	handleSubtree(pChild, child);
            }
        }
        
 
    }
    public void loadTreeFromFile(String fileName)
    {
	com.pst.Node pRoot=null;
	
	Document doc = parseFile(fileName);
	org.w3c.dom.Node root = doc.getDocumentElement();
	pRoot = new com.pst.Node();
        pRoot.setNodeType(NodeType.Sequence);
        
        this.root = pRoot;
        this.root.setID(getNextID());
        this.root.setLevel(0);
        nodes = new ArrayList<com.pst.Node>();
        nodes.add(pRoot);
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) 
        {
            org.w3c.dom.Node child = children.item(i);
            handleSubtree(this.root, child);
        }
        
        
        // write all child nodes recursively
               

    }
    private com.pst.Node handleSequenceNode(org.w3c.dom.Node nd)
    {
	com.pst.Node result = new com.pst.Node();
	result.setNodeType(NodeType.Sequence);
	
	NamedNodeMap attributes = nd.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            String currentAttributeName = currentAttribute.getNodeName();
            
            if ("condition".equals(currentAttributeName))
            {
                result.setCondition(currentAttribute.getNodeValue());
//                result.setResolved(false);
            }
            if ("label".equals(currentAttributeName)) {
                
                // set node type according to xml node value
                result.setLabel(currentAttribute.getNodeValue());
            }
            
        }
        return result;

    }
    private com.pst.Node handleXChoiceNode(org.w3c.dom.Node nd)
    {
	com.pst.Node result = new com.pst.Node();
	result.setNodeType(NodeType.XChoice);
	return result;

    }
    private com.pst.Node handleIChoiceNode(org.w3c.dom.Node nd)
    {
	com.pst.Node result = new com.pst.Node();
	result.setNodeType(NodeType.IChoice);
	return result;

    }
    private com.pst.Node handleParallelNode(org.w3c.dom.Node nd)
    {
	com.pst.Node result = new com.pst.Node();
	result.setNodeType(NodeType.Parallel);
	return result;

    }
    private com.pst.Node handleActivityNode(org.w3c.dom.Node nd)
    {
	com.pst.Node result = new com.pst.Node();
	result.setNodeType(NodeType.Activity);
	
	NamedNodeMap attributes = nd.getAttributes();

        for (int j = 0; j < attributes.getLength(); j++) {
            Node currentAttribute = attributes.item(j);
            String currentAttributeName = currentAttribute.getNodeName();
            
            if ("condition".equals(currentAttributeName))
            {
                result.setCondition(currentAttribute.getNodeValue());
//                result.setResolved(false);
            }
            if ("label".equals(currentAttributeName)) {
                
                // set node type according to xml node value
                result.setLabel(currentAttribute.getNodeValue());
            }
            
        }
        return result;

    }
    protected Document parseFile(String fileName) {
        //log.debug("Parsing XML file... " + fileName);
        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            //log.error("Wrong XML parser configuration.", e);
            return null;
        }
        File sourceFile = new File(fileName);
        try {
            doc = docBuilder.parse(sourceFile);
        } catch (SAXException e) {
            //log.error("Wrong XML file structure.", e);
            return null;
        } catch (IOException e) {
            //log.error("Could not read source file.", e);
            return null;
        }
        //log.debug("XML file parsed");
        return doc;
    }
    
    public void normalizeTree()
    {
	boolean result = normalizeControlFlow();
	while(result)
	    result = normalizeControlFlow();
    }
    private boolean normalizeControlFlow()
    {
	List<com.pst.Node> toDelete = new ArrayList<com.pst.Node>();
	List<com.pst.Node> clones = new ArrayList<com.pst.Node>();
	for (com.pst.Node n : nodes)
	    clones.add(n.clone());
	for (com.pst.Node  n : clones)
	{
	    com.pst.Node eqNode=null;
	    for (com.pst.Node s :nodes)
		if (s.equals(n))
		    eqNode = s;
	    if (n.getNodeType() == NodeType.Parallel || n.getNodeType() == NodeType.XChoice || n.getNodeType() == NodeType.IChoice )
	    {		if (n.getChildren().size() == 1)
		{
		    for (com.pst.Node c : n.getChildren().get(0).getChildren())
		    {
			insertNode(c, eqNode.getParent(), getOrder(eqNode, eqNode.getParent()));
		    }
		    toDelete.add(eqNode);
		}
		else if (n.getChildren().size() == 0)
		    toDelete.add(eqNode);
	    }
	    else if (n.getNodeType() == NodeType.Sequence)
		if (n.getChildren().size() == 0)
		    toDelete.add(eqNode);
	}
	boolean somethingDeleted = false;
	for (com.pst.Node d : toDelete)
	{
	    deleteNode(d, d.getParent());
	    somethingDeleted = true;
	}
	return somethingDeleted;
    }
}
