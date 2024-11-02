package dev.tronxi.papayaclient.peer;

public enum PeerMessageType {

    INVALID(-1),
    PART_FILE(0),
    ASK_FOR_RESOURCES(1);


    private final int value;

    PeerMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PeerMessageType fromValue(int value) {
        for (PeerMessageType type : PeerMessageType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return INVALID;
    }
}
