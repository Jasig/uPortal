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

import org.xml.sax.*;
import org.jasig.portal.utils.*;
import org.jasig.portal.services.LogService;
import java.util.Map;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.utils.threading.ThreadPool;
import org.jasig.portal.utils.threading.WorkTracker;
import org.jasig.portal.utils.threading.WorkerTask;


/**
 * This class takes care of initiating channel rendering thread, 
 * monitoring it for timeouts, retreiving cache, and returning 
 * rendering results and status.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelRenderer
{
    public static final boolean CACHE_CHANNELS=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.ChannelRenderer.cache_channels");
    public static final int RENDERING_SUCCESSFUL=0;
    public static final int RENDERING_FAILED=1;
    public static final int RENDERING_TIMED_OUT=2;
    public static final String[] renderingStatus={"successful","failed","timed out"};

    protected IChannel channel;
    protected ChannelRuntimeData rd;
    protected Map channelCache;
    protected Map cacheTables;

    protected boolean rendering;
    protected boolean donerendering;

    protected Thread workerThread;
    protected WorkTracker workTracker;

    protected Worker worker;

    protected long startTime;
    protected long timeOut = java.lang.Long.MAX_VALUE;

    protected boolean ccacheable;

    protected static ThreadPool tp=null;
    protected static Map systemCache=null;

    protected SetCheckInSemaphore groupSemaphore;
    protected Object groupRenderingKey;
    private Object cacheWriteLock;



    /**
     * Default contstructor
     *
     * @param chan an <code>IChannel</code> value
     * @param runtimeData a <code>ChannelRuntimeData</code> value
     * @param threadPool a <code>ThreadPool</code> value
     */
    public ChannelRenderer (IChannel chan,ChannelRuntimeData runtimeData, ThreadPool threadPool) {
        this.channel=chan;
        this.rd=runtimeData;
        rendering = false;
        ccacheable=false;
        cacheWriteLock=new Object();
        tp = threadPool;

        if(systemCache==null) {
            systemCache=ChannelManager.systemCache;
        }

        this.groupSemaphore=null;
        this.groupRenderingKey=null;
    }


    /**
     * Default contstructor
     *
     * @param chan an <code>IChannel</code> value
     * @param runtimeData a <code>ChannelRuntimeData</code> value
     * @param threadPool a <code>ThreadPool</code> value
     * @param groupSemaphore a <code>SetCheckInSemaphore</code> for the current rendering group
     * @param groupRenderingKey an <code>Object</code> to be used for check ins with the group semaphore
     */
    public ChannelRenderer (IChannel chan,ChannelRuntimeData runtimeData, ThreadPool threadPool, SetCheckInSemaphore groupSemaphore, Object groupRenderingKey) {
        this(chan,runtimeData,threadPool);
        this.groupSemaphore=groupSemaphore;
        this.groupRenderingKey=groupRenderingKey;
    }


    /**
     * Sets the channel on which ChannelRenderer is to operate.
     *
     * @param channel an <code>IChannel</code>
     */
    public void setChannel(IChannel channel) {
        LogService.instance().log(LogService.DEBUG,"ChannelRenderer::setChannel() : channel is being reset!");        
        this.channel=channel;
        if(worker!=null) {
            worker.setChannel(channel);
        }
        // clear channel chace
        channelCache=null;
        cacheWriteLock=new Object();
    }
    
    /**
     * Obtains a content cache specific for this channel instance.
     *
     * @return a key->rendering map for this channel
     */
    Map getChannelCache() {
	if(channelCache==null) {
	    if((channelCache=(SoftHashMap)cacheTables.get(channel))==null) {
		channelCache=new SoftHashMap(1);
		cacheTables.put(channel,channelCache);
	    }
	}
	return channelCache;
    }


    /**
     * Set the timeout value
     * @param value timeout in milliseconds
     */
    public void setTimeout (long value) {
        timeOut = value;
    }

    public void setCacheTables(Map cacheTables) {
	this.cacheTables=cacheTables;
    }

    /**
     * Informs ChannelRenderer that a character caching scheme
     * will be used for the current rendering.
     * @param setting a <code>boolean</code> value
     */
    public void setCharacterCacheable(boolean setting) {
        this.ccacheable=setting;
    }

  /**
   * Start rendering of the channel in a new thread.
   * Note that rendered information will be accumulated in a
   * buffer until outputRendering() function is called.
   * startRendering() is a non-blocking function.
   */
  public void startRendering ()
  {
    // start the rendering thread

    worker = new Worker (channel,rd);
    workTracker=tp.execute(worker);
    rendering = true;
    startTime = System.currentTimeMillis ();
  }

    public void startRendering(SetCheckInSemaphore groupSemaphore, Object groupRenderingKey) {
        this.groupSemaphore=groupSemaphore;
        this.groupRenderingKey=groupRenderingKey;
        this.startRendering();
    }

  /**
   * Output channel rendering through a given ContentHandler.
   * Note: call of outputRendering() without prior call to startRendering() is equivalent to
   * sequential calling of startRendering() and then outputRendering().
   * outputRendering() is a blocking function. It will return only when the channel completes rendering
   * or fails to render by exceeding allowed rendering time.
   * @param out Document Handler that will receive information rendered by the channel.
   * @return error code. 0 - successful rendering; 1 - rendering failed; 2 - rendering timedOut;
   */
    public int outputRendering (ContentHandler out) throws Throwable {
        int renderingStatus=completeRendering();
        if(renderingStatus==RENDERING_SUCCESSFUL) {
            SAX2BufferImpl buffer;
            if ((buffer=worker.getBuffer())!=null) {
                // unplug the buffer :)
                try {
                    buffer.setAllHandlers(out);
                    buffer.outputBuffer();
                    return RENDERING_SUCCESSFUL;
                } catch (SAXException e) {
                    // worst case scenario: partial content output :(
                    LogService.instance().log(LogService.ERROR, "ChannelRenderer::outputRendering() : following SAX exception occured : "+e);
                    throw e;
                }
            } else {
                LogService.instance().log(LogService.ERROR, "ChannelRenderer::outputRendering() : output buffer is null even though rendering was a success?! trying to rendering for ccaching ?"); 
                throw new PortalException("unable to obtain rendering buffer");
            }
        }
        return renderingStatus;
    }


    /**
     * Requests renderer to complete rendering and return status.
     * This does exactly the same things as outputRendering except for the
     * actual stream output.
     *
     * @return an <code>int</code> return status value
     */

    public int completeRendering() throws Throwable {
        if (!rendering) {
            this.startRendering ();
        }
        boolean abandoned=false;
        long timeOutTarget = startTime + timeOut;
      
      
        // separate waits caused by rendering group
        if(groupSemaphore!=null) {
            while(!worker.isSetRuntimeDataComplete() && System.currentTimeMillis() < timeOutTarget && !workTracker.isJobComplete()) {
                long wait=timeOutTarget-System.currentTimeMillis();
                if(wait<=0) { wait=1; }
                try {
                    synchronized(groupSemaphore) {
                        groupSemaphore.wait(wait);
                    }
                } catch (InterruptedException ie) {}
            }
            if(!worker.isSetRuntimeDataComplete() && !workTracker.isJobComplete()) {
                workTracker.killJob();
                abandoned=true;
                LogService.instance().log(LogService.DEBUG,"ChannelRenderer::outputRendering() : killed. (key="+groupRenderingKey.toString()+")");
            } else {
                groupSemaphore.waitOn();
            }
            // reset timer for rendering
            timeOutTarget=System.currentTimeMillis()+timeOut;
        }
      
        if(!abandoned) {
            while(System.currentTimeMillis() < timeOutTarget && !workTracker.isJobComplete()) {
                long wait=timeOutTarget-System.currentTimeMillis();
                if(wait<=0) { wait=1; }
                try {
                    synchronized(workTracker) {
                        workTracker.wait(wait);
                    }
                } catch (InterruptedException ie) {}
            }
          
            if(!workTracker.isJobComplete()) {
                workTracker.killJob();
                abandoned=true;
                LogService.instance().log(LogService.DEBUG,"ChannelRenderer::outputRendering() : killed.");
            } else {
                abandoned=!workTracker.isJobSuccessful();
            }
          
        }
      
        if (!abandoned && worker.done ()) {
            if (worker.successful() && (((worker.getBuffer())!=null) || (ccacheable && worker.cbuffer!=null))) {
                return RENDERING_SUCCESSFUL;

            } else {
                // rendering was not successful
                Throwable e;
                if((e=worker.getThrowable())!=null) throw new InternalPortalException(e);
                // should never get there, unless thread.stop() has seriously messed things up for the worker thread.
                return RENDERING_FAILED;
            }
        } else {
            Throwable e;
            e = workTracker.getException();
            if (e != null) {
                throw new InternalPortalException(e);
            } else {
                // Assume rendering has timed out
                return RENDERING_TIMED_OUT;
            }
        }
    }


    /**
     * Returns rendered buffer.
     * This method does not perform any status checks, so make sure to call completeRendering() prior to invoking this method.
     *
     * @return rendered buffer
     */
    public SAX2BufferImpl getBuffer() {
        if(worker!=null) {
            return worker.getBuffer();
        } else {
            return null;
        }
    }

    /**
     * Returns a character output of a channel rendering.
     */
    public String getCharacters() {
        if(worker!=null) {
            return worker.getCharacters();
        } else {
            LogService.instance().log(LogService.DEBUG,"ChannelRenderer::getCharacters() : worker is null already !");
            return null;
        }
    }


    /**
     * Sets a character cache for the current rendering.
     */
    public void setCharacterCache(String chars) {
        if(worker!=null) {
            worker.setCharacterCache(chars);
        }
    }

    /**
     * I am not really sure if this will take care of the runaway rendering threads.
     * The alternative is kill them explicitly in ChannelManager.
     */
    protected void finalize () throws Throwable  {
       if(workTracker!=null && !workTracker.isJobComplete())
            workTracker.killJob();
       super.finalize ();
    }


    protected class Worker extends WorkerTask{
        private boolean successful;
        private boolean done;
        private boolean setRuntimeDataComplete;
        private boolean decremented;
        private IChannel channel;
        private ChannelRuntimeData rd;
	private SAX2BufferImpl buffer;
        private String cbuffer;
        private Throwable exc=null;

	protected class ChannelCacheEntry {
	    private Object buffer;
	    private final Object validity;
	    public ChannelCacheEntry() {
		buffer=null;
		validity=null;
	    }
	    public ChannelCacheEntry(Object buffer,Object validity) {
		this.buffer=buffer;
		this.validity=validity;
	    }
	}

        public Worker (IChannel ch, ChannelRuntimeData runtimeData) {
            this.channel=ch;  this.rd=runtimeData;
            successful = false; done = false; setRuntimeDataComplete=false;
	    buffer=null; cbuffer=null;
        }

        public void setChannel(IChannel ch) {
            this.channel=ch;
        }

        public boolean isSetRuntimeDataComplete() {
            return this.setRuntimeDataComplete;
        }

        public void run () {
            try {
                if(rd!=null) {
                    channel.setRuntimeData(rd);
                }
                setRuntimeDataComplete=true;
                
                if(groupSemaphore!=null) {
                    groupSemaphore.checkInAndWaitOn(groupRenderingKey);
                }

		if(CACHE_CHANNELS) {
		    // try to obtain rendering from cache
		    if(channel instanceof ICacheable ) {
			ChannelCacheKey key=((ICacheable)channel).generateKey();
			if(key!=null) {
			    if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
				ChannelCacheEntry entry=(ChannelCacheEntry)systemCache.get(key.getKey());
				if(entry!=null) {
				    // found cached page
				    // check page validity
				    if(((ICacheable)channel).isCacheValid(entry.validity) && (entry.buffer!=null)) {
					// use it
                                        if(ccacheable && (entry.buffer instanceof String)) {
                                            cbuffer=(String)entry.buffer;
                                            LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : retrieved system-wide cached character content based on a key \""+key.getKey()+"\"");
                                        } else if(entry.buffer instanceof SAX2BufferImpl) {
                                            buffer=(SAX2BufferImpl) entry.buffer;
                                            LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : retrieved system-wide cached content based on a key \""+key.getKey()+"\"");
                                        }
				    } else {
					// remove it
					systemCache.remove(key.getKey());
					LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : removed system-wide unvalidated cache based on a key \""+key.getKey()+"\"");
				    }
				}
			    } else {
				// by default we assume INSTANCE_KEY_SCOPE
				ChannelCacheEntry entry=(ChannelCacheEntry)getChannelCache().get(key.getKey());
				if(entry!=null) {
				    // found cached page
				    // check page validity
				    if(((ICacheable)channel).isCacheValid(entry.validity) && (entry.buffer!=null)) {
					// use it
                                        if(ccacheable && (entry.buffer instanceof String)) {
                                            cbuffer=(String)entry.buffer;
                                            LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : retrieved instance-cached character content based on a key \""+key.getKey()+"\"");

                                        } else if(entry.buffer instanceof SAX2BufferImpl) {
                                            buffer=(SAX2BufferImpl) entry.buffer;
                                            LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : retrieved instance-cached content based on a key \""+key.getKey()+"\"");
                                        }
				    } else {
					// remove it
					getChannelCache().remove(key.getKey());
					LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : removed unvalidated instance-cache based on a key \""+key.getKey()+"\"");
				    }
				}
			    }
			}

                        // future work: here we should synchronize based on a particular cache key.
                        // Imagine a VERY popular cache entry timing out, then portal will attempt
                        // to re-render the page in many threads (serving many requests) simultaneously.
                        // If one was to synchronize on writing cache for a particular key, one thread
                        // would render and others would wait for it to complete. 

                        // check if need to render
                        if((ccacheable && cbuffer==null && buffer==null) || ((!ccacheable) && buffer==null)) {
                            // need to render again and cache the output
                            buffer = new SAX2BufferImpl ();
                            buffer.startBuffering();
                            channel.renderXML(buffer);

                            // save cache
                            if(key!=null) {

                                if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                                    systemCache.put(key.getKey(),new ChannelCacheEntry(buffer,key.getKeyValidity()));
                                    LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : recorded system cache based on a key \""+key.getKey()+"\"");
                                } else {
                                    getChannelCache().put(key.getKey(),new ChannelCacheEntry(buffer,key.getKeyValidity()));
                                    LogService.instance().log(LogService.DEBUG,"ChannelRenderer.Worker::run() : recorded instance cache based on a key \""+key.getKey()+"\"");
                                }
                            }
                        }
		    } else {
			buffer = new SAX2BufferImpl ();
			buffer.startBuffering();
			channel.renderXML(buffer);
		    }
		} else  {
		    // in the case when channel cache is not enabled
		    buffer = new SAX2BufferImpl ();
		    buffer.startBuffering();
		    channel.renderXML (buffer);
		}
                successful = true;
            } catch (Exception e) {
                if(groupSemaphore!=null) {
                    groupSemaphore.checkIn(groupRenderingKey);
                }
                this.setException(e);
            }
            done = true;
        }

        public boolean successful () {
            return this.successful;
        }

	public SAX2BufferImpl getBuffer() {
	    return this.buffer;
	}

        /**
         * Returns a character output of a channel rendering.
         */
        public String getCharacters() {
            if(ccacheable) {
                return this.cbuffer;
            } else {
                LogService.instance().log(LogService.ERROR,"ChannelRenderer.Worker::getCharacters() : attempting to obtain character data while character caching is not enabled !");
                return null;
            }
        }

        /**
         * Sets a character cache for the current rendering.
         */
        public void setCharacterCache(String chars) {
            cbuffer=chars;
            if(CACHE_CHANNELS) {
                // try to obtain rendering from cache
                if(channel instanceof ICacheable ) {
                    ChannelCacheKey key=((ICacheable)channel).generateKey();
                    if(key!=null) {
                        LogService.instance().log(LogService.DEBUG,"ChannelRenderer::setCharacterCache() : called on a key \""+key.getKey()+"\"");
                        ChannelCacheEntry entry=null;
                        if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                            entry=(ChannelCacheEntry)systemCache.get(key.getKey());
                            if(entry==null) {
                                LogService.instance().log(LogService.DEBUG,"ChannelRenderer::setCharacterCache() : setting character cache buffer based on a system key \""+key.getKey()+"\"");
                                entry=new ChannelCacheEntry(chars,key.getKeyValidity());
                            } else {
                                entry.buffer=chars;
                            }
                            systemCache.put(key.getKey(),entry);
                        } else {
                            // by default we assume INSTANCE_KEY_SCOPE
                            entry=(ChannelCacheEntry)getChannelCache().get(key.getKey());
                            if(entry==null) {
                                LogService.instance().log(LogService.DEBUG,"ChannelRenderer::setCharacterCache() : no existing cache on a key \""+key.getKey()+"\"");
                                entry=new ChannelCacheEntry(chars,key.getKeyValidity());
                            } else {
                                entry.buffer=chars;
                            }
                            getChannelCache().put(key.getKey(),entry);
                        }
                    } else {
                        LogService.instance().log(LogService.WARN,"ChannelRenderer::setCharacterCache() : channel cache key is null !");
                    }
                }
            }
        }

        public boolean done () {
            return this.done;
        }

        public Throwable getThrowable() {
            return this.getException();
        }
    }
}
