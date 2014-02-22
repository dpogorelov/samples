package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.disp.Route;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;
import ru.csbi.transport.gwtapp.client.data.FlexProxy;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.*;
import ru.csbi.transport.gwtapp.routes.commons.client.service.JournalValidityServiceAsync;

/**
 * @author d.pogorelov
 */
public class ScheduleRouteValidityPanel extends EditableBeanListPanel<ScheduleRouteValidity, EditableTexts>
{
  public interface ValidityChange
  {
    void reloadCurrentPage();
  }

  private static final EditableTexts panelTexts = GWT.create(EditableTexts.class);
  private JournalValidityServiceAsync journalValidityService = JournalValidityServiceAsync.Util.getInstance();
  private ScheduleRouteValiditySearchPanel searchPanel;
  private Route route;
  private ValidityChange validityChange;
  private ScheduleRouteValidity saveValidity;

  public ScheduleRouteValidityPanel(Route _route, ValidityChange _validityChange)
  {
    super(GWT.<GWTBeanInfo>create(ScheduleRouteValidity.class), ScheduleRouteValidity.class, panelTexts);
    route = _route;
    validityChange = _validityChange;
    searchPanel.init(route);
  }

  @Override
  protected BeanPanel<ScheduleRouteValidity> createSearchForm(SearchController searchController)
  {
    searchPanel = new ScheduleRouteValiditySearchPanel(searchController);
    return searchPanel;
  }

  @Override
  protected DataProxy<List<ScheduleRouteValidity>> createDataProxy()
  {
    return new FlexProxy<ScheduleRouteValidity>()
    {
      @Override
      public void loadList(FetchCriteria fetchCriteria, AsyncCallback callback)
      {
        if( route != null )
        {
          Map<String, Serializable> filter = fetchCriteria.getOrCreateFilterModel();
          filter.put("route.id", route.getId());
        }
        journalValidityService.getScheduleRouteValidityList(fetchCriteria, callback);
      }
    };
  }

  @Override
  protected BeanTableView<ScheduleRouteValidity> createTableView(DataProxy<List<ScheduleRouteValidity>> _listDataProxy)
  {
    return new ScheduleRouteValidityBeanView(beanInfo, _listDataProxy);
  }

  @Override
  protected BeanPropertyWindow<ScheduleRouteValidity> createAddWindow(
     SaveController<ScheduleRouteValidity> saveController)
  {
    BeanPropertyWindow<ScheduleRouteValidity> window = new BeanPropertyWindow<ScheduleRouteValidity>
       (texts.addName(), texts.saveName(), new ScheduleRouteValidityPropertyPanel(route, true),
          saveController);
    window.setSize(430, 330);
    return window;
  }

  @Override
  protected BeanPropertyWindow<ScheduleRouteValidity> createEditWindow(
     SaveController<ScheduleRouteValidity> saveController)
  {
    BeanPropertyWindow<ScheduleRouteValidity> window = new BeanPropertyWindow<ScheduleRouteValidity>
       (texts.editName(), texts.saveName(), new ScheduleRouteValidityPropertyPanel(route, false),
          saveController);
    window.setSize(430, 330);
    return window;
  }

  @Override
  protected void performAdd(final Window parent, final ScheduleRouteValidity bean)
  {
    final MessageBox waitBox = MessageBox.wait(texts.addName(), texts.addProgress(), "");

    journalValidityService.checkSwitchBeforeAdd(bean, new AsyncCallback<String>()
    {
      @Override
      public void onFailure(Throwable caught)
      {
        waitBox.close();

        MessageBox alertBox = MessageBox.alert(texts.saveName(), caught.getMessage(), null);
        alertBox.setIcon(MessageBox.ERROR);
      }

      @Override
      public void onSuccess(String result)
      {
        waitBox.close();

        if( result != null )
        {
          MessageBox.alert(texts.saveName(), result, new Listener<MessageBoxEvent>()
          {
            @Override
            public void handleEvent(MessageBoxEvent be)
            {
              Button button = be.getButtonClicked();
              if( Dialog.OK.equals(button.getItemId()) )
              {
                addBean(parent, bean);
              }
            }
          });
        }
        else
        {
          addBean(parent, bean);
        }
      }
    });
  }

