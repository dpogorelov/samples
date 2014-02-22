package ru.csbi.transport.service.fileexchange.remote;

import java.util.Map;

/**
 * @author d.pogorelov
 * @version $Revision$
 */
public interface RemoteControl
{
  Map<Object, Object> status();

  void shutdown();
}
