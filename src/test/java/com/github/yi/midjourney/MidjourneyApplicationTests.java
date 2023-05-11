package com.github.yi.midjourney;

import com.github.yi.midjourney.util.CosUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MidjourneyApplicationTests {
    @Autowired
    CosUtil cosUtil;

    /**
     * join查询
     */
    @Test
    public void upTest() {
        String url = "https://cdn.discordapp.com/attachments/1105311905368784940/1106124460068700220/aoxue_6168649868003003Cute_Magical_Flying_Dogs_fantasy_art_draw_2edd985f-73e8-4066-8d40-db9c521b7af2.png";
        String cosUrl = cosUtil.cosUpload(url);
        System.out.println(cosUrl);
    }
}
