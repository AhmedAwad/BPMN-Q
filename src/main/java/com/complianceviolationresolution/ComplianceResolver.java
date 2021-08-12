package com.complianceviolationresolution;

import java.util.ArrayList;
import java.util.List;
import com.pst.*;
import com.pst.Node.NodeType;
import com.bpmnq.compliancechecker.*;


public final class ComplianceResolver
{
    private ProcessStructureTree tree;
    private BusinessContext bc;
    private Planner planner;
    public ComplianceResolver(ProcessStructureTree t, BusinessContext b)
    {
	tree = t;
	bc = b;
	bc.fullLoad();
	planner = new Planner(bc);
    }
    public ProcessStructureTree getTree()
    {
	return tree;
    }
    public BusinessContext getBusinessContext()
    {
	return bc;
    }
    
    public enum ViolationType { none, splittingChoice, DifferentBranches, LackofActivity, InverseOrder}
    
    public ViolationType getViolationTypeOfLeadsTo(Node src, Node dst)
    {
	Node lcp = tree.leastCommonParent(src, dst);
	if (tree.getOccurrencesOfLabel(dst.getLabel()).size() == 0)
	    return ViolationType.LackofActivity;
//	if (lcp == null)
//	    return ViolationType.LackofActivity;
	
	if (lcp.getNodeType() == NodeType.Parallel || lcp.getNodeType() == NodeType.XChoice || lcp.getNodeType() == NodeType.IChoice)
	    return ViolationType.DifferentBranches;
	else if (lcp.getNodeType() == NodeType.Sequence)
	{
	    if (tree.getOrder(dst, lcp) < tree.getOrder(src, lcp))
		return ViolationType.InverseOrder;
	    else
	    {
		List<Node> path = tree.findPath(dst, lcp);
		for (Node nd : path)
		    if (nd.getNodeType() == NodeType.XChoice || nd.getNodeType() == NodeType.IChoice)
			if (!tree.alwaysExecuted(nd, dst.getLabel()))
			    return ViolationType.splittingChoice;
			
			    
		return ViolationType.none;
	    }
	}
	else // the lcp is loop
	{
	    // loop node must have exactly two children the mandatory and optional ones
	    return ViolationType.splittingChoice;
	}
    }
    public ProcessStructureTree resolveSplittingChoiceViolation(Node src, Node dst)
    {
	ProcessStructureTree compTree = tree;//.clone();
	Node lcp = compTree.leastCommonParent(src, dst);
	Node choiceSplit=null;
	if (lcp.getNodeType()== NodeType.Sequence && compTree.getOrder(src, lcp) < compTree.getOrder(dst, lcp))
	{
	    List<Node> path = compTree.findPath(dst, lcp);
	    for (int i = path.size()-1; i >= 0; i--)
	    {
		if (path.get(i).getNodeType() == NodeType.XChoice && !compTree.alwaysExecuted(path.get(i), dst.getLabel()))
		{
		    choiceSplit = path.get(i);
		    break;
		}
		
	    }
	    if (choiceSplit == null)
	    	return compTree;
	    else
	    {
		// 1 Check for contradicting activities
		String contra = bc.getContradictingActivities(dst.getLabel().toLowerCase());
		List<Node> children = choiceSplit.getChildren();
		List<Node> toDelete = new ArrayList<Node>();
		if (contra != null)
		{
		    String[] acts = contra.split(",");
		    
		    for (Node ch: children)
			for(int c=0;c < acts.length;c++)
			{
			    if (compTree.sometimeExecuted(ch, acts[c]))
			    // we have to remove that branch from the tree
			    {
				//
				toDelete.add(ch);
			    }
			}
		   
		}
		// now insert missing activity on the remaining branches
		
		
		children = choiceSplit.getChildren();
//		toDelete.clear();
		for (Node ch: children)
		{
		    if (!compTree.alwaysExecuted(ch, dst.getLabel()))
		    {
			// this part needs to be replaced with a planner output
			List<List<String>> initState = generateCumulativeEffect(choiceSplit, null);
			List<String> finalState = new ArrayList<String>();
			String branchCondition = ch.getCondition();
			ProcessStructureTree pTree = null;
			if (!(branchCondition == null || branchCondition.length() == 0))
			{
			    String cnd[] = branchCondition.split("and");
			    for (String c : cnd)
				finalState.add(c.trim());
			    
			}
			finalState.add("EXECUTED "+dst.getLabel().replace(" " , "-"));
			for (List<String> ini : initState)
		    	{
		    	    pTree = planner.findPlan(ini, finalState);
		    	    if (pTree == null)
		    	    {
		    		toDelete.add(ch);
		    	    }
		    	}
			// here we need to merge the tree with the existing one
			if (pTree != null)
			{
			    List<Node> toAdd = new ArrayList<Node>();
			    for (Node n : pTree.getRoot().getChildren())
			    {    
				boolean found = false;
				for(Node m : ch.getChildren())
				{
				    if (m.getLabel().equalsIgnoreCase(n.getLabel()))
				    {
					found = true;
					break;
				    }

				}
				if (!found)
				    toAdd.add(n);
			    }
			    for (Node x:toAdd)
				compTree.insertNode(x, ch);
			}
//			Node newOccurrence = dst.clone();
//			compTree.insertNode(newOccurrence, ch);
		    }
		}
//		compTree.normalizeTree();
		 for (Node n : toDelete)
			compTree.deleteNode(n, choiceSplit);
		return compTree;
		    
	    }
		
	    
	}
	else
	    return compTree;
    }
    public ProcessStructureTree resolveDifferentBranchesViolation(Node src, Node dst)
    {
        ProcessStructureTree compTree = tree;//.clone();
        Node lcp = compTree.leastCommonParent(src, dst);
        if (lcp.getNodeType() == NodeType.Parallel)
            return resolveParallelBranchesViolation(src,dst,lcp);
        else if (lcp.getNodeType() == NodeType.XChoice)
            return resolveLackOfActivityViolation(src,dst);
        return null;
    }
    public ProcessStructureTree resolveInverseOrderViolation(Node src, Node dst)
        {
    	Node prev = tree.getPreviousNode(dst);
    	if (prev == null)
    	    prev = dst.getParent().getParent();
    	ProcessStructureTree pTree=null;
    	List<List<String>> initState = generateCumulativeEffect(prev, null);
    	List<String> finalState = new ArrayList<String>();
    	finalState.add("EXECUTED "+src.getLabel().replace(" ", "-"));
    	finalState.add("!EXECUTED "+dst.getLabel().replace(" ", "-"));
    	for (List<String> ini : initState)
    	{
    	    pTree = planner.findPlan(ini, finalState);
    	    if (pTree == null)
    		break;
    	}
    	while (pTree == null )
    	{
    	    // I have reached the target start point and no possible plan
    	    // then return false and then conclude that no possible resolution
    	    if  (prev == null)
    	    {
    		//prev = prev.getParent().getParent();
    		return null;
    	    }
    	    if (prev.equals(src))
    	    {
    		return null;
    	    }
    	    // try one step before
    	    prev = tree.getPreviousNode(prev);
    	    
    	    initState = generateCumulativeEffect(prev, null);
    	    for (List<String> ini : initState)
    	    {
    		pTree = planner.findPlan(ini,finalState);
    		if (pTree == null)
    		    break;
    	    }
    	}
    	if (pTree != null)
    	{
    	    // can we move the dst and all its subsequents to the point after src?
    	    Node subSeq = tree.getSubSequenceAfter(dst);
    	    subSeq.addChild(dst.clone(), 1);
    	    Node subSeq2 = tree.getSubSequenceAfter(src);
    	    subSeq2.addChild(src.clone(), 1);
    	    
    	    List<Node> toDel = new ArrayList<Node>();
    	    for (Node o : subSeq2.getChildren())
    		if (subSeq.getChildren().contains(o))
    		    toDel.add(o);
    	    
    	    for (Node d : toDel)
    		subSeq.deleteChild(d);
    	    
    	    // we have to remove them from the tree
    	    for(Node n : subSeq.getChildren())
    		tree.deleteNode(n, dst.getParent());
    	    finalState.clear();
    	    for (Node n : subSeq.getChildren())
    	    {
    		List<Node> act = getActivities(n);
    		for(Node a :act)
    		    finalState.add("EXECUTED "+a.getLabel().replace(" ", "-"));
    	    }
    	    initState = generateCumulativeEffect(src, null);
    	    for (List<String> ini : initState)
    	    {
    		pTree = planner.findPlan(ini, finalState);
    		if (pTree == null)
    		    return null;
    	    }
    	    if (pTree != null)
    	    {
    		Node next = tree.getNextNode(src);
    		int point = tree.getOrder(src, src.getParent());
    		if (next == null)
    		{
    		    for (Node n:subSeq.getChildren())
    			tree.insertNode(n, src.getParent(), ++point);
    		}
    		else
    		{
    		    Node pNode = new Node();
    		    Node seq1;
    		    seq1 = tree.getSubSequenceAfter(src);
    		    pNode.setNodeType(NodeType.Parallel);
    		    tree.insertNode(pNode, src.getParent(), tree.getOrder(src,src.getParent())+1);
    		    for(Node n : seq1.getChildren())
    			tree.deleteNode(n, src.getParent());
    		    tree.insertNode(seq1, pNode);
    		    tree.insertNode(subSeq, pNode);
    		    return tree;
    		}
    	    }
    	}
    	return null;
    //	    boolean result = handlePreviousContradictingNode(tree.getRoot().getChildren().get(0), src, dst);
    //	    if (!result)
    //		return null;
    //	    return tree;
        }
    public ProcessStructureTree resolveLackOfActivityViolation(Node src, Node dst)
        {
    	//TODO: Implement this
    
    	List<Node> contradictingNodes = new ArrayList<Node>();
    	List<String> contradictingActs = new ArrayList<String>();
    //	for (Node n : planActs)
    //	{
    	    String contraaa = bc.getContradictingActivities(dst.getLabel().toLowerCase());
    	    if (contraaa != null)
    		for (String s :contraaa.split(","))
    		{
    		    if (!contradictingActs.contains(s))
    			contradictingActs.add(s);
    		}
    //	}
    	// check the tree for occurrences of contradicting nodes
    	List<Node> possibleContradictingNodes;
    	for (String s : contradictingActs)
    	{
    	    possibleContradictingNodes = tree.getOccurrencesOfLabel(s);
    	    for (Node n : possibleContradictingNodes)
    	    {
    		Node lcp = tree.leastCommonParent(src, n);
    		if (lcp.getNodeType()!= NodeType.XChoice)
    		    contradictingNodes.add(n);
    	    }
    	}
    	if (contradictingNodes.size() != 0)
    	{
    	    List<Node> toRemove = new ArrayList<Node>();
    	    //toRemove.addAll(contradictingNodes);
    	    for (Node contra : contradictingNodes)
    	    {
    		if (toRemove.contains(contra))
    		    continue;
    		toRemove.add(contra);
    		// determine the position of the contra regarding the src activity
    		Node lcp = tree.leastCommonParent(src, contra);
    		
    		
    		// if it not a choice block then we have to handle it
    		if (lcp.getNodeType() == NodeType.Sequence)
    		{    
    		    if (tree.getOrder(contra, lcp) < tree.getOrder(src, lcp))
    		    {
    			boolean ok = handlePreviousContradictingNode(tree.getRoot().getChildren().get(0),src, contra);
    			if (!ok)
    			    return null;
    		    }
    		    else
    		    {
    			boolean ok = handleNextContradictingNode(src,dst, contra);
    			if (!ok)
    			    return null;
    		    }
    		}
    		else if (lcp.getNodeType() == NodeType.Parallel)
    		{
    		    boolean ok = handleParallelContradictingNode(lcp,src,contra);
    		    if (!ok)
    			    return null;
    		}
    		else if (lcp.getNodeType() == NodeType.Loop)
    		{
    		    // we have to investigate that case
    		}
    		else if (lcp.equals(src)) // if the user asks for forcing order between contradicting nodes
    		    return null;
    //		toRemove.add(contra);
    //		Node prev = tree.getPreviousNode(contra);
    //		if (prev == null)
    //		    prev = contra.getParent();
    //		ProcessStructureTree tmpTree;
    //		while (!prev.equals(src) && !prev.equals(tree.getRoot()) )
    //		{
    ////		    if (prev == null) // we need to go one level up
    ////			prev = 
    //		    tmpTree = planner.findPlan(generateCumulativeEffect(prev, null), goalState);
    //		    if (tmpTree == null)
    //		    {
    //			toRemove.add(prev);
    //			prev = tree.getPreviousNode(prev);
    //		    }
    //		    else
    //			break; // we have found the pre cut point
    //		     
    //		     
    //		}
    	    }
    	}
    	else
    	{
    	    // no contradictions
    	    // we insert a parallel block with the activities
    	    
    	    List<List<String>> initState = generateCumulativeEffect(src, null);
    	    List<String> goalState = new ArrayList<String>();
    	    goalState.add("EXECUTED "+ dst.getLabel().replace(" ", "-"));
    	    ProcessStructureTree planTree=null ;
    	    for (List<String> ini : initState)
    	    {
    		planTree = planner.findPlan(ini, goalState);
    		if (planTree == null)
    		    break;
    	    }
    	    if (planTree == null)
    		return null;
    	    // check whether the original tree has the generated plan as a subsequence
    	    // of course, exclude the last node
    	    Node subSeq = planTree.getSubSequenceBefore(planTree.getRoot().getChildren().get(planTree.getRoot().getChildren().size()-1));
    	    List<Node> superSeq = tree.getSuperSequenceOf(subSeq);
    	    
    	    if (superSeq.size() > 0)
    	    {
    		for (Node ss : superSeq)
    		{
    		    Node lcp = tree.leastCommonParent(src, ss);
    		    if (lcp == null)
    			continue;
    		    if (lcp.getNodeType() != NodeType.Sequence)
    			continue;
    		    if (tree.getOrder(ss, lcp) < tree.getOrder(src, lcp))
    			continue;
    		    Node inserted = planTree.getRoot().getChildren().get(planTree.getRoot().getChildren().size()-1);
    		    tree.insertNode(inserted, ss, ss.getChildren().size()+1);
    		}
    	    }
    	    else
    	    {
    		Node next = tree.getNextNode(src);
    
    		if (next == null)
    		{
    		    // just attach it to the end of the sequence
    		    for (Node n : planTree.getRoot().getChildren())
    		    {
    			tree.insertNode(n.clone(), src.getParent(), src.getParent().getChildren().size()+1);
    		    }
    		}
    		else
    		{
    		    Node parallel = new Node();
    		    parallel.setNodeType(NodeType.Parallel);
    		    Node sequence = new Node();
    		    sequence.setNodeType(NodeType.Sequence);
    		    sequence.setCondition("true");
    		    tree.insertNode(parallel, src.getParent(), tree.getOrder(src, src.getParent())+1);
    		    tree.insertNode(sequence, parallel);
    		    next =tree.getNextNode(parallel); 
    		    while( next != null)
    		    {
    			tree.deleteNode(next, parallel.getParent());
    			tree.insertNode(next, sequence, sequence.getChildren().size()+1);
    			next =tree.getNextNode(parallel);
    		    }
    
    		    tree.insertNode(planTree.getRoot(), parallel);
    		    //tree.deleteNode(next, src.getParent());
    
    		}
    	    }
    	}
    //	tree.normalizeTree();
    	return tree;
        }
    private List<Node> getActivities(Node nd)
    {
	List<Node> result = new ArrayList<Node>();
	if (nd.getNodeType() == NodeType.Activity)
	    result.add(nd);
	else if (nd.getNodeType() == NodeType.Sequence || nd.getNodeType() == NodeType.Parallel)
	{
	    for (Node nn: nd.getChildren())
		result.addAll(getActivities(nn));
	}
	else if (nd.getNodeType() == NodeType.IChoice || nd.getNodeType() == NodeType.XChoice)
	{
	    for (Node nn: nd.getChildren())
	    {
		result.addAll(getActivities(nn));
		break;
	    }
	}
	else if (nd.getNodeType() == NodeType.Loop)
	{
	    for (Node nn: nd.getChildren())
	    {
		if (nn.getNodeType() == NodeType.Mandatory)
		{
		    result.addAll(getActivities(nn.getChildren().get(0)));
		    break;
		}
	    }
	}
	
	return result;
    }
    

