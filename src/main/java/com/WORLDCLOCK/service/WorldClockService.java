package com.WORLDCLOCK.service;

import com.WORLDCLOCK.Repository.CityRepository;
import com.WORLDCLOCK.model.CityInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service

public class WorldClockService {

@Autowired
private CityRepository cityRepository;
private final String API_KEY="1e4fa19db5eb8eaec7690c1960ce7182";
    private final WebClient webClient = WebClient.create("https://api.openweathermap.org");
    private final ObjectMapper objectMapper = new ObjectMapper();


    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void init(){
        if(cityRepository.count() == 0){
            // ΠΡΟΣΟΧΗ: Η σειρά είναι (Όνομα, Χώρα, Ζώνη Ώρας, Longitude (Μήκος), Latitude (Πλάτος))
            cityRepository.save(new CityInfo("Θεσσαλονίκη", "Ελλάδα", "Europe/Athens", 22.9444, 40.6401));
            cityRepository.save(new CityInfo("Λονδίνο", "Ηνωμένο Βασίλειο", "Europe/London", -0.1278, 51.5074));
            cityRepository.save(new CityInfo("Παρίσι", "Γαλλία", "Europe/Paris", 2.3522, 48.8566));
            cityRepository.save(new CityInfo("Νέα Υόρκη", "ΗΠΑ", "America/New_York", -74.0060, 40.7128)); // Διορθώθηκε το 40.1728 σε 40.7128
            cityRepository.save(new CityInfo("Λος Άντζελες", "ΗΠΑ", "America/Los_Angeles", -118.2437, 34.0522));
            cityRepository.save(new CityInfo("Τόκιο", "Ιαπωνία", "Asia/Tokyo", 139.6917, 35.6895)); // Διορθώθηκε το 139.6503
            cityRepository.save(new CityInfo("Σίδνεϊ", "Αυστραλία", "Australia/Sydney", 151.2093, -33.8688));
            cityRepository.save(new CityInfo("Ντουμπάι", "ΗΑΕ", "Asia/Dubai", 55.2708, 25.2048));
            cityRepository.save(new CityInfo("Μόσχα", "Ρωσία", "Europe/Moscow", 37.6173, 55.7558));
            cityRepository.save(new CityInfo("Σαγκάη", "Κίνα", "Asia/Shanghai", 121.4737, 31.2304));
            cityRepository.save(new CityInfo("Σάο Πάολο", "Βραζιλία", "America/Sao_Paulo", -46.6333, -23.5505));
        }
        updateAllWeather();
    }

public List<CityInfo>getAllCitiesWithData(){
    List<CityInfo> cities=cityRepository.findAll();
    for (CityInfo city:cities){
        enrichWithTime(city);
    }
    return cities;
}

    public CityInfo addCity(CityInfo city){
    enrichWithWeather(city);
    enrichWithTime(city);
    return cityRepository.save(city);
    }
    public void deleteCity(Long id){
        cityRepository.deleteById(id);
    }

    public void updateAllWeather(){
    List<CityInfo> cities=cityRepository.findAll();
    for (CityInfo city:cities){
        enrichWithWeather(city);
        enrichWithTime(city);
        cityRepository.save(city);
    }
    }
    public CityInfo getCityData(String name,String country,String timezone,double lat,double lon){
    CityInfo city=new CityInfo(name,country,timezone,lat,lon);
    enrichWithTime(city);
    enrichWithWeather(city);
    return city;
    }

    private void enrichWithTime(CityInfo city) {
        try {
            ZoneId zoneId = ZoneId.of(city.getTimezone());
            ZonedDateTime zdt = ZonedDateTime.now(zoneId);
            city.setLocaltime(zdt.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            city.setDate(zdt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            city.setDayofweek(zdt.format(DateTimeFormatter.ofPattern("EEEE"))); // Διορθώθηκε το typo εδώ
        } catch (Exception e) {
            city.setLocaltime("--:--:--");
            city.setDate("--");
            city.setDayofweek("--"); // Διορθώθηκε το typo εδώ
        }
    }

    private void enrichWithWeather(CityInfo city) {
        try {
            // Βάζουμε το Locale.US για την υποδιαστολή
            String url = String.format(java.util.Locale.US, "/data/2.5/weather?lat=%.4f&lon=%.4f&units=metric&appid=%s",
                    city.getLatitude(), city.getLongtitude(), this.API_KEY);



            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();



            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode main = root.path("main");
                JsonNode wind = root.path("wind");
                JsonNode weatherArray = root.path("weather");

                city.setTemperature(main.path("temp").asDouble());
                city.setFeelslike(main.path("feels_like").asDouble());
                city.setHumidity(main.path("humidity").asInt());
                city.setWindspeed(wind.path("speed").asDouble());

                if (weatherArray.isArray() && !weatherArray.isEmpty()) {
                    int weatherCode = weatherArray.get(0).path("id").asInt();
                    city.setWeathermain(this.getWeathermain(weatherCode));
                    city.setWeatherdescription(this.getWeatherdescription(weatherCode));
                    city.setWeathericon(this.getWeatherEmoji(weatherCode));
                }
            }
        } catch (Exception e) {
            city.setTemperature(Double.NaN);
            city.setWeatherdescription("Σφάλμα");
            city.setWeathericon("❌");
            city.setWeathermain("Error");
        }
    }
    private String getWeathermain(int code) {
        if (code >= 200 && code < 300) return "Thunderstorm";
        if (code >= 300 && code < 400) return "Drizzle";
        if (code >= 500 && code < 600) return "Rain";
        if (code >= 600 && code < 700) return "Snow";
        if (code >= 700 && code < 800) return "Atmosphere"; // Ομίχλη, σκόνη κλπ.
        if (code == 800) return "Clear";
        if (code > 800) return "Clouds";
        return "Unknown";
    }

    private String getWeatherdescription(int code) {
        if (code >= 200 && code < 300) return "Καταιγίδα";
        if (code >= 300 && code < 400) return "Ψιχάλα";
        if (code >= 500 && code < 600) return "Βροχή";
        if (code >= 600 && code < 700) return "Χιόνι";
        if (code >= 700 && code < 800) return "Ομίχλη";
        if (code == 800) return "Αίθριος Ουρανός";
        if (code == 801) return "Λίγα Σύννεφα";
        if (code == 802) return "Αραιή Συννεφιά";
        if (code == 803 || code == 804) return "Συννεφιά";
        return "Άγνωστες συνθήκες";
    }

    private String getWeatherEmoji(int code) {
        if (code >= 200 && code < 300) return "⛈️";
        if (code >= 300 && code < 400) return "🌦️";
        if (code >= 500 && code < 600) return "🌧️";
        if (code >= 600 && code < 700) return "❄️";
        if (code >= 700 && code < 800) return "🌫️";
        if (code == 800) return "☀️";
        if (code == 801 || code == 802) return "🌤️";
        if (code == 803 || code == 804) return "☁️";
        return "❓";
    }

    public List<String> getAvailableTimezones() {
        return ZoneId.getAvailableZoneIds().stream().sorted().toList();
    }
}
