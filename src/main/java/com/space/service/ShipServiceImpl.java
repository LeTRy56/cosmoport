package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;


    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean needToChangeRating = false;

        final String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name))
                oldShip.setName(name);
            else throw new IllegalArgumentException();
        }

        final String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet))
                oldShip.setPlanet(planet);
            else throw new IllegalArgumentException();
        }

        final ShipType shipType = newShip.getShipType();
        if (shipType != null) {
            if (isShipTypeValid(shipType))
                oldShip.setShipType(shipType);
            else throw new IllegalArgumentException();
        }

        final Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
                needToChangeRating = true;
            }
            else throw new IllegalArgumentException();
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            needToChangeRating = true;
        }

        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                needToChangeRating = true;
            }
            else throw new IllegalArgumentException();
        }

        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize))
                oldShip.setCrewSize(crewSize);
            else throw new IllegalArgumentException();
        }

        if (needToChangeRating)
            oldShip.setRating(computeRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate()));

        shipRepository.save(oldShip);
        return oldShip;
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public List<Ship> getShipsList(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Ship> ships = new ArrayList<>();
        shipRepository.findAll().forEach(ship -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && ship.getShipType() != shipType) return;
            if (beforeDate != null && ship.getProdDate().after(beforeDate)) return;
            if (afterDate != null && ship.getProdDate().before(afterDate)) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;

            ships.add(ship);
        });
        return ships;
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null)
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        return ships;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }

    @Override
    public boolean isShipValid(Ship ship) {

        return ship != null && isStringValid(ship.getName()) && isStringValid(ship.getPlanet()) &&
                isShipTypeValid(ship.getShipType()) && isCrewSizeValid(ship.getCrewSize()) &&
                isSpeedValid(ship.getSpeed()) && isProdDateValid(ship.getProdDate());
    }

    @Override
    public Double computeRating(double speed, boolean isUsed, Date prod) {
        final double numerator = 80 * speed * (isUsed ? 0.5 : 1);
        final double denominator = 3019 - getYear(prod) + 1;
        final double result = numerator / denominator;
        return round(result);
    }

    @Override
    public Long convertToLong(String stringId) {
        if (stringId == null) return null;
        else try {
            return Long.parseLong(stringId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getYear(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public Double round(Double value) {
        return Math.round(value * 100) / 100D;
    }

    public boolean isStringValid(String string) {
        return string != null && string.length() <= 50 && !string.isEmpty();
    }

    public boolean isCrewSizeValid(Integer size) {
        final int minSize = 1;
        final int maxSize = 9999;
        return size != null && size.compareTo(minSize) >= 0 && size.compareTo(maxSize) <= 0;
    }

    public boolean isSpeedValid(Double speed) {
        final double minSpeed = 0.01d;
        final double maxSpeed = 0.99d;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    public boolean isProdDateValid(Date prodDate) {
        final int minProdYear = 2800;
        final int maxProdYear = 3019;
        return prodDate != null && getYear(prodDate).compareTo(minProdYear) >= 0 &&
                getYear(prodDate).compareTo(maxProdYear) <= 0;
    }

    public boolean isShipTypeValid(ShipType shipType) {
        return shipType != null && Arrays.asList(ShipType.values()).contains(shipType);
    }
}
