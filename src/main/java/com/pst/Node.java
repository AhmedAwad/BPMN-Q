package com.pst;

import java.util.ArrayList;
import java.util.List;

public final class Node {
	
	private String label;
	private int level;
	private Node parent;
	private List<Node> children;
	private int ID;
	private String condition;
	private String exitCondition;
	private String loopCondition;
	private int childrenStar;
	public Node()
	{
		label = "";
		ID = 0;
		level = 0;
		parent = null;
		children = new ArrayList<Node>();
		condition ="";
		exitCondition = "";
		loopCondition="";
		childrenStar=0;
	}
	public enum NodeType {Sequence, Parallel,Activity,XChoice,IChoice,Loop,Mandatory,Optional}
	private NodeType nodeType;
	public NodeType getNodeType()
	{
		return nodeType;
	}
	public void setNodeType(NodeType nd)
	{
		nodeType = nd;
	}
	public String getLabel()
	{
		return label;
	}
	public void setLabel(String l)
	{
		label = l;
	}
	public String getCondition()
	{
		return condition;
	}
	public void setCondition(String l)
	{
		if (nodeType == NodeType.Sequence )
			condition = l;
	}
	public String getLoopCondition()
	{
		return loopCondition;
	}
	public void setLoopCondition(String l)
	{
		if (nodeType == NodeType.Loop )
			loopCondition = l;
	}
	public String getExitCondition()
	{
		return exitCondition;
	}
	public void setExitCondition(String l)
	{
		if (nodeType == NodeType.Loop )
			exitCondition = l;
	}
	public int getLevel()
	{
		return level;
	}
	public void setParent(Node p)
	{
		parent = p;
	}
	
	public Node getParent()
	{
		return parent;
	}
	public boolean equals(Object other)
	{
		if (!(other instanceof Node))
			return false;
		return ((Node) other).ID == this.ID;
	}
	public int getChildrenStarCount()
	{
	    return childrenStar;
	}
	public void addChild(Node c, int pos)
	{
//		if (children.size() > pos)
//		{
//			List<Node> tmp = new ArrayList<Node>();
//			int s = children.size();
//			for (int i = pos-1; i < s; i++)
//			{
//				tmp.add(children.get(i));
//			}
//			children.removeAll(tmp);
//			children.add(c);
//			for(int i = 0; i < tmp.size();i++)
//			{
//				children.add(tmp.get(i));
//			}
//			tmp = null;
//		}
//		else
//		{
//			addChild(c);
//		}
	    if (children.size() == 0)
		children.add(c);
	    else
		children.add(pos-1, c);
	    c.setParent(this);
	    childrenStar+=1+c.getChildrenStarCount();
	
	}
	public void addChild(Node c)
	{
		this.children.add(c);
		c.setParent(this);
		childrenStar+=1+c.getChildrenStarCount();
	}
	public List<Node> getChildren()
	{
		return children;
	}
	public int getID()
	{
		return ID;
	}
	public void setID(int id)
	{
		ID=id;
	}
	public Node clone()
	{
		Node cln = new Node();
		cln.setNodeType(this.getNodeType());
		cln.setCondition(this.getCondition());
		cln.setExitCondition(this.getExitCondition());
		cln.setLoopCondition(this.getLoopCondition());
		cln.setLabel(this.getLabel());
		cln.setID(this.getID());
		//cln.setParent(this.parent);
		for (Node nd : this.children)
			cln.addChild(nd.clone());
		return cln;
		
	}
	public void deleteChild(Node child)
	{
	    if (this.children.remove(child))
	    {
		childrenStar-= (1+child.getChildren().size());
	    }
	}
	public void clearChildren()
	{
		children.clear();
	}
	public String toString()
	{
	    if (nodeType == NodeType.Activity)
		return "Activity("+label+")";
	    else if (nodeType == NodeType.IChoice)
		return "Inclusive Choice Block";
	    else if (nodeType == NodeType.Loop)
		return "Loop  Block";
	    else if (nodeType == NodeType.Mandatory)
		return "Mandatory part of the loop";
	    else if (nodeType == NodeType.Optional)
		return "Optional part of the loop";
	    else if (nodeType == NodeType.Parallel)
	    	return "Parallel Block";
	    else if (nodeType == NodeType.Sequence)
		return "Sequence Block";
	    else //if (nodeType == NodeType.XChoice)
		return "Exclusive Choice Block";
	}
	public List<Node> copyChildren()
	{
	    List<Node> result = new ArrayList<Node>();
	    for (Node n : children)
	    {
		result.add(n.clone());
	    }
	    return result;
	}
	
	public void setLevel(int l)
	{
	    level = l;
	}
}
