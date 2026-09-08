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
import net.ccbluex.liquidbounce.utils.entity.PositionExtrapolation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil

internal class SpearDash private constructor(private val movement: Vec3, private val ticks: Int) {

    private val brakeMovement = movement.scale(-1.0)
    private var elapsedTicks = 0

    val isFinished get() = elapsedTicks > ticks * 2

    val currentMovement: Vec3
        get() = when {
            elapsedTicks < ticks -> movement
            elapsedTicks < ticks * 2 -> brakeMovement
            else -> Vec3.ZERO
        }

    fun advance(): Vec3 = currentMovement.also { elapsedTicks++ }

    companion object {
        fun towards(target: SpearTarget, maxSpeed: Double): SpearDash {
            val ticks = ceil(target.dashDistance / maxSpeed).toInt().coerceAtLeast(1)
            val direction = target.entity.directionToPositionIn(ticks)

            return SpearDash(direction.scale(target.dashDistance / ticks), ticks)
        }
    }

}

private fun LivingEntity.directionToPositionIn(ticks: Int): Vec3 {
    val direction = PositionExtrapolation.getBestForEntity(this)
        .getPositionInTicks(ticks.toDouble())
        .subtract(player.eyePosition)
        .normalize()

    return direction.takeIf { it.lengthSqr() > 0 } ?: player.lookAngle
}
