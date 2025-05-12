package org.novize.api.dtos.timer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimerUpdateDto {
    private Long remainingTimeMillis;
    private Boolean timerActive;
}

