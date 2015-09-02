package com.datatorrent.alerts.conf;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertsProperties extends Properties {
  private static final long serialVersionUID = 5431582743985211890L;

  private static final Logger logger = LoggerFactory.getLogger(AlertsProperties.class);
  
  private final String PROP_ALERTS_PROP_FILE = "alerts.conf.file";
  private final String ALERTS_PROP_FILE_NAME = "dt-alerts.properties";
  
  private static AlertsProperties instance;
  
  public static AlertsProperties instance()
  {
    if(instance == null )
    {
      synchronized(AlertsProperties.class)
      {
        if(instance == null )
        {
          instance = new AlertsProperties();
          instance.load();
        }
      }
    }
    return instance;
  }
  
  public void load()
  {
    String filePath = System.getProperty(PROP_ALERTS_PROP_FILE);
    if(filePath == null )
    {
      URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(ALERTS_PROP_FILE_NAME);
      if(fileUrl != null)
        filePath = fileUrl.getPath();
    }
    if(filePath == null || filePath.isEmpty())
    {
      logger.warn("Can Not find configure file {}.", ALERTS_PROP_FILE_NAME);
      return;
    }
    try
    {
      load( new FileReader(filePath) );
    }
    catch(IOException e)
    {
      logger.warn("load properties exception.", e);
    }
  }
}