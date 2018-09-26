package zamani.com.taximeter.model;

public final class Place {

    private String displayName;
    private double lat;
    private double lon;

    public Place(String displayName, double lat, double lon) {
        this.displayName = displayName;
        this.lat = lat;
        this.lon = lon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

}
