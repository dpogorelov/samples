package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.io.Serializable;
import java.util.*;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ru.csbi.transport.domain.rep.Report;
import ru.csbi.transport.domain.rep.ReportParam;
import ru.csbi.transport.domain.report.ReportParamType;
import ru.csbi.transport.domain.rep.ReportSub;
import ru.csbi.transport.gwtapp.client.ui.panels.commons.BaseFastReport;

/**
 * @author d.pogorelov
 */
public abstract class ReportBaseDialog extends Window
{
  private ReportDatePanel reportDatePanel;
  private ReportPanel reportPanel;

  public ReportBaseDialog(String title, ReportDatePanel _reportDatePanel, ReportPanel _reportPanel)
  {
    setHeading(title);
    setModal(true);
    setLayout(new BorderLayout());
    reportDatePanel = _reportDatePanel;
    reportPanel = _reportPanel;

    BorderLayoutData northLayout = new BorderLayoutData(Style.LayoutRegion.NORTH, 300);
    northLayout.setSplit(true);
    add(reportDatePanel, northLayout);

    BorderLayoutData centerLayout = new BorderLayoutData(Style.LayoutRegion.CENTER);
    centerLayout.setSplit(true);
    add(reportPanel, centerLayout);

    addButton(createPrintButton());
    addButton(createCancelButton());

    setSize(550, 750);
  }
  
  private Button createPrintButton()
  {
    Button printBtn = new Button("Печать", new SelectionListener<ButtonEvent>()
    {
      @Override
      public void componentSelected(ButtonEvent ce)
      {
        onPrintPressed();
      }
    });

    printBtn.ensureDebugId("printButton");

    return printBtn;
  }

  private Button createCancelButton()
  {
    Button cancelButton = new Button(GXT.MESSAGES.messageBox_cancel(), new SelectionListener<ButtonEvent>()
    {
      @Override
      public void componentSelected(ButtonEvent ce)
      {
        hide(ce.getButton());
      }
    });

    cancelButton.ensureDebugId("cancelButton");

    return cancelButton;
  }

  protected void onPrintPressed()
  {
    List<Report> lstReport = reportPanel.getSelectedItems();
    if( lstReport.size() == 0 )
    {
      MessageBox.alert("Ошибка", "Необходимо выбрать отчет для печати", null);
    }
    else
    {
      ReportDateValue reportDateValue = reportDatePanel.getCurrentPanelValue();
      Date dateBegin = reportDateValue.getDateBegin();
      Date dateEnd = reportDateValue.getDateEnd();
      if( dateBegin == null )
      {
        MessageBox.alert("Ошибка", "Необходимо ввести дату начала или выбрать запись из Журнала действия", null);
      }
      else
      {
        for( Report report : lstReport )
        {
          createReport(report, getParams(report, dateBegin, dateEnd), reportPanel.getReportFormat());
        }
      }
    }
  }

  protected Map<String, Serializable> getParams(Report report, Date dateBegin, Date dateEnd)
  {
    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put(ReportParamType.DATE_BEGIN.getSysName(), dateBegin);
    params.put(ReportParamType.DATE_END.getSysName(), dateEnd);

    return params;
  }

  private BaseFastReport getBaseFastReport(final Report report, BaseFastReport.ReportFormat reportFormat)
  {
    return new BaseFastReport(reportFormat)
    {
      @Override
      public Long getReportId() {
        return report.getId();
      }

      @Override
      protected String getReportName(Map<String, Serializable> params)
      {
        return report.getFileName();
      }
    };
  }

  private BaseFastReport.ReportFormat getDefaultReportFormat()
  {
    return BaseFastReport.ReportFormat.xls;
  }

  private void createReport(Report report, Map<String, Serializable> params, BaseFastReport.ReportFormat reportFormat)
  {
    BaseFastReport fastReport = getBaseFastReport(report, reportFormat != null ? reportFormat : getDefaultReportFormat());

    if( report.getCreateSubs() != null )
    {
      Map<String, String> subReports = fastReport.getSubreports();
      for( ReportSub sub : report.getCreateSubs() )
      {
        subReports.put(sub.getParameterName(), sub.getReportSubFileName() + ".jasper");
      }
    }

    fillParams(report, fastReport, params);
  }

  protected void fillParams(Report report, final BaseFastReport fastReport, final Map<String, Serializable> params)
  {
    for( ReportParam reportParam : report.getCreateParams() )
    {
      ReportParamType paramType = reportParam.getParamType();
      String paramName = analyzeParamName(paramType);
      if( params.containsKey(paramName) )
      {
        fastReport.addReportParam(paramName, params.get(paramName));
      }
    }

    fastReport.createReport();
  }

  protected String analyzeParamName(ReportParamType paramType)
  {
    return paramType.getSysName();
  }
}
