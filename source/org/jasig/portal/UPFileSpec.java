/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.StringTokenizer;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

/**
 * This helper class allows for easy access to the information contained in the ever-changing 
 * .uP file URL spec. The .uP file syntax is likely to change often, therefore we encourage developers
 * to use this class instead of trying to parse the .uP file on your own.
 * <p>Note: in case you're wondering what in the world ".uP file" is, take a look at the portal URLs. 
 * The context path ends with a file-like specification that always has ".uP" at the end ... 
 * that's what we call a ".uP" file. It is used to provide information on how different requests
 * should be processed.</p>
 * <p>Current .uP file syntax looks like this: <code><b>"[tag.tagId.]{method}.methodId.[target.targetId.][*.]uP"</b></code>, 
 * where "[]" denote optional expressions and "{}" choice-defined expressions. The "{method}" field, at the moment has
 * two choices: "render" and "worker".</p>
 * <p> uPortal will assume that the .uP file spec is always well-formed, so don't try to construct it on your own, use 
 * <code>baseActionURL</code> or one of the <code>workerActionURL</code>s. </p>
 * 
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version: $Revision$
 */
public class UPFileSpec {
    // some URL construction elements
    public static final String TAG_URL_ELEMENT="tag";
    public static final String TARGET_URL_ELEMENT="target";
    public static final String WORKER_URL_ELEMENT="worker";
    public static final String DETACH_URL_ELEMENT="detach";
    public static final String RENDER_URL_ELEMENT="render";
    public static final String PORTAL_URL_SEPARATOR=".";
    public static final String PORTAL_URL_SUFFIX="uP";

    // individual worker URL elements
    public static final String FILE_DOWNLOAD_WORKER = "download";    


    // int values for methods
    public static final int RENDER_METHOD=0;
    public static final int WORKER_METHOD=1;

    String tagId=null;
    String method=null;
    String methodNodeId=null;
    String targetNodeId=null;
    String uPFile_extras=null;

    /**
     * Creates a new <code>UPFileSpec</code> instance with all values being null.
     *
     */
    public UPFileSpec() {
    }
    
    /**
     * Construct a .uP file spec object for a .uP file contained in a given request.
     *
     * @param req a <code>HttpServletRequest</code> value
     */
    public UPFileSpec(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        String uPFile = servletPath.substring(servletPath.lastIndexOf('/')+1, servletPath.length());        
        analyze(uPFile);
    }

    /**
     * Construct a .uP file spec object by providing the actual .uP file string
     *
     * @param uPFile a <code>String</code> value
     */
    public UPFileSpec(String uPFile) {
        analyze(uPFile);
    }

    /**
     * Copy constructor.
     *
     * @param up an <code>UPFileSpec</code> value to copy the values from
     */
    public UPFileSpec(UPFileSpec up) {
        this.tagId=up.getTagId();
        this.method=up.getMethod();
        this.methodNodeId=up.getMethodNodeId();
        this.targetNodeId=up.getTargetNodeId();
        this.uPFile_extras=up.getUPFileExtras();
    }


    /**
     * A building constructor.
     *
     * @param tagId a tag id <code>String</code> value (can be <code>null</code>)
     * @param method a method <code>String</code> value (required, must be one of the <code>UPFileSpec.*_METHOD</code> constants, i.e.  {@link RENDER_METHOD} or {@link WORKER_METHOD})
     * @param methodNodeId a method node id <code>String</code> value (required value, can <b>not</b> be <code>null</code>)
     * @param targetNodeId a target id <code>String</code> value (can be <code>null</code>)
     * @param extraElements a <code>String</code> to be incorporated into the file name before the suffix (".uP"). These values will be available from the {@link getUPFileExras()} result when .uP file is parsed. (can be <code>null</code>)
     * @exception PortalException if an invalid method code is passed or no methodNodeId is present.
     */
    public UPFileSpec(String tagId,int method,String methodNodeId,String targetNodeId,String extraElements) throws PortalException {
        this.setTagId(tagId);
        this.setMethod(method);
        this.setMethodNodeId(methodNodeId);
        this.setTargetNodeId(targetNodeId);
        this.setUPFileExtras(extraElements);
    }

