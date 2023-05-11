package com.github.yi.midjourney.service;


import com.github.yi.midjourney.util.Message;

public interface DiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> upscale(String messageId, int index, String messageHash);

	Message<Void> variation(String messageId, int index, String messageHash);

	Message<Void> reset(String messageId, String messageHash);

}