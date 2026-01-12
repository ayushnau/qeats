package com.crio.qeats.controller;

import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)
@Log4j2
public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;

  @GetMapping(RESTAURANTS_API)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants(
      @RequestParam(name = "latitude") Double latitude,
      @RequestParam(name = "longitude") Double longitude) {

    // Validation as required by tests
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      return ResponseEntity.badRequest().build();
    }

    GetRestaurantsRequest request =
        new GetRestaurantsRequest(latitude, longitude);

    log.info("getRestaurants called with {}", request);

    GetRestaurantsResponse response =
        restaurantService.findAllRestaurantsCloseBy(request, LocalTime.now());

    log.info("getRestaurants returned {}", response);

    return ResponseEntity.ok(response);
  }
}
