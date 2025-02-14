package ru.greemlab.botmedicine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_schedule")
public class GroupSchedule {

    @Id
    private Long groupChatId;

    private String scheduleUrl;

}

