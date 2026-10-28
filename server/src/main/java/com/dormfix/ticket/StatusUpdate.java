package com.dormfix.ticket;

import java.time.LocalDateTime;

import com.dormfix.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "StatusUpdates")
public class StatusUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Updates are a history record for one ticket and one acting user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private TicketStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private TicketStatus newStatus;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected StatusUpdate() { }

    public StatusUpdate(Ticket ticket, User updatedBy, TicketStatus oldStatus,
            TicketStatus newStatus, String note) {
        this.ticket = ticket;
        this.updatedBy = updatedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public User getUpdatedBy() { return updatedBy; }
    public TicketStatus getOldStatus() { return oldStatus; }
    public TicketStatus getNewStatus() { return newStatus; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
