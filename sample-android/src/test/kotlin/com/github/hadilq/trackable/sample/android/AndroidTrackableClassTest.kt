package com.github.hadilq.trackable.sample.android

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AndroidTrackableClassTest {

    @Test
    fun `trackable by reflection`() {
        val trackable = AndroidTrackableClass()
        val method = trackable.javaClass.getMethod("getTrack")
        assertThat(method.invoke(trackable)).isEqualTo("AndroidTrackableClass")
    }

//    @Test
//    fun trackable() {
//        assertThat(AndroidTrackableClass().track).isEqualTo("AndroidTrackableClass")
//    }


}
