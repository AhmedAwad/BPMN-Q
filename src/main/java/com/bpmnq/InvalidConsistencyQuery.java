/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bpmnq;

/**
 *
 * @author nemo
 */
public class InvalidConsistencyQuery extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 3187666376263977626L;

    public InvalidConsistencyQuery(){};

    @Override
    public String toString() {
	   return "Invalid ConsystencyQuery! No model has been linked to this query";
        }


}
