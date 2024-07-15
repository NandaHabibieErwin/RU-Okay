package com.blackjack.ru_okay.agora

import android.content.Context
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

object AgoraManager {
    private var rtcEngine: RtcEngine? = null

    fun initialize(context: Context, appId: String, eventHandler: IRtcEngineEventHandler) {
        rtcEngine = RtcEngine.create(context, appId, eventHandler)
    }

    fun getRtcEngine(): RtcEngine? {
        return rtcEngine
    }

    fun destroy() {
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
    }
}
