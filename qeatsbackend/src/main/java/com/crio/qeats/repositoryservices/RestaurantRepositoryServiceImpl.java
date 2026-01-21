/*
 * Copyright (c) Crio.Do 2019. All rights reserved
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoUtils;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestaurantRepository restaurantRepository; // needed for the test

    @Autowired
    private Provider<ModelMapper> modelMapperProvider;

    private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
        LocalTime openingTime = LocalTime.parse(res.getOpensAt());
        LocalTime closingTime = LocalTime.parse(res.getClosesAt());
        return time.isAfter(openingTime) && time.isBefore(closingTime);
    }

    @Override
    public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
                                                      Double longitude,
                                                      LocalTime currentTime,
                                                      Double servingRadiusInKms) {

        // For the test: fetch from repository so Mockito can intercept it
        List<RestaurantEntity> candidates = restaurantRepository.findAll();

        // Filter by distance + open time
        List<Restaurant> result = new ArrayList<>();
        ModelMapper mapper = modelMapperProvider.get();

        for (RestaurantEntity entity : candidates) {
            double distanceInKm = GeoUtils.findDistanceInKm(
                    latitude, longitude,
                    entity.getLatitude(),
                    entity.getLongitude()
            );
            if (isOpenNow(currentTime, entity) && distanceInKm <= servingRadiusInKms) {
                result.add(mapper.map(entity, Restaurant.class));
            }
        }

        return result;
    }
}
