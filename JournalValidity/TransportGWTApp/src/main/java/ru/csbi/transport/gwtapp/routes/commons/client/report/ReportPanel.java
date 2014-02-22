package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.csbi.gwt.beans.GWTBeanInfo;
import ru.csbi.gwt.table.BeanTableView;
import ru.csbi.transport.domain.rep.Report;
import ru.csbi.transport.gwtapp.client.data.FlexProxy;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.*;
import ru.csbi.transport.gwtapp.reports.client.service.ReportsServiceAsync;

/**
 * @author d.pogorelov
 */
public class ReportPanel extends BeanListPanel<Report, EditableTexts>
{
  private static final EditableTexts panelTexts = GWT.create(EditableTexts.class);

  private ReportsServiceAsync reportService = ReportsServiceAsync.Util.getInstance();

  public ReportPanel()
  {
    super(GWT.<GWTBeanInfo>create(Report.class), Report.class, panelTexts);
    setHeading("Выбор отчетов");
  }

  @Override
  protected DataProxy<List<Report>> createDataProxy()
  {
    return new FlexProxy<Report>()
    {
      @Override
      public void loadList(FetchCriteria fetchCriteria, AsyncCallback callback)
      {
        reportService.getReportsByRole(getReportRoles(), callback);
      }
    };
  }

  @Override
  protected BeanTableView<Report> createTableView(DataProxy<List<Report>> _listDataProxy)
  {
    return new BeanTableView<Report>(beanInfo, _listDataProxy)
    {
      @Override
      protected Grid<BeanModel> createGrid(ListStore<BeanModel> _store, ColumnModel _columnModel)
      {
        List<ColumnConfig> lstColumnConfig = _columnModel.getColumns();
        lstColumnConfig.clear();

        CheckBoxSelectionModel<BeanModel> sm = new CheckBoxSelectionModel<BeanModel>();

        ColumnConfig colCheckBox = sm.getColumn();
        lstColumnConfig.add(colCheckBox);
        _columnModel.getColumns().add(new ColumnConfig("name", "Отчет", 250));
        
        Grid<BeanModel> grid = super.createGrid(_store, _columnModel);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        
        return grid;
      }
    };
  }

  @Override
  protected BeanPanel<Report> createSearchForm(SearchController searchController)
  {
    return null;
  }

  //TODO временная реализация с жестким хранением роли
  protected String[] getReportRoles()
  {
    return new String[]{"ROUTE_OPERATOR"};
  }

  protected BaseFastReport.ReportFormat getReportFormat()
  {
    return null;
  }
}
