/**
 * WapOptionsScreen.java
 * 
 * Copyright � 1998-2010 Research In Motion Ltd.
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

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;


/** 
 * A screen that allows the user to specify the WAP parameters 
 * to use when opening an HTTP connection.
 */
public final class WapOptionsScreen extends MainScreen
{   
    private static String WAP_PARAMETERKEY_GWAYIP = ";WapGatewayIP=";
    private static String WAP_PARAMETERKEY_GWAYPORT = ";WapGatewayPort=";
    private static String WAP_PARAMETERKEY_APN = ";WapGatewayAPN=";
    private static String WAP_PARAMETERKEY_SRCIP = ";WapSourceIP=";
    private static String WAP_PARAMETERKEY_SRCPORT = ";WapSourcePort=";
    private static String WAP_DEFAULT_GWAYPORT = "9201";
    private static String WAP_DEFAULT_SOURCEIP = "127.0.0.1";
    private static String WAP_DEFAULT_SOURCEPORT = "8205";    
    
    private UiApplication _app;
    private EditField _gateway;
    private EditField _gatewayPort;
    private EditField _apn;
    private EditField _sourceIP;
    private EditField _sourcePort;
    private String _wapParameters = "";
    private MainScreen _this; 
    private MenuItem _save;     
    
    // Constructor
    public WapOptionsScreen(UiApplication app)
    {
        super();
        _this = this;
        _app = app;
         
        
        _save = new MenuItem("Ok" , 105, 10) 
        {
            public void run()
            {
                formatWapParameters();
                _app.popScreen(_this);
            }
        };
        
        setTitle(new LabelField("Wap Options"));
        
        _gateway = new EditField("Gateway Port: " , null);
        _gatewayPort = new EditField("Gateway Port: " , WAP_DEFAULT_GWAYPORT, EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
        _sourcePort = new EditField("Source Port: " , WAP_DEFAULT_SOURCEPORT, EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
        _apn = new EditField("APN: " , null);
        _sourceIP = new EditField("Source IP: " , WAP_DEFAULT_SOURCEIP);
        
        add(_gateway);
        add(_gatewayPort);
        add(_apn);
        add(_sourcePort);
        add(_sourceIP); 
              
        addMenuItem(_save);
    }     
   
    /**
     * Pushes this screen on to the stack.
     */
    void display()
    {
        _app.pushScreen(this); 
    }
    
    /**
     * Formats all the fields into the wap parameter string for inclusion in 
     * a Connector.open call.
     */
    private void formatWapParameters()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(WAP_PARAMETERKEY_GWAYIP);
        sb.append(_gateway.getText());
        sb.append(WAP_PARAMETERKEY_GWAYPORT);
        sb.append(_gatewayPort.getText());
        sb.append(WAP_PARAMETERKEY_APN);
        sb.append(_apn.getText());
        sb.append(WAP_PARAMETERKEY_SRCIP);
        sb.append(_sourceIP.getText());
        sb.append(WAP_PARAMETERKEY_SRCPORT);
        sb.append(_sourcePort.getText());
        
        _wapParameters = sb.toString();        
    }
    
    /**
     * Returns a preformatted wap parameter string, appropriate to append to an 
     * HTTP Connector.open string.
     * 
     * @return a preformatted wap parameter string.
     */
    String getWapParameters()
    {
        return _wapParameters;
    }

    /**
     * Called when there have been changes and user saves screen.
     */
    public void save()
    {
        formatWapParameters();
    }
}
