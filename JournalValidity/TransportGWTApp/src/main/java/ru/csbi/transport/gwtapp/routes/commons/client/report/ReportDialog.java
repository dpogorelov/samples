package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ru.csbi.transport.domain.disp.RouteVariant;
import ru.csbi.transport.domain.rep.Report;
import ru.csbi.transport.domain.report.ReportParamType;

/**
 * @author d.pogorelov
 */
public class ReportDialog extends ReportBaseDialog
{
  private final static String STATIONS_REPORT_FILE_NAME = "sked_stations";

  private RouteVariant routeVariant;

  public ReportDialog(String title, final RouteVariant _routeVariant)
  {
    super(title, new ReportDatePanel()
    {
      @Override
      protected ContentPanel getValidityPanel(ReportValidityDateLoad reportValidityDateLoad)
      {
        return new ReportValidityDatePanel(_routeVariant, reportValidityDateLoad);
      }
    }, new ReportPanel());

    routeVariant = _routeVariant;
  }

  @Override
  protected Map<String, Serializable> getParams(Report report, Date dateBegin, Date dateEnd)
  {
    Map<String, Serializable> params = super.getParams(report, dateBegin, dateEnd);
    params.put(ReportParamType.ROUTE_VARIANT.getSysName(), routeVariant.getId());
    params.put(ReportParamType.ROUTE.getSysName(), routeVariant.getRoute().getId());
    if( report.getFileName().startsWith(STATIONS_REPORT_FILE_NAME) )
    {
      params.put(ReportParamType.DATE_BEGIN.getSysName(), new Date());
    }

    return params;
  }
}
