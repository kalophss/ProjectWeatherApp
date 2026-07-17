package com.WORLDCLOCK.model;

import jakarta.persistence.GenerationType;

@jakarta.persistence.Entity
public class CityInfo {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    private String timezone;
    @jakarta.persistence.Column(name="local_time")
    private String localtime;
    @jakarta.persistence.Column(name = "city_date")
    private String date;
    private String dayofweek;
    private double latitude;
    private double longtitude;

    //Weather Fields
    private double temperature;
    private double feelslike;
    private int humidity;
    private double windspeed;
    private String weatherdescription;
    private String weathericon;
    private String weathermain;

    public CityInfo(){}
    public CityInfo(String name,String country,String timezone,double longtitude,double latitude){
        this.name=name;
        this.country=country;
        this.timezone=timezone;
        this.latitude=latitude;
        this.longtitude=longtitude;
    }
    public String getName() {
        return name;
    }
        public void setName(String name){
            this.name=name;
        }
        public String getCountry(){
            return country;
        }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDayofweek() {
        return dayofweek;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek=dayofweek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocaltime() {
        return localtime;
    }

    public void setLocaltime(String localtime) {
        this.localtime = localtime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getFeelslike() {
        return feelslike;
    }

    public void setFeelslike(double feelslike) {
        this.feelslike = feelslike;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(double windspeed) {
        this.windspeed = windspeed;
    }

    public String getWeatherdescription() {
        return weatherdescription;
    }

    public void setWeatherdescription(String weatherdescription) {
        this.weatherdescription = weatherdescription;
    }

    public String getWeathericon() {
        return weathericon;
    }

    public void setWeathericon(String weathericon) {
        this.weathericon = weathericon;
    }

    public String getWeathermain() {
        return weathermain;
    }

    public void setWeathermain(String weathermain) {
        this.weathermain = weathermain;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}