    private List<List<String>> generateCumulativeEffect(Node till, Node exclude)
    {
	List<List<Node>> preActs = new ArrayList<List<Node>>();
	List<Node> l = new ArrayList<Node>();
	preActs.add(l);
	preActs = getPreActs(tree.getRoot(), till, preActs);

	List<String> factBase = new ArrayList<String>();
	List<List<String>> factBaseL = new ArrayList<List<String>>();
	for (List<Node> pre :preActs)
	{
	    factBase.clear();
	    for (String s : bc.getAllDataObjectStates())
	    {
		if (s.contains("initial"))
		    factBase.add(s);
	    }
	    for (int i = 0 ; i < pre.size();i++)
	    {
		factBase = removeContradictingPrecondition(pre.get(i),factBase);
		factBase = applyEffect(pre.get(i), factBase);

	    }
	    factBaseL.add(factBase);
	}

	return factBaseL;
    }

    private List<List<Node>> getPreActs(Node seq, Node n, List<List<Node>> preActs)
    {
	List<List<Node>> preActsL = new ArrayList<List<Node>>();
	List<List<Node>> visited = new ArrayList<List<Node>>();
	List<Node> currentList;
	// we have to adjust the level of comparison
	Node compSeq;
	if (tree.findPath(n, seq) != null)
	    compSeq = seq;
	else
	    compSeq = n.getParent();
	boolean firstTime = false;
	while(preActs.size() > 0)
	{
	    currentList = preActs.remove(0);
	    firstTime = true;
	    visited.add(currentList);
	if (n != null)
	{
	    for (Node nd: seq.getChildren())
	    {
		if (currentList.size() > 0 && firstTime)
		{
		    Node last = currentList.get(currentList.size()-1);
		    if (tree.getOrder(last, seq) > tree.getOrder(nd, seq))
			continue;
		    if (tree.getOrder(last, compSeq) == tree.getOrder(nd, compSeq) && nd.getNodeType() == NodeType.XChoice)
			continue;
		    firstTime = false;
		}
		if (tree.getOrder(nd, compSeq) < tree.getOrder(n, compSeq))
		{
		    if (nd.getNodeType() != NodeType.XChoice)
		    {
			for (Node v : getActivities(nd))
			    if (!currentList.contains(v))
				currentList.add(v);
		    }
		    else
		    {
			List<List<Node>> preActs2 = new ArrayList<List<Node>>();
			List<Node> z = new ArrayList<Node>();
			for (Node nx : currentList)
			    z.add(nx.clone());
			
			for (Node m : nd.getChildren())
			{
			    preActs2.add(z);
//			    preActs2.addAll(getActivities(m));
			    for (List<Node> nl :getPreActs(m, n, preActs2))
				if (!visited.contains(nl))
				{
				    preActs.add(nl);
				    visited.add(nl);
				}
			    
			    z = new ArrayList<Node>();
				for (Node nx : currentList)
				    z.add(nx.clone());
			    
			}
//			if (nd.getChildren().size() == 1)
//			    preActs.add(currentList);
//			currentList = preActs.remove(0);
			break;
		    }
		}
		else if (tree.getOrder(nd, compSeq) == tree.getOrder(n, compSeq))
		{
		    // if nd is a block then we need to handle it
		    if (!nd.equals(n))
		    {
			List<Node> path = tree.findPath(n, nd);
			List<List<Node>> preActs2 = new ArrayList<List<Node>>();
			preActs2.add(currentList);
			preActs.addAll(getPreActs(path.get(1), n, preActs2));
			
		    }
		    else
		    {
			if (nd.getNodeType() == NodeType.Activity)
			    currentList.add(nd);
			preActsL.add(currentList);
		    }

		}
	    }
	    if (!preActsL.contains(currentList))
		preActsL.add(currentList);
	}
	}
	// clean up covered paths
	List<List<Node>> toDel = new ArrayList<List<Node>>();
	for (List<Node> outer : preActsL)
	    for(List<Node> inner :preActsL)
		if (outer.toString().replace("[", "").replace("]", "").contains(inner.toString().replace("[", "").replace("]", "")) )
			if (!outer.toString().equals(inner.toString()))
			    toDel.add(inner);
	preActsL.removeAll(toDel);
	return preActsL;
    }
    private List<String> removeContradictingPrecondition(Node node, List<String> factBase)
    {
	String pre = bc.getActivityDataPreCondition(node.getLabel());
	
	// Start parsing
	if (pre.contains(","))// check for non deterministic effect
	{
	    String disConds[] = pre.split(",");
	    // take arbitrarily one of them
	    //String conConds[] = disConds[0].split("and");
	    for(String sss : disConds)
	    {
		String conConds[] = sss.split("and");
		for (String s : conConds)
		{
		    
		    // we have to remove the contradicting effects
		    String conta = bc.getContradictingState(s.trim());
		    if (conta != null)
		    {
			for (String os: conta.split(","))
			{
			    factBase.remove(os.trim());
			}
		    }
		}
	    }
	}
	else
	{
	    String conConds[] = pre.split("and");
	    for (String s : conConds)
	    {
//		 we have to remove the contradicting effects
		String conta = bc.getContradictingState(s.trim());
		if (conta != null)
		{
		    for (String os: conta.split(","))
		    {
			factBase.remove(os.trim());
		    }
		}
	    }
	}
	return factBase;
    }
    private List<String> applyEffect(Node node, List<String> factBase)
    {
	String postP = bc.getActivityDataPostConditionPositive(node.getLabel());
	String postN = bc.getActivityDataPostConditionNegative(node.getLabel());
	// Start parsing
	if (postP != null)
	if (postP.contains(","))// check for non deterministic effect
	{
	    String disConds[] = postP.split(",");
	    // take arbitrarily one of them
	    //String conConds[] = disConds[0].split("and");
	    for(String sss : disConds)
	    {
		String conConds[] = sss.split("and");
		for (String s : conConds)
		{
		    if (!factBase.contains(s.trim()))
			factBase.add(s.trim());
//		    // we have to remove the contradicting effects
//		    String conta = bc.getContradictingState(s.trim());
//		    if (conta != null)
//		    {
//			for (String os: conta.split(","))
//			{
//			    factBase.remove(os.trim());
//			}
//		    }
		}
	    }
	}
	else
	{
	    String conConds[] = postP.split("and");
	    for (String s : conConds)
	    
		factBase.add(s);
	}
	if (postN != null)
	if (postN.contains(","))// check for non deterministic effect
	{
	    String disConds[] = postN.split(",");
	    // take arbitrarily one of them
	    for(int i = 0; i < disConds.length;i++)
	    {
		String conConds[] = disConds[i].split("and");
		for (String s : conConds)
		    factBase.remove(s);
	    }
	}
	else
	{
	    String conConds[] = postN.split("and");
	    for (String s : conConds)
	    
		factBase.remove(s.trim());
	}
	factBase.add("EXECUTED "+node.getLabel().replace(" ", "-"));
	return factBase;
    }
    private ProcessStructureTree resolveParallelBranchesViolation(Node src, Node dst,Node lcp)
    {
	//TODO: Implement this
	Node pSource=null, pDestination=null;
	for (Node nd : lcp.getChildren())
	{
	    if (tree.findPath(src, nd) != null)
	    {
		pSource = nd;
	    }
	    
	    if (tree.findPath(dst, nd) != null)
	    {
		pDestination = nd;
	    }
	}
	if (tree.getOrder(src, pSource) <= tree.getOrder(dst, pDestination))
	{
	    // we move the source and all its preceding nodes out of the parallel block
	    List<Node> movebefore = new ArrayList<Node>();
	    for (Node nd : pSource.getChildren())
	    {
		if (tree.getOrder(nd, pSource) <= tree.getOrder(src, pSource))
		{
		    movebefore.add(nd);
		    
		}
	    }
	    // remove them from the current pSource
	    for (Node nd : movebefore)
	    {
		tree.deleteNode(nd, pSource);
		tree.insertNode(nd, lcp.getParent(), tree.getOrder(lcp, lcp.getParent())-1);
	    }
	}
	else
	{
	    List<Node> moveafter = new ArrayList<Node>();
	    for (Node nd : pDestination.getChildren())
	    {
		if (tree.getOrder(nd, pDestination) >= tree.getOrder(dst, pDestination))
		{
		    moveafter.add(nd);
		    
		}
	    }
	    // remove them from the current pSource
	    for (Node nd : moveafter)
	    {
		tree.deleteNode(nd, pDestination);
		tree.insertNode(nd, lcp.getParent(), tree.getOrder(lcp, lcp.getParent())+1);
	    }
	}
//	tree.normalizeTree();
	return tree;
	
    }
    private boolean handleParallelContradictingNode(Node parallel, Node src, Node contra)
    {
	Node seq = parallel.getParent();
	Node seqSrc=null,seqContra=null;
	for (Node n : parallel.getChildren())
	{
	    if (tree.sometimeExecuted(n, src.getLabel()))
	    {
		seqSrc = n;
	    }
	    if (tree.sometimeExecuted(n, contra.getLabel()))
	    {
		seqContra = n;
	    }
	}
	if (seqSrc != null)
	{
	    tree.deleteNode(seqSrc, seqSrc.getParent());
	    for (Node m : seqSrc.getChildren())
		tree.insertNode(m, seq, tree.getOrder(parallel, seq)-1);
	}
	if (seqContra != null)
	{
	    int pos = tree.getOrder(parallel, seq);
	    tree.deleteNode(seqContra, seqContra.getParent());
	    for (Node m : seqContra.getChildren())
		tree.insertNode(m, seq,++pos);
	}
	
	return true;
	
	
	
    }
    
