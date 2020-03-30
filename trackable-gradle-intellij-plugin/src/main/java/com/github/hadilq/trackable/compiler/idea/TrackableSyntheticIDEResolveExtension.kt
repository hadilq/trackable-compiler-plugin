package com.github.hadilq.trackable.compiler.idea

import com.github.hadilq.trackable.compiler.TrackableSyntheticResolveExtension
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class TrackableSyntheticIDEResolveExtension : TrackableSyntheticResolveExtension(
    { _, _, _ -> },
    Name.identifier("track"),
    FqName("com.github.hadilq.trackable.annotations.Trackable"),
    "trackWith"
)
