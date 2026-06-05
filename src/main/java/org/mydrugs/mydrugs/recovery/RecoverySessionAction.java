package org.mydrugs.mydrugs.recovery;

public enum RecoverySessionAction {
    MUSIC("message.mydrugs.recovery_session.grounded_music", true, false),
    HEADPHONES("message.mydrugs.recovery_session.grounded_headphones", true, false),
    TEA("message.mydrugs.recovery_session.grounded_tea", true, false),
    PREPARED_TEA("message.mydrugs.recovery_session.prepared_tea", true, false),
    BREATHING("message.mydrugs.recovery_session.grounded_breathing", true, false),
    DIARY("message.mydrugs.recovery_session.reflected_diary", false, true),
    THERAPY("message.mydrugs.recovery_session.reflected_therapy", false, true),
    SLEEP("message.mydrugs.recovery_session.reflected_sleep", false, true),
    ALCOVE("message.mydrugs.recovery_session.reflected_alcove", false, true);

    private final String messageKey;
    private final boolean grounding;
    private final boolean reflection;

    RecoverySessionAction(String messageKey, boolean grounding, boolean reflection) {
        this.messageKey = messageKey;
        this.grounding = grounding;
        this.reflection = reflection;
    }

    public String messageKey() {
        return messageKey;
    }

    public boolean grounding() {
        return grounding;
    }

    public boolean reflection() {
        return reflection;
    }
}
