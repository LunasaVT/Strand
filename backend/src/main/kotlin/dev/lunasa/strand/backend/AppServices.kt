/*
 * Strand - Open your Minecraft world to anyone, anywhere.
 * Copyright (C) 2026  Lunasa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lunasa.strand.backend

import dev.lunasa.strand.backend.mojang.MojangClient
import dev.lunasa.strand.backend.security.KeyManager
import dev.lunasa.strand.backend.security.TokenService
import dev.lunasa.strand.backend.service.AuthCodeService
import dev.lunasa.strand.backend.service.ChallengeService
import dev.lunasa.strand.backend.service.InviteService
import dev.lunasa.strand.backend.service.SessionService
import dev.lunasa.strand.backend.service.UserService

class AppServices(
    val config: Config,
    val keys: KeyManager,
    val tokens: TokenService,
    val users: UserService,
    val challenges: ChallengeService,
    val authCodes: AuthCodeService,
    val sessions: SessionService,
    val invites: InviteService,
    val mojang: MojangClient,
)
