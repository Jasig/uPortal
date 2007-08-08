/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal.groups;

 import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for creating <code>ReferencePersonNameFinders</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferencePersonNameFinderFactory implements IEntityNameFinderFactory {
    private static final Log log = LogFactory.getLog(ReferencePersonNameFinderFactory.class);
    
/**
 * ReferencePersonNameFinderFactory constructor comment.
 */
public ReferencePersonNameFinderFactory() {
        super();
}
/**
 * Return a finder instance.
 * @return org.jasig.portal.groups.IEntityNameFinder
 * @exception org.jasig.portal.groups.GroupsException
 */
public IEntityNameFinder newFinder() throws GroupsException
{
    try
        { return ReferencePersonNameFinder.singleton(); }
    catch ( SQLException sqle )
    {
        log.error(sqle.getMessage(), sqle);
        throw new GroupsException(sqle);
    }
}
}
