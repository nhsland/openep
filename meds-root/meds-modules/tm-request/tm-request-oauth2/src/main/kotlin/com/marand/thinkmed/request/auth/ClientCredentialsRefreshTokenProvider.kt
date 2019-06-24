package com.marand.thinkmed.request.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.util.LinkedMultiValueMap

/**
 * Makes Client Credentials Access Token Provider able to refresh tokens
 *
 * @author Nejc Korasa
 */

class ClientCredentialsRefreshTokenProvider : ResourceOwnerPasswordAccessTokenProvider() {

    override fun supportsRefresh(resource: OAuth2ProtectedResourceDetails): Boolean = true

    @Throws(UserRedirectRequiredException::class, OAuth2AccessDeniedException::class)
    override fun refreshAccessToken(
            resource: OAuth2ProtectedResourceDetails,
            refreshToken: OAuth2RefreshToken,
            request: AccessTokenRequest): OAuth2AccessToken {

        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken.value)
            add("client_id", resource.clientId)
            add("client_secret", resource.clientSecret)
        }

        return retrieveToken(request, resource, form, HttpHeaders())
    }
}