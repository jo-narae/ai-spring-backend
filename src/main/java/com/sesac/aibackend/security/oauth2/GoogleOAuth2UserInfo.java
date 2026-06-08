package com.sesac.aibackend.security.oauth2;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo{
    private final OidcUser oidUser;

    public GoogleOAuth2UserInfo(OidcUser oidcUser){
        this.oidUser = oidcUser;
    }

    @Override
    public String getProvider(){
        return "google";
    }

    @Override
    public String getProviderId(){
        return oidUser.getSubject();
    }

    @Override
    public String getEmail(){
        return oidUser.getEmail();
    }
}
