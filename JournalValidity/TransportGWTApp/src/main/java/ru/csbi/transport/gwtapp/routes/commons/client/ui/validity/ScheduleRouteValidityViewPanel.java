package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.DataProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.disp.RouteVariantValidity;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;
import ru.csbi.transport.gwtapp.client.data.FlexProxy;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BeanListPanel;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BeanPanel;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.EditableTexts;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchController;
import ru.csbi.transport.gwtapp.routes.commons.client.service.JournalValidityServiceAsync;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValidityViewPanel extends BeanListPanel<ScheduleRouteValidity, EditableTexts> implements ScheduleRouteValidityChange
{
  private static final EditableTexts panelTexts = GWT.create(EditableTexts.class);

  private RouteVariantValidity routeVariantValidity;

  protected JournalValidityServiceAsync journalValidityService = JournalValidityServiceAsync.Util.getInstance();

  protected ScheduleRouteValidityViewPanel()
  {
    super(GWT.<GWTBeanInfo>create(ScheduleRouteValidity.class), ScheduleRouteValidity.class, panelTexts);
  }

  @Override
  protected BeanTableView<ScheduleRouteValidity> createTableView(DataProxy<List<ScheduleRouteValidity>> _listDataProxy)
  {
    return new ScheduleRouteValidityBeanView(beanInfo, _listDataProxy);
  }

  @Override
  protected BeanPanel<ScheduleRouteValidity> createSearchForm(SearchController searchController)
  {
    return null;
  }

  @Override
  protected DataProxy<List<ScheduleRouteValidity>> createDataProxy()
  {
    return new FlexProxy<ScheduleRouteValidity>()
    {
      @Override
      public void loadList(FetchCriteria fetchCriteria, AsyncCallback callback)
      {
        getScheduleRouteValidityList(fetchCriteria, callback);
      }
    };
  }

  protected void getScheduleRouteValidityList(FetchCriteria fetchCriteria, AsyncCallback callback)
  {
    if( routeVariantValidity != null )
    {
      journalValidityService.getScheduleRouteValidityList(fetchCriteria, routeVariantValidity, callback);
    }
    else
    {
      callback.onSuccess(new ArrayList<ScheduleRouteValidity>());
    }
  }

  @Override
  public void onRouteVariantValidityChange(RouteVariantValidity _routeVariantValidity)
  {
    routeVariantValidity = _routeVariantValidity;
    reloadFirstPage();
  }
}
