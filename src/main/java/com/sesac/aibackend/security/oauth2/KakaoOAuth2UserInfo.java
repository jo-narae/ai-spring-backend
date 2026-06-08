package com.sesac.aibackend.security.oauth2;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    @Override
    public String getProvider(){
        return "kakao";
    }
    @Override
    public String getProviderId(){
        return String.valueOf(attributes.get("id"));
    }
    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        if(kakaoAccount == null){
            return null;
        }
        return (String) kakaoAccount.get("email");
    }
}
