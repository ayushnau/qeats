/*
 * Copyright (c) Crio.Do 2019. All rights reserved
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

        // Most common interpretation: open if time is strictly after open and strictly before close
        return time.isAfter(openingTime) && time.isBefore(closingTime);
    }

    /**
     * Finds all restaurants that are:
     * - within the serving radius (using GeoHash prefix + exact distance check)
     * - currently open at the given time
     */
    @Override
    public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
                                                      Double longitude,
                                                      LocalTime currentTime,
                                                      Double servingRadiusInKms) {

        // Step 1: Generate GeoHash prefix (precision ~5–10 km range)
        // Length 6 is usually good balance for ~1.2 km precision, covering the 3–5 km radius
        int geoHashPrecision = 6;
        String geoHashPrefix = GeoHash.withCharacterPrecision(latitude, longitude, geoHashPrecision)
                                      .toBase32();

        // Step 2: Query MongoDB for restaurants whose geoHash starts with this prefix
        Query query = new Query();
        query.addCriteria(Criteria.where("geoHash").regex("^" + Pattern.quote(geoHashPrefix)));

        List<RestaurantEntity> candidates = mongoTemplate.find(query, RestaurantEntity.class);

        // Step 3: Filter candidates by exact distance and open status
        List<Restaurant> result = new ArrayList<>();
        ModelMapper mapper = modelMapperProvider.get();

        for (RestaurantEntity entity : candidates) {
            if (isRestaurantCloseByAndOpen(entity, currentTime, latitude, longitude, servingRadiusInKms)) {
                Restaurant restaurantDto = mapper.map(entity, Restaurant.class);
                result.add(restaurantDto);
            }
        }

        return result;
    }

    /**
     * Checks if a restaurant is open at the given time and within serving radius.
     */
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

        // Use <= to include restaurants exactly at the radius border
        return distanceInKm <= servingRadiusInKms;
    }
}