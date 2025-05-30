package org.gitanimals.guild.controller

import jakarta.servlet.http.HttpServletResponse
import org.gitanimals.core.AuthorizationException
import org.gitanimals.core.ErrorResponse
import org.gitanimals.core.FieldType
import org.gitanimals.core.auth.RequiredUserEntryPoints
import org.gitanimals.core.auth.UserEntryPoint
import org.gitanimals.guild.app.*
import org.gitanimals.guild.app.request.CreateGuildRequest
import org.gitanimals.guild.app.response.*
import org.gitanimals.guild.controller.request.JoinGuildRequest
import org.gitanimals.guild.domain.GuildIcons
import org.gitanimals.guild.domain.GuildService
import org.gitanimals.guild.domain.SearchFilter
import org.gitanimals.guild.domain.extension.GuildFieldTypeExtension.isGuildField
import org.gitanimals.guild.domain.request.ChangeGuildRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class GuildController(
    private val guildService: GuildService,
    private val createGuildFacade: CreateGuildFacade,
    private val joinGuildFacade: JoinGuildFacade,
    private val acceptJoinGuildFacade: AcceptJoinGuildFacade,
    private val kickGuildFacade: KickGuildFacade,
    private val changeGuildFacade: ChangeGuildFacade,
    private val joinedGuildFacade: GetJoinedGuildFacade,
    private val searchGuildFacade: SearchGuildFacade,
    private val changeMainPersonaFacade: ChangeMainPersonaFacade,
    private val leaveGuildFacade: LeaveGuildFacade,
    private val drawGuildFacade: DrawGuildFacade,
    private val denyJoinGuildFacade: DenyJoinGuildFacade,
) {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/guilds")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun createGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @RequestBody createGuildRequest: CreateGuildRequest,
    ) = createGuildFacade.createGuild(token, createGuildRequest)

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/guilds/{guildId}")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun joinGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestBody joinGuildRequest: JoinGuildRequest,
    ) = joinGuildFacade.joinGuild(token, guildId, joinGuildRequest.personaId.toLong())


    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/guilds/{guildId}/accepts")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun acceptJoinGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestParam("user-id") acceptUserId: Long,
    ) = acceptJoinGuildFacade.acceptJoin(guildId, acceptUserId)


    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/guilds/{guildId}")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun kickFromGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestParam("user-id") kickUserId: Long,
    ) = kickGuildFacade.kickMember(guildId, kickUserId)


    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/guilds/{guildId}")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun changeGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestBody changeGuildRequest: ChangeGuildRequest,
    ) = changeGuildFacade.changeGuild(guildId, changeGuildRequest)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guilds/{guildId}")
    fun getGuildById(@PathVariable("guildId") guildId: Long): GuildResponse {
        val guild = guildService.getGuildById(
            guildId,
            GuildService.loadMembers,
            GuildService.loadWaitMembers,
        )

        return GuildResponse.from(guild)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guilds")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun getAllJoinedGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
    ): GuildsResponse {
        val guilds = joinedGuildFacade.getJoinedGuilds()

        return GuildsResponse.from(guilds)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guilds/search")
    fun searchGuilds(
        @RequestParam(name = "text", defaultValue = "") text: String,
        @RequestParam(name = "page-number", defaultValue = "0") pageNumber: Int,
        @RequestParam(name = "filter", defaultValue = "RANDOM") filter: SearchFilter,
        @RequestParam(name = "key", defaultValue = "0") key: Int,
    ): GuildPagingResponse {
        val guilds = searchGuildFacade.search(
            key = key,
            text = text,
            pageNumber = pageNumber,
            filter = filter,
        )

        return GuildPagingResponse.from(guilds, pageNumber)
    }

    @GetMapping("/guilds/icons")
    @ResponseStatus(HttpStatus.OK)
    fun findAllGuildIcons(): GuildIconsResponse {
        return GuildIconsResponse(
            GuildIcons.entries.map { it.getImagePath() }.toList()
        )
    }

    @GetMapping("/guilds/backgrounds")
    @ResponseStatus(HttpStatus.OK)
    fun findAllGuildBackgrounds(): GuildBackgroundResponse {
        return GuildBackgroundResponse(FieldType.entries.filter { it.isGuildField() })
    }

    @PostMapping("/guilds/{guildId}/personas")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun changeMainPersona(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestParam("persona-id") personaId: Long,
    ) = changeMainPersonaFacade.changeMainPersona(
        token = token,
        guildId = guildId,
        personaId = personaId,
    )

    @DeleteMapping("/guilds/{guildId}/leave")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun leaveGuild(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
    ) = leaveGuildFacade.leave(guildId)

    @PostMapping("guilds/{guildId}/deny")
    @RequiredUserEntryPoints([UserEntryPoint.GITHUB])
    fun deny(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @PathVariable("guildId") guildId: Long,
        @RequestParam("user-id") denyUserId: Long,
    ) = denyJoinGuildFacade.denyJoin(
        guildId = guildId,
        denyUserId = denyUserId,
    )

    @GetMapping(value = ["/guilds/{guildId}/draw"], produces = ["image/svg+xml"])
    fun draw(
        @PathVariable("guildId") guildId: Long,
        response: HttpServletResponse,
    ): String {
        response.cacheControl(3600)
        return drawGuildFacade.drawGuild(guildId)
    }

    @GetMapping("/internals/guilds/by-title/{title}")
    fun getGuildByTitle(
        @PathVariable("title") title: String,
    ): GuildResponse {
        val guild = guildService.getGuildByTitle(
            title,
            GuildService.loadMembers,
            GuildService.loadWaitMembers,
        )

        return GuildResponse.from(guild)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ErrorResponse =
        ErrorResponse.from(exception)

    @ExceptionHandler(AuthorizationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAuthorizationException(exception: AuthorizationException): ErrorResponse =
        ErrorResponse.from(exception)

    private fun HttpServletResponse.cacheControl(maxAgeSeconds: Int): HttpServletResponse {
        this.setHeader(
            HttpHeaders.CACHE_CONTROL,
            "no-cache, no-store, must-revalidate, max-age=$maxAgeSeconds"
        )
        this.setHeader(HttpHeaders.PRAGMA, "no-cache")
        return this
    }
}
