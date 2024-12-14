package com.twins.core.global.model.user.service;

import com.twins.core.global.model.user.GlobalUser;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationService {

    boolean isInserting(String nickname);

    void addInserting(String nickname);

    void removeInserting(String nickname);

    void put(GlobalUser globalUser);

    void update(GlobalUser globalUser);

    CompletableFuture<List<Object[]>> update(Collection<GlobalUser> globalUsers);

    void remove(String nickname);

    GlobalUser get(String nickname);

    GlobalUser getData(String nickname);

    CompletableFuture<GlobalUser> getAsyncData(String nickname);

    Collection<GlobalUser> getAll();

    CompletableFuture<List<Object[]>> getTop();

    Set<GlobalUser> getPendingUpdates();

    Set<Player> getVillainOnlineUsers();

}
