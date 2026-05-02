package org.mydrugs.mydrugs.core.drug.use;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.Optional;

public record DrugUseResult(Status status, Optional<PsyKnowledgeKey> grantedKnowledge, @Nullable String messageKey) {
    public static DrugUseResult success(Optional<PsyKnowledgeKey> grantedKnowledge) {
        return new DrugUseResult(Status.SUCCESS, grantedKnowledge, null);
    }

    public static DrugUseResult noDrugModel() {
        return new DrugUseResult(Status.NO_DRUG_MODEL, Optional.empty(), null);
    }

    public static DrugUseResult blocked(String messageKey) {
        return new DrugUseResult(Status.BLOCKED_MISSING_KNOWLEDGE, Optional.empty(), messageKey);
    }

    public static DrugUseResult failed() {
        return new DrugUseResult(Status.FAILED, Optional.empty(), null);
    }

    public boolean consumed() {
        return this.status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        BLOCKED_MISSING_KNOWLEDGE,
        NO_DRUG_MODEL,
        FAILED
    }
}
