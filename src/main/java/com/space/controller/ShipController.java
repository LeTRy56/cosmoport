package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {
    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping("/ships")
    public List<Ship> getShipsList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        final List<Ship> ships = shipService.getShipsList(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        final List<Ship> sortedShips = shipService.sortShips(ships, order);
        return shipService.getPage(sortedShips, pageNumber, pageSize);
    }

    @GetMapping("/ships/count")
    public Integer getShipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {
        return shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize,
                maxCrewSize, minRating, maxRating).size();
    }

    @PostMapping("/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (!shipService.isShipValid(ship))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (ship.getUsed() == null) ship.setUsed(false);
        ship.setId(null);
        final Double rating = shipService.computeRating(ship.getSpeed(), ship.getUsed(), ship.getProdDate());
        ship.setRating(rating);

        final Ship savedShip = shipService.saveShip(ship);
        return new ResponseEntity<>(savedShip, HttpStatus.OK);
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable("id") String stringId) {
        final Long id = shipService.convertToLong(stringId);
        if (id == null || id <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Ship ship = shipService.getShip(id);
        if (ship == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(
            @PathVariable("id") String stringId,
            @RequestBody Ship ship) {
        final ResponseEntity<Ship> entity = getShip(stringId);
        final Ship savedShip = entity.getBody();
        if (savedShip == null) return entity;

        final Ship result;
        try {
            result = shipService.updateShip(savedShip, ship);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") String stringId) {
        final ResponseEntity<Ship> entity = getShip(stringId);
        final Ship ship = entity.getBody();
        if (ship == null)
            return entity;
        shipService.deleteShip(ship);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
