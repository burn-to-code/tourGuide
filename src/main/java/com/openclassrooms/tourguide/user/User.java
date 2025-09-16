package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

public class User {
	private final UUID userId;
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
	private Date latestLocationTimestamp;
	private final List<VisitedLocation> visitedLocations = new CopyOnWriteArrayList<>();
	private final List<UserReward> userRewards = new CopyOnWriteArrayList<>();
	private UserPreferences userPreferences = new UserPreferences();
	private List<Provider> tripDeals = new ArrayList<>();
	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	
	public UUID getUserId() {
		return userId;
	}
	
	public String getUserName() {
		return userName;
	}

    @SuppressWarnings("unused")
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

    @SuppressWarnings("unused")
    public String getPhoneNumber() {
		return phoneNumber;
	}

    @SuppressWarnings("unused")
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

    @SuppressWarnings("unused")
	public String getEmailAddress() {
		return emailAddress;
	}

    @SuppressWarnings("unused")
	public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
		this.latestLocationTimestamp = latestLocationTimestamp;
	}

    @SuppressWarnings("unused")
	public Date getLatestLocationTimestamp() {
		return latestLocationTimestamp;
	}
	
	public void addToVisitedLocations(VisitedLocation visitedLocation) {
		visitedLocations.add(visitedLocation);
	}
	
	public List<VisitedLocation> getVisitedLocations() {
		return visitedLocations;
	}

    @SuppressWarnings("unused")
	public void clearVisitedLocations() {
		visitedLocations.clear();
	}
	
	public void addUserReward(UserReward userReward) {
        boolean alreadyRewarded = userRewards.stream()
                .anyMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName));
        if (!alreadyRewarded) {
            userRewards.add(userReward);
        }
	}
	
	public List<UserReward> getUserRewards() {
		return userRewards;
	}
	
	public UserPreferences getUserPreferences() {
		return userPreferences;
	}

    @SuppressWarnings("unused")
	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	public VisitedLocation getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}
	
	public void setTripDeals(List<Provider> tripDeals) {
		this.tripDeals = tripDeals;
	}

    @SuppressWarnings("unused")
	public List<Provider> getTripDeals() {
		return tripDeals;
	}

}
