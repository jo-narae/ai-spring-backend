package com.sesac.aibackend.security.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo create(String registrationId, Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if ("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo((OidcUser) principal);
        }
        if ("kakao".equals(registrationId)) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return new KakaoOAuth2UserInfo(oauth2User.getAttributes());
        }

        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
    }
}