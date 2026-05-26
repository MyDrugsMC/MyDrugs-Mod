package org.mydrugs.mydrugs.core.drug.use;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.List;

public record DrugUseResult(Status status, List<PsyKnowledgeKey> grantedKnowledge, @Nullable String messageKey) {
    public DrugUseResult {
        grantedKnowledge = List.copyOf(grantedKnowledge);
    }

    public static DrugUseResult success(List<PsyKnowledgeKey> grantedKnowledge) {
        return new DrugUseResult(Status.SUCCESS, grantedKnowledge, null);
    }

    public static DrugUseResult noDrugModel() {
        return new DrugUseResult(Status.NO_DRUG_MODEL, List.of(), null);
    }

    public static DrugUseResult blocked(String messageKey) {
        return new DrugUseResult(Status.BLOCKED_MISSING_KNOWLEDGE, List.of(), messageKey);
    }

    public static DrugUseResult failed() {
        return new DrugUseResult(Status.FAILED, List.of(), null);
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
