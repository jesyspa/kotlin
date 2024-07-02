/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

sealed interface ResolutionClaim {
    fun addPrimaryClaim(resolvable: Resolvable): ResolutionClaim
    fun addSecondaryClaim(resolvable: Resolvable): ResolutionClaim
    val winner: Resolvable?
}

class SecondaryClaims : ResolutionClaim {
    val claims = mutableSetOf<Resolvable>()

    override fun addPrimaryClaim(resolvable: Resolvable): ResolutionClaim = OnePrimaryClaim(resolvable)

    override fun addSecondaryClaim(resolvable: Resolvable): ResolutionClaim {
        claims.add(resolvable)
        return this
    }

    override val winner: Resolvable?
        get() = if (claims.size == 1) claims.first() else null
}

class OnePrimaryClaim(override val winner: Resolvable) : ResolutionClaim {
    override fun addPrimaryClaim(resolvable: Resolvable): ResolutionClaim {
        require(resolvable == winner) { "Conflicting claimants: $winner, $resolvable" }
        return this
    }

    // Secondary claims are ignored when there is a primary claimant.
    override fun addSecondaryClaim(resolvable: Resolvable): ResolutionClaim = this
}
