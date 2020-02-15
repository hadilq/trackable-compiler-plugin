package com.github.hadilq.trackable.sample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {

    @Test
    fun `trackable by reflection`() {
        val trackable = TrackableClass()
        val method = trackable.javaClass.getMethod("getTrack")
        assertThat(method.invoke(trackable)).isEqualTo("TrackableClass")
    }

//    @Test
//    fun trackable() {
//        assertThat(TrackableClass().track).isEqualTo("TrackableClass")
//    }
}
