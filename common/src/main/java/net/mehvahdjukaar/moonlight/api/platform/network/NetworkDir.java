package net.mehvahdjukaar.moonlight.api.platform.network;

public enum NetworkDir {
    SERVER_BOUND, CLIENT_BOUND;


    public NetworkDir getOpposite() {
        return this == SERVER_BOUND ? CLIENT_BOUND : SERVER_BOUND;
    }

}