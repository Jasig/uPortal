/**
 * Copyright � 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Describes a published channel.
 * @author George Lindholm, ITServices, UBC
 * @version $Revision$
 */
public class ChannelDefinition {
  private int id;
  private String chanFName;
  private String chanName;
  private String chanDesc;
  private String chanTitle;
  private String chanClass;
  private int chanTimeout;
  private int chanTypeId;
  private int chanPupblUsrId;
  private int chanApvlId;
  private Date chanPublDt;
  private Date chanApvlDt;
  private boolean chanEditable;
  private boolean chanHasHelp;
  private boolean chanHasAbout;
  private List parameters;

  /**
   * Constructs a channel definition.
   * @param id the channel definition ID
   */
  public ChannelDefinition(int id) {
    this.id = id;
    this.chanTitle = "";
    this.chanDesc = "";
    this.chanClass = "";
    this.parameters = new ArrayList();
  }

  // Getter methods
  public int getId() { return id; }
  public String getFName() { return chanFName; }
  public String getName() { return chanName; }
  public String getDescription() { return chanDesc; }
  public String getTitle() { return chanTitle; }
  public String getJavaClass() { return chanClass; }
  public int getTimeout() { return chanTimeout; }
  public int getTypeId() { return chanTypeId; }
  public int getPublisherId() { return chanPupblUsrId; }
  public int getApproverId() { return chanApvlId; }
  public Date getPublishDate() { return chanPublDt; }
  public Date getApprovalDate() { return chanApvlDt;}
  public boolean isEditable() { return chanEditable; }
  public boolean hasHelp() { return chanHasHelp; }
  public boolean hasAbout() { return chanHasAbout; }
  public ChannelParameter[] getParameters() { return (ChannelParameter[])parameters.toArray(new ChannelParameter[0]); }

  // Setter methods
  public void setFName(String fname) {this.chanFName =fname; }
  public void setName(String name) {this.chanName = name; }
  public void setDescription(String descr) {this.chanDesc = descr; }
  public void setTitle(String title) {this.chanTitle = title; }
  public void setJavaClass(String javaClass) {this.chanClass = javaClass; }
  public void setTimeout(int timeout) {this.chanTimeout = timeout; }
  public void setTypeId(int typeId) {this.chanTypeId = typeId; }
  public void setPublisherId(int publisherId) {this.chanPupblUsrId = publisherId; }
  public void setApproverId(int approvalId) {this.chanApvlId = approvalId; }
  public void setPublishDate(Date publishDate) {this.chanPublDt = publishDate; }
  public void setApprovalDate(Date approvalDate) {this.chanApvlDt = approvalDate; }
  public void setEditable(boolean editable) {this.chanEditable = editable; }
  public void setHasHelp(boolean hasHelp) {this.chanHasHelp = hasHelp; }
  public void setHasAbout(boolean hasAbout) {this.chanHasAbout = hasAbout; }
  public void setParameters(ChannelParameter[] parameters) { this.parameters = Arrays.asList(parameters); };

  public void addParameter(String name, String value, String override) {
    parameters.add(new ChannelParameter(name, value, override));
  }

  /**
   * Minimum attributes a channel must have
   */
  private Element getBase(Document doc, String idTag, String chanClass,
    boolean editable, boolean hasHelp, boolean  hasAbout) {
    Element channel = doc.createElement("channel");
    ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(idTag, channel);
    channel.setAttribute("ID", idTag);
    channel.setAttribute("chanID", id + "");
    channel.setAttribute("timeout", chanTimeout + "");
    channel.setAttribute("name", chanName);
    channel.setAttribute("title", chanTitle);
    channel.setAttribute("fname", chanFName);
    channel.setAttribute("class", chanClass);
    channel.setAttribute("typeID", chanTypeId + "");
    channel.setAttribute("editable", editable ? "true" : "false");
    channel.setAttribute("hasHelp", hasHelp ? "true" : "false");
    channel.setAttribute("hasAbout", hasAbout ? "true" : "false");
    return channel;
  }

  private final Element nodeParameter(Document doc, String name, int value) {
    return nodeParameter(doc, name, Integer.toString(value));
  }

  private final Element nodeParameter(Document doc, String name, String value) {
    Element parameter = doc.createElement("parameter");
    parameter.setAttribute("name", name);
    parameter.setAttribute("value", value);
    return parameter;
  }

  private final void addParameters(Document doc, Element channel) {
    if (parameters != null) {
      for (int i = 0; i < parameters.size(); i++) {
        ChannelParameter cp = (ChannelParameter) parameters.get(i);

        Element parameter = nodeParameter(doc, cp.name, cp.value);
        if (cp.override) {
          parameter.setAttribute("override", "yes");
        }
        channel.appendChild(parameter);
      }
    }
  }

  /**
   * Display a message where this channel should be
   */
  public Element getDocument(Document doc, String idTag, String statusMsg, int errorId) {
    Element channel = getBase(doc, idTag, "org.jasig.portal.channels.CError", false, false, false);
    addParameters(doc, channel);
    channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
    channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
    channel.appendChild(nodeParameter(doc, "CErrorErrorId", errorId));
    return channel;
  }

  /**
   * return an xml representation of this channel
   */
  public Element getDocument(Document doc, String idTag) {
    Element channel = getBase(doc, idTag, chanClass, chanEditable, chanHasHelp, chanHasAbout);
    channel.setAttribute("description", chanDesc);
    addParameters(doc, channel);
    return channel;
  }

  /**
   * Is it time to reload me from the data store
   */
  public boolean refreshMe() {
    return false;
  }

  /**
   * Describes a channel definition parameter
   * A channel can have zero or more parameters.
   */
  public class ChannelParameter {
    String name;
    String value;
    boolean override;
    String descr;

    public ChannelParameter(String name, String value, String override) {
      this(name, value, RDBMServices.dbFlag(override));
    }

    public ChannelParameter(String name, String value, boolean override) {
      this.name = name;
      this.value = value;
      this.override = override;
    }

    // Getter methods
    public String getName() { return name; }
    public String getValue() { return value; }
    public boolean getOverride() { return override; }
    public String getDescription() {return descr; }

    // Setter methods
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setOverride(boolean override) { this.override = override; }
    public void setDescription(String descr) { this.descr = descr; }
  }
}

