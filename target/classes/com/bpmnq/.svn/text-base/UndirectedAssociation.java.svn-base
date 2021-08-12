package com.bpmnq;

import com.bpmnq.GraphObject.GraphObjectType;

public final class UndirectedAssociation extends Association implements Cloneable
{
    public Path path;

    public UndirectedAssociation()
    {
	super();
	path = null;
    }

    public UndirectedAssociation(GraphObject dataObject, Path path)
    {
	if (dataObject.type == GraphObjectType.DATAOBJECT)
	{
	    frmDataObject = new DataObject();
	    frmDataObject.doID = dataObject.getID();
	    frmDataObject.name = dataObject.getName();
	    frmDataObject.setState(dataObject.type2);
	}

	this.path = path;
    }

    public Object clone()
    {
	UndirectedAssociation clone = (UndirectedAssociation)super.clone();
	clone.path = (Path)this.path.clone();
	return clone;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((path == null) ? 0 : path.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof UndirectedAssociation))
	    return false;
	final UndirectedAssociation other = (UndirectedAssociation) obj;
	if (path == null)
	{
	    if (other.path != null)
		return false;
	} else if (!path.equals(other.path))
	    return false;
	return true;
    }
}
