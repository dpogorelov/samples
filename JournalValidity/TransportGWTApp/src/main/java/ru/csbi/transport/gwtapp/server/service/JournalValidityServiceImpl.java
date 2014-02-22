package ru.csbi.transport.gwtapp.server.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import ru.csbi.transport.domain.adm.ActionLevel;
import ru.csbi.transport.domain.adm.ActionType;
import ru.csbi.transport.domain.disp.*;
import ru.csbi.transport.domain.mon.RaceExecution;
import ru.csbi.transport.domain.nsi.DaysOfWeek;
import ru.csbi.transport.gwtapp.client.service.ActionLogService;
import ru.csbi.transport.gwtapp.client.service.FetchCriteria;
import ru.csbi.transport.gwtapp.client.service.ServerException;
import ru.csbi.transport.gwtapp.routes.commons.client.service.ActivationService;
import ru.csbi.transport.gwtapp.routes.commons.client.service.JournalValidityService;
import ru.csbi.transport.gwtapp.routes.commons.client.service.NotificationService;
import ru.csbi.transport.gwtapp.server.event.PSVEventListener;
import ru.csbi.transport.gwtapp.server.notification.NotificationMessages;
import ru.csbi.transport.gwtapp.subsidy.client.service.AgreementRouteService;
import ru.csbi.util.TimeInterval;

/**
 * @author d.pogorelov
 */
public class JournalValidityServiceImpl implements JournalValidityService
{
  private static final Logger log = Logger.getLogger(JournalValidityServiceImpl.class);

  @PersistenceContext
  private EntityManager entityManager;

  private ActivationService activationService;

  private NotificationService notificationService;

  private AgreementRouteService agreementRouteService;

  private ActionLogService actionLog;

  private HibernateCriteriaBuilderFactory criteriaBuilderFactory;

  private PSVEventListener eventListener;

  public void setActivationService(ActivationService _activationService)
  {
    activationService = _activationService;
  }

  public void setNotificationService(NotificationService _notificationService)
  {
    notificationService = _notificationService;
  }

  public void setAgreementRouteService(AgreementRouteService _agreementRouteService)
  {
    agreementRouteService = _agreementRouteService;
  }

  public void setActionLog(ActionLogService _actionLog)
  {
    actionLog = _actionLog;
  }

  public void setCriteriaBuilderFactory(HibernateCriteriaBuilderFactory _criteriaBuilderFactory)
  {
    criteriaBuilderFactory = _criteriaBuilderFactory;
  }

