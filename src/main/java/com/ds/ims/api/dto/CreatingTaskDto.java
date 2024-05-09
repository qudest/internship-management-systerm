package com.ds.ims.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * DTO для создания задачи
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatingTaskDto {
    String title;
}
