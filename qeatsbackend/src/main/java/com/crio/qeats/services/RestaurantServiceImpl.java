/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;

  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;

  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    if (getRestaurantsRequest == null
        || getRestaurantsRequest.getLatitude() == null
        || getRestaurantsRequest.getLongitude() == null) {
      return new GetRestaurantsResponse();
    }

    Double servingRadius =
        isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

    List<Restaurant> restaurants = restaurantRepositoryService
        .findAllRestaurantsCloseBy(
            getRestaurantsRequest.getLatitude(),
            getRestaurantsRequest.getLongitude(),
            currentTime,
            servingRadius
        );

    return new GetRestaurantsResponse(restaurants);
  }

  private boolean isPeakHour(LocalTime time) {
    return
        (!time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(10, 0)))
     || (!time.isBefore(LocalTime.of(13, 0)) && !time.isAfter(LocalTime.of(14, 0)))
     || (!time.isBefore(LocalTime.of(19, 0)) && !time.isAfter(LocalTime.of(21, 0)));
  }
}
