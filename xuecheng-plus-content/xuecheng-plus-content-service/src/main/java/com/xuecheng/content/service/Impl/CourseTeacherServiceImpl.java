package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.Exception.addCourseTeacherException;
import com.xuecheng.base.Exception.deleteTeachplanException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> querylistCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher) {
        //先查询id，根据id进行添加或者修改
        Long id = courseTeacher.getId();
        if(id==null) {
            //如果没有id，就是添加教师信息
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert < 1) {
                throw new addCourseTeacherException("添加教师异常");
            }
        }else {
            //如果有，就是修改教师信息
            int i = courseTeacherMapper.updateById(courseTeacher);
            if(i<1){
                throw new addCourseTeacherException("修改教师异常");
            }
        }
        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long id) {
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId).eq(CourseTeacher::getId,id);
        int delete = courseTeacherMapper.delete(lambdaQueryWrapper);
        if(delete<1){
            throw new deleteTeachplanException("删除教师信息失败");
        }
    }
}
