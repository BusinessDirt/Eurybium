package github.businessdirt.eurybium.features.waypoints

import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.commands.SuggestionProviders.dynamic
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArguments.int
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArguments.string
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.processors.EurybiumModule

/** Registers ordered-route commands and dynamic suggestions for saved routes and export formats. */
@EurybiumModule
object OrderedWaypointsCommand {

    /** Registers the primary command and its aliases; numeric positions and counts start at one. */
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("eybordered") {
            description = "Manage ordered waypoint routes."
            category = CommandCategory.USERS_ACTIVE
            aliases = mutableListOf("eybo")

            literal("load", "import") {
                description = "Load a saved or repository route, or import the clipboard when no name is supplied."
                arg("name", WaypointRouteArgument, dynamic(OrderedWaypoints::getRouteNames)) { name ->
                    callback { OrderedWaypoints.load(getArg(name)) }
                }

                simpleCallback { OrderedWaypoints.load("") }
            }

            literal("unload", "clear") {
                description = "Unloads the current ordered waypoints."
                simpleCallback(OrderedWaypoints::unload)
            }

            literal("skip") {
                description = "Skips the next waypoint."
                arg("amount", int(min = 1)) { amount ->
                    callback { OrderedWaypoints.skip(getArg(amount)) }
                }

                simpleCallback { OrderedWaypoints.skip(1) }
            }

            literal("skipto") {
                description = "Select a waypoint by its route number."
                arg("number", int(min = 1)) { number ->
                    callback { OrderedWaypoints.skipTo(getArg(number)) }
                }

                simpleCallback { OrderedWaypoints.skipTo(1) }
            }

            literal("unskip") {
                description = "Move backward through the route, wrapping at the beginning."
                arg("amount", int(min = 1)) { amount ->
                    callback { OrderedWaypoints.unskip(getArg(amount)) }
                }

                simpleCallback { OrderedWaypoints.unskip(1) }
            }

            literal("delete", "remove") {
                description = "Deletes the waypoint with the inputted number."
                arg("number", int(min = 1)) { number ->
                    callback { OrderedWaypoints.delete(getArg(number)) }
                }
            }

            literal("add", "insert") {
                description = "Insert a waypoint beneath the player at the selected route position."
                arg("number", int(min = 1)) { number ->
                    callback { OrderedWaypoints.add(getArg(number)) }
                }
            }

            literal("export") {
                description = "Exports the loaded ordered waypoints to clipboard."
                arg("format", string(), dynamic(OrderedWaypoints::getWaypointFormats)) { format ->
                    callback { OrderedWaypoints.export(getArg(format)) }
                }

                simpleCallback { OrderedWaypoints.export("coleweight") }
            }

            literal("save") {
                description = "Saves the loaded ordered waypoints to your config."
                arg("name", WaypointRouteArgument) { name ->
                    callback { OrderedWaypoints.save(getArg(name)) }
                }
            }

            literal("erase", "delete-route") {
                description = "Erases the route with the specified name."
                arg("name", WaypointRouteArgument, dynamic(OrderedWaypoints::getRouteNames)) { name ->
                    callback { OrderedWaypoints.erase(getArg(name)) }
                }
            }
        }
    }
}
