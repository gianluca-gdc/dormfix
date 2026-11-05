package com.dormfix.ticket;

import com.dormfix.user.Role;

/** Plain business rules for changing a ticket's status. */
public final class StatusRules {
    private StatusRules() { }

    public static boolean isAllowed(TicketStatus from, TicketStatus to, Role actorRole, boolean actorIsOwner) {
        if (actorRole == Role.STAFF) {
            return (from == TicketStatus.OPEN && to == TicketStatus.IN_PROGRESS)
                    || (from == TicketStatus.IN_PROGRESS && to == TicketStatus.RESOLVED);
        }

        return actorRole == Role.RESIDENT && actorIsOwner
                && from == TicketStatus.RESOLVED && to == TicketStatus.OPEN;
    }
}
