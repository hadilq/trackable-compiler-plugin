package com.github.hadilq.trackable.sample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {

    @Test
    fun `TrackableClass trackable by reflection`() {
        val trackable = TrackableClass()
        val method = trackable.javaClass.getMethod("getTrack")
        assertThat(method.invoke(trackable)).isEqualTo("TrackableClass")
    }

    @Test
    fun `TrackableClassTrackItWith trackable by reflection`() {
        val trackable = TrackableWithTrackItWithClass()
        val method = trackable.javaClass.getMethod("getTrack")
        assertThat(method.invoke(trackable)).isEqualTo("NotTrackableWithTrackItWithClass")
    }

//    @Test
//    fun trackable() {
//        assertThat(TrackableClass().track).isEqualTo("TrackableClass")
//    }
}
