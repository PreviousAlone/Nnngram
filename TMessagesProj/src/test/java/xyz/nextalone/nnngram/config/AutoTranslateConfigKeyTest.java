package xyz.nextalone.nnngram.config;

/**
 * Lightweight JVM regression test without Android or JUnit dependencies.
 */
public final class AutoTranslateConfigKeyTest {

    public static void main(String[] args) {
        assertEquals("private dialog key", "autoTranslate_42", AutoTranslateConfigKey.forDialog(42, 0));
        assertEquals("group dialog key", "autoTranslate_-42", AutoTranslateConfigKey.forDialog(-42, 0));
        assertEquals("topic dialog key", "autoTranslate_-42_7", AutoTranslateConfigKey.forDialog(-42, 7));
        assertEquals("legacy group key", "autoTranslate_42", AutoTranslateConfigKey.legacyPositiveChatKey(-42, 0));
        assertEquals("legacy topic key", "autoTranslate_42_7", AutoTranslateConfigKey.legacyPositiveChatKey(-42, 7));
        assertEquals("private dialog has no legacy chat key", null, AutoTranslateConfigKey.legacyPositiveChatKey(42, 0));

        assertTrue("migrate untouched legacy group value",
                AutoTranslateConfigKey.shouldMigrateLegacyPositiveChatKey(-42, false, false, true));
        assertFalse("canonical signed value takes precedence",
                AutoTranslateConfigKey.shouldMigrateLegacyPositiveChatKey(-42, true, false, true));
        assertFalse("migration marker prevents restoring a removed override",
                AutoTranslateConfigKey.shouldMigrateLegacyPositiveChatKey(-42, false, true, true));
        assertFalse("private dialog value is never treated as legacy chat data",
                AutoTranslateConfigKey.shouldMigrateLegacyPositiveChatKey(42, false, false, true));
        assertFalse("missing legacy value does not create an override",
                AutoTranslateConfigKey.shouldMigrateLegacyPositiveChatKey(-42, false, false, false));
    }

    private static void assertEquals(String label, String expected, String actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(String label, boolean actual) {
        if (!actual) {
            throw new AssertionError(label + ": expected true");
        }
    }

    private static void assertFalse(String label, boolean actual) {
        if (actual) {
            throw new AssertionError(label + ": expected false");
        }
    }
}
