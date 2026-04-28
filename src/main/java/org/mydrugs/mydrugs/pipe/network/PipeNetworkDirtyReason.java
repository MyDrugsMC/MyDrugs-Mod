package org.mydrugs.mydrugs.pipe.network;

public enum PipeNetworkDirtyReason {
    PIPE_PLACED,
    PIPE_REMOVED,
    NEIGHBOR_CHANGED,
    SIDE_CONFIG_CHANGED,
    FILTER_CHANGED,
    CHUNK_LOAD,
    CHUNK_UNLOAD,
    CAPABILITY_INVALIDATED
}
