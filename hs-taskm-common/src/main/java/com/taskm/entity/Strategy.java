package com.taskm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Strategy entity representing a trading strategy.
 * Strategies can be written in multiple programming languages and executed in containers.
 */
@Data
@TableName("strategy")
public class Strategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Unique strategy name
     */
    private String name;

    /**
     * Strategy description
     */
    private String description;

    /**
     * Programming language (python, javascript, java, etc.)
     */
    private String language;

    /**
     * Strategy code to be executed
     */
    private String code;

    /**
     * Creation timestamp
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
