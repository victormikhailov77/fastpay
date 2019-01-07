package org.fastpay.entity;

import lombok.Data;

@Data
public class FilterParametersDto extends Transfer {
    private Long limit;
    private String sort;
    private String order;
}
