package com.kusitms.mainservice.domain.user.auth;

import com.kusitms.mainservice.domain.user.auth.google.GoogleAuthProvider;
import com.kusitms.mainservice.domain.user.auth.google.GoogleUserInfo;
import com.kusitms.mainservice.domain.user.auth.kakao.KakaoAuthProvider;
import com.kusitms.mainservice.domain.user.auth.kakao.KakaoUserInfo;
import com.kusitms.mainservice.domain.user.domain.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RestTemplateProvider {
    private final GoogleAuthProvider googleAuthProvider;
    private final KakaoAuthProvider kakaoAuthProvider;

    public PlatformUserInfo getUserInfoUsingRestTemplate(Platform platform, String accessToken) {
        ResponseEntity<String> platformResponse = getUserInfoFromPlatform(platform, accessToken);
        return getUserInfoFromPlatformInfo(platform, platformResponse.getBody());
    }

    private ResponseEntity<String> getUserInfoFromPlatform(Platform platform, String accessToken) {
        if (platform.equals(Platform.KAKAO))
            return kakaoAuthProvider.createGetRequest(accessToken);
        return googleAuthProvider.createGetRequest(accessToken);
    }

    private PlatformUserInfo getUserInfoFromPlatformInfo(Platform platform, String platformInfo) {
        if (platform.equals(Platform.KAKAO)) {
            KakaoUserInfo kakaoUserInfo = kakaoAuthProvider.getKakaoUserInfoFromPlatformInfo(platformInfo);
            return PlatformUserInfo.createPlatformUserInfo(
                    Long.toString(kakaoUserInfo.getId()),
                    kakaoUserInfo.getKakaoAccount().getEmail(),
                    kakaoUserInfo.getKakaoAccount().getProfile().getNickname(),
                    kakaoUserInfo.getPicture());
        } else {
            GoogleUserInfo googleUserInfo = googleAuthProvider.getGoogleUserInfoFromPlatformInfo(platformInfo);
            return PlatformUserInfo.createPlatformUserInfo(
                    Long.toString(googleUserInfo.getId()),
                    googleUserInfo.getEmail(),
                    googleUserInfo.getName(),
                    googleUserInfo.getPicture());
        }
    }
}