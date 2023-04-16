package Model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

//TODO: Replace all this with a Task object
public class MapTaskItem {

    private String title;
    private String address;
    private LatLng location;

    public MapTaskItem(String title, String address, LatLng location) {
        this.title = title;
        this.address = address;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapTaskItem)) return false;
        MapTaskItem that = (MapTaskItem) o;
        return Objects.equals(title, that.title)
                && Objects.equals(address, that.address)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, address, location);
    }
}
