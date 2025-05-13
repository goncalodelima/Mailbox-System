package com.twins.core.global.model.user.service;

import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationService {

    void put(GlobalUser globalUser);

    void update(GlobalUser globalUser);

    void updateNickname(UUID uuid, String nickname);

    CompletableFuture<List<Object[]>> update(Collection<GlobalUser> globalUsers);

    List<Object[]> updateOnDisable(Collection<GlobalUser> globalUsers);

    void remove(UUID uuid);

    @Nullable GlobalUser get(UUID uuid);

    @Nullable GlobalUser getData(UUID uuid, String nickname);

    CompletableFuture<GlobalUser> getAsyncData(UUID uuid);

    CompletableFuture<Boolean> addMailboxAsync(UUID uuid, Mailbox mailbox);

    Collection<GlobalUser> getAll();

    CompletableFuture<List<Object[]>> getTop();

    Map<UUID, GlobalUser> getPendingUpdates();

    Set<Player> getVillainOnlineUsers();

}
