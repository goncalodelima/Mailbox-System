package com.twins.core.global.model.mail;

import com.twins.core.global.model.mail.type.MailboxType;

import java.util.Objects;

public class Mailbox {

    private final MailboxType type;
    private final String location;
    private final long currentTime;
    private boolean toDelete;

    public Mailbox(MailboxType type, String location, long currentTime) {
        this.type = type;
        this.location = location;
        this.currentTime = currentTime;
    }

    public MailboxType getType() {
        return type;
    }

    public String getLocation() {
        return location;
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
        if (o == null || getClass() != o.getClass()) return false;
        Mailbox mailbox = (Mailbox) o;
        return currentTime == mailbox.currentTime && type == mailbox.type && Objects.equals(location, mailbox.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, location, currentTime);
    }

}
