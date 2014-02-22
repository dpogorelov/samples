package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import com.extjs.gxt.ui.client.widget.TabPanel;

import ru.csbi.gwt.actions.ActionPanel;

/**
 * @author d.pogorelov
 */
@ActionPanel( "transport.psv.journalValidity" )
public class JournalValidityPanel extends TabPanel
{
  public JournalValidityPanel()
  {
    add(new ScheduleRouteValidityItem(null, null));

    add(new RouteVariantValidityItem(null));
  }
}
