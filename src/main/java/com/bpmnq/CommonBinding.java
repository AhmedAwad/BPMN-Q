package com.bpmnq;

/**
 * Purpose?
 *
 * @author Ahmed Awad
 */
public final class CommonBinding implements Cloneable
{
    String commonNode = "";
    String succs = "";
    String preds = "";
    String succsCommonBinding = "";
    String predsCommonBinding = "";

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone()
    {
	try
	{
	    return super.clone();
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}
    }
    

}
