/**
 * Factory.java	Java 1.3.0 Mon Dec 11 17:52:47 EST 2000
 *
 * Copyright 1999 by ObjectSpace, Inc.,
 * 14850 Quorum Dr., Dallas, TX, 75240 U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of ObjectSpace, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with ObjectSpace.
 */

package org.jasig.portal.layout;

public class Factory
  {
  public static ITab newTab()
    {
    return new org.jasig.portal.layout.Tab();
    }

  public static IColumn newColumn()
    {
    return new org.jasig.portal.layout.Column();
    }

  public static IChannel newChannel()
    {
    return new org.jasig.portal.layout.Channel();
    }

  public static ILayout newLayout()
    {
    return new org.jasig.portal.layout.Layout();
    }

  public static IParameter newParameter()
    {
    return new org.jasig.portal.layout.Parameter();
    }

  }