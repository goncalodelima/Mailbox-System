package com.twins.core.global.model.user.service;

import com.minecraftsolutions.database.Database;
import com.twins.core.CorePlugin;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.global.model.user.repository.UserFoundationRepository;
import com.twins.core.global.model.user.repository.UserRepository;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class UserService implements UserFoundationService {

    private final Map<String, GlobalUser> cache = new ConcurrentHashMap<>();

    private final Set<String> insertions = ConcurrentHashMap.newKeySet();

    private final Set<GlobalUser> pendingUpdates = new HashSet<>();

    private final Set<Player> villainOnlineUsers = new HashSet<>();

    private final UserFoundationRepository userRepository;

    public UserService(Database database) {
        userRepository = new UserRepository(database);
        userRepository.setup();
        userRepository.deleteExpiredBuffs();
    }

    @Override
    public boolean isInserting(String nickname) {
        return insertions.contains(nickname);
    }

    @Override
    public void addInserting(String nickname) {
        insertions.add(nickname);
    }

    @Override
    public void removeInserting(String nickname) {
        insertions.remove(nickname);
    }

    @Override
    public void put(GlobalUser globalUser) {
        cache.put(globalUser.getName(), globalUser);
    }

    @Override
    public void update(GlobalUser globalUser) {
        pendingUpdates.add(globalUser);
    }

    @Override
    public CompletableFuture<List<Object[]>> update(Collection<GlobalUser> globalUsers) {
        return userRepository.insertOrUpdate(globalUsers);
    }

    @Override
    public void remove(String nickname) {
        cache.remove(nickname);
    }

    @Override
    public GlobalUser get(String nickname) {
        return cache.get(nickname);
    }

    @Override
    public GlobalUser getData(String nickname) {
        GlobalUser userRepositoryOne = userRepository.findOne(nickname);
        return Objects.requireNonNullElseGet(userRepositoryOne, () -> new GlobalUser(nickname, 0, 0, null, false, LanguageType.EN, true, true, false, 0, 0, new CopyOnWriteArrayList<>()));
    }

    @Override
    public CompletableFuture<GlobalUser> getAsyncData(String nickname) {
        return CompletableFuture.supplyAsync(() -> userRepository.findOne(nickname), CorePlugin.INSTANCE.getAsyncExecutor())
                .exceptionally(e -> {
                    CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve global user data", e);
                    return null;
                });
    }

    @Override
    public Collection<GlobalUser> getAll() {
        return cache.values();
    }

    @Override
    public CompletableFuture<List<Object[]>> getTop() {
        return CompletableFuture.supplyAsync(userRepository::findTop, CorePlugin.INSTANCE.getAsyncExecutor())
                .exceptionally(e -> {
                    CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve all reputation users data", e);
                    return null;
                });
    }

    @Override
    public Set<GlobalUser> getPendingUpdates() {
        return pendingUpdates;
    }

    @Override
    public Set<Player> getVillainOnlineUsers() {
        return villainOnlineUsers;
    }

}
