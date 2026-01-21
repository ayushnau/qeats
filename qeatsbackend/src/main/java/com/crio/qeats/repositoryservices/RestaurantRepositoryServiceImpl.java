/*
 * Copyright (c) Crio.Do 2019. All rights reserved
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
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

@Primary
@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Provider<ModelMapper> modelMapperProvider;

    private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
        LocalTime openingTime = LocalTime.parse(res.getOpensAt());
        LocalTime closingTime = LocalTime.parse(res.getClosesAt());

        // Open if time is strictly after open and strictly before close
        return time.isAfter(openingTime) && time.isBefore(closingTime);
    }

    @Override
    public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
                                                      Double longitude,
                                                      LocalTime currentTime,
                                                      Double servingRadiusInKms) {

        // Fetch all restaurants from Mongo
        List<RestaurantEntity> candidates = mongoTemplate.findAll(RestaurantEntity.class);

        List<Restaurant> result = new ArrayList<>();
        ModelMapper mapper = modelMapperProvider.get();

        for (RestaurantEntity entity : candidates) {
            if (isRestaurantCloseByAndOpen(entity, currentTime, latitude, longitude, servingRadiusInKms)) {
                result.add(mapper.map(entity, Restaurant.class));
            }
        }

        return result;
    }

    private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
                                               LocalTime currentTime,
                                               Double latitude,
                                               Double longitude,
                                               Double servingRadiusInKms) {

        if (!isOpenNow(currentTime, restaurantEntity)) {
            return false;
        }

        double distanceInKm = GeoUtils.findDistanceInKm(
                latitude, longitude,
                restaurantEntity.getLatitude(),
                restaurantEntity.getLongitude()
        );

        return distanceInKm <= servingRadiusInKms;
    }
}
