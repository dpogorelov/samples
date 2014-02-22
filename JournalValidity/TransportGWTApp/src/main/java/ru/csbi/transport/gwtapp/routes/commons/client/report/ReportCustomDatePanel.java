package ru.csbi.transport.gwtapp.routes.commons.client.ui.report;

import java.util.Date;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.*;

import ru.csbi.transport.gwtapp.client.ui.panels.commons.GwtDateField;

/**
 * @author d.pogorelov
 */
public class ReportCustomDatePanel extends ContentPanel implements ReportDateValue
{
  private GwtDateField dateBeginField = new GwtDateField();
  private GwtDateField dateEndField = new GwtDateField();

  public ReportCustomDatePanel(Date dateBegin, Date dateEnd)
  {
    setLayout(new BorderLayout());
    setHeaderVisible(false);
    setBodyBorder(false);

    FormData formData = new FormData();
    formData.setWidth(90);

    LayoutContainer columnDate = getColumnContainer();
    dateBeginField.setValue(dateBegin);
    dateBeginField.setFieldLabel("Начало действия");
    dateBeginField.setData("formData", formData);
    columnDate.add(dateBeginField);

    dateEndField.setValue(dateEnd);
    dateEndField.setFieldLabel("Окончание действия");
    dateEndField.setData("formData", formData);
    columnDate.add(dateEndField);

    add(columnDate, new BorderLayoutData(Style.LayoutRegion.NORTH));
  }

  private LayoutContainer getColumnContainer()
  {
    LayoutContainer column = new LayoutContainer();
    FormLayout formLayout = new FormLayout();
    formLayout.setLabelAlign(FormPanel.LabelAlign.LEFT);
    formLayout.setLabelWidth(175);
    column.setLayout(formLayout);

    return column;
  }

  @Override
  public Date getDateBegin()
  {
    return dateBeginField.getValue();
  }

  @Override
  public Date getDateEnd()
  {
    return dateEndField.getValue();
  }
}
