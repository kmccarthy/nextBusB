package com.abk.nextBusB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.ui.UiApplication;

/**
 * The ConnectionThread class manages the HTTP connection. If a fetch call 
 * is made and another request is made while the first is still active, the
 * first fetch will be terminated and the second one will start processing.
 */
public class PostHttpOrig extends Thread
{
    private static final int TIMEOUT = 500; // ms

	private static String HEADER_CONTENTTYPE = "content-type";
    private static String CONTENTTYPE_TEXTHTML = "text/html";
    

    private String _theUrl;
    private volatile boolean _stop = false;
    
    private volatile boolean _postStarted = false;
    
	private String _json;
	private HTTPDemo _app;;

    private String getUrl()
    {
        return _theUrl;
    }
    
    private String getData()
    {
        return _json;
    }
    
    
    /**
     * Tells whether the thread has started fetching yet.
     * @return True if the fetching has started, false otherwise
     */
    private boolean isStarted()
    {
        return _postStarted;
    }            

    public void post(HTTPDemo app, String url, String json) {
    	_postStarted = true;
    	_theUrl = url;
    	_json = json;
    	_app = app;
    }
    
    /**
     * Stop the thread. 
     */
    public void stop()
    {
        _stop = true;
    }

    /**
     * This method is where the thread retrieves the content from the page 
     * whose url is associated with this thread.
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        for(;;)
        {
            // Thread control
            while( ! _postStarted && !_stop)  
            {
                // Sleep for a bit so we don't spin.
                try 
                {
                    sleep(TIMEOUT);
                } 
                catch (InterruptedException e) 
                {
                    HTTPDemo.errorDialog("Thread#sleep(long) threw " + e.toString());
                }
            }
            
            // Exit condition
            if ( _stop )
            {
                return;
            }
                              
            String content = "";
                
            // Open the connection and extract the data.
            try 
            {
            	HttpConnection conn;
            	if (_postStarted) {
            		//String url = getUrl() + ";deviceside=true;" + "interface=wifi;";
            		String url = getUrl() + ";interface=wifi";
	            	conn = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
	            	//conn = (HttpConnection) Connector.open(getUrl(), Connector.READ_WRITE);
	                conn.setRequestMethod(HttpConnection.POST);
	                conn.setRequestProperty("Content-Language", "en-US");
	                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	                
	                OutputStream os = conn.openOutputStream();
	                os.write(getData().getBytes("UTF-8"));
	                os.flush();
	                os.close();
            	} else {
            		conn = (HttpConnection) Connector.open(getUrl());
            	}

                int status = conn.getResponseCode();
                
                if (status == HttpConnection.HTTP_OK)
                {
                    InputStream input = conn.openInputStream();

                    byte[] data = new byte[256];
                    int len = 0;
                    int size = 0;
                    StringBuffer raw = new StringBuffer();
                        
                    while ( -1 != (len = input.read(data)) )
                    {
                        // Exit condition for the thread. An IOException is 
                        // thrown because of the call to  httpConn.close(), 
                        // causing the thread to terminate.
                        if ( _stop )
                        {
                        	conn.close();
                            input.close();
                        } 
                        raw.append(new String(data, 0, len));
                        size += len;    
                    }   
                             
                    raw.insert(0, "bytes received]\n");
                    raw.insert(0, size);
                    raw.insert(0, '[');
                    content = raw.toString();
                    input.close();                      
                } 
                else 
                {                            
                    content = "response code = " + status;
                }                    
            } 
            catch (IOCancelledException e) 
            {       
                System.out.println(e.toString());                        
                return;
            }
            catch (IOException e) 
            {       
                HTTPDemo.errorDialog(e.toString());                        
                return;
            }
            
            
            // Make sure status thread doesn't overwrite our content
            stopStatusThread();
            _app.updateContent(content);                        
               
            // We're finished with the operation so reset
            // the start state.  
            _postStarted = false;
        }
    }

    /**
     * Stops the status thread
     */
    private void stopStatusThread()
    {
        _app._statusThread.pause();
        try 
        {
            //synchronized(_app._statusThread)
            //{
                // Check the paused condition, in case the notify fires prior to our wait, in which 
                // case we may never see that notify.
                while ( !_app._statusThread.isPaused() );
                {
                    _app._statusThread.wait();
                }
            //}
        } 
        catch (InterruptedException e) 
        {
            HTTPDemo.errorDialog("StatusThread#wait() threw " + e.toString());
        }
    }
}