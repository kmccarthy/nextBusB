/**
 * HTTPDemo.java
 * 
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.abk.nextBusB;

import java.io.*;

import javax.microedition.io.*;

import org.json.me.JSONObject;

//import com.abk.newBus.InterpretApi;
//import com.example.nextBus.R;


import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.io.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

/**
 * This sample makes a an http or https connection to a specified URL and
 * retrieves and displays html content.  
 */
public class HTTPDemo extends UiApplication
{
    private HTTPDemoScreen _mainScreen;
    private BasicEditField _url;
    //private RichTextField _content;
    private BrowserField _content;
    

    
    public StatusThread _statusThread = new StatusThread();
    private PostHttp _postHttpThread = new PostHttp();
    private static HTTPDemo theApp;
    
   /**
    * Entry point for application.
    * @param args Command line arguments.
    */
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        theApp = new HTTPDemo();
        theApp.enterEventDispatcher();
    }     
    
    // Constructor
    public HTTPDemo()
    {
        _mainScreen = new HTTPDemoScreen();
        _mainScreen.setTitle("Your Next Bus");

        try {
        	_url = new BasicEditField("Bus Stop #: " , "", 4, BasicEditField.FILTER_NUMERIC);     
        } catch (Exception e) {
			//Log.e("Exception", e.getMessage());
        	 errorDialog("new EditField threw " + e.toString());
		} finally {
		}
        //_url.setCursorPosition(1);
        _mainScreen.add(_url);

        _mainScreen.add(new SeparatorField());

        //_content = new RichTextField();
        _content = new BrowserField();
        _mainScreen.add(_content);

        // Start the helper threads.
        _statusThread.start();
        _postHttpThread.start();

        pushScreen(_mainScreen); 
    }
    
   
   

    /**
     * Method to update the content field.
     * @param text The text to display.
     */
    public void updateContent(final String text)
    {
    	final String _text = text;
        // This will create significant garbage, but avoids threading issues
        // (compared with creating a static Runnable and setting the text).
        UiApplication.getUiApplication().invokeLater(new Runnable() 
        {
            public void run() {
            	String display = "";
            	try {
            		
            		JSONObject json = new JSONObject(text);
        	    	
        	    	if (json.isNull("GetRouteSummaryForStopResult")) {
        	    		display = "No Route summary Information";
        	    	} else {
        		    	JSONObject summary = json.getJSONObject("GetRouteSummaryForStopResult");
        		    	display = InterpretApi.interpretRouteSummaryForStopResult(summary);
        	    	}
            	} catch (Exception e) {
        			//Log.e("Exception", e.getMessage());
        			display = "No Results, please try another stop (ex001)";
        		} finally {
        		}
            	
                //_content.setText(display);
                _content.displayContent(display,"");//"http://localhost"
            }
        });
    }

    /**
     * The StatusThread class manages display of the status message while lengthy 
     * HTTP/HTML operations are taking place.
     */
    public class StatusThread extends Thread
    {        
        private static final int TIMEOUT = 500; // ms
        private static final int THREAD_TIMEOUT = 500;  

        private volatile boolean _stop = false;
        private volatile boolean _running = false;
        private volatile boolean _isPaused = false;


        private void go() {
            _running = true;
            _isPaused = false;
        }

        public void pause() {
            _running = false;
            //_isPaused = true;
        }

        public boolean isPaused(){
            return _isPaused;
        }

        private void stop() {
            _stop = true;
        }

        public void run()
        {
            int i = 0;
            
            String[] statusMsg = new String[6];
            StringBuffer status = new StringBuffer("Working");
            statusMsg[0] = status.toString();
            
            
            for ( int j = 1; j < 6; ++j) {
                statusMsg[j] = status.append(" .").toString();
            }

            for (;;) {
                while (!_stop && !_running)   {
                    // Sleep a bit so we don't spin.
                    try  {
                        sleep(THREAD_TIMEOUT);
                    } 
                    catch ( InterruptedException e)  {
                        errorDialog("Thread#sleep(long) threw " + e.toString());
                    }
                }
                
                if ( _stop ) { return; }
                
                // at this point _running is True OR False
                i = 0;
                
                // Clear the status buffer.
                status.delete(0, status.length()); 
                
                for ( ;; ) {
                    // We're not synchronizing on the boolean flag! Therefore, value is declared volatile.
                    if ( _stop ) { return; }
                    
                    if ( !_running ) {
                        _isPaused = true;
                        
                        synchronized(this) {
                        	_isPaused = true;
                            this.notify();
                        }
                        break;
                    }

                    updateContent(statusMsg[++i%6]);

                    try  {                        
                        Thread.sleep(TIMEOUT); // Wait for a bit.
                    } 
                    catch (InterruptedException e)  {
                        errorDialog("Thread.sleep(long) threw " + e.toString());
                    }
                }
            }
        }
    }    
    
    /**
     * This is the main screen that displays the content fetched by the 
     * ConnectionThread.
     */
    private class HTTPDemoScreen extends MainScreen
    {        
        /**
         * @see net.rim.device.api.ui.container.MainScreen#makeMenu(Menu,int)
         */
        protected void makeMenu(Menu menu, int instance) {
            super.makeMenu(menu, instance);
        }
        
        
        /**
         * Prevent the save dialog from being displayed.
         * 
         * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
         */
        public boolean onSavePrompt() {
            return true;
        }   
        
        
        /**
         * @see net.rim.device.api.ui.Screen#close()
         */
        public void close() {
            _statusThread.stop();
            _postHttpThread.stop();
            
            super.close();
        } 

        /**
         * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
         */
        protected boolean keyChar(char key, int status, int time) {           
            if ( getLeafFieldWithFocus() == _url && key == Characters.ENTER )
            {
            	String url = "https://api.octranspo1.com/v1.2/GetNextTripsForStopAllRoutes";
            	
            	String data = "appID=ed824758&apiKey=c3c57e1c882c12fcea3684ce89176205&format=json&stopNo=";
            	data += _url.getText();
            	
                _postHttpThread.post(theApp, url, data);
                _statusThread.go();

                return true; // I've absorbed this event, so return true.
            }
            else
            {
                return super.keyChar(key, status, time);
            }
        }
    }
    
    /**
     * Presents a dialog to the user with a given message
     * @param message The text to display
     */
    public static void errorDialog(final String message)
    {
        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
            public void run()
            {
                Dialog.alert(message);
            } 
        });
    }
    
    public class PostHttp extends Thread
    {
        private static final int TIMEOUT = 500; // ms

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
                                 
                        //raw.insert(0, "bytes received]\n");
                        //raw.insert(0, size);
                        //raw.insert(0, '[');
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
                updateContent(content);                          
                _postStarted = false;
            }
        }

        /**
         * Stops the status thread
         */
        private void stopStatusThread() {
            _statusThread.pause();
            synchronized(_statusThread) {
                // Check the paused condition, in case the notify fires prior to our wait, in which 
                // case we may never see that notify.
                while ( !_statusThread.isPaused() )
                {
                	try {
                		_statusThread.wait();
                	}
                	catch (InterruptedException e) {
                        HTTPDemo.errorDialog("StatusThread#wait() threw " + e.toString());
                    }
                }
            }
        }
    }
}
