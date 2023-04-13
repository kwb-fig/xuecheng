package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    //查询该课程所有老师信息
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> querylistCourseTeacher(@PathVariable("id") Long courseId){
        return courseTeacherService.querylistCourseTeacher(courseId);
    }

    //添加和修改教师
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.addCourseTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable("courseId") Long courseId,@PathVariable("id") Long id){
        courseTeacherService.deleteCourseTeacher(courseId,id);
    }
}
