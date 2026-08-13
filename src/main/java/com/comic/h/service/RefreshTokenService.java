package com.comic.h.service;

import java.util.Optional;

import com.comic.h.entity.RefreshToken;
import com.comic.h.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(User user);

    int deleteExpiredTokens();
}
