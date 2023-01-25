package com.disneystreaming.recognito.client.models

case class RecognitoCredentials(
                                 clientId: String,
                                 username: String,
                                 password: String,
                                 issuedForService: String,
                                 region: String
                               )
