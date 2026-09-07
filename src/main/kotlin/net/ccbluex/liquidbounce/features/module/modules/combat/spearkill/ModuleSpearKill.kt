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

import net.ccbluex.liquidbounce.config.types.group.ToggleableValueGroup
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.ModuleCategories
import net.ccbluex.liquidbounce.render.drawBox
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.ccbluex.liquidbounce.render.renderEnvironment
import net.ccbluex.liquidbounce.render.withPositionRelativeToCamera
import net.ccbluex.liquidbounce.utils.entity.useItem
import net.ccbluex.liquidbounce.utils.item.isSpear
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.item.component.KineticWeapon
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

/**
 * Spear kill module
 *
 * Automatically attacks enemies using a charged spear.
 */
object ModuleSpearKill : ClientModule("SpearKill", ModuleCategories.COMBAT, aliases = listOf("AutoSpear")) {

    private val maxTargetDistance by float("MaxTargetDistance", 50f, 3f..200f)
    private val maxAllowedSpeed by float("MaxSpeed", 7f, 2f..10f)

    private object Preview : ToggleableValueGroup(this, "Preview", true) {
        val fillColor by color("FillColor", Color4b.RED.alpha(67))
        val outlineColor by color("OutlineColor", Color4b.WHITE.alpha(167))
    }

    init {
        tree(Preview)
    }

    private var dash: SpearDash? = null
    private var previewTarget: LivingEntity? = null

    internal val currentDashMovement: Vec3?
        get() = if (enabled) dash?.currentMovement?.takeIf { it.lengthSqr() > 0 } else null

    private val isChargingSpear get() = player.isUsingItem && player.useItem.isSpear
    private val holdsSpear get() = player.mainHandItem.isSpear || player.offhandItem.isSpear

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent>(priority = EventPriorityConvention.FINAL_DECISION) {
        if (!holdsSpear || !isChargingSpear) {
            stopDash()
            return@handler
        }

        val target = findTargetIfNeeded()
        previewTarget = target?.entity

        val spear = player.useItem.get(DataComponents.KINETIC_WEAPON) ?: run {
            stopDash()
            return@handler
        }

        if (!spear.isReadyToLaunch) {
            dash = null
            return@handler
        }

        val activeDash = dash
        if (activeDash != null) {
            applyDash(activeDash)
        } else if (target != null && spear.canStillDamage && mc.options.keyAttack.isDown) {
            dash = SpearDash.towards(target, maxAllowedSpeed.toDouble())
        } else {
            rechargeIfSpent(spear)
        }
    }

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        if (!Preview.enabled || !isChargingSpear) return@handler
        val target = previewTarget ?: return@handler

        event.renderEnvironment {
            withPositionRelativeToCamera {
                target.hitboxes.forEach { drawBox(it, Preview.fillColor, Preview.outlineColor) }
            }
        }
    }

    private fun findTargetIfNeeded(): SpearTarget? {
        val needsTarget = Preview.enabled || (dash == null && mc.options.keyAttack.isDown)
        if (!needsTarget) return null

        return findSpearTarget(maxTargetDistance.toDouble(), maxAllowedSpeed.toDouble())
    }

    private fun applyDash(activeDash: SpearDash) {
        player.deltaMovement = activeDash.advance()

        if (activeDash.isFinished) {
            dash = null
        }
    }

    private fun stopDash() {
        previewTarget = null
        if (dash != null) player.deltaMovement = Vec3.ZERO
        dash = null
    }

    private fun rechargeIfSpent(spear: KineticWeapon) {
        if (!mc.options.keyUse.isDown || !spear.isSpent) return

        val hand = player.usedItemHand
        interaction.releaseUsingItem(player)
        useItem(hand)
    }

    private val KineticWeapon.isReadyToLaunch get() = player.ticksUsingItem > delayTicks

    private val KineticWeapon.isSpent get() = player.ticksUsingItem > computeDamageUseDuration()

    private val KineticWeapon.canStillDamage
        get() = player.ticksUsingItem < computeDamageUseDuration() - delayTicks

    private val LivingEntity.hitboxes: List<AABB>
        get() = if (this is EnderDragon) subEntities.map { it.boundingBox } else listOf(boundingBox)

}
