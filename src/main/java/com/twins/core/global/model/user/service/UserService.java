package com.twins.core.global.model.user.service;

import com.minecraftsolutions.database.Database;
import com.twins.core.CorePlugin;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.global.model.user.repository.UserFoundationRepository;
import com.twins.core.global.model.user.repository.UserRepository;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class UserService implements UserFoundationService {

    private final Map<UUID, GlobalUser> cache = new ConcurrentHashMap<>();

    private final Map<UUID, GlobalUser> pendingUpdates = new ConcurrentHashMap<>();

    private final Set<Player> villainOnlineUsers = new HashSet<>();

    private final UserFoundationRepository userRepository;

    public UserService(Database database) {
        userRepository = new UserRepository(database);
        userRepository.setup();
        userRepository.deleteExpiredBuffs();
    }

    @Override
    public void put(GlobalUser globalUser) {
        cache.put(globalUser.getUuid(), globalUser);
    }

    @Override
    public void update(GlobalUser globalUser) {
        pendingUpdates.put(globalUser.getUuid(), globalUser);
    }

    @Override
    public void updateNickname(UUID uuid, String nickname) {
        userRepository.updateNickname(uuid, nickname);
    }

    @Override
    public CompletableFuture<List<Object[]>> update(Collection<GlobalUser> globalUsers) {
        return userRepository.insertOrUpdate(globalUsers);
    }

    @Override
    public List<Object[]> updateOnDisable(Collection<GlobalUser> globalUsers) {
        return userRepository.insertOrUpdateOnDisable(globalUsers);
    }

    @Override
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public GlobalUser get(UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public GlobalUser getData(UUID uuid, String nickname) {

        try {
            GlobalUser userRepositoryOne = userRepository.findOne(uuid);
            return Objects.requireNonNullElseGet(userRepositoryOne, () -> new GlobalUser(uuid, nickname, 0, 0, null, true, false, false, false, false, false, false, LanguageType.EN, true, true, false, 0, 0, ConcurrentHashMap.newKeySet(), null));
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public CompletableFuture<GlobalUser> getAsyncData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {

            try {
                return userRepository.findOne(uuid);
            } catch (Exception e) {
                return null;
            }

        }, CorePlugin.INSTANCE.getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Boolean> addMailboxAsync(UUID uuid, Mailbox mailbox) {
        return userRepository.addMailboxAsync(uuid, mailbox);
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
    public Map<UUID, GlobalUser> getPendingUpdates() {
        return pendingUpdates;
    }

    @Override
    public Set<Player> getVillainOnlineUsers() {
        return villainOnlineUsers;
    }

}
