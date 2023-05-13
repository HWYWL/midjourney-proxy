package com.github.yi.midjourney;

import com.github.yi.midjourney.util.ConvertUtils;
import org.junit.jupiter.api.Test;

public class CommonTest {
    @Test
    void getPercentage() {
        String prompt = "**[5583618615898049]Cute Magical Flying Dogs, digital painting, mystery, adventure --niji 5** - <@1099695402535637063> (0%) (fast)";
        String percentage = ConvertUtils.findTaskPercentageByFinalPrompt(prompt);
        System.out.println(percentage);
    }
}
