package com.kusitms.mainservice.domain.user.domain;

import com.kusitms.mainservice.global.error.exception.InvalidValueException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static com.kusitms.mainservice.global.error.ErrorCode.INVALID_USER_TYPE;

@RequiredArgsConstructor
@Getter
public enum UserType {
    PM("PM"),
    MARKETER("마케터"),
    DESIGNER("디자이너"),
    DEVELOPER("개발자"),
    PLANNER("기획자"),
    EDITOR("에디터");

    private final String stringUserType;

    public static UserType getEnumUserTypeFromStringUserType(String stringUserType) {
        return Arrays.stream(values())
                .filter(userType -> userType.stringUserType.equals(stringUserType))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(INVALID_USER_TYPE));
    }
}