  @Override
  protected void performDelete(final ScheduleRouteValidity selectedItem)
  {
    final MessageBox waitBox = MessageBox.wait(texts.deleteName(), texts.deleteProgress(), "");

    journalValidityService.getSwitchIds(selectedItem, new AsyncCallback<String>()
    {
      @Override
      public void onFailure(Throwable caught)
      {
        waitBox.close();

        MessageBox alertBox = MessageBox.alert(texts.deleteName(), caught.getMessage(), null);
        alertBox.setIcon(MessageBox.ERROR);
      }

      @Override
      public void onSuccess(String result)
      {
        waitBox.close();

        if( result != null )
        {
          String messageConfirm = "Расписание имеет переключение. Удаление периода действия расписания " + selectedItem
             .getPeriodString() + " приведет к удалению периодов действия связанных расписаний: " + result + ". Удалить период действия?";

          MessageBox.confirm(texts.deleteName(), messageConfirm, new Listener<MessageBoxEvent>()
          {
            @Override
            public void handleEvent(MessageBoxEvent be)
            {
              Button button = be.getButtonClicked();
              if( Dialog.YES.equals(button.getItemId()) )
              {
                removeBean(selectedItem);
              }
            }
          });
        }
        else
        {
          removeBean(selectedItem);
        }
      }
    });
  }

  @Override
  protected void performSave(final Window parent, final ScheduleRouteValidity bean)
  {
    final MessageBox waitBox = MessageBox.wait(texts.saveName(), texts.saveProgress(), "");

    journalValidityService.getSwitchIds(bean, new AsyncCallback<String>()
    {
      @Override
      public void onFailure(Throwable caught)
      {
        waitBox.close();

        MessageBox alertBox = MessageBox.alert(texts.saveName(), caught.getMessage(), null);
        alertBox.setIcon(MessageBox.ERROR);
      }

      @Override
      public void onSuccess(String result)
      {
        waitBox.close();

        if( result != null )
        {
          String messageConfirm = "Расписание содержит переключения, период действия будет изменен у всех связанных расписаний: " + result +
             ". Сохранить период действия?";

          MessageBox.confirm(texts.saveName(), messageConfirm, new Listener<MessageBoxEvent>()
          {
            @Override
            public void handleEvent(MessageBoxEvent be)
            {
              Button button = be.getButtonClicked();
              if( Dialog.YES.equals(button.getItemId()) )
              {
                saveBean(parent, bean);
              }
            }
          });
        }
        else
        {
          saveBean(parent, bean);
        }
      }
    });
  }

  private void addBean(Window parent, ScheduleRouteValidity bean)
  {
    super.performAdd(parent, bean);
  }

  private void removeBean(ScheduleRouteValidity bean)
  {
    super.performDelete(bean);
  }

  private void saveBean(Window parent, ScheduleRouteValidity bean)
  {
    super.performSave(parent, bean);
  }

  @Override
  protected void onEdit(ScheduleRouteValidity bean)
  {
    saveValidity = new ScheduleRouteValidity(bean);
    super.onEdit(bean);
  }

  @Override
  protected void serviceAdd(ScheduleRouteValidity bean, AsyncCallback<Void> callback)
  {
    journalValidityService.addSRValidity(bean, callback);
  }

  @Override
  protected void serviceSave(final ScheduleRouteValidity bean, final AsyncCallback<Void> callback)
  {
    journalValidityService.saveSRValidity(bean, new AsyncCallback<Void>()
    {
      @Override
      public void onFailure(Throwable caught)
      {
        bean.copyOf(saveValidity);
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(Void result)
      {
        callback.onSuccess(result);
      }
    });
  }

  @Override
  protected void serviceRemove(ScheduleRouteValidity bean, AsyncCallback<Void> callback)
  {
    journalValidityService.removeSRValidity(bean, callback);
  }

  @Override
  protected ScheduleRouteValidity createNewBean()
  {
    return new ScheduleRouteValidity();
  }

  @Override
  public void reloadCurrentPage()
  {
    super.reloadCurrentPage();
    if( validityChange != null )
    {
      validityChange.reloadCurrentPage();
    }
  }
}
