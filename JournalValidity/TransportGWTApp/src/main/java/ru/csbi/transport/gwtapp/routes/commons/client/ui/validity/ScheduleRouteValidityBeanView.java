package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValidityBeanView extends BeanTableView<ScheduleRouteValidity>
{
  public ScheduleRouteValidityBeanView(GWTBeanInfo beanInfo, DataProxy<List<ScheduleRouteValidity>> dataProxy)
  {
    super(beanInfo, beanInfo.getColumns("dateBegin", "dateEnd", "hardSchedule", "description"), dataProxy);
  }

  @Override
  protected Grid<BeanModel> createGrid(ListStore<BeanModel> store, ColumnModel columnModel)
  {
    columnModel.getColumns().addAll(0, getCustomColumns());

    return super.createGrid(store, columnModel);
  }

  private List<ColumnConfig> getCustomColumns()
  {
    List<ColumnConfig> lstColumns = new ArrayList<ColumnConfig>();
    lstColumns.add(new ColumnConfig("scheduleRoute.id", "ID", 50));
    lstColumns.add(new ColumnConfig("scheduleRoute.daysOfWeek", "Дни недели", 100));
    lstColumns.add(new ColumnConfig("route.routeNumber", "Номер маршрута", 120));
    lstColumns.add(new ColumnConfig("routeVariant.id", "ID в.м.", 50));

    return lstColumns;
  }
}
