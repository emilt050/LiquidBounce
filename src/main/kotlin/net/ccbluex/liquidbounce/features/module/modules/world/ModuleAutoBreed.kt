/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.event.waitTicks
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.ModuleCategories
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsValueGroup
import net.ccbluex.liquidbounce.utils.aiming.utils.raytraceBox
import net.ccbluex.liquidbounce.utils.block.SwingMode
import net.ccbluex.liquidbounce.utils.entity.box
import net.ccbluex.liquidbounce.utils.entity.interactEntity
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.raytracing.isLookingAtEntity
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.phys.EntityHitResult
import java.util.UUID

/**
 * AutoBreed module
 *
 * Automatically breeds animals.
 */
object ModuleAutoBreed : ClientModule("AutoBreed", ModuleCategories.WORLD) {
    private val delay by intRange("Delay", 3..4, 0..40, "ticks")
    private val range by float("Range", 4.5f, 1f..6f)

    private val rotations = tree(RotationsValueGroup(this))
    private val swingMode by enumChoice("SwingMode", SwingMode.DO_NOT_HIDE)

    private val animalsFed = LinkedHashMap<UUID, Int>()
    private var tickCounter = 0


    private const val BREEDING_COOLDOWN_TICKS = 6000

    override fun onEnabled() {
        animalsFed.clear()
        tickCounter = 0
    }

    private fun canBreed(animal: Animal): Boolean {
        return !animal.isBaby &&
            !animal.isInLove &&
            !animalsFed.containsKey(animal.uuid) &&
            (animal.isFood(player.mainHandItem) || animal.isFood(player.offhandItem))
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        animalsFed.entries.removeIf { it.value <= tickCounter }
        tickCounter++

        val maxRange = minOf(range.toDouble(), player.entityInteractionRange())
        val maxSquaredRange = maxRange * maxRange

        var target: Animal? = null
        var closestDistance = Double.MAX_VALUE

        for (entity in world.entitiesForRendering()) {
            if (entity !is Animal || !canBreed(entity)) continue

            val distance = player.squaredBoxedDistanceTo(entity)
            if (distance > maxSquaredRange || distance >= closestDistance) continue

            closestDistance = distance
            target = entity
        }

        val breedTarget = target ?: return@tickHandler

        val rotationWithVector = raytraceBox(
            eyes = player.eyePosition,
            box = breedTarget.box,
            range = maxRange,
            wallsRange = 0.0,
        ) ?: return@tickHandler

        RotationManager.setRotationTarget(
            rotationWithVector.rotation,
            valueGroup = rotations,
            priority = Priority.IMPORTANT_FOR_USAGE_3,
            provider = this@ModuleAutoBreed
        )

        val serverRotation = RotationManager.serverRotation

        if (isLookingAtEntity(
                toEntity = breedTarget,
                rotation = serverRotation,
                range = maxRange,
                throughWallsRange = 0.0
            ) == null
        ) {
            return@tickHandler
        }

        val useOffhand = breedTarget.isFood(player.offhandItem)
        val hand = if (useOffhand) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

        val interactionResult = interactEntity(
            entity = breedTarget,
            hitResult = EntityHitResult(breedTarget, rotationWithVector.vec),
            hand = hand,
            swingMode = swingMode,
        ) ?: return@tickHandler

        if (interactionResult.consumesAction()) {
            animalsFed[breedTarget.uuid] = tickCounter + BREEDING_COOLDOWN_TICKS + breedTarget.inLoveTime
            waitTicks(delay.random())
        }
    }
}
