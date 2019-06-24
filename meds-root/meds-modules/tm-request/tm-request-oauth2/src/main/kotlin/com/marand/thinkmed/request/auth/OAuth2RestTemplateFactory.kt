package com.marand.thinkmed.request.auth

import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails
import org.springframework.security.oauth2.common.AuthenticationScheme
import org.springframework.web.client.RestTemplate

/**
 * Factory for all types of oauth2 rest templates. Rest templates are reusable, no need to create new instance for each usage.
 *
 * @see RestTemplate
 *
 * @author Nejc Korasa
 */

open class OAuth2RestTemplateFactory {

    /**
     * Creates client template
     *
     * Token is retrieved using provided client id and secret.
     */
    fun createClientTemplate(
            clientId: String,
            clientSecret: String,
            accessTokenUri: String,
            gsonMessageConverter: GsonHttpMessageConverter? = null): OAuth2RestTemplate {

        clientId.takeIf { it.isNotBlank() } ?: throw IllegalStateException("clientId must not be blank!")
        clientSecret.takeIf { it.isNotBlank() } ?: throw IllegalStateException("clientSecret must not be blank!")
        accessTokenUri.takeIf { it.isNotBlank() } ?: throw IllegalStateException("accessTokenUri must not be blank!")

        val resourceDetails = ClientCredentialsResourceDetails().apply {

            clientAuthenticationScheme = AuthenticationScheme.form
            this.clientId = clientId
            this.clientSecret = clientSecret
            this.accessTokenUri = accessTokenUri
        }

        return OAuth2RestTemplate(resourceDetails, DefaultOAuth2ClientContext()).apply {

            setAccessTokenProvider(AccessTokenProviderChain(listOf(
                    ClientCredentialsRefreshTokenProvider(),
                    ClientCredentialsAccessTokenProvider())))

            gsonMessageConverter?.let { messageConverters = listOf(it) }
        }
    }

    /**
     * Creates resource owner template
     *
     * Token is retrieved using parameters provided (clientId, clientSecret, accessTokeUri, ...).
     */
    fun createResourceOwnerTemplate(
            clientId: String,
            clientSecret: String,
            accessTokenUri: String,
            username: String,
            password: String,
            gsonMessageConverter: GsonHttpMessageConverter? = null): OAuth2RestTemplate {

        clientId.takeIf { it.isNotBlank() } ?: throw IllegalStateException("clientId must not be blank!")
        clientSecret.takeIf { it.isNotBlank() } ?: throw IllegalStateException("clientSecret must not be blank!")
        accessTokenUri.takeIf { it.isNotBlank() } ?: throw IllegalStateException("accessTokenUri must not be blank!")
        username.takeIf { it.isNotBlank() } ?: throw IllegalStateException("username must not be blank!")
        password.takeIf { it.isNotBlank() } ?: throw IllegalStateException("password must not be blank!")

        val resourceDetails = ResourceOwnerPasswordResourceDetails().apply {

            this.username = username
            this.password = password
            clientAuthenticationScheme = AuthenticationScheme.form
            this.clientId = clientId
            this.clientSecret = clientSecret
            this.accessTokenUri = accessTokenUri
        }

        return OAuth2RestTemplate(resourceDetails, DefaultOAuth2ClientContext()).apply {

            setAccessTokenProvider(AccessTokenProviderChain(listOf(ResourceOwnerPasswordAccessTokenProvider())))

            gsonMessageConverter?.let { messageConverters = listOf(it) }
        }
    }

    /**
     * Creates token relay template
     *
     * Token is taken from Spring's security token.
     */
    fun createTokenRelayTemplate(gsonMessageConverter: GsonHttpMessageConverter? = null): TokenRelayOAuth2RestTemplate =
            TokenRelayOAuth2RestTemplate.build().apply { gsonMessageConverter?.let { messageConverters = listOf(it) } }
}