/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

/**
 * Performs resolution of names within a single phase.
 *
 * The object should not be used again after `resolveAll` is called.
 */
class PhaseResolver {
    private val resolvables = mutableListOf<Resolvable>()

    fun register(resolvable: Resolvable) {
        resolvables.add(resolvable)
    }

    fun resolveAll() {
        val claims = buildClaims()
        for (resolvable in resolvables) {
            val secondary = resolvable.secondary.firstOrNull { claims[it]?.winner == resolvable }
            if (secondary != null) {
                resolvable.commitResolution(secondary)
            } else {
                resolvable.commitResolution(resolvable.primary)
            }
        }
    }

    private fun buildClaims() : Map<ChunkedName, ResolutionClaim> {
        val claims = mutableMapOf<ChunkedName, ResolutionClaim>().withDefault { SecondaryClaims() }
        for (resolvable in resolvables) {
            claims[resolvable.primary] = claims.getValue(resolvable.primary).addPrimaryClaim(resolvable)
            for (secondary in resolvable.secondary) {
                claims[secondary] = claims.getValue(secondary).addSecondaryClaim(resolvable)
            }
        }
        return claims
    }
}