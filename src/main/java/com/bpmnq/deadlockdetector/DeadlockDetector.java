
package com.bpmnq.deadlockdetector;
import com.bpmnq.*;
import com.bpmnq.GraphObject.GraphObjectType;
public class DeadlockDetector 
{
	public static QueryGraph checkAndJoinLoopDeadlock()
	{
		QueryGraph qry = new QueryGraph();
		GraphObject andJoin,activityX,activityY;
		andJoin = new GraphObject();
		//x1.name="Members warned?";
		andJoin.type = GraphObjectType.GATEWAY;
		andJoin.type2 = "AND JOIN";
		//x1.id = 14;
		
		activityX = new GraphObject();
		//x2.id = 22;
		activityX.setName("@X");
		activityX.type = GraphObjectType.ACTIVITY;
		activityX.type2 = "";
		
		activityY = new GraphObject();
		activityY.setName("@Y");
		activityY.type = GraphObjectType.ACTIVITY;
		activityY.type2="";
		
		
		qry.add(andJoin);
		qry.add(activityX);
		qry.add(activityY);
		
		qry.addEdge(activityX, andJoin);
		
		qry.addEdge(activityY, andJoin);
		
		qry.add(new Path(andJoin, activityX,""));
		qry.addNegativePath(andJoin, activityY);
		
		return qry;
	}
	public static QueryGraph checkIndirectXORSplitANDJoinDeadlock()
	{
		QueryGraph qry = new QueryGraph();
		GraphObject xorSplit, andSplit,andJoin,activityX;
		
		andSplit = new GraphObject();
		//x1.name="Members warned?";
		andSplit.type = GraphObjectType.GATEWAY;
		andSplit.type2 = "AND SPLIT";
		
		xorSplit = new GraphObject();
		//x1.name="Members warned?";
		xorSplit.type = GraphObjectType.GATEWAY;
		xorSplit.type2 = "XOR SPLIT";
		
		andJoin = new GraphObject();
		//x1.name="Members warned?";
		andJoin.type = GraphObjectType.GATEWAY;
		andJoin.type2 = "AND JOIN";
		//x1.id = 14;
		
		activityX = new GraphObject();
		//x2.id = 22;
		activityX.setName("@X");
		activityX.type = GraphObjectType.ACTIVITY;
		activityX.type2 = "";
		
		qry.add(andJoin);
		qry.add(andSplit);
		qry.add(xorSplit);
		qry.add(activityX);
		
		qry.addEdge(activityX, andJoin);
			
		qry.add(new Path(andSplit, andJoin, ""));
		qry.add(new Path(xorSplit, activityX, ""));
		qry.addNegativePath(andSplit, activityX);
		
		return qry;
	}
	
	public static QueryGraph checkDirectXORSplitANDJoinDeadlock()
	{
		QueryGraph qry = new QueryGraph();
		GraphObject xorSplit, andJoin;
		
		xorSplit = new GraphObject();
		//x1.name="Members warned?";
		xorSplit.type = GraphObjectType.GATEWAY;
		xorSplit.type2 = "XOR SPLIT";
		
		andJoin = new GraphObject();
		//x1.name="Members warned?";
		andJoin.type = GraphObjectType.GATEWAY;
		andJoin.type2 = "AND JOIN";
		//x1.id = 14;
		
		qry.add(andJoin);
		
		qry.add(xorSplit);
		
		qry.add(new Path(xorSplit, andJoin, ""));
				
		return qry;
	}
}
