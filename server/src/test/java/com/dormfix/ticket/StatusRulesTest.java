package com.dormfix.ticket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dormfix.user.Role;
import org.junit.jupiter.api.Test;

class StatusRulesTest {

    @Test
    void staffCanMoveOpenToInProgress() {
        // TC-11
        assertTrue(StatusRules.isAllowed(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, Role.STAFF, false));
    }

    @Test
    void staffCannotSkipOpenToResolved() {
        // TC-12
        assertFalse(StatusRules.isAllowed(TicketStatus.OPEN, TicketStatus.RESOLVED, Role.STAFF, false));
    }

    @Test
    void staffCannotReopenResolvedTicket() {
        // TC-13
        assertFalse(StatusRules.isAllowed(TicketStatus.RESOLVED, TicketStatus.OPEN, Role.STAFF, false));
    }

    @Test
    void ownerResidentCanReopenResolvedTicket() {
        // TC-14
        assertTrue(StatusRules.isAllowed(TicketStatus.RESOLVED, TicketStatus.OPEN, Role.RESIDENT, true));
    }

    @Test
    void nonOwnerResidentCannotReopenResolvedTicket() {
        assertFalse(StatusRules.isAllowed(TicketStatus.RESOLVED, TicketStatus.OPEN, Role.RESIDENT, false));
    }

    @Test
    void residentCannotMoveOpenTicketToInProgress() {
        assertFalse(StatusRules.isAllowed(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, Role.RESIDENT, true));
    }

    @Test
    void sameStatusIsNotAllowed() {
        assertFalse(StatusRules.isAllowed(TicketStatus.OPEN, TicketStatus.OPEN, Role.STAFF, false));
    }

    @Test
    void staffCanMoveInProgressToResolved() {
        assertTrue(StatusRules.isAllowed(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, Role.STAFF, false));
    }
}
