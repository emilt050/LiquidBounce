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
package net.ccbluex.liquidbounce.utils.movement

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.MinecraftShortcuts
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket

/** Shares the original `mayfly` value while flight modules temporarily force it off. */
internal object MayflyOverrideManager : EventListener, MinecraftShortcuts {

    private val subscribers = ReferenceOpenHashSet<ClientModule>()
    private var originalMayFly = false

    fun subscribe(module: ClientModule) {
        if (subscribers.isEmpty()) {
            originalMayFly = player.abilities.mayfly
        }

        subscribers += module
        player.abilities.mayfly = false
    }

    fun unsubscribe(module: ClientModule) {
        subscribers -= module

        if (subscribers.isEmpty()) {
            player.abilities.mayfly = originalMayFly
        }
    }

    /** Keep the saved value current while the local ability flag is overridden. */
    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (subscribers.isNotEmpty() && event.packet is ClientboundPlayerAbilitiesPacket) {
            originalMayFly = event.packet.canFly()
        }
    }

}
