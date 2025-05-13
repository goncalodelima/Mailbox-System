package com.twins.core.global.model.user.repository;

import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    void deleteExpiredBuffs();

    void updateNickname(UUID uuid, String nickname);

    CompletableFuture<List<Object[]>> insertOrUpdate(Collection<GlobalUser> globalUsers);

    List<Object[]> insertOrUpdateOnDisable(Collection<GlobalUser> globalUsers);

    GlobalUser findOne(UUID uuid) throws Exception;

    CompletableFuture<Boolean> addMailboxAsync(UUID uuid, Mailbox mailbox);

    List<Object[]> findTop();

}
