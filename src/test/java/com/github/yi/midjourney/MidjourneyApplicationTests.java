package com.github.yi.midjourney;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yi.midjourney.model.Action;
import com.github.yi.midjourney.model.Task;
import com.github.yi.midjourney.model.TaskStatus;
import com.github.yi.midjourney.service.TaskLogService;
import com.github.yi.midjourney.util.CosUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MidjourneyApplicationTests {
    @Autowired
    CosUtil cosUtil;
    @Autowired
    TaskLogService taskLogService;

    /**
     * join查询
     */
    @Test
    public void upTest() {
        String url = "https://cdn.discordapp.com/attachments/1105311905368784940/1106124460068700220/aoxue_6168649868003003Cute_Magical_Flying_Dogs_fantasy_art_draw_2edd985f-73e8-4066-8d40-db9c521b7af2.png";
        String cosUrl = cosUtil.cosUpload(url);
        System.out.println(cosUrl);
    }

    @Test
    public void logTest() {
        LambdaQueryWrapper<Task> userInfoQueryWrapper = new LambdaQueryWrapper<>();
        String taskId = "3852634506949162";
        userInfoQueryWrapper.eq(Task::getRelatedTaskId, taskId)
                .in(Task::getAction, Arrays.asList(Action.UPSCALE, Action.VARIATION))
                .ne(Task::getTaskStatus, TaskStatus.SUCCESS)
                .orderByDesc(Task::getId);

        Task task = taskLogService.getOne(userInfoQueryWrapper,false);

        System.out.println(task);
    }
}
