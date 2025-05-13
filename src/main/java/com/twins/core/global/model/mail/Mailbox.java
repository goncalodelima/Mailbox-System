package com.twins.core.global.model.mail;

import com.twins.core.dailytops.DailyTopType;
import com.twins.core.global.model.mail.type.MailboxType;

import java.util.Objects;
import java.util.UUID;

public class Mailbox {

    private final UUID uuid;
    private final MailboxType type;
    private final String location;
    private final DailyTopType rankingType;
    private final long currentTime;
    private boolean toDelete;

    public Mailbox(MailboxType type, String location, long currentTime) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.location = location;
        this.rankingType = null;
        this.currentTime = currentTime;
    }

    public Mailbox(UUID uuid, MailboxType type, String location, long currentTime) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        this.rankingType = null;
        this.currentTime = currentTime;
    }

    public Mailbox(MailboxType type, String location, DailyTopType rankingType, long currentTime) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.location = location;
        this.rankingType = rankingType;
        this.currentTime = currentTime;
    }

    public Mailbox(UUID uuid, MailboxType type, String location, DailyTopType rankingType, long currentTime) {
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        this.rankingType = rankingType;
        this.currentTime = currentTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public MailboxType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public DailyTopType getRankingType() {
        return rankingType;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public boolean isToDelete() {
        return toDelete;
    }

    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mailbox mailbox)) return false;
        return Objects.equals(uuid, mailbox.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

}
