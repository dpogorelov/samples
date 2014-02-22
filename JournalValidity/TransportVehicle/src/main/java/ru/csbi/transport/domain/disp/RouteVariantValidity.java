package ru.csbi.transport.domain.disp;

import java.util.Date;

import ru.csbi.gwt.beans.GWTBean;
import ru.csbi.gwt.dto.ClientSide;
import ru.csbi.gwt.dto.DTO;
import ru.csbi.transport.domain.core.SCOEntity;
import ru.csbi.transport.domain.nsi.TransportType;
import ru.csbi.util.beans.BeanDesc;
import ru.csbi.util.beans.OrderByDesc;

/**
 * @author d.pogorelov
 */
@BeanDesc( columns = {"routeNumber", "idRv", "transportType", "dateBegin", "dateEnd"} )
@DTO
public class RouteVariantValidity extends SCOEntity implements GWTBean
{
  private RouteVariant routeVariant;

  private Date dateBegin;

  private Date dateEnd;

  public RouteVariantValidity()
  {}

  @ClientSide( "public RouteVariantValidity(RouteVariant _routeVariant, java.util.Date _dateBegin, java.util.Date _dateEnd)\n" +
     "  {\n" +
     "    routeVariant = _routeVariant;\n" +
     "    dateBegin = _dateBegin;\n" +
     "    dateEnd = _dateEnd;\n" +
     "  }" )
  public RouteVariantValidity(RouteVariant _routeVariant, Date _dateBegin, Date _dateEnd)
  {
    routeVariant = _routeVariant;
    dateBegin = _dateBegin;
    dateEnd = _dateEnd;
  }

  public RouteVariant getRouteVariant()
  {
    return routeVariant;
  }

  public void setRouteVariant(RouteVariant _routeVariant)
  {
    routeVariant = _routeVariant;
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

  @ClientSide("public Route getRoute()\n" +
     "  {\n" +
     "    return routeVariant.getRoute();\n" +
     "  }")
  public Route getRoute()
  {
    return routeVariant.getRoute();
  }
  
  @OrderByDesc
  @ClientSide( "public String getRouteNumber()\n" +
     "  {\n" +
     "    return getRoute().getRouteNumber();\n" +
     "  }" )
  public String getRouteNumber()
  {
    return getRoute().getRouteNumber();
  }

  @OrderByDesc
  @ClientSide( "public Long getIdRv()\n" +
     "  {\n" +
     "    return routeVariant.getIdRv();\n" +
     "  }" )
  public Long getIdRv()
  {
    return routeVariant.getIdRv();
  }

  @ClientSide("public ru.csbi.transport.domain.nsi.TransportType getTransportType()\n" +
     "  {\n" +
     "    return getRoute().getTransportType();\n" +
     "  }")
  public TransportType getTransportType()
  {
    return getRoute().getTransportType();
  }
}