  public void setEventListener(PSVEventListener _eventListener)
  {
    eventListener = _eventListener;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<RouteVariantValidity> getRouteVariantList(FetchCriteria fetchCriteria)
  {
    Map<String, Object> mapParams = new HashMap<String, Object>();
    String having = getHavingRouteVariantList(fetchCriteria, mapParams);

    Query query = entityManager.createQuery(
       "select new ru.csbi.transport.domain.disp.RouteVariantValidity(v.scheduleRoute.routeVariant, vj.dateBegin as dateBegin, min(v.dateEnd) as dateEnd) " +
          "  from disp.SR_Validity v, disp.SR_Validity vj " +
          " where not exists (select vr.dateEnd " +
          "                     from disp.SR_Validity vr " +
          "                    where v.dateEnd + :oneSecond >= vr.dateBegin " +
          "                      and (vr.dateEnd is null or v.dateEnd + :oneSecond <= vr.dateEnd) " +
          "                      and vr.scheduleRoute.routeVariant.id = v.scheduleRoute.routeVariant.id) " +
          "   and v.scheduleRoute.id = vj.scheduleRoute.id " +
          "   and (v.dateEnd is null or vj.dateBegin <= v.dateEnd) " +
          "   and not exists (select vr.dateBegin " +
          "                     from disp.SR_Validity vr " +
          "                    where vj.dateBegin - :oneSecond >= vr.dateBegin " +
          "                      and (vr.dateEnd is null or vj.dateBegin - :oneSecond <= vr.dateEnd) " +
          "                      and vr.scheduleRoute.routeVariant.id = v.scheduleRoute.routeVariant.id) " +
          getFilterRouteVariantList(fetchCriteria, mapParams) +
          "group by vj.dateBegin, v.scheduleRoute.routeVariant, v.scheduleRoute.routeVariant.route.routeNumber " +
          having +
          getOrderRouteVariantList(fetchCriteria));

    query.setParameter("oneSecond", 1D / TimeUnit.HOURS.toSeconds(24));
    for( Map.Entry<String, Object> entry : mapParams.entrySet() )
    {
      query.setParameter(entry.getKey(), entry.getValue());
    }

    int start = fetchCriteria.getStart();
    if( start >= 0 ) query.setFirstResult(start);

    int limit = fetchCriteria.getLimit();
    if( limit >= 0 ) query.setMaxResults(limit);

    return query.getResultList();
  }

  private String getHavingRouteVariantList(FetchCriteria fetchCriteria, Map<String, Object> mapParams)
  {
    Map<String, Serializable> filterModel = fetchCriteria.getOrCreateFilterModel();
    StringBuilder filter = new StringBuilder("");
    if( filterModel.containsKey("validityTimestamp") )
    {
      filter.append("having ");
      TimeInterval interval = (TimeInterval) filterModel.remove("validityTimestamp");
      if( interval.getStart() != null )
      {
        String paramName = "params0";
        filter.append(
           "( (vj.dateBegin <= :" + paramName + " and (min(v.dateEnd) is null or min(v.dateEnd) >= :" + paramName + ")) or vj.dateBegin >= :" + paramName + " ) ");
        mapParams.put(paramName, interval.getStart());
      }
      if( interval.getEnd() != null )
      {
        String paramName = "params1";
        filter.append("and vj.dateBegin <= :").append(paramName).append(" ");
        mapParams.put(paramName, interval.getEnd());
      }
    }

    return filter.toString();
  }

  private String getFilterRouteVariantList(FetchCriteria fetchCriteria, Map<String, Object> mapParams)
  {
    Map<String, Serializable> filterModel = fetchCriteria.getOrCreateFilterModel();
    StringBuilder filter = new StringBuilder("");
    int countParams = mapParams.size();
    for( Map.Entry<String, Serializable> entry : filterModel.entrySet() )
    {
      String paramName = "params" + countParams;
      String propertyName = entry.getKey();
      Serializable value = entry.getValue();

      filter.append("and v." + propertyName + " = :" + paramName + " ");
      mapParams.put(paramName, value);

      countParams++;
    }

    return filter.toString();
  }

  private String getOrderRouteVariantList(FetchCriteria fetchCriteria)
  {
    String sortField = fetchCriteria.getSortField();
    StringBuilder order = new StringBuilder("");
    if( sortField != null )
    {
      order.append("order by ");
      if( sortField.equals("routeNumber") )
      {
        order.append("v.scheduleRoute.routeVariant.route.routeNumber");
      }
      else if( sortField.equals("idRv") )
      {
        order.append("v.scheduleRoute.routeVariant.id");
      }
      else
      {
        order.append(sortField);
      }

      order.append(" ").append(fetchCriteria.getSortDirection().toString());
    }

    return order.toString();
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ScheduleRouteValidity> getScheduleRouteValidityList(FetchCriteria fetchCriteria,
                                                                  RouteVariantValidity routeVariantValidity)
  {
    Session session = (Session) entityManager.getDelegate();
    Criteria criteria = session.createCriteria(ScheduleRouteValidity.class);

    criteria.createAlias("scheduleRoute", "scheduleRoute");
    criteria.createAlias("scheduleRoute.routeVariant", "routeVariant");
    criteria.createAlias("routeVariant.route", "route");
    criteria.add(Restrictions.eq("routeVariant.id", routeVariantValidity.getIdRv()));
    TimeInterval interval = new TimeInterval(routeVariantValidity.getDateBegin(), routeVariantValidity.getDateEnd());
    ScheduleRouteValidity.addIntervalRestriction(criteria, interval);

    HibernateCriteriaBuilder criteriaBuilder = criteriaBuilderFactory.createCriteriaBuilder(ScheduleRouteValidity.class,
       criteria);
    criteriaBuilder.applyFetchCriteria(fetchCriteria);

    return criteria.list();
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ScheduleRouteValidity> getScheduleRouteValidityList(FetchCriteria fetchCriteria)
  {
    Session session = (Session) entityManager.getDelegate();
    Criteria criteria = session.createCriteria(ScheduleRouteValidity.class);

    Map<String, Serializable> filter = fetchCriteria.getFilterModel();

    criteria.createAlias("scheduleRoute", "scheduleRoute");
    criteria.createAlias("scheduleRoute.daysOfWeek", "daysOfWeek");
    criteria.createAlias("scheduleRoute.routeVariant", "routeVariant");
    criteria.createAlias("routeVariant.route", "route");

    if( filter.containsKey("validityTimestamp") )
    {
      TimeInterval interval = (TimeInterval) filter.remove("validityTimestamp");
      ScheduleRouteValidity.addIntervalRestriction(criteria, interval);
    }

    for( Map.Entry<String, Serializable> entry : filter.entrySet() )
    {
      criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
    }

    filter.clear();

    HibernateCriteriaBuilder criteriaBuilder = criteriaBuilderFactory.createCriteriaBuilder(ScheduleRouteValidity.class,
       criteria);
    criteriaBuilder.applyFetchCriteria(fetchCriteria);

    return criteria.list();
  }

  @Override
  @Transactional(rollbackFor = {ServerException.class})
  public void addSRValidity(ScheduleRouteValidity srValidity) throws ServerException
  {
    ScheduleRoute scheduleRouteRef = entityManager
       .getReference(ScheduleRoute.class, srValidity.getScheduleRoute().getId());
    if( scheduleRouteRef.getStatus() != StatusActivity.ACTIVE )
    {
      throw new ServerException("В Журнал действия можно добавлять только расписания в статусе Активный");
    }

    srValidity.setScheduleRoute(scheduleRouteRef);

    setDateEndPreviousValidity(srValidity, true);
    checkCrossingDate(srValidity, false);

    scheduleRouteRef.getValidityList().add(srValidity);
    entityManager.flush();

    addActionLog(ActionType.CREATE, srValidity);
    List<RouteVariant> lstVariantsNotification = addNotification(srValidity, null);

    StringBuilder msgCrossing = new StringBuilder();
    for( ScheduleRoute switchScheduleRoute : activationService.findDependentSchedules(scheduleRouteRef.getId()) )
    {
      if( scheduleRouteRef.getStatus() == StatusActivity.ACTIVE )
      {
        ScheduleRoute switchScheduleRouteRef = entityManager
           .getReference(ScheduleRoute.class, switchScheduleRoute.getId());

        ScheduleRouteValidity switchValidity = new ScheduleRouteValidity(srValidity);
        switchValidity.setHardSchedule(false);
        switchValidity.setScheduleRoute(switchScheduleRouteRef);
        setDateEndPreviousValidity(switchValidity, true);

        try
        {
          checkCrossingDate(switchValidity, true);

          switchScheduleRouteRef.getValidityList().add(switchValidity);
          entityManager.flush();
          addActionLog(ActionType.CREATE, switchValidity);
          lstVariantsNotification = addNotification(switchValidity, lstVariantsNotification);
        }
        catch( ServerException e )
        {
          msgCrossing.append(e.getMessage());
        }
      }
    }

    if( msgCrossing.length() != 0 )
    {
      throw new ServerException("Расписание имеет переключение.\n" + msgCrossing.toString());
    }
  }

  private List<RouteVariant> addNotification(ScheduleRouteValidity srValidity, List<RouteVariant> lstVariants)
  {
    eventUpdatedJournal(srValidity);

    if( lstVariants == null )
    {
      lstVariants = new ArrayList<RouteVariant>();
    }

    RouteVariant routeVariant = srValidity.getRouteVariant();
    if( !lstVariants.contains(routeVariant) )
    {
      lstVariants.add(routeVariant);
      if( srValidity.getDateBegin().before(new Date()) )
      {
        notificationService.addNotificationExpireBeginDate(srValidity);

        //информирование в субсидировании о возможно измененном кол-ве рейс
        String raceInfo = agreementRouteService.getContractNumberList(routeVariant);
        log.debug("raceInfo=[" + raceInfo + "] for variant" + routeVariant);
        if( raceInfo != null )
        {
          if( agreementRouteService.isDifferentParams(routeVariant) )
          {
            log.debug("need check plan parameters");

            String notificationMessage = NotificationMessages.getNotificationMessages().getNotificationMessage(
               "route.detour.is.must.be.checked");
            notificationMessage = MessageFormat.format(
               notificationMessage,
               routeVariant.getInfo(),
               raceInfo);

            notificationService
               .addRouteVariantNotification(routeVariant, NotificationDept.ROUTE, NotificationDept.SUBSIDY,
                  NotificationStatus.ACTIVE,
                  NotificationEvent.ROUTE_IS_READY_IN_TIME,
                  notificationMessage);
          }


          String raceInfoMessage = NotificationMessages.getNotificationMessages()
             .getNotificationMessage("route.races.is.must.be.checked");

          raceInfoMessage = MessageFormat.format(
             raceInfoMessage,
             routeVariant.getInfo(),
             raceInfo);
          notificationService
             .addRouteVariantNotification(routeVariant, NotificationDept.ROUTE, NotificationDept.SUBSIDY,
                NotificationStatus.ACTIVE,
                NotificationEvent.ROUTE_IS_READY_IN_TIME,
                raceInfoMessage);
        }
      }
    }

    return lstVariants;
  }

  @Override
  @Transactional(rollbackFor = {ServerException.class})
  public void saveSRValidity(ScheduleRouteValidity srValidity) throws ServerException
  {
    // Менять ScheduleRoute в журнале действия запрещено, меняются только даты
    Map<Long, Long> mapEvents = new HashMap<Long, Long>();

    ScheduleRouteValidity oldValidity = entityManager.getReference(ScheduleRouteValidity.class, srValidity.getId());
    boolean oldHardSchedule = oldValidity.isHardSchedule();
    ScheduleRoute scheduleRoute = oldValidity.getScheduleRoute();
    Date oldDateBegin = oldValidity.getDateBegin();
    Date oldDateEnd = oldValidity.getDateEnd();
    mapEvents.put(srValidity.getId(), scheduleRoute.getId());

    setDateEndPreviousValidity(srValidity, false);
    checkCrossingDate(srValidity, false);
    entityManager.merge(srValidity);

    addEditActionLog(srValidity, oldHardSchedule);
    List<RouteVariant> lstVariantNotification = addSaveNotification(srValidity, oldDateBegin, oldDateEnd, null);

    StringBuilder msgCrossing = new StringBuilder();
    for( ScheduleRouteValidity switchValidity : getSwitchValidity(scheduleRoute.getId(), oldDateBegin, oldDateEnd) )
    {
      ScheduleRouteValidity oldSwitchValidity = entityManager
         .getReference(ScheduleRouteValidity.class, switchValidity.getId());
      boolean oldSwitchHardSchedule = oldSwitchValidity.isHardSchedule();
      switchValidity.setDateBegin(srValidity.getDateBegin());
      switchValidity.setDateEnd(srValidity.getDateEnd());
      mapEvents.put(switchValidity.getId(), oldSwitchValidity.getScheduleRoute().getId());

      setDateEndPreviousValidity(switchValidity, false);

      try
      {
        checkCrossingDate(switchValidity, true);
        entityManager.merge(switchValidity);

        addEditActionLog(switchValidity, oldSwitchHardSchedule);
        lstVariantNotification = addSaveNotification(switchValidity, oldDateBegin, oldDateEnd, lstVariantNotification);
      }
      catch( ServerException e )
      {
        msgCrossing.append(e.getMessage());
      }
    }

    if( msgCrossing.length() != 0 )
    {
      throw new ServerException("Расписание имеет переключение.\n" + msgCrossing.toString());
    }

    entityManager.flush();
    eventUpdatedJournal(mapEvents);
  }

  private List<RouteVariant> addSaveNotification(ScheduleRouteValidity srValidity, Date oldDateBegin, Date oldDateEnd,
                                                 List<RouteVariant> lstVariants)
  {
    if( lstVariants == null )
    {
      lstVariants = new ArrayList<RouteVariant>();
    }

    RouteVariant routeVariant = srValidity.getRouteVariant();
    if( !lstVariants.contains(routeVariant) )
    {
      lstVariants.add(routeVariant);
      Date dateBegin = srValidity.getDateBegin();
      Date dateEnd = srValidity.getDateEnd();

      // Анализ изменились ли даты по Журналу действия
      if( !dateBegin.equals(oldDateBegin) ||
         (dateEnd != null && oldDateEnd != null && !dateEnd.equals(oldDateEnd)) ||
         ((dateEnd == null && oldDateEnd != null) || (dateEnd != null && oldDateEnd == null)) )
      {
        notificationService.addNotificationPeriodChanged(srValidity);

        //see ROUTE-120
        Date now = new Date();
        boolean new_working = ((dateBegin.before(now) || dateBegin.equals(now)) && (dateEnd == null || dateEnd
           .after(now) || dateEnd.equals(now)));

        if( new_working )  // ВМ действующий
        {
          notificationService.addNotificationExpireBeginDate(srValidity);
        }
      }
    }

    return lstVariants;
  }

  @Override
  @Transactional(rollbackFor = {ServerException.class})
  public void removeSRValidity(ScheduleRouteValidity srValidity) throws ServerException
  {
    Map<Long, Long> mapEvents = new HashMap<Long, Long>();
    srValidity = entityManager.getReference(ScheduleRouteValidity.class, srValidity.getId());

    ScheduleRoute scheduleRoute = srValidity.getScheduleRoute();
    Date dateBegin = srValidity.getDateBegin();
    Date dateEnd = srValidity.getDateEnd();
    mapEvents.put(srValidity.getId(), scheduleRoute.getId());

    checkRaceExecution(srValidity);
    addActionLog(ActionType.DELETE, srValidity);
    entityManager.remove(srValidity);

    for( ScheduleRouteValidity validity : getSwitchValidity(scheduleRoute.getId(), dateBegin, dateEnd) )
    {
      mapEvents.put(validity.getId(), validity.getScheduleRoute().getId());
      checkRaceExecution(validity);
      addActionLog(ActionType.DELETE, validity);
      entityManager.remove(validity);
    }

    entityManager.flush();
    eventUpdatedJournal(mapEvents);
  }

  private void checkRaceExecution(ScheduleRouteValidity validity) throws ServerException
  {
    ScheduleRoute scheduleRoute = validity.getScheduleRoute();
    ScheduleRoute scheduleRouteRef = entityManager.getReference(ScheduleRoute.class, scheduleRoute.getId());
    Session session = (Session) entityManager.getDelegate();

    Criteria criteria = session.createCriteria(RaceExecution.class);
    RaceExecution.addScheduleRouteRestriction(criteria, scheduleRouteRef);
    RaceExecution
       .addDateIntervalRestriction(criteria, new TimeInterval(validity.getDateBegin(), validity.getDateEnd()));

    if( !criteria.list().isEmpty() )
    {
      throw new ServerException("Невозможно удалить расписание " + scheduleRoute.getId() +
         " из Журнала действия, т.к., по нему зафиксирована транспортная работа.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public String checkSwitchBeforeAdd(ScheduleRouteValidity srValidity)
  {
    ScheduleRoute scheduleRoute = srValidity.getScheduleRoute();
    StringBuilder msgSwitch = new StringBuilder();
    msgSwitch.append(checkPreviousValidity(srValidity));

    boolean isExistSwitches = false;
    for( ScheduleRoute switchScheduleRoute : activationService.findDependentSchedules(scheduleRoute.getId()) )
    {
      if( switchScheduleRoute.getStatus() == StatusActivity.ACTIVE )
      {
        Route route = switchScheduleRoute.getRoute();

        if( !isExistSwitches )
        {
          msgSwitch.append("Расписание имеет переключение.\n");
          isExistSwitches = true;
        }

        msgSwitch.append("<br/>Для связанного расписания " + switchScheduleRoute.getId() + " по маршруту " + route
           .getRouteNumber() + " будет добавлен период аналогичный действия.\n");

        ScheduleRouteValidity fictiveValidity = new ScheduleRouteValidity(srValidity);
        fictiveValidity.setScheduleRoute(switchScheduleRoute);

        msgSwitch.append(checkPreviousValidity(fictiveValidity));
      }
    }

    return msgSwitch.length() != 0 ? msgSwitch.toString() : null;
  }

  private String checkPreviousValidity(ScheduleRouteValidity srValidity)
  {
    StringBuilder result = new StringBuilder("");
    List<ScheduleRouteValidity> previousValidities = getPreviousValidity(srValidity, false);
    if( !previousValidities.isEmpty() )
    {
      for( ScheduleRouteValidity previousValidity : previousValidities )
      {
        ScheduleRoute previousScheduleRoute = previousValidity.getScheduleRoute();
        result.append("Имеются предыдущие периоды для расписаний (" + previousScheduleRoute.getId());
        for( ScheduleRouteValidity validity : getSwitchValidity(previousScheduleRoute.getId(),
           previousValidity.getDateBegin(), previousValidity.getDateEnd()) )
        {
          ScheduleRoute switchSR = validity.getScheduleRoute();
          result.append("," + switchSR.getId());
        }
        result.append("), которые будут закрыты.");
      }
    }

    return result.toString();
  }

  @Override
  @Transactional( readOnly = true )
  public String getSwitchIds(ScheduleRouteValidity srValidity)
  {
    StringBuilder stringBuilder = new StringBuilder();

    ScheduleRoute scheduleRoute = srValidity.getScheduleRoute();
    for( ScheduleRoute scheduleRouteSwitch : activationService.findDependentSchedules(scheduleRoute.getId()) )
    {
      if( stringBuilder.length() > 0 )
      {
        stringBuilder.append(",");
      }
      stringBuilder.append(scheduleRouteSwitch.getId().toString());
    }

    return stringBuilder.length() > 0 ? stringBuilder.toString() : null;
  }

  @SuppressWarnings( "unchecked" )
  private List<ScheduleRouteValidity> getSwitchValidity(Long scheduleRouteId, Date dateBegin, Date dateEnd)
  {
    Set<ScheduleRoute> switchs = activationService.findDependentSchedules(scheduleRouteId);

    List<ScheduleRouteValidity> result = new ArrayList<ScheduleRouteValidity>();
    if( !switchs.isEmpty() )
    {
      int i = 0;
      Object[] switchIds = new Object[switchs.size()];
      for( ScheduleRoute switchRoute : activationService.findDependentSchedules(scheduleRouteId) )
      {
        switchIds[i++] = switchRoute.getId();
      }

      Session session = (Session) entityManager.getDelegate();
      Criteria criteria = session.createCriteria(ScheduleRouteValidity.class);
      criteria.add(Restrictions.in("scheduleRoute.id", switchIds));
      criteria.add(Restrictions.eq("dateBegin", dateBegin));
      criteria.add(dateEnd == null ? Restrictions.isNull("dateEnd") : Restrictions.eq("dateEnd", dateEnd));
      result = criteria.list();
    }

    return result;
  }

  @SuppressWarnings( "unchecked" )
  private void setDateEndPreviousValidity(ScheduleRouteValidity srValidity, boolean autoCheckPreviousSwitchValidity)
     throws ServerException
  {
    for( ScheduleRouteValidity previousValidity : getPreviousValidity(srValidity, autoCheckPreviousSwitchValidity) )
    {
      previousValidity.setDateEnd(new Date(srValidity.getDateBegin().getTime() - TimeUnit.SECONDS.toMillis(1)));
      if( autoCheckPreviousSwitchValidity )
      {
        saveSRValidity(previousValidity);
      }
    }

    if( !autoCheckPreviousSwitchValidity )
    {
      entityManager.flush();
    }
  }

  @SuppressWarnings( "unchecked" )
  private List<ScheduleRouteValidity> getPreviousValidity(ScheduleRouteValidity srValidity,
                                                          boolean autoCheckPreviousSwitchValidity)
  {
    List<ScheduleRouteValidity> result = new ArrayList<ScheduleRouteValidity>();

    Route route = srValidity.getRoute();
    ScheduleRoute scheduleRoute = srValidity.getScheduleRoute();

    Session session = (Session) entityManager.getDelegate();
    Criteria criteria = session.createCriteria(ScheduleRouteValidity.class);
    ScheduleRouteValidity.addRouteRestriction(criteria, route.getId());
    criteria
       .add(Restrictions.and(Restrictions.isNull("dateEnd"), Restrictions.lt("dateBegin", srValidity.getDateBegin())));
    if( srValidity.getId() != null )
    {
      criteria.add(Restrictions.not(Restrictions.eq("id", srValidity.getId())));
    }

    List<ScheduleRouteValidity> resultList = criteria.list();
    for( ScheduleRouteValidity previousValidity : resultList )
    {
      DaysOfWeek previousDaysOfWeek = previousValidity.getScheduleRoute().getDaysOfWeek();
      DaysOfWeek srDaysOfWeek = entityManager.getReference(DaysOfWeek.class, scheduleRoute.getDaysOfWeek().getId());
      if( previousDaysOfWeek.isSubsetOf(srDaysOfWeek) )
      {
        if( autoCheckPreviousSwitchValidity )
        {
          session.evict(previousValidity);
        }
        result.add(previousValidity);
      }
    }

    return result;
  }

  @SuppressWarnings( "unchecked" )
  private void checkCrossingDate(ScheduleRouteValidity srValidity, boolean isSwitching) throws ServerException
  {
    Route route = srValidity.getRoute();
    ScheduleRoute scheduleRoute = srValidity.getScheduleRoute();
    Date dateEnd = srValidity.getDateEnd();
    Date dateBegin = srValidity.getDateBegin();

    // Проверка даты начала и окончания действия
    if( dateEnd != null && dateBegin != null && dateEnd.before(dateBegin) )
    {
      throw new ServerException(
         "Дата окончания действия варианта маршрута не может быть меньше даты начала действия варианта маршрута");
    }

    Session session = (Session) entityManager.getDelegate();
    Criteria criteria = session.createCriteria(ScheduleRouteValidity.class);
    ScheduleRouteValidity.addRouteRestriction(criteria, route.getId());
    criteria.add(Restrictions.or(Restrictions.isNull("dateEnd"), Restrictions.ge("dateEnd", dateBegin)));

    if( dateEnd != null )
    {
      criteria.add(Restrictions.le("dateBegin", dateEnd));
    }
    if( srValidity.getId() != null )
    {
      criteria.add(Restrictions.ne("id", srValidity.getId()));
    }

    List<ScheduleRouteValidity> resultList = criteria.list();
    for( ScheduleRouteValidity crossingValidity : resultList )
    {
      DaysOfWeek crossingDaysOfWeek = crossingValidity.getScheduleRoute().getDaysOfWeek();
      DaysOfWeek srDaysOfWeek = entityManager.getReference(DaysOfWeek.class, scheduleRoute.getDaysOfWeek().getId());
      if( crossingDaysOfWeek.isDaysCrossing(srDaysOfWeek) )
      {
        if( isSwitching )
        {
          throw new ServerException(
             "Период действия связанного расписания " + getMessageValidity(srValidity,
                srDaysOfWeek) + " пересекается с периодом действия расписания " + getMessageValidity(crossingValidity,
                crossingDaysOfWeek) + ".\n");
        }
        else
        {
          throw new ServerException(
             "Период действия расписания пересекается с периодом действия расписания " + getMessageValidity(
                crossingValidity, crossingDaysOfWeek));
        }
      }
    }
  }

  private String getMessageValidity(ScheduleRouteValidity validity, DaysOfWeek daysOfWeek)
  {
    return validity.getScheduleRoute().getId() + ":" + daysOfWeek + " " + validity
       .getPeriodString() + " по маршруту " + validity.getRoute().getRouteNumber();
  }

  private void addActionLog(ActionType actionType, ScheduleRouteValidity validity)
  {
    actionLog
       .addActionLog(ActionLevel.ENTITY, actionType, validity, validity.toString(), validity.getRouteVariant().getId());
    entityManager.flush();
  }

  private void addEditActionLog(ScheduleRouteValidity newValidity, boolean oldHardSchedule)
  {
    String addMessage = "";
    if( newValidity.isHardSchedule() != oldHardSchedule )
    {
      addMessage = (newValidity.isHardSchedule() ? " установлен" : " снят") + " признак жесткости";
    }

    actionLog.addActionLog(ActionLevel.ENTITY, ActionType.EDIT, newValidity, newValidity.toString() + addMessage,
       newValidity.getRouteVariant().getId());
    entityManager.flush();
  }

  private void eventUpdatedJournal(ScheduleRouteValidity _scheduleRouteValidity)
  {
    eventUpdatedJournal(_scheduleRouteValidity.getId(), _scheduleRouteValidity.getScheduleRoute().getId());
  }

  private void eventUpdatedJournal(Map<Long, Long> mapEvents)
  {
    for( Map.Entry<Long, Long> entryEvent : mapEvents.entrySet() )
    {
      eventUpdatedJournal(entryEvent.getKey(), entryEvent.getValue());
    }
  }

  private void eventUpdatedJournal(Long validityId, Long scheduleRouteId)
  {
    eventListener.journalValidityUpdated(validityId, scheduleRouteId);
  }

}