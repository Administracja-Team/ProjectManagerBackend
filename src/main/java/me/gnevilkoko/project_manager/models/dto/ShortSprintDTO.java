package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShortSprintDTO {
    private long id;
    private String name;
    private String description;
    private int tasks;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("is_ended")
    private boolean isEnded;

    @JsonProperty("is_started")
    private boolean isStarted;

    @JsonProperty("done_percents")
    private double donePercents;
}
