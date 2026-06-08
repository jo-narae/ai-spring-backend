package com.sesac.aibackend.security.oauth2;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
}
