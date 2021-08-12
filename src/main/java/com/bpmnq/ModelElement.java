/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmnq;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nemo
 */
public class ModelElement {

    private String id;
    private String userDefinedID;
    private String splitJoin;
    private String type;
    private String parent;
    private String name;
    private String source;
    private String target;
    private String linkedModel;
    private String similarityMatch;
    private List<String> outgoing;
    private String exclude;
    private String gatewaytype;
    private String activitytype;
    private String eventtype;

    private String temporalProperty;

    public String getTemporalProperty() {
        return temporalProperty;
    }

    public void setTemporalProperty(String temporalProperty) {
        this.temporalProperty = temporalProperty;
    }


    private boolean visited=false;

    public String getSplitJoin() {
        return splitJoin;
    }

    public void setSplitJoin(String splitJoin) {
        this.splitJoin = splitJoin;
    }


    


    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public String getUserDefinedID() {
        return userDefinedID;
    }

    public void setUserDefinedID(String userDefinedID) {
        this.userDefinedID = userDefinedID;
    }

    public ModelElement() {
    }

    ;

//    public ModelElement(String id, String type, String subType, String parrent, String name, String source, String target, String linkedModel, String similarityMatch, String outgoing) {
//        this.id = id;
//        this.type = type;
//        this.subType = subType;
//        this.parent = parrent;
//        this.name = name;
//        this.source = source;
//        this.target = target;
//        this.linkedModel = linkedModel;
//        this.similarityMatch = similarityMatch;
//        this.outgoing = outgoing;
//
//    }
    public String getActivitytype() {
        return activitytype;
    }

    public void setActivitytype(String activitytype) {
        this.activitytype = activitytype;
    }

    public String getEventtype() {
        return eventtype;
    }

    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    public String getGatewaytype() {
        return gatewaytype;
    }

    public void setGatewaytype(String gatewaytype) {
        this.gatewaytype = gatewaytype;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkedModel() {
        return linkedModel;
    }

    public void setLinkedModel(String linkedModel) {
        this.linkedModel = linkedModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOutgoing() {
        return outgoing;
    }

    public void addOutgoing(String outgoing) {
        if (this.outgoing == null) {
            this.outgoing = new ArrayList<String>();
            this.outgoing.add(outgoing);
        } else {
            this.outgoing.add(outgoing);
        }
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getSimilarityMatch() {
        return similarityMatch;
    }

    public void setSimilarityMatch(String similarityMatch) {
        this.similarityMatch = similarityMatch;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ModelElement{" + "id=" + id + "type=" + type + "parent=" + parent + "name=" + name + "source=" + source + "target=" + target + "linkedModel=" + linkedModel + "similarityMatch=" + similarityMatch + "outgoing=" + outgoing + "exclude=" + exclude + "gatewaytype=" + gatewaytype + "activitytype=" + activitytype + "eventtype=" + eventtype + '}';
    }
}
