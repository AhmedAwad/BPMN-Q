package com.bpmnq;
import java.util.*;

/**
 * This class is used to be an element in the visited list by the find
 * paths algorithm
 *
 * @author Ahmed Awad
 */
final class VisitedListElement
{
    GraphObject elem;
    List<GraphObject> successorsVisited;
    List<GraphObject> successorsAll;

    VisitedListElement()
    {
	successorsAll = new ArrayList<GraphObject>();
	successorsVisited = new ArrayList<GraphObject>();
    }
}
