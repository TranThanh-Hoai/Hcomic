package com.comic.h.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.comic.h.entity.Comic;
import com.comic.h.exception.ForbiddenException;

@Component("comicSecurity")
public class ComicSecurityEvaluator {

    public void verifyOwnership(Comic comic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        if (isAdmin(authentication)) {
            return;
        }

        String currentUsername = authentication.getName();
        if (comic == null || comic.getUploader() == null || comic.getUploader().getUsername() == null 
                || !comic.getUploader().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new ForbiddenException("You do not have permission to perform action on this comic");
        }
    }

    public boolean isOwnerOrAdmin(Comic comic, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        String currentUsername = authentication.getName();
        return comic != null && comic.getUploader() != null && comic.getUploader().getUsername() != null 
                && comic.getUploader().getUsername().equalsIgnoreCase(currentUsername);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
