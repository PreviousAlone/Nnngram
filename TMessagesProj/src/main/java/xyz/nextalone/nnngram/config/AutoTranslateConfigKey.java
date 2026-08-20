/*
 * Copyright (C) 2019-2026 Nnngram Contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package xyz.nextalone.nnngram.config;

final class AutoTranslateConfigKey {
    private static final String KEY_PREFIX = "autoTranslate_";
    private static final String SIGNED_DIALOG_MIGRATION_PREFIX = "autoTranslateSignedDialogV2_";

    private AutoTranslateConfigKey() {
    }

    static String forDialog(long dialogId, long topicId) {
        return KEY_PREFIX + dialogId + topicSuffix(topicId);
    }

    static String legacyPositiveChatKey(long dialogId, long topicId) {
        if (dialogId >= 0) {
            return null;
        }
        return KEY_PREFIX + -dialogId + topicSuffix(topicId);
    }

    static String signedDialogMigrationMarker(long dialogId, long topicId) {
        return SIGNED_DIALOG_MIGRATION_PREFIX + dialogId + topicSuffix(topicId);
    }

    static boolean shouldMigrateLegacyPositiveChatKey(
            long dialogId,
            boolean hasCanonicalValue,
            boolean hasMigrationMarker,
            boolean hasLegacyValue
    ) {
        return dialogId < 0 && !hasCanonicalValue && !hasMigrationMarker && hasLegacyValue;
    }

    private static String topicSuffix(long topicId) {
        return topicId != 0 ? "_" + topicId : "";
    }
}
