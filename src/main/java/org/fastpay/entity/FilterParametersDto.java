package org.fastpay.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FilterParametersDto extends Transfer {
    private Long limit;
    private String sort;
    private String order;
}
