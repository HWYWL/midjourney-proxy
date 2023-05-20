package com.github.yi.midjourney.dto;

import com.github.yi.midjourney.model.UserInfo;
import lombok.*;

/**
 * @author YI
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO extends UserInfo {
    /**
     * 登录授权的token
     */
    private String satoken;

    @Override
    public String toString() {
        return super.toString() + "ToString{" + "secondName='" + satoken + '\'' + '}';
    }
}
