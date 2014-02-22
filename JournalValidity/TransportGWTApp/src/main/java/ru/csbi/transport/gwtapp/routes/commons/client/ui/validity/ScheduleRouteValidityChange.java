package ru.csbi.transport.gwtapp.routes.commons.client.ui.validity;

import ru.csbi.transport.domain.disp.RouteVariantValidity;

/**
 * @author d.pogorelov
 */
public interface ScheduleRouteValidityChange
{
  void onRouteVariantValidityChange(RouteVariantValidity routeVariantValidity);
}
