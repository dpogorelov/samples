package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.RouteVariantValidity;

/**
 * @author d.pogorelov
 */
public class RouteVariantValidityItem extends TabItem
{
  public RouteVariantValidityItem(Route route)
  {
    super("По маршрутам");
    setLayout(new BorderLayout());

    final ScheduleRouteValidityViewPanel srPanel = new ScheduleRouteValidityViewPanel();
    RouteVariantValidityPanel rvPanel = new RouteVariantValidityPanel(route);
    rvPanel.addSelectionChangedListener(new SelectionChangedListener<BeanModel>()
    {
      @Override
      public void selectionChanged(SelectionChangedEvent<BeanModel> se)
      {
        RouteVariantValidity routeVariantvalidity = se.getSelectedItem() != null ? (RouteVariantValidity) se.getSelectedItem().getBean() : null;
        onRouteVariantChange(srPanel, routeVariantvalidity);
      }
    });

    BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
    centerData.setSplit(true);
    add(rvPanel, centerData);

    BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 300);
    southData.setSplit(true);
    add(srPanel, southData);
  }

  private void onRouteVariantChange(ScheduleRouteValidityChange validityChange, RouteVariantValidity routeVariantValidity)
  {
    validityChange.onRouteVariantValidityChange(routeVariantValidity);
  }
}
