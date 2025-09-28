package com.openclassrooms.tourguide;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.mockito.Mockito;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import static org.junit.jupiter.api.Assertions.*;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
        assertEquals(1, userRewards.size());
	}

	@Test
	public void isWithinAttractionProximity() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void nearAllAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}

    @Test
    void shouldCalculateRewardsForAllUsers() {
        GpsUtil gpsUtil = Mockito.mock(GpsUtil.class);
        RewardCentral rewardCentral = Mockito.mock(RewardCentral.class);
        RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);

        Attraction attraction = new Attraction("TestAttraction", "city", "state", 0, 0);
        Mockito.when(gpsUtil.getAttractions()).thenReturn(List.of(attraction));

        InternalTestHelper.setInternalUserNumber(2);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        List<User> users = tourGuideService.getAllUsers();

        for (User user : users) {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), gpsUtil.getAttractions().get(0), new Date()));
        }

        rewardsService.calculateRewardsForAllUsers(users);

        for (User user : users) {
            assertFalse(user.getUserRewards().isEmpty(), "User " + user.getUserId() + " should have rewards");
        }
    }

}
