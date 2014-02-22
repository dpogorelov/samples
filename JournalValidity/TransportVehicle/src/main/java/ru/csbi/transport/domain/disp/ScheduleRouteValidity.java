package ru.csbi.transport.domain.disp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import ru.csbi.gwt.dto.ClientSide;
import ru.csbi.gwt.dto.DTO;
import ru.csbi.transport.domain.core.FCOMutableEntity;
import ru.csbi.util.TimeInterval;
import ru.csbi.util.beans.BeanDesc;
import ru.csbi.util.beans.OrderByDesc;

/**
 * Периоды действия расписаний
 *
 * @author d.pogorelov
 */
@Entity( name = "disp.SR_Validity" )
@DTO
@BeanDesc( columns = {"scheduleRoute", "dateBegin", "dateEnd", "hardSchedule", "description"} )
@Cache( usage = CacheConcurrencyStrategy.READ_WRITE )
public class ScheduleRouteValidity extends FCOMutableEntity
{
  @ManyToOne( optional = false, fetch = FetchType.LAZY )
  @JoinColumn( name = "schedule_route", insertable = false, updatable = false, nullable = false )
  private ScheduleRoute scheduleRoute;

  @Temporal( TemporalType.TIMESTAMP )
  @Basic( optional = false )
  private Date dateBegin;

  @Temporal( TemporalType.TIMESTAMP )
  private Date dateEnd;

  private boolean hardSchedule;

  private String description;

  public static final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
  
  public ScheduleRouteValidity()
  {}

  @ClientSide("public ScheduleRouteValidity(ScheduleRouteValidity other)\n" +
     "  {\n" +
     "    copyOf(other);\n" +
     "  }")
  public ScheduleRouteValidity(ScheduleRouteValidity other)
  {
    copyOf(other);
  }

  public static void addRouteRestriction(Criteria validityCriteria, Long routeId)
  {
    validityCriteria.createAlias("scheduleRoute", "scheduleRoute");
    validityCriteria.createAlias("scheduleRoute.routeVariant", "routeVariant");
    validityCriteria.add(Restrictions.eq("routeVariant.route.id", routeId));
  }

  public static void addIntervalRestriction(Criteria validityCriteria, TimeInterval interval)
  {
    if( interval.getStart() != null )
    {
      Disjunction disjunction = Restrictions.disjunction();
      disjunction.add(Restrictions.and(Restrictions.le("dateBegin", interval.getStart()),
         Restrictions.or(Restrictions.isNull("dateEnd"), Restrictions.ge("dateEnd", interval.getStart()))));
      disjunction.add(Restrictions.ge("dateBegin", interval.getStart()));
      validityCriteria.add(disjunction);
    }
    if( interval.getEnd() != null )
    {
      validityCriteria.add(Restrictions.le("dateBegin", interval.getEnd()));
    }
  }

  @ClientSide("public void copyOf(ScheduleRouteValidity other)\n" +
     "  {\n" +
     "    scheduleRoute = other.getScheduleRoute();\n" +
     "    dateBegin = other.getDateBegin();\n" +
     "    dateEnd = other.getDateEnd();\n" +
     "    hardSchedule = other.isHardSchedule();\n" +
     "    description = other.getDescription();\n" +
     "  }")
  public void copyOf(ScheduleRouteValidity other)
  {
    scheduleRoute = other.getScheduleRoute();
    dateBegin = other.getDateBegin();
    dateEnd = other.getDateEnd();
    hardSchedule = other.isHardSchedule();
    description = other.getDescription();
  }

  public ScheduleRoute getScheduleRoute()
  {
    return scheduleRoute;
  }

  public void setScheduleRoute(ScheduleRoute _scheduleRoute)
  {
    scheduleRoute = _scheduleRoute;
  }

  @OrderByDesc
  public Date getDateBegin()
  {
    return dateBegin;
  }

  public void setDateBegin(Date _dateBegin)
  {
    dateBegin = _dateBegin;
  }

  @OrderByDesc
  public Date getDateEnd()
  {
    return dateEnd;
  }

  public void setDateEnd(Date _dateEnd)
  {
    dateEnd = _dateEnd;
  }

  @OrderByDesc
  public boolean isHardSchedule()
  {
    return hardSchedule;
  }

  public void setHardSchedule(boolean _hardSchedule)
  {
    hardSchedule = _hardSchedule;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String _description)
  {
    description = _description;
  }

  @ClientSide( "public RouteVariant getRouteVariant()\n" +
     "  {\n" +
     "    return scheduleRoute.getRouteVariant();\n" +
     "  }" )
  public RouteVariant getRouteVariant()
  {
    return scheduleRoute.getRouteVariant();
  }

  @ClientSide( "public Route getRoute()\n" +
     "  {\n" +
     "    return scheduleRoute.getRoute();\n" +
     "  }" )
  public Route getRoute()
  {
    return scheduleRoute.getRoute();
  }

  @ClientSide( "public String getPeriodString()\n" +
     "  {\n" +
     "    return com.google.gwt.i18n.client.DateTimeFormat.getMediumDateFormat().format(dateBegin) + \"-\" + (dateEnd != null ? com.google.gwt.i18n.client.DateTimeFormat.getMediumDateFormat().format(dateEnd) : \"дата окончания не указана\");\n" +
     "  }" )
  public String getPeriodString()
  {
    return dateFormat.format(dateBegin) + "-" + (dateEnd != null ? dateFormat.format(dateEnd) : "дата окончания не указана");
  }

  public boolean isIncludeTime(Date time)
  {
    boolean checkDateBegin = dateBegin.before(time) || dateBegin.equals(time);
    boolean checkDateEnd = dateEnd == null || (dateEnd.after(time) || dateEnd.equals(time));

    return checkDateBegin && checkDateEnd;
  }

  @Override
  @ClientSide( "public String toString()\n" +
     "  {\n" +
     "    return \"Журнал действия расписание с id=\" + scheduleRoute.getId() + \" период \" + getPeriodString();\n" +
     "  }" )
  public String toString()
  {
    return "Журнал действия расписание с id=" + scheduleRoute.getId() + " период " + getPeriodString();
  }
}
