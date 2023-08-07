package io.github.weasleyj.simple.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * User
 *
 * @author weasley
 * @version 1.0.0
 */
@Data
public class User implements Serializable {
    private Long id = 1L;
    private String name = "张三";
    private Integer age = 22;
}
