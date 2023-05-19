package com.github.yi.midjourney.dto;

import com.github.yi.midjourney.model.UserInfo;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * @author Administrator
 */
@Getter
@Setter
public class UserInfoDTO extends UserInfo {
    /**
     * 用户登录token
     */
    private String satoken;

    public UserInfoDTO(Integer id, String userName, String password, Integer state, String groundsProhibitio, Timestamp vipStartTime, Timestamp vipEndTime) {
        super(id, userName, password, state, groundsProhibitio, vipStartTime, vipEndTime);
    }
}
