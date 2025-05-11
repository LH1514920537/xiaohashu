package com.quanxiaoha.xiaohashu.note.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelDO {
    private Long id;

    private String name;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Boolean isDeleted;
}