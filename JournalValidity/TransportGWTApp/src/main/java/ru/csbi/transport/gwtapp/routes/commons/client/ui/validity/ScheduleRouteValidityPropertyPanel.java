package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.ScheduleRoute;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;
import ru.csbi.transport.domain.disp.StatusActivity;
import ru.csbi.transport.gwtapp.client.data.FlexProxy;
import ru.csbi.transport.gwtapp.client.service.*;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.*;
import ru.csbi.transport.gwtapp.routes.commons.client.service.RouteServiceAsync;
import ru.csbi.transport.gwtapp.routes.commons.client.ui.schedule.ScheduleRouteBeanView;
import ru.csbi.transport.gwtapp.routes.commons.client.ui.schedule.ScheduleRouteSearchPanel;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValidityPropertyPanel extends BeanPanel<ScheduleRouteValidity>
{
  private RouteServiceAsync service = RouteServiceAsync.Util.getInstance();

  public ScheduleRouteValidityPropertyPanel(final Route route, boolean isEditScheduleRoute)
  {
    super(GWT.<GWTBeanInfo>create(ScheduleRouteValidity.class), ScheduleRouteValidity.class);

    SelectionPanel<ScheduleRoute, EditableTexts> scheduleRoutePanel = new SelectionPanel<ScheduleRoute, EditableTexts>(GWT.<GWTBeanInfo> create(ScheduleRoute.class), ScheduleRoute.class)
    {
      @Override
      protected BeanPanel<ScheduleRoute> createSearchForm(SearchController searchController)
      {
        return new ScheduleRouteSearchPanel(searchController, route)
        {
          @Override
          protected void setAdvancedProperties()
          {
            statusField.setSimpleValue(StatusActivity.ACTIVE);
            statusField.setEnabled(false);
          }

          @Override
          public String getStatusName()
          {
            return "status";
          }
        };
      }

      @Override
      protected BeanTableView<ScheduleRoute> createTableView(DataProxy<List<ScheduleRoute>> _listDataProxy)
      {
        return new ScheduleRouteBeanView(beanInfo, _listDataProxy);
      }

      @Override
      protected DataProxy<List<ScheduleRoute>> createDataProxy()
      {
        return new FlexProxy<ScheduleRoute>()
        {
          @Override
          public void loadList(FetchCriteria fetchCriteria, AsyncCallback callback)
          {
            if( fetchCriteria.getSortField() == null )
              fetchCriteria.setSortField("id");

            if( fetchCriteria.getSortDirection() == null )
              fetchCriteria.setSortDirection(SortDirection.DESC);

            if( route != null )
            {
              Map<String, Serializable> filter = fetchCriteria.getOrCreateFilterModel();  
              filter.put("route.id", route.getId());
            }
            
            service.getScheduleRoutes(fetchCriteria, callback);
          }
        };
      }
    };
    SelectionField<ScheduleRoute> scheduleRouteField = new SelectionField<ScheduleRoute>(scheduleRoutePanel, "id", 750, 600)
    {
      protected void applyFilter(final AsyncCallback<List> callback) throws UnknownEntityNameException, ServerException
      {
        fastFilterModel.put(filteredField, getRawValue());
        FetchCriteria fetchCriteria = new FetchCriteria();
        fetchCriteria.setFilterModel(fastFilterModel);
        fetchCriteria.setLimit(1);

        service.getScheduleRoutes(fetchCriteria, new AsyncCallback<List<ScheduleRoute>>()
        {
          @Override
          public void onFailure(Throwable caught) { callback.onFailure(caught); }
          @Override
          public void onSuccess(List<ScheduleRoute> result) { callback.onSuccess(result); }
        });
        //RepositoryListServiceAsync repository = RepositoryListServiceAsync.Util.getInstance();
        //repository.getObjectList(selectionDialog.getBeanTypeName(), fetchCriteria, callback);
      }
    };
    scheduleRouteField.setAllowBlank(false);
    scheduleRouteField.setEnabled(isEditScheduleRoute);
    addField("scheduleRoute", scheduleRouteField);

    final GwtDateTimeField dateBeginField = new GwtDateTimeField();
    dateBeginField.getDatePicker().addListener(Events.Select, new Listener<ComponentEvent>()
    {
      @Override
      public void handleEvent(ComponentEvent be)
      {
        Date date = dateBeginField.getValue();

        DateWrapper dw = new DateWrapper(date);
        dw = dw.clearTime();
        dw = dw.addHours(3);

        dateBeginField.setValue(dw.asDate());
      }
    });

    DateTimeFastFormatter beginFastFormatter = (DateTimeFastFormatter) dateBeginField.getFastFormatter();
    beginFastFormatter.setDefaultHours(3);
    beginFastFormatter.setDefaultMinutes(0);

    dateBeginField.setAllowBlank(false);

    addField("dateBegin", dateBeginField);

    GwtDateTimeField dateEndField = new GwtDateTimeField();

    addField("dateEnd", dateEndField);

    CheckBox hardScheduleField = new CheckBox();
    hardScheduleField.setBoxLabel("");
    addField("hardSchedule", hardScheduleField);

    TextArea descriptionField = new TextArea();
    descriptionField.setHeight(110);
    descriptionField.setMaxLength(255);
    addField("description", descriptionField);
  }
}
