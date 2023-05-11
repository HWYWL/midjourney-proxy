package com.github.yi.midjourney.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yi.midjourney.service.NotifyService;
import com.github.yi.midjourney.support.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void notifyTaskChange(Task task) {
		String paramsStr = null;
		try {
			paramsStr = OBJECT_MAPPER.writeValueAsString(task);
			log.debug("任务变更, 触发推送, task: {}", paramsStr);

			// todo 内部数据推送
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}

		// 如果有天回调地址，可以执行回调函数
		String notifyHook = task.getNotifyHook();
		if (CharSequenceUtil.isNotBlank(notifyHook)) {
			postJson(notifyHook, paramsStr);
		}
	}

	private void postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return;
			}
			log.warn("回调通知接口失败, code: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
		} catch (RestClientException e) {
			log.warn("回调通知接口失败, {}", e.getMessage());
		}
	}

}
