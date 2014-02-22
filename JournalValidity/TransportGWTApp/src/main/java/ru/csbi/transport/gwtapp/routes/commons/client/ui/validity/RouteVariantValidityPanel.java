package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.RouteVariantValidity;
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
public class RouteVariantValidityPanel extends BeanListPanel<RouteVariantValidity, EditableTexts>
{
  private static final EditableTexts panelTexts = GWT.create(EditableTexts.class);
  private RouteVariantValiditySearchPanel searchPanel;
  private JournalValidityServiceAsync journalValidityService = JournalValidityServiceAsync.Util.getInstance();
  private Route route;
  
  public RouteVariantValidityPanel(Route _route)
  {
    super(GWT.<GWTBeanInfo>create(RouteVariantValidity.class), RouteVariantValidity.class, panelTexts);
    route = _route;
    if( searchPanel != null )
    {
      searchPanel.init(route);
    }
  }

  @Override
  protected BeanPanel<RouteVariantValidity> createSearchForm(SearchController searchController)
  {
    searchPanel = new RouteVariantValiditySearchPanel(searchController);

    return searchPanel;
  }

  @Override
  protected BeanTableView<RouteVariantValidity> createTableView(DataProxy<List<RouteVariantValidity>> _listDataProxy)
  {
    BeanTableView<RouteVariantValidity> tableView = super.createTableView(_listDataProxy);
    Grid<BeanModel> grid = tableView.getGrid();
    ColumnModel columnModel = grid.getColumnModel();

    DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    columnModel.getColumnById("dateBegin").setDateTimeFormat(format);
    columnModel.getColumnById("dateEnd").setDateTimeFormat(format);

    return tableView;
  }

  @Override
  protected DataProxy<List<RouteVariantValidity>> createDataProxy()
  {
    return new FlexProxy<RouteVariantValidity>()
    {
      @Override
      public void loadList(FetchCriteria fetchCriteria, AsyncCallback callback)
      {
        getRouteVariantList(fetchCriteria, callback);
      }
    };
  }

  protected void getRouteVariantList(FetchCriteria fetchCriteria, AsyncCallback callback)
  {
    if( route != null )
    {
      Map<String, Serializable> filter = fetchCriteria.getOrCreateFilterModel();
      filter.put("scheduleRoute.routeVariant.route.id", route.getId());
    }
    journalValidityService.getRouteVariantList(fetchCriteria, callback);
  }
}
