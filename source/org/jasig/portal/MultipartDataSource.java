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

/**
 * Wrapper class to make com.oreilly.servlet.multipart.FilePart object
 * available as a DataSource.
 *
 * We have to buffer the data stream since the MimePart class will try to
 * read the stream several times (??) and we can't rewind the HttpRequest stream.
 *
 * @author George Lindholm, ITServices, UBC
 * @version $Revision$
*/
package org.jasig.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.oreilly.servlet.multipart.FilePart;

public class MultipartDataSource implements DataSource {
  ByteArrayOutputStream buff = null;
  String contentType = null;
  String filename = null;

  public MultipartDataSource(FilePart filePart) throws IOException {
    contentType = filePart.getContentType();
    filename = filePart.getFileName();
    buff = new ByteArrayOutputStream();
    filePart.writeTo(buff);
  }

  public void finalize() {
    buff = null;
  }

  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(buff.toByteArray());
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("getOutputStream() not implemented");
  }

  public String getContentType() {
    return contentType;
  }

  public String getName() {
    return filename;
  }

  public File getFile() throws Exception {
    throw new Exception("getFile() not implemented");
  }

  public void setFileTypeMap(javax.activation.FileTypeMap p0) throws Exception {
    throw new Exception("setFileTypeMap() not implemented");
  }
}
