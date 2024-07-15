package com.blackjack.ru_okay.consult

data class CallRequest(
    val callerId: String = "",
    val calleeId: String = "",
    val status: String = "",
    val channelName: String = ""
)
