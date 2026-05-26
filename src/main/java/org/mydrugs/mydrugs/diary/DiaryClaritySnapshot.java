package org.mydrugs.mydrugs.diary;

import java.util.List;

public record DiaryClaritySnapshot(
        DiaryThought thought,
        List<DiaryBreadcrumb> breadcrumbs,
        List<DiaryBlocker> blockers,
        List<DiaryMemory> memories,
        List<DiaryWarning> warnings,
        boolean diagnosticMode
) {
    public static final DiaryClaritySnapshot EMPTY = new DiaryClaritySnapshot(
            new DiaryThought("diary.mydrugs.thought.start"),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            false
    );

    public DiaryClaritySnapshot {
        if (thought == null) {
            thought = EMPTY.thought();
        }
        breadcrumbs = List.copyOf(breadcrumbs == null ? List.of() : breadcrumbs);
        blockers = List.copyOf(blockers == null ? List.of() : blockers);
        memories = List.copyOf(memories == null ? List.of() : memories);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