    private boolean handleNextContradictingNode(Node src, Node dst, Node contra)
    {
	ViolationType vt = getViolationTypeOfLeadsTo(src, contra);
	Node choice ;
	if (vt == ViolationType.splittingChoice)
	{
	    choice = tree.getEnclosingBlock(contra,NodeType.XChoice);
	    if (choice == null)
		return false;
	}
	else //if (vt == ViolationType.none)
	{
	    choice = tree.getPreviousNode(contra);
	}
	return insertChoiceBlockforContradictingActivity(choice, src, dst);
    }
    private boolean insertChoiceBlockforContradictingActivity(Node choice, Node src, Node dst)
    {
	List<List<String>> cummEffect = generateCumulativeEffect(choice, null);
	List<String> goalState = new ArrayList<String>();
	goalState.add("EXECUTED "+ dst.getLabel().replace(" ", "-"));
	ProcessStructureTree pTree=null;
	
	for (List<String> ini : cummEffect)
	{
	    pTree = planner.findPlan(ini, goalState);
	    if (pTree == null)
		break;
	}
	
	while (pTree == null )
	{
	    // I have reached the target start point and no possible plan
	    // then return false and then conclude that no possible resolution
	    if (choice.equals(src))
	    {
		return false;
	    }
	    // try one step before
	    Node choicebefore = choice;
	    choice = tree.getPreviousNode(choice);
	    if  (choice == null)
	    {
		choice = choicebefore.getParent().getParent();
	    }
	    cummEffect = generateCumulativeEffect(choice, null);
	    for (List<String> ini : cummEffect)
	    {
		pTree = planner.findPlan(ini, goalState);
		if (pTree == null)
		    break;
	    }
	}
	if (pTree != null)
	{
	    // we have to insert the found plan in a choice block
	    if (choice.getNodeType() == NodeType.XChoice)
	    {
		// we have to insert another choice branch
		Node seq = pTree.getRoot().clone();
		tree.insertNode(seq, choice);
	    }
	    else
	    {
		// we have to insert a choice block immediately after that point
		Node nChoice = new Node();
		nChoice.setNodeType(NodeType.XChoice);
		Node seq1, seq2;
		seq1 = pTree.getRoot().clone();
		seq2 = new Node();
		seq2.setNodeType(NodeType.Sequence);
		List<Node> insertSeq2 = new ArrayList<Node>();
		for (Node n : choice.getParent().getChildren())
		{
		    if (tree.getOrder(n, choice.getParent()) > tree.getOrder(choice, choice.getParent()))
		    {
			insertSeq2.add(n);
		    }
		}
		tree.insertNode(nChoice, choice.getParent(), tree.getOrder(choice, choice.getParent())+1);
		tree.insertNode(seq1, nChoice);
		tree.insertNode(seq2, nChoice);
		
		
		for (Node n : insertSeq2)
		{
		    tree.deleteNode(n, choice.getParent());
		    tree.insertNode(n, seq2, seq2.getChildren().size()+1);
		}
	    }
	    return true;
	}
	    return false;
    }
    private boolean handlePreviousContradictingNode(Node src,Node dst, Node contra)
    {
	Node prev = tree.getPreviousNode(contra);
	if (prev == null)
	    prev = contra.getParent().getParent();
	ProcessStructureTree pTree=null;
	List<List<String>> initState = generateCumulativeEffect(prev, null);
	List<String> finalState = new ArrayList<String>();
	finalState.add("EXECUTED "+dst.getLabel().replace(" ", "-"));
	finalState.add("!EXECUTED "+contra.getLabel().replace(" ", "-"));
	for (List<String> ini : initState)
	{
	    pTree = planner.findPlan(ini, finalState);
	    if (pTree == null)
		break;
	}
	while (pTree == null )
	{
	    // I have reached the target start point and no possible plan
	    // then return false and then conclude that no possible resolution
	    if  (prev == null)
	    {
		//prev = prev.getParent().getParent();
		return false;
	    }
	    if (prev.equals(src))
	    {
		return false;
	    }
	    // try one step before
	    prev = tree.getPreviousNode(prev);
	    
	    initState = generateCumulativeEffect(prev, null);
	    for (List<String> ini : initState)
	    {
		pTree = planner.findPlan(ini,finalState);
		if (pTree == null)
		    break;
	    }
	}
	if (pTree != null)
	{
	    // we have to insert the found plan in a parallel block
	    Node representative = tree.getRepresentativeNodeAtLevel(dst,prev.getLevel());
	    
	    Node pNode = new Node();
	    Node seq1,seq2;
	    
	    seq1 = new Node();
	    seq1.setNodeType(NodeType.Sequence);
	    seq2 = tree.getSubSequenceAfter(prev);
	    //seq2.setNodeType(NodeType.Sequence);
	    pNode.setNodeType(NodeType.Parallel);
	    tree.insertNode(pNode, prev.getParent(), tree.getOrder(prev,prev.getParent())+1);
	    tree.insertNode(seq1, pNode);
	    
	    for (Node mm : seq2.getChildren())
		tree.deleteNode(mm, prev.getParent());
	    
	    tree.insertNode(seq2, pNode);
	    
	    List<Node> toDelete = new ArrayList<Node>();
	    for (Node mm : seq2.getChildren())
		if (pTree.sometimeExecuted(pTree.getRoot(), mm.getLabel()))
		    toDelete.add(mm);
	    
	    for (Node n : toDelete)
		tree.deleteNode(n, seq2);
	    
	    for (Node n:pTree.getRoot().getChildren())
	    {
		if (n.getLabel().equals(prev.getLabel()))
		    continue;
		if (n.getNodeType() == NodeType.Activity)
		{
		    if (tree.sometimeExecuted(representative, n.getLabel()))
		    {
			tree.deleteNode(representative, representative.getParent());
			tree.insertNode(representative, seq1, seq1.getChildren().size()+1);
			break;
		    }
		    else
		    {
			tree.insertNode(n.clone(), seq1, seq1.getChildren().size()+1);
		    }
		}
		else // this is a parallel block child
		{
		    for (Node seq : n.getChildren())
			for (Node act : seq.getChildren())
			{
			    if (tree.sometimeExecuted(representative, act.getLabel()))
			    {
				tree.deleteNode(representative, representative.getParent());
				tree.insertNode(representative, seq1, seq1.getChildren().size()+1);
				break;
			    }
			    else
			    {
				tree.insertNode(act.clone(), seq1, seq1.getChildren().size()+1);
			    }
			}
		}
		
	    }
	    // Now add those activities that are not in plan to the other parallel branch
	    for(Node nn : pTree.getActivities())
	    {
		Node toDel=null;
		for (Node mm : seq2.getChildren())
		{
		    if (mm.equals(nn))
		    {
			toDel = mm;
		    }
		}
		if (toDel != null)
		    tree.deleteNode(toDel, seq2);
	    }
	    return true;
	}
	return false;
    }
}
