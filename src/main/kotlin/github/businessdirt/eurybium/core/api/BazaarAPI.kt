package github.businessdirt.eurybium.core.api

import com.google.gson.JsonParser
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.utils.PostModInitializationEvent
import github.businessdirt.eurybium.utils.Scheduler
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.concurrent.fixedRateTimer

object BazaarAPI {
    private val client: HttpClient = HttpClient.newHttpClient()
    private const val BASE_URL = "https://api.hypixel.net/v2/skyblock/bazaar"

    /**
     * Get all products as a map.
     */
    val allProducts: MutableMap<String, BazaarProduct> = mutableMapOf()

    @HandleEvent
    fun onPostModInitializationEvent(event: PostModInitializationEvent) {
        // update every 5 Minutes
        //EurybiumMod.scheduler.scheduleCyclic(0, 6000) { this.update() }
        fixedRateTimer("BazaarAPI-fixed-rate-timer", period = 300000) { update() }
    }

    /**
     * Updates the local product cache from Hypixel Bazaar API.
     */
    fun update() {
        try {
            val request = HttpRequest.newBuilder().uri(URI(BASE_URL)).GET().build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) EurybiumMod.logger.error(
                "BazaarAPI::update() - Failed to fetch data: {}",
                response.statusCode()
            )

            val root = JsonParser.parseString(response.body()).asJsonObject
            val productMap = root.getAsJsonObject("products")

            allProducts.clear()
            for (key in productMap.keySet()) {
                val quickStatus = productMap.getAsJsonObject(key)
                    .getAsJsonObject("quick_status")

                allProducts.put(
                    key, BazaarProduct(
                        key,
                        quickStatus.get("buyPrice").asDouble,
                        quickStatus.get("sellPrice").asDouble,
                        quickStatus.get("buyVolume").asInt,
                        quickStatus.get("sellVolume").asInt
                    )
                )
            }
        } catch (e: Exception) {
            EurybiumMod.logger.error("BazaarAPI::update() - {}", e.message)
        }
    }

    /**
     * Get a product by its ID (e.g. "ENCHANTED_DIAMOND").
     */
    fun getProduct(id: String): BazaarProduct? {
        val product = allProducts[id]
        if (product == null) {
            EurybiumMod.logger.warn("No product with id {}", id)
            return null
        }

        return allProducts[id]
    }

    val productIds: MutableSet<String>
        get() = allProducts.keys

    @JvmRecord
    data class BazaarProduct(
        val id: String,
        val buyPrice: Double,
        val sellPrice: Double,
        val buyVolume: Int,
        val sellVolume: Int
    )
}