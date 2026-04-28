package org.mydrugs.mydrugs.pipe.network;

import org.mydrugs.mydrugs.pipe.PipeResourceKind;

public record PipeNetworkKey(PipeResourceKind kind, long id) {
}
