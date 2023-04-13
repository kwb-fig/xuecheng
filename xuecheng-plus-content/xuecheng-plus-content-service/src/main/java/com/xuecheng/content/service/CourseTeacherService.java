package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacher> querylistCourseTeacher(Long courseId);

    CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long courseId, Long id);
}
