package com.twins.core.global.model.user.repository;

import com.twins.core.global.model.user.GlobalUser;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    void deleteExpiredBuffs();

    CompletableFuture<List<Object[]>> insertOrUpdate(Collection<GlobalUser> globalUsers);

    GlobalUser findOne(String nickname);

    List<Object[]> findTop();

}
