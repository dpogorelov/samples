package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.transport.domain.disp.RouteVariant;
import ru.csbi.transport.domain.disp.RouteVariantValidity;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BeanPanel;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.SearchController;
import ru.csbi.transport.gwtapp.routes.commons.client.ui.validity.RouteVariantValidityPanel;

/**
 * @author d.pogorelov
 */
public class ReportValidityDatePanel extends RouteVariantValidityPanel implements ReportDateValue
{
  private RouteVariant routeVariant;
  
  public ReportValidityDatePanel(RouteVariant _routeVariant, final ReportValidityDateLoad validityDateLoad)
  {
    super(null);
    routeVariant = _routeVariant;
    final ListStore<BeanModel> listStore = getGrid().getStore();
    listStore.getLoader().addLoadListener(new LoadListener()
    {
      @Override
      public void loaderLoad(LoadEvent le)
      {
        validityDateLoad.load(listStore.getCount());
        if( listStore.getCount() == 1 )
        {
          getGrid().getSelectionModel().select(0, false);
        }
      }
    });
  }
  
  @Override
  protected BeanPanel<RouteVariantValidity> createSearchForm(SearchController searchController)
  {
    return null;
  }

  @Override
  protected void getRouteVariantList(FetchCriteria fetchCriteria, AsyncCallback callback)
  {
    Map<String, Serializable> filter = fetchCriteria.getOrCreateFilterModel();
    filter.put("scheduleRoute.routeVariant.id", routeVariant.getId());

    super.getRouteVariantList(fetchCriteria, callback);
  }

  @Override
  public Date getDateBegin()
  {
    return getSelectedItem() != null ? getSelectedItem().getDateBegin() : null;
  }

  @Override
  public Date getDateEnd()
  {
    return getSelectedItem() != null ? getSelectedItem().getDateEnd() : null;
  }
}
