package ru.csbi.transport.service.fileexchange.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.MBeanServerConnection;

import org.apache.log4j.Logger;
import org.springframework.jmx.access.MBeanProxyFactoryBean;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;

/**
 * @author d.pogorelov
 * @version $Revision$
 */
@SuppressWarnings( {"UseOfSystemOutOrSystemErr"} )
public class RemoteControlImpl implements RemoteControlImplMBean
{
  private static final int JMX_CONNECTION_TIMEOUT = 10 * 1000;
  private static final String CONTROL_OBJECT_NAME = "ru.csbi.transport.service.fileexchange:name=control";

  private static final Logger log = Logger.getLogger(RemoteControlImpl.class);

  private ReentrantLock shutdownLock = new ReentrantLock();

  private enum CmdKeyword
  {
    STATUS,
    SHUTDOWN
  }

  private CountDownLatch shutdownLatch;

  public RemoteControlImpl()
  {
  }

  public void setShutdownLatch(CountDownLatch _shutdownLatch)
  {
    shutdownLatch = _shutdownLatch;
  }

  @Override
  public synchronized void shutdown()
  {
    log.debug("shutdown()");

    shutdownLock.lock();

    try
    {
      if( shutdownLatch.getCount() <= 0 )
        return;

      log.debug("Shutdown preparation done");
      shutdownLatch.countDown();
    }
    finally
    {
      shutdownLock.unlock();
    }
  }

  @Override
  public Map<Object, Object> status()
  {
    Map<Object, Object> map = new HashMap<Object, Object>();
    map.put("run", shutdownLatch.getCount() > 0);
    return map;
  }


  private static void operate(String hosport, CmdKeyword cmd, String[] params) throws Exception
  {
    int idx = hosport.indexOf(':');
    String host = hosport.substring(0, idx);
    int port = Integer.parseInt(hosport.substring(idx + 1));

    String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";

    MBeanServerConnectionFactoryBean cf = new MBeanServerConnectionFactoryBean();

    try
    {
      Map<String, Object> environment = new HashMap<String, Object>();
      environment.put("jmx.remote.x.request.waiting.timeout", JMX_CONNECTION_TIMEOUT);

      cf.setEnvironmentMap(environment);
      cf.setServiceUrl(url);
      cf.afterPropertiesSet();

      MBeanServerConnection conn = cf.getObject();

      MBeanProxyFactoryBean bean = new MBeanProxyFactoryBean();

      try
      {
        bean.setObjectName(CONTROL_OBJECT_NAME);
        bean.setProxyInterface(RemoteControl.class);
        bean.setServer(conn);
        bean.afterPropertiesSet();

        RemoteControl sc = (RemoteControl) bean.getObject();

        switch( cmd )
        {
          case STATUS:
          {
            System.out.println("Status");
            System.out.println(sc.status());
            break;
          }
          case SHUTDOWN:
          {
            System.out.println("Shutdown");
            try
            {
              sc.shutdown();
            }
            catch( Exception e )
            {
              log.error("Error: " + e, e);
            }
            break;
          }
        }
      }
      finally
      {
        try
        {
          bean.destroy();
        }
        catch( Throwable ignore )
        {
        }
      }
    }
    finally
    {
      try
      {
        cf.destroy();
      }
      catch( Throwable e )
      {
        log.error("Error: " + e, e);
      }
    }
  }


  public static void main(String[] args) throws Exception
  {
    if( args.length < 2 )
    {
      System.out
         .println("Usage: java -cp ... " + RemoteControlImpl.class.getName() + " <jmx_host>:<jmx_port> <command>");
      return;
    }

    String hosport = args[0]; // localhost:19050
    CmdKeyword cmd = CmdKeyword.valueOf(args[1].toUpperCase());

    String[] params = new String[args.length - 2];
    System.arraycopy(args, 2, params, 0, args.length - 2);

    operate(hosport, cmd, params);
  }
}
