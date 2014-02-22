package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;
import ru.csbi.transport.domain.nsi.DaysOfWeek;
import ru.csbi.transport.domain.nsi.TransportType;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BeanListComboBox;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchController;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchPanel;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValiditySearchPanel extends SearchPanel<ScheduleRouteValidity>
{
  private TextField<String> routeField;
  private BeanListComboBox<TransportType> comboTransportType;

  public ScheduleRouteValiditySearchPanel(SearchController searchController)
  {
    super(GWT.<GWTBeanInfo>create(ScheduleRouteValidity.class), ScheduleRouteValidity.class, searchController);
    setColumnCount(3, 350);
    setPreferredHeight(160);

    NumberField idField = new NumberField();
    idField.setFieldLabel("ID расписания");
    idField.setPropertyEditorType(Long.class);
    addField("scheduleRoute.id", idField, 0);

    BeanListComboBox<DaysOfWeek> comboDaysOfWeek = new BeanListComboBox<DaysOfWeek>(
       GWT.<GWTBeanInfo>create(DaysOfWeek.class));
    comboDaysOfWeek.setFieldLabel("Дни недели");
    addField("scheduleRoute.daysOfWeek", comboDaysOfWeek, 0);

    DateField actionFromField = new DateField();
    actionFromField.setFieldLabel("Дата с");
    actionFromField.setWidth(90);
    addField("validityTimestamp.start", actionFromField, 1);

    DateField actionToField = new DateField();
    actionToField.setFieldLabel("по");
    actionToField.setWidth(90);
    addField("validityTimestamp.end", actionToField, 1);

    routeField = new TextField<String>();
    routeField.setFieldLabel("Номер маршрута");
    addField("route.routeNumber", routeField, 2);

    NumberField idRouteVariantField = new NumberField();
    idRouteVariantField.setFieldLabel("ID варианта");
    idRouteVariantField.setPropertyEditorType(Long.class);
    addField("routeVariant.id", idRouteVariantField, 2);

    comboTransportType = new BeanListComboBox<TransportType>(GWT.<GWTBeanInfo>create(TransportType.class));
    comboTransportType.setFieldLabel("Тип ТС");
    addField("route.transportType", comboTransportType, 2);
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
