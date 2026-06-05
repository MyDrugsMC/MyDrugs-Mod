package org.mydrugs.mydrugs.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModCommandsTest {
    @Test
    void burstRecreateIsAdminGatedAndRequiresConfirmLiteral() {
        assertTrue(ModCommands.burstRecreateRequiresConfirmForTest());
    }
}
