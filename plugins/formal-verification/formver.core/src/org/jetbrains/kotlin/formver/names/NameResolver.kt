/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

/**
 * Resolver responsible for resolving all names globally.
 */
class NameResolver {
    val phaseResolvers = mutableMapOf<ResolutionPhase, PhaseResolver>().withDefault { PhaseResolver() }

    fun register(name: Resolvable) {
        when (name) {
            is ResolvableNamespace -> registerInPhase(name, name.phase)
            else -> registerInPhase(name, GlobalPhase)
        }
    }

    fun registerInPhase(name: Resolvable, phase: ResolutionPhase) {
        phaseResolvers.getValue(phase).register(name)
        name.registerDependencies(this, phase)
    }

    fun resolveAll() {
        for (phaseResolver in phaseResolvers.values) {
            phaseResolver.resolveAll()
        }
    }
}

class ResolutionException(message: String) : Exception(message)