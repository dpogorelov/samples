package ru.csbi.transport.gwtapp.routes.commons.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import ru.csbi.transport.domain.disp.RouteVariantValidity;
import ru.csbi.transport.domain.disp.ScheduleRouteValidity;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.service.ServerException;

/**
 * @author d.pogorelov
 */
@RemoteServiceRelativePath( "service/JournalValidity" )
public interface JournalValidityService extends RemoteService
{
  String checkSwitchBeforeAdd(ScheduleRouteValidity srValidity);

  String getSwitchIds(ScheduleRouteValidity srValidity);

  List<RouteVariantValidity> getRouteVariantList(FetchCriteria fetchCriteria);

  List<ScheduleRouteValidity> getScheduleRouteValidityList(FetchCriteria fetchCriteria, RouteVariantValidity routeVariantValidity);

  List<ScheduleRouteValidity> getScheduleRouteValidityList(FetchCriteria fetchCriteria);

  void addSRValidity(ScheduleRouteValidity srValidity) throws ServerException;

  void saveSRValidity(ScheduleRouteValidity srValidity) throws ServerException;

  void removeSRValidity(ScheduleRouteValidity srValidity) throws ServerException;
}
