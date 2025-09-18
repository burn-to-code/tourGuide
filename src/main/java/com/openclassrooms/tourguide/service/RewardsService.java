package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private final int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private final RewardCentral rewardsCentral;
    private final List<Attraction> attractions;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.rewardsCentral = rewardCentral;
        this.attractions = gpsUtil.getAttractions();
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    @SuppressWarnings("unused")
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calcule et attribue les récompenses pour un utilisateur donné.
     *
     * <p>Pour chaque localisation visitée de l'utilisateur, cette méthode parcourt
     * la liste des attractions (en parallèle) et vérifie si l'utilisateur est
     * proche d'une attraction et n'a pas encore reçu de récompense pour celle-ci.
     * Si ces conditions sont remplies, une nouvelle récompense est ajoutée
     * à l'utilisateur.</p>
     *
     * <p>Cette méthode est optimisée pour le traitement parallèle sur la liste
     * des attractions afin de réduire le temps de calcul.</p>
     *
     * @param user l'utilisateur pour lequel les récompenses doivent être calculées
     */
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();

        userLocations.forEach(visitedLocation -> attractions.parallelStream()
                .filter(a -> (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(a.attractionName))) && nearAttraction(visitedLocation, a))
                .forEach(a -> user.addUserReward(new UserReward(visitedLocation, a, getRewardPoints(a, user)))));
	}

    /**
     * Calcule les récompenses pour une liste d'utilisateurs en parallèle.
     *
     * <p>Cette méthode utilise un pool de threads dont la taille est proportionnelle
     * au nombre de cœurs disponibles pour paralléliser le calcul des récompenses
     * pour tous les utilisateurs de la liste. Chaque utilisateur est traité de
     * manière asynchrone avec {@link CompletableFuture}.</p>
     *
     * @param users la liste des utilisateurs pour lesquels les récompenses doivent être calculées
     */
    public void calculateRewardsForAllUsers(List<User> users) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService ex =  Executors.newFixedThreadPool(cores*4);

        try {
            List<CompletableFuture<Void>> futures = users.stream()
                    .map(user -> CompletableFuture.runAsync(() -> calculateRewards(user), ex))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            ex.shutdown();
        }
    }

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        int attractionProximityRange = 200;
        return !(getDistance(attraction, location) > attractionProximityRange);
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

    public int getRewardPointsForUser(Attraction attraction, UUID userId) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userId);
    }

	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