    /**
     * Set a tag id
     *
     * @param id a <code>String</code> value
     */
    public void setTagId(String id) {
        this.tagId=id;
    }

    /**
     * Set a method.
     *
     * @param method a method <code>String</code> value (required, must be one of the <code>UPFileSpec.*_METHOD</code> constants, i.e.  {@link RENDER_METHOD} or {@link WORKER_METHOD})
     * @exception PortalException if an invalid method id is passed.
     */
    public void setMethod(int method) throws PortalException {
        if(method==RENDER_METHOD) {
            this.method=RENDER_URL_ELEMENT;
        } else if(method==WORKER_METHOD) {
            this.method=WORKER_URL_ELEMENT;
        } else {
            throw new PortalException("Invalid method code!");
        }
    }

    /**
     * Set method node id.
     *
     * @param nodeId a <code>String</code> value
     */
    public void setMethodNodeId(String nodeId) {
        this.methodNodeId=nodeId;
    }

    /**
     * Set target node id
     *
     * @param nodeId a <code>String</code> value
     */
    public void setTargetNodeId(String nodeId) {
        this.targetNodeId=nodeId;
    }
    
    /**
     * Set extras to be appended to the spec before the suffix element (".uP")
     *
     * @param extras a <code>String</code> value
     */
    public void setUPFileExtras(String extras) {
        this.uPFile_extras=extras;
    }
        

    /**
     * Returns a tag identifier.
     *
     * @return a <code>String</code> tag value,  <code>null</code> if no tag was specified.
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * Determine method name
     *
     * @return a <code>String</code> method name,  <code>null</code> if no method was specified.
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Determine Id specified by the method element.
     *
     * @return a <code>String</code> method node Id value, <code>null</code> if no method was specified.
     */
    public String getMethodNodeId() {
        return methodNodeId;
    }

    /**
     * Determine Id specified by the "target" element.
     *
     * @return a <code>String</code> target Id value, <code>null</code> if no target was specified.
     */
    public String getTargetNodeId() {
        return targetNodeId;
    }

    /**
     * Get the full .uP file <code>String</code>.
     *
     * @return a <code>String</code> value
     */
    public String getUPFile() throws PortalException {
        return (buildUPFileBase(tagId,method,methodNodeId,targetNodeId,uPFile_extras)).concat(PORTAL_URL_SUFFIX);
    }

    /**
     * Returns a "cleaned-up" version of the .uP file with all known 
     * fields (i.e. tag, method and target) removed. This can be used by
     * 
     *
     * @return a <code>String</code> value, <code>null</code> if none were encountered.
     */
    public String getUPFileExtras() {
        return uPFile_extras;
    }


    /**
     * Constructs a .uP file
     *
     * @param tagId a tag id <code>String</code> value (can be <code>null</code>)
     * @param method a method <code>String</code> value (required, must be one of the <code>UPFileSpec.*_METHOD</code> constants, i.e.  {@link RENDER_METHOD} or {@link WORKER_METHOD})
     * @param methodNodeId a method node id <code>String</code> value (required value, can <b>not</b> be <code>null</code>)
     * @param targetNodeId a target id <code>String</code> value (can be <code>null</code>)
     * @param extraElements a <code>String</code> to be incorporated into the file name before the suffix (".uP"). These values will be available from the {@link getUPFileExras()} result when .uP file is parsed. (can be <code>null</code>)
     * @return a <code>String</code> value
     * @exception PortalException if an invalid method code is passed or no methodNodeId is present.
     */
    public static String buildUPFile(String tagId,int method,String methodNodeId,String targetNodeId,String extraElements) throws PortalException {
        return (buildUPFileBase(tagId,method,methodNodeId,targetNodeId,extraElements)).concat(PORTAL_URL_SUFFIX);
    }


