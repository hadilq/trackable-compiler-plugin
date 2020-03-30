package com.github.hadilq.trackable.sample.android

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AndroidTrackableClassTest {

    @Test
    fun `AndroidTrackableClass trackable by reflection`() {
        val trackable = AndroidTrackableClass()
        val method = trackable.javaClass.getMethod("track")
        assertThat(method.invoke(trackable)).isEqualTo("AndroidTrackableClass")
    }

    @Test
    fun `AndroidTrackableWithTrackItWithClass trackable by reflection`() {
        val trackable = AndroidTrackableWithTrackItWithClass()
        val method = trackable.javaClass.getMethod("track")
        assertThat(method.invoke(trackable)).isEqualTo("NotAndroidTrackableWithTrackItWithClass")
    }

    @Test
    fun trackable() {
        assertThat(AndroidTrackableClass().track()).isEqualTo("AndroidTrackableClass")
    }
}
