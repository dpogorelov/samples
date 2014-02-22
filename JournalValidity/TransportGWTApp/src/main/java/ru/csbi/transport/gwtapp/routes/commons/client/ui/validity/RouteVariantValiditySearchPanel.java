package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.util.Date;

import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.RouteVariantValidity;
import ru.csbi.transport.domain.nsi.TransportType;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BeanListComboBox;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchController;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchPanel;

/**
 * @author d.pogorelov
 */
public class RouteVariantValiditySearchPanel extends SearchPanel<RouteVariantValidity>
{
  private DateField actionFromField;
  private TextField<String> routeField;
  private BeanListComboBox<TransportType> comboTransportType;
  
  public RouteVariantValiditySearchPanel(SearchController searchController)
  {
    super(GWT.<GWTBeanInfo>create(RouteVariantValidity.class), RouteVariantValidity.class, searchController);
    setColumnCount(2, 350);
    setPreferredHeight(160);

    routeField = new TextField<String>();
    routeField.setFieldLabel("Номер маршрута");
    addField("scheduleRoute.routeVariant.route.routeNumber", routeField, 0);

    NumberField idRouteVariantField = new NumberField();
    idRouteVariantField.setFieldLabel("ID варианта");
    idRouteVariantField.setPropertyEditorType(Long.class);
    addField("scheduleRoute.routeVariant.id", idRouteVariantField, 0);

    comboTransportType = new BeanListComboBox<TransportType>(GWT.<GWTBeanInfo>create(TransportType.class));
    comboTransportType.setFieldLabel("Тип ТС");
    addField("scheduleRoute.routeVariant.route.transportType", comboTransportType, 0);

    actionFromField = new DateField();
    actionFromField.setFieldLabel("Дата с");
    actionFromField.setWidth(90);
    actionFromField.setValue(new DateWrapper(new Date()).clearTime().asDate());
    addField("validityTimestamp.start", actionFromField, 1);

    DateField actionToField = new DateField();
    actionToField.setFieldLabel("по");
    actionToField.setWidth(90);
    addField("validityTimestamp.end", actionToField, 1);
  }

  @Override
  public void reset()
  {
    super.reset();
    actionFromField.clear();
  }

  public void init(Route route)
  {
    if( route != null )
    {
      routeField.setEnabled(false);
      routeField.setValue(route.getRouteNumber());
      comboTransportType.setEnabled(false);
      comboTransportType.setSimpleValue(route.getTransportType());
    }
  }
}
