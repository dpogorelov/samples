/**
 * $Id$
 *
 * Copyright (C) 2013 CSBI. All Rights Reserved
 */
package ru.csbi.transport.service.fileexchange;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author d.pogorelov
 * @version $Revision$
 */
public class Main
{
  private static final Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args)
  {
    try
    {
      log.info("Loading application context...");

      ConfigurableApplicationContext context = new FileSystemXmlApplicationContext("config/application-config.xml");

      context.registerShutdownHook();

      CountDownLatch shutdownLatch = (CountDownLatch) context.getBean("shutdownLatch");

      log.info("File exchange service started.");

      shutdownLatch.await();

      log.info("File exchange service shutdown...");

      context.close();
    }
    catch( Exception e )
    {
      log.error("Error: " + e, e);
    }
  }
}
