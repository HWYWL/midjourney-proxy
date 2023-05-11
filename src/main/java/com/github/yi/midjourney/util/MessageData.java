package com.github.yi.midjourney.util;

import com.github.yi.midjourney.model.Action;
import lombok.Data;

@Data
public class MessageData {
	private Action action;
	private String prompt;
	private int index;
	private String status;
}
