package com.github.yi.midjourney.dto;

import com.github.yi.midjourney.model.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YI
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO extends Task {
    /**
     * 当前任务排队的位置
     */
    Integer taskPosition;

    @Override
    public String toString() {
        return super.toString() + "ToStringDemo{" + "secondName='" + taskPosition + '\'' + '}';
    }
}
