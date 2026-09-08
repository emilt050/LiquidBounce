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
package net.ccbluex.liquidbounce.features.module.modules.combat.spearkill

import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.client.world
import net.ccbluex.liquidbounce.utils.raytracing.hasLineOfSight
import net.ccbluex.liquidbounce.utils.raytracing.traceFromPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.math.sqrt

private const val MIN_DASH_DISTANCE = 3.0

internal data class SpearTarget(val entity: LivingEntity, val dashDistance: Double)

internal fun findSpearTarget(maxDistance: Double, maxSpeed: Double): SpearTarget? {
    val eye = player.eyePosition
    val lookEnd = eye.add(player.lookAngle.scale(maxDistance))
    val searchBox = player.boundingBox.expandTowards(lookEnd.subtract(eye)).inflate(1.0)

    return world
        .getEntitiesOfClass(LivingEntity::class.java, searchBox) { it.isUnderCrosshair(eye, lookEnd) }
        .sortedBy { player.distanceToSqr(it) }
        .firstNotNullOfOrNull { it.asDashTarget(eye, maxDistance, maxSpeed) }
}

private fun LivingEntity.isUnderCrosshair(eye: Vec3, lookEnd: Vec3) =
    this !== player && isAlive && boundingBox.clip(eye, lookEnd).isPresent

private fun LivingEntity.asDashTarget(eye: Vec3, maxDistance: Double, maxSpeed: Double): SpearTarget? {
    val distance = sqrt(player.distanceToSqr(this))
    if (distance !in MIN_DASH_DISTANCE..maxDistance) return null
    if (!hasLineOfSight(eye, boundingBox.center)) return null

    val dashDistance = dashDistanceStoppingShortOf(distance, maxSpeed)
    return if (isPathClear(eye, dashDistance)) SpearTarget(this, dashDistance) else null
}

private fun dashDistanceStoppingShortOf(distance: Double, maxSpeed: Double): Double {
    val ticks = ceil(distance / maxSpeed - 0.5).toInt().coerceAtLeast(1)
    return 2.0 * distance * ticks / (2.0 * ticks + 1)
}

private fun isPathClear(eye: Vec3, distance: Double): Boolean {
    val hit = traceFromPlayer(range = distance, block = ClipContext.Block.COLLIDER)
    return hit.type == HitResult.Type.MISS || hit.location.distanceTo(eye) >= distance
}
