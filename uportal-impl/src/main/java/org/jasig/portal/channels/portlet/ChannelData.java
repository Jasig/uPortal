/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;

/**
 * An object that keeps track of session data.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelData {
    private boolean portletWindowInitialized = false;
    private PortletWindow portletWindow = null;
    private Map userInfo = null;
    private boolean processedAction = false;
    private boolean receivedEvent = false;
    private boolean focused = false;
    private PortletMode newPortletMode = null;
    private long lastRenderTime = Long.MIN_VALUE;
    private String expirationCache = null;
    private WindowState newWindowState = null;
        
    public boolean isPortletWindowInitialized() { return this.portletWindowInitialized; }
    public PortletWindow getPortletWindow() { return this.portletWindow; }
    public Map getUserInfo() { return this.userInfo; }
    public boolean hasProcessedAction() { return this.processedAction; }
    public boolean hasReceivedEvent() { return this.receivedEvent; }
    public boolean isFocused() { return this.focused; }
    public PortletMode getNewPortletMode() { return this.newPortletMode; }
    public WindowState getNewWindowState() { return this.newWindowState; }
    public long getLastRenderTime() { return this.lastRenderTime; }
    public String getExpirationCache() { return this.expirationCache; }
        
    public void setPortletWindowInitialized(boolean portletWindowInitialized) { this.portletWindowInitialized = portletWindowInitialized; }
    public void setPortletWindow(PortletWindow portletWindow) { this.portletWindow = portletWindow; }
    public void setUserInfo(Map userInfo) { this.userInfo = userInfo; }
    public void setProcessedAction(boolean processedAction) { this.processedAction = processedAction; }
    public void setReceivedEvent(boolean receivedEvent) { this.receivedEvent = receivedEvent; }
    public void setFocused(boolean focused) { this.focused = focused; }
    public void setNewPortletMode(PortletMode newPortletMode) { this.newPortletMode = newPortletMode; }
    public void setNewWindowState(WindowState newWindowState) { this.newWindowState = newWindowState; }
    public void setLastRenderTime(long lastRenderTime) { this.lastRenderTime = lastRenderTime; }
    public void setExpirationCache(String expirationCache) { this.expirationCache = expirationCache; }
}

