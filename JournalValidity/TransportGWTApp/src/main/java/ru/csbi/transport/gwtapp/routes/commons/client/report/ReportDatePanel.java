package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.util.Date;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author d.pogorelov
 */
public abstract class ReportDatePanel extends ContentPanel
{
  private static final String RADIO_JOURNAL = "journal";
  private static final String RADIO_CUSTOM = "custom";

  private ContentPanel validityPanel;
  private ReportCustomDatePanel customDateContainer;
  private LayoutContainer centerLayoutContainer;
  private Margins margins = new Margins(10, 0, 0, 10);
  private ReportDateValue currentPanelValue;

  public ReportDatePanel()
  {
    setHeading("Выбор даты для печати отчета");
    setLayout(new BorderLayout());

    final Radio dateFromJournal = new Radio();
    dateFromJournal.setBoxLabel("Из Журнала действия");
    dateFromJournal.setValue(true);
    final Radio dateCustom = new Radio();
    dateCustom.setBoxLabel("Задать даты вручную");

    final RadioGroup radioGroup = new RadioGroup();
    radioGroup.add(dateFromJournal);
    radioGroup.add(dateCustom);
    dateFromJournal.setName(RADIO_JOURNAL);
    dateCustom.setName(RADIO_CUSTOM);
    radioGroup.setOrientation(Style.Orientation.VERTICAL);

    radioGroup.addListener(Events.Change, new Listener<BaseEvent>()
    {
      public void handleEvent(BaseEvent be)
      {
        RadioGroup radioGroup = (RadioGroup) be.getSource();
        Radio radio = radioGroup.getValue();
        if( radio != null && radio.getName().equals(RADIO_CUSTOM) )
        {
          processRadio(validityPanel, customDateContainer, true);
          currentPanelValue = customDateContainer;
        }
        else
        {
          processRadio(customDateContainer, validityPanel, false);
          currentPanelValue = (ReportDateValue)validityPanel;
        }
      }
    });

    validityPanel = getValidityPanel(new ReportValidityDateLoad()
    {
      @Override
      public void load(int count)
      {
        if( count > 0 )
        {
          dateFromJournal.setEnabled(true);
          radioGroup.setValue(dateFromJournal);
        }
        else
        {
          dateFromJournal.setEnabled(false);
          radioGroup.setValue(dateCustom);
        }
      }
    });

    customDateContainer = new ReportCustomDatePanel(getDateBeginDefault(), getDateEndDefault());

    BorderLayoutData northLayout = new BorderLayoutData(Style.LayoutRegion.NORTH, 70);
    northLayout.setMargins(margins);
    add(radioGroup, northLayout);

    centerLayoutContainer = new LayoutContainer();
    centerLayoutContainer.setLayout(new FitLayout());
    centerLayoutContainer.add(validityPanel);
    currentPanelValue = (ReportDateValue)validityPanel;

    BorderLayoutData centerLayout = new BorderLayoutData(Style.LayoutRegion.CENTER);
    add(centerLayoutContainer, centerLayout);
  }

  protected abstract ContentPanel getValidityPanel(ReportValidityDateLoad reportValidityDateLoad);

  private void processRadio(Widget removeW, Widget addW, boolean isMargins)
  {
    MarginData marginData = new MarginData();
    if( isMargins )
    {
      marginData.setMargins(margins);
    }

    centerLayoutContainer.remove(removeW);
    centerLayoutContainer.add(addW, marginData);
    centerLayoutContainer.layout();
  }

  public ReportDateValue getCurrentPanelValue()
  {
    return currentPanelValue;
  }

  protected Date getDateBeginDefault()
  {
    return new Date();
  }
  
  protected Date getDateEndDefault()
  {
    return null;
  }
}
