/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

/**
 * Records the removal of a published channel in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system 
 * based on the Spring ApplicationEventPublisher and Event Listeners. 
 * For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class RecordChannelDefinitionRemovedWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  ChannelDefinition channelDef;
  
  public RecordChannelDefinitionRemovedWorkerTask(IPerson person, ChannelDefinition channelDef) {
    this.person = person;
    this.channelDef = channelDef;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordChannelDefinitionRemoved(this.person, this.channelDef);
  }
}



