/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This class takes care of initiating channel rendering thread, 
 * monitoring it for timeouts, retreiving cache, and returning 
 * rendering results and status.  It is used by ChannelRenderer.
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelCacheEntry {
    protected Object buffer;
    protected final Object validity;
    public ChannelCacheEntry() {
        buffer = null;
        validity = null;
    }
    public ChannelCacheEntry(Object buffer, Object validity) {
        this.buffer = buffer;
        this.validity = validity;
    }
}

