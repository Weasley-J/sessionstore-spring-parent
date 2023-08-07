package io.github.weasleyj.simple.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User DTO
 *
 * @author weasley
 * @version 1.0.0
 */
@Data
public class UserDTO implements Serializable {
    private Long uid;
    private String name;
    private Integer age;
}
