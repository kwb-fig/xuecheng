package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
  * 课程分类管理的接口
 */
public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
