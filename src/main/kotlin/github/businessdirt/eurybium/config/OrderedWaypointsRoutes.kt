package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.Waypoints

class OrderedWaypointsRoutes {
    @Expose
    var routes: MutableMap<String, Waypoints<EurybiumWaypoint>>? = null
}