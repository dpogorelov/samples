package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ru.csbi.transport.domain.disp.Route;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValidityItem extends TabItem
{
  public ScheduleRouteValidityItem(Route route, ScheduleRouteValidityPanel.ValidityChange validityChange)
  {
    super("По расписаниям");
    setLayout(new FitLayout());
    add(new ScheduleRouteValidityPanel(route, validityChange));
  }
}
