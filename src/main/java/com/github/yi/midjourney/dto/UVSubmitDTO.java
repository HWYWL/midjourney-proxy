package com.github.yi.midjourney.dto;

import lombok.Data;

/**
 * 选择操作
 *
 * @author YI
 */
@Data
public class UVSubmitDTO {
    /**
     * content: id u1.
     */
    private String content;
    /**
     * notifyHook of caller
     */
    private String notifyHook;
}
