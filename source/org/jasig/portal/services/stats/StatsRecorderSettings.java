/**
 * Copyright � 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.services.stats;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;

/**
 * This class is responsible for maintaining the on/off settings
 * for each type of portal event that the StatsRecorder can record.
 * StatsRecorder will consult with this class before it attempts
 * to record an event.  The initial values for each setting will
 * be read upon portal startup from portal.properties.  At runtime,
 * the settings in this class can be controlled via
 * <code>StatsRecorder.set()</code>.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class StatsRecorderSettings {
  
  // Types of portal events that StatsRecorder can record
  public static final int RECORD_LOGIN = 0;
  public static final int RECORD_LOGOUT = 1;
  public static final int RECORD_SESSION_CREATED = 2;
  public static final int RECORD_SESSION_DESTROYED = 3;
  public static final int RECORD_CHANNEL_DEFINITION_PUBLISHED = 4;
  public static final int RECORD_CHANNEL_DEFINITION_MODIFIED = 5;
  public static final int RECORD_CHANNEL_DEFINITION_REMOVED = 6;
  public static final int RECORD_CHANNEL_ADDED_TO_LAYOUT = 7;
  public static final int RECORD_CHANNEL_UPDATED_IN_LAYOUT = 8;
  public static final int RECORD_CHANNEL_MOVED_IN_LAYOUT = 9;
  public static final int RECORD_CHANNEL_REMOVED_FROM_LAYOUT = 10;
  public static final int RECORD_FOLDER_ADDED_TO_LAYOUT = 11;
  public static final int RECORD_FOLDER_UPDATED_IN_LAYOUT = 12;
  public static final int RECORD_FOLDER_MOVED_IN_LAYOUT = 13;
  public static final int RECORD_FOLDER_REMOVED_FROM_LAYOUT = 14;
  public static final int RECORD_CHANNEL_INSTANTIATED = 15;
  public static final int RECORD_CHANNEL_RENDERED = 16;
  public static final int RECORD_CHANNEL_TARGETED = 17;
  
  // The settings...
  private boolean recordLogin = false;
  private boolean recordLogout = false;
  private boolean recordSessionCreated = false;
  private boolean recordSessionDestroyed = false;
  private boolean recordChannelDefinitionPublished = false;
  private boolean recordChannelDefinitionModified = false;
  private boolean recordChannelDefinitionRemoved = false;
  private boolean recordChannelAddedToLayout = false;
  private boolean recordChannelUpdatedInLayout = false;
  private boolean recordChannelMovedInLayout = false;
  private boolean recordChannelRemovedFromLayout = false;
  private boolean recordFolderAddedToLayout = false;
  private boolean recordFolderUpdatedInLayout = false;
  private boolean recordFolderMovedInLayout = false;
  private boolean recordFolderRemovedFromLayout = false;
  private boolean recordChannelInstantiated = false;
  private boolean recordChannelRendered = false;
  private boolean recordChannelTargeted = false;
  
  private static StatsRecorderSettings settingsInstance = null;
  
  /**
   * Constructor with private access so that StatsRecorderSettings
   * maintains only one instance of itself.
   */
  private StatsRecorderSettings() {
		try {
      String prefix = this.getClass().getName() + ".";
	    // Read in the initial settings from portal.properties
      recordLogin = PropertiesManager.getPropertyAsBoolean(prefix + "recordLogin");
      recordLogout = PropertiesManager.getPropertyAsBoolean(prefix + "recordLogout");
      recordSessionCreated = PropertiesManager.getPropertyAsBoolean(prefix + "recordSessionCreated");
      recordSessionDestroyed = PropertiesManager.getPropertyAsBoolean(prefix + "recordSessionDestroyed");
      recordChannelDefinitionPublished = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelDefinitionPublished");
      recordChannelDefinitionModified = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelDefinitionModified");
      recordChannelDefinitionRemoved = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelDefinitionRemoved");
      recordChannelAddedToLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelAddedToLayout");
      recordChannelUpdatedInLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelUpdatedInLayout");
      recordChannelMovedInLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelMovedInLayout");
      recordChannelRemovedFromLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelRemovedFromLayout");
      recordFolderAddedToLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordFolderAddedToLayout");
      recordFolderUpdatedInLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordFolderUpdatedInLayout");
      recordFolderMovedInLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordFolderMovedInLayout");
      recordFolderRemovedFromLayout = PropertiesManager.getPropertyAsBoolean(prefix + "recordFolderRemovedFromLayout");
      recordChannelInstantiated = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelInstantiated");
      recordChannelRendered = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelRendered");
      recordChannelTargeted = PropertiesManager.getPropertyAsBoolean(prefix + "recordChannelTargeted");
      
		} catch (Exception e) {
			LogService.log(LogService.ERROR, e);
		}
  }  
  
  /**
   * Creates an instance of StatsRecorderSettings.
   * @return settingsInstance, a <code>StatsRecorderSettings</code>
   * instance
   */
  public final static synchronized StatsRecorderSettings instance() {
    if (settingsInstance == null) { 
      settingsInstance = new StatsRecorderSettings();
    }
    return settingsInstance;
  }
    
  /**
   * Get the value of a particular setting.
   * @param setting, the setting
   * @return value, the value for the setting
   */
  public boolean get(int setting) {
    boolean value = false;
    switch (setting) {
      case RECORD_LOGIN:
        value = recordLogin;
        break;
      case RECORD_LOGOUT:
        value = recordLogout;
        break;
      case RECORD_SESSION_CREATED:
        value = recordSessionCreated;
        break;
      case RECORD_SESSION_DESTROYED:
        value = recordSessionDestroyed;
        break;
      case RECORD_CHANNEL_DEFINITION_PUBLISHED:
        value = recordChannelDefinitionPublished;
        break;
      case RECORD_CHANNEL_DEFINITION_MODIFIED:
        value = recordChannelDefinitionModified;
        break;
      case RECORD_CHANNEL_DEFINITION_REMOVED:
        value = recordChannelDefinitionRemoved;
        break;
      case RECORD_CHANNEL_ADDED_TO_LAYOUT:
        value = recordChannelAddedToLayout;
        break;
      case RECORD_CHANNEL_UPDATED_IN_LAYOUT:
        value = recordChannelUpdatedInLayout;
        break;
      case RECORD_CHANNEL_MOVED_IN_LAYOUT:
        value = recordChannelMovedInLayout;
        break;
      case RECORD_CHANNEL_REMOVED_FROM_LAYOUT:
        value = recordChannelRemovedFromLayout;
        break;
      case RECORD_FOLDER_ADDED_TO_LAYOUT:
        value = recordFolderAddedToLayout;
        break;
      case RECORD_FOLDER_UPDATED_IN_LAYOUT:
        value = recordFolderUpdatedInLayout;
        break;
      case RECORD_FOLDER_MOVED_IN_LAYOUT:
        value = recordFolderMovedInLayout;
        break;
      case RECORD_FOLDER_REMOVED_FROM_LAYOUT:
        value = recordFolderRemovedFromLayout;
        break;
      case RECORD_CHANNEL_INSTANTIATED:
        value = recordChannelInstantiated;
        break;
      case RECORD_CHANNEL_RENDERED:
        value = recordChannelRendered;
        break;
      case RECORD_CHANNEL_TARGETED:
        value = recordChannelTargeted;
        break;
        
      default:
        break;        
    }
    
    return value;
  }    
    
  /**
   * Set the value of a particular setting.
   * @param setting, the setting to change
   * @param newValue, the new value for the setting
   */
  public void set(int setting, boolean newValue) {
    switch (setting) {
      case RECORD_LOGIN:
        recordLogin = newValue;
        break;
      case RECORD_LOGOUT:
        recordLogout = newValue;
        break;
      case RECORD_SESSION_CREATED:
        recordSessionCreated = newValue;
        break;
      case RECORD_SESSION_DESTROYED:
        recordSessionDestroyed = newValue;
        break;
      case RECORD_CHANNEL_DEFINITION_PUBLISHED:
        recordChannelDefinitionPublished = newValue;
        break;
      case RECORD_CHANNEL_DEFINITION_MODIFIED:
        recordChannelDefinitionModified = newValue;
        break;
      case RECORD_CHANNEL_DEFINITION_REMOVED:
        recordChannelDefinitionRemoved = newValue;
        break;
      case RECORD_CHANNEL_ADDED_TO_LAYOUT:
        recordChannelAddedToLayout = newValue;
        break;
      case RECORD_CHANNEL_UPDATED_IN_LAYOUT:
        recordChannelUpdatedInLayout = newValue;
        break;
      case RECORD_CHANNEL_MOVED_IN_LAYOUT:
        recordChannelMovedInLayout = newValue;
        break;
      case RECORD_CHANNEL_REMOVED_FROM_LAYOUT:
        recordChannelRemovedFromLayout = newValue;
        break;
      case RECORD_FOLDER_ADDED_TO_LAYOUT:
        recordFolderAddedToLayout = newValue;
        break;
      case RECORD_FOLDER_UPDATED_IN_LAYOUT:
        recordFolderUpdatedInLayout = newValue;
        break;
      case RECORD_FOLDER_MOVED_IN_LAYOUT:
        recordFolderMovedInLayout = newValue;
        break;
      case RECORD_FOLDER_REMOVED_FROM_LAYOUT:
        recordFolderRemovedFromLayout = newValue;
        break;
      case RECORD_CHANNEL_INSTANTIATED:
        recordChannelInstantiated = newValue;
        break;
      case RECORD_CHANNEL_RENDERED:
        recordChannelRendered = newValue;
        break;
      case RECORD_CHANNEL_TARGETED:
        recordChannelTargeted = newValue;
        break;
        
      default:
        break;
    }
  }   
  
  /**
   * Returns a String representation of this object.
   * @param theSettings, the settings as a String
   */  
  public String toString() {
    StringBuffer sb = new StringBuffer(1024);
    sb.append("StatsRecorderSettings: \n");
    sb.append("  recordLogin=" + recordLogin).append("\n");
    sb.append("  recordLogout=" + recordLogout).append("\n");
    sb.append("  recordSessionCreated=" + recordSessionCreated).append("\n");
    sb.append("  recordSessionDestroyed=" + recordSessionDestroyed).append("\n");
    sb.append("  recordChannelDefinitionPublished=" + recordChannelDefinitionPublished).append("\n");
    sb.append("  recordChannelDefinitionModified=" + recordChannelDefinitionModified).append("\n");
    sb.append("  recordChannelDefinitionRemoved=" + recordChannelDefinitionRemoved).append("\n");
    sb.append("  recordChannelAddedToLayout=" + recordChannelAddedToLayout).append("\n");
    sb.append("  recordChannelUpdatedInLayout=" + recordChannelUpdatedInLayout).append("\n");
    sb.append("  recordChannelMovedInLayout=" + recordChannelMovedInLayout).append("\n");
    sb.append("  recordChannelRemovedFromLayout=" + recordChannelRemovedFromLayout).append("\n");
    sb.append("  recordFolderAddedToLayout=" + recordFolderAddedToLayout).append("\n");
    sb.append("  recordFolderUpdatedInLayout=" + recordFolderUpdatedInLayout).append("\n");
    sb.append("  recordFolderMovedInLayout=" + recordFolderMovedInLayout).append("\n");
    sb.append("  recordFolderRemovedFromLayout=" + recordFolderRemovedFromLayout).append("\n");
    sb.append("  recordChannelInstantiated=" + recordChannelInstantiated).append("\n");
    sb.append("  recordChannelRendered=" + recordChannelRendered).append("\n");
    sb.append("  recordChannelTargeted=" + recordChannelTargeted).append("\n");
    
    return sb.toString();
  }
}