    /**
     * Constructs a .uP file, without the suffix (actual ".uP") so it can be extended further.
     *
     * @param tagId a tag id <code>String</code> value (can be <code>null</code>)
     * @param method a method <code>String</code> value (required, must be one of the <code>UPFileSpec.*_METHOD</code> constants, i.e.  {@link RENDER_METHOD} or {@link WORKER_METHOD})
     * @param methodNodeId a method node id <code>String</code> value (required value, can <b>not</b> be <code>null</code>)
     * @param targetNodeId a target id <code>String</code> value (can be <code>null</code>)
     * @param extraElements a <code>String</code> to be incorporated into the file name before the suffix (".uP"). These values will be available from the {@link getUPFileExras()} result when .uP file is parsed. (can be <code>null</code>)
     * @return a <code>String</code> value
     * @exception PortalException if an invalid method code is passed or no methodNodeId is present.
     */
    public static String buildUPFileBase(String tagId,int method,String methodNodeId,String targetNodeId,String extraElements) throws PortalException {
        String methodName=null;

        if(method==RENDER_METHOD) {
            methodName=RENDER_URL_ELEMENT;
        } else if(method==WORKER_METHOD) {
            methodName=WORKER_URL_ELEMENT;
        } else {
            throw new PortalException("Invalid method code!");
        }
        return buildUPFileBase(tagId,methodName,methodNodeId,targetNodeId,extraElements);
    }


    protected static String buildUPFileBase(String tagId,String method,String methodNodeId,String targetNodeId,String extraElements) throws PortalException {
        StringBuffer sb=new StringBuffer();
        if(tagId!=null) {
            sb.append("tag").append(PORTAL_URL_SEPARATOR);
            sb.append(tagId).append(PORTAL_URL_SEPARATOR);
        }

        if(method!=null) {
            sb.append(method).append(PORTAL_URL_SEPARATOR);
        } else {
            throw new PortalException("UPFileSpec: method can not be null!");
        }

        if(methodNodeId!=null) { 
            sb.append(methodNodeId).append(PORTAL_URL_SEPARATOR);
        } else {
            throw new PortalException("UPFileSpec: method node Id can not be null!");
        }

        if(targetNodeId!=null) {
            sb.append("target").append(PORTAL_URL_SEPARATOR);
            sb.append(targetNodeId).append(PORTAL_URL_SEPARATOR);
        }

        if(extraElements!=null) {
            sb.append(extraElements).append(PORTAL_URL_SEPARATOR);
        }

        return sb.toString();

    }

    protected void analyze(String uPFile) {
        StringTokenizer uPTokenizer=new StringTokenizer(uPFile,PORTAL_URL_SEPARATOR);
        // determine tag or method
        if(uPTokenizer.hasMoreTokens()) {
            String currentToken=uPTokenizer.nextToken();
            // is it a "tag" ?
            if(currentToken.equals(TAG_URL_ELEMENT)) {
                // yes it's, a tag
                if(uPTokenizer.hasMoreElements()) {
                    // we'll assume that the next toke is always an Id ...
                    tagId=uPTokenizer.nextToken();
                    if(uPTokenizer.hasMoreElements()) {
                        currentToken=uPTokenizer.nextToken();
                    } else {
                        return;
                    }
                } else {
                    // nothing after the "tag" element
                    return;
                }
            }

            // determine method
            if(currentToken.equals(RENDER_URL_ELEMENT)) {
                // render method
                method=currentToken;
            } else if(currentToken.equals(WORKER_URL_ELEMENT)) {
                // worker method
                method=currentToken;
            } else {
                // unknown method
                uPFile_extras=sinkTokenization(uPTokenizer,PORTAL_URL_SEPARATOR,currentToken);
                return;
            }

            // determine method target
            if(uPTokenizer.hasMoreElements()) {
                methodNodeId=uPTokenizer.nextToken();
            } else {
                return;
            }

            // see if a target is specified
            if(uPTokenizer.hasMoreElements()) {
                currentToken=uPTokenizer.nextToken();
            } else {
                return;
            }

            if(currentToken.equals(TARGET_URL_ELEMENT)) {
                // yes, target is specified
                if(uPTokenizer.hasMoreElements()) {
                    targetNodeId=uPTokenizer.nextToken();
                } else {
                    return;
                }
            }

            // sink the rest into the uPFile_extras
            uPFile_extras=sinkTokenization(uPTokenizer,PORTAL_URL_SEPARATOR,currentToken);

            
        } else {
            // blank .uP file ?
            return;
        }
    }
    
