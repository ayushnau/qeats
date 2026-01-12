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

    if (getRestaurantsRequest == null || getRestaurantsRequest.getLatitude() == null
        || getRestaurantsRequest.getLongitude() == null) {
      return new GetRestaurantsResponse(); // return empty response if invalid request
    }

    // Determine radius based on peak hours
    Double servingRadius;
    int hour = currentTime.getHour();
    int minute = currentTime.getMinute();
    boolean isPeak = 
        (hour >= 8 && hour < 10) ||       // 8AM-10AM
        (hour == 13) ||                   // 1PM-2PM
        (hour >= 19 && hour < 21);        // 7PM-9PM

    servingRadius = isPeak ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

    // Call repository to get restaurants within radius and filter by current time
    List<Restaurant> restaurants = restaurantRepositoryService
        .findAllRestaurantsCloseBy(
            getRestaurantsRequest.getLatitude(),
            getRestaurantsRequest.getLongitude(),
            currentTime,
            servingRadius
        );

    return new GetRestaurantsResponse(restaurants);
  }
}
