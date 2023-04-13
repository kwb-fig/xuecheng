package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(Long courseId);

    void addTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(Long id);

    void moveUp(Long id);

    void moveDown(Long id);

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
     TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 删除指定教学计划-媒资绑定信息
     *
     * @param teachplanId 教学计划id
     * @param mediaId     媒资id
     */
    void deleteTeachplanMedia(Long teachplanId, String mediaId);
}
