package org.mydrugs.mydrugs.diary;

public record DiaryThought(String textKey) {
    public DiaryThought {
        if (textKey == null || textKey.isBlank()) {
            textKey = "diary.mydrugs.thought.start";
        }
    }
}
