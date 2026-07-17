package com.WORLDCLOCK.controller;

import com.WORLDCLOCK.model.CityInfo;
import com.WORLDCLOCK.service.WorldClockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
public class WorldClockController{
    private final WorldClockService service ;
        public WorldClockController(WorldClockService service) {
            this.service=service;
        }
        @GetMapping("/")
    public String index(Model model){
            List<CityInfo> cities=service.getAllCitiesWithData();
            model.addAttribute("cities",cities);
            model.addAttribute("timezones",service.getAvailableTimezones());
            return "index";
        }


    @org.springframework.web.bind.annotation.GetMapping("/api/add-city")
    @org.springframework.web.bind.annotation.ResponseBody
    public CityInfo addCity(
            @org.springframework.web.bind.annotation.RequestParam String name,
            @org.springframework.web.bind.annotation.RequestParam String country,
            @org.springframework.web.bind.annotation.RequestParam String timezone,
            @org.springframework.web.bind.annotation.RequestParam double latitude,
            @org.springframework.web.bind.annotation.RequestParam double longtitude) {

        // Φτιάχνουμε τη νέα πόλη (θυμήσου, ο constructor σου παίρνει πρώτα το longtitude!)
        CityInfo newCity = new CityInfo(name, country, timezone, longtitude, latitude);

        // Τη στέλνουμε στο Service για να πάρει καιρό και να σωθεί στη Βάση!
        return service.addCity(newCity);

    }
    @org.springframework.web.bind.annotation.DeleteMapping("/api/delete-city/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public void deleteCity(@org.springframework.web.bind.annotation.PathVariable Long id) {
        // Καλούμε το service να διαγράψει την πόλη με αυτό το ID
        service.deleteCity(id);
    }
    @GetMapping("/api/cities")
    @ResponseBody
    public List<CityInfo> getAllCities() {
        // Καλούμε την έτοιμη μέθοδο του Service που φέρνει όλες τις default πόλεις
        // με ανανεωμένα δεδομένα καιρού και ώρας
        return service.getAllCitiesWithData();
    }
    }