    /**
     * Sinks tokens back into a string. All except for the last one.
     *
     * @param st a <code>StringTokenizer</code> value
     * @param delimiter a <code>String</code> delimeter value used to produce the tokenization
     * @param initialValue a <code>String</code> value to which to append remaining tokens
     * @return a <code>String</code> value
     */
    private static String sinkTokenization(StringTokenizer st,  String delimiter, String initialValue) {
        StringBuffer sb;
        if(initialValue!=null) {
            sb=new StringBuffer(initialValue);
        } else {
            sb=new StringBuffer();
        }
                    
        while(st.hasMoreTokens()) {
            String token=st.nextToken();
            if(st.hasMoreTokens()) {
                sb.append(delimiter);
                sb.append(token);
            }
        }
        return sb.toString();
    }


    /**
     * A test self-test method for the class.
     *
     */
    public static void main(String[] args) {
        String tagValue="someTagValue";
        String methodValue=RENDER_URL_ELEMENT;
        String methodNodeIdValue="renderNodeIdValue";
        String targetNodeIdValue="someTargetValue";

        String manual_uPFile=TAG_URL_ELEMENT+PORTAL_URL_SEPARATOR+tagValue+PORTAL_URL_SEPARATOR+methodValue+PORTAL_URL_SEPARATOR+methodNodeIdValue+PORTAL_URL_SEPARATOR+TARGET_URL_ELEMENT+PORTAL_URL_SEPARATOR+targetNodeIdValue+PORTAL_URL_SEPARATOR+PORTAL_URL_SUFFIX;

        // test build ability
        String uPFile=null;
        try {
            uPFile=UPFileSpec.buildUPFile(tagValue,RENDER_METHOD,methodNodeIdValue,targetNodeIdValue,null);
            if(!uPFile.equals(manual_uPFile)) {
                System.out.println("Manual check of the buildUPFile failed: expected=\""+manual_uPFile+"\", returned=\""+uPFile+"\"");
            } else {
                System.out.println("Manual check of the buildUPFile passed.");
            }
        } catch (Exception e) {
            System.out.println("Manual check of the buildUPFile failed.");
            e.printStackTrace();
        }


        // test incremental build ability
        try {
            UPFileSpec upf=new UPFileSpec();
            upf.setTagId(tagValue);
            upf.setMethod(RENDER_METHOD);
            upf.setMethodNodeId(methodNodeIdValue);
            upf.setTargetNodeId(targetNodeIdValue);
            uPFile=upf.getUPFile();

            if(!uPFile.equals(manual_uPFile)) {
                System.out.println("Manual check of the incremental UPFile build failed: expected=\""+manual_uPFile+"\", returned=\""+uPFile+"\"");
            } else {
                System.out.println("Manual check of the incremental UPFile build passed.");
            }
        } catch (Exception e) {
            System.out.println("Manual check of the incremental UPFile build failed.");
            e.printStackTrace();
        }

        System.out.println("uPFile=\""+uPFile+"\"");
        
        // test parsing
        UPFileSpec fs=new UPFileSpec(uPFile);
        try {
            StringBuffer causes=new StringBuffer();
            boolean failed=false;
            if(fs.getTagId()==null || !tagValue.equals(fs.getTagId())) {
                causes.append("Wrong tag id real=\""+tagValue+"\", parsed=\""+fs.getTagId()+"\". ");
                failed=true;
            }
            if(fs.getMethod()==null || !methodValue.equals(fs.getMethod())) {
                causes.append("Wrong method real=\""+methodValue+"\", parsed=\""+fs.getMethod()+"\". ");
                failed=true;
            }
            if(fs.getMethodNodeId()==null || !methodNodeIdValue.equals(fs.getMethodNodeId())) {
                causes.append("Wrong methodNodeId real=\""+methodNodeIdValue+"\", parsed=\""+fs.getMethodNodeId()+"\". ");
                failed=true;
            }
            if(fs.getTargetNodeId()==null || !targetNodeIdValue.equals(fs.getTargetNodeId())) {
                causes.append("Wrong targetNodeId real=\""+targetNodeIdValue+"\", parsed=\""+fs.getTargetNodeId()+"\". ");
                failed=true;
            }
            
            if(failed) {
                System.out.println("Parse test failed: "+causes.toString()); 
            } else {
                System.out.println("Parse test passed.");
            }

        } catch (Exception e) {
            System.out.println("Parse test failed: "); e.printStackTrace();
        }
        

        
        
    }

}
