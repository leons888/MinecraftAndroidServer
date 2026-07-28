package com.bdscontrol.app;

final class PlayitManager {
    // Playit agent is not an Android SDK. The app can manage a user-supplied Linux ARM64 agent,
    // but cannot claim tunnel status until the agent emits it. This class keeps that boundary explicit.
    private final LogManager log; PlayitManager(LogManager l){log=l;}
    void unavailable(){log.event("error","Playit agent integration requires a compatible ARM64 agent binary and accepted Playit terms; no fake status is shown.");}
}
