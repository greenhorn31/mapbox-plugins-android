package com.mapbox.mapboxsdk.plugins.utils

import android.os.Handler
import android.os.Looper
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

fun MapboxMap.querySourceFeatures(sourceId: String): List<Feature> {
  return this.getSourceAs<GeoJsonSource>(sourceId)?.querySourceFeatures(null) as List<Feature>
}

fun MapboxMap.isLayerVisible(layerId: String): Boolean {
  return this.getLayer(layerId)?.visibility?.value?.equals(Property.VISIBLE)!!
}

class MapboxTestingUtils {
  companion object {

    /**
     * Used to increase style load time for stress testing.
     */
    const val MAPBOX_HEAVY_STYLE = "asset://heavy_style.json"

    private const val DATA_PUSH_INTERVAL = 1L

    /**
     * Pushes data updates every [DATA_PUSH_INTERVAL] milliseconds until the style has been loaded,
     * checked with [StyleChangeIdlingResource].
     */
    fun pushSourceUpdates(styleChangeIdlingResource: StyleChangeIdlingResource, update: () -> Unit) {
      val mainHandler = Handler(Looper.getMainLooper())
      val runnable = object : Runnable {
        override fun run() {
          update.invoke()
          if (!styleChangeIdlingResource.isIdleNow) {
            mainHandler.postDelayed(this, DATA_PUSH_INTERVAL)
          }
        }
      }

      if (!styleChangeIdlingResource.isIdleNow) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
          runnable.run()
        } else {
          mainHandler.post(runnable)
        }
      }
    }
  }
}