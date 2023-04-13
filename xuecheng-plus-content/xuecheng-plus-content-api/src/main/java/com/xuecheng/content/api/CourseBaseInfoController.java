package com.xuecheng.content.api;

import com.xuecheng.base.Util.Result;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/11 15:44
 */
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping ("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") // 拥有课程列表查询的授权方可访问（jwt中保存了UserDetails信息）
    public PageResult<CourseBase> list(PageParams pageParams,
                       @RequestBody(required=false) QueryCourseParamsDto queryCourseParamsDto) {
        //获取令牌中用户信息
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);
    }

    //新增课程
    @PostMapping("/course")
    public CourseBaseInfoDto addCourse(@RequestBody AddCourseDto addCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.AddCourseBase(companyId,addCourseDto);
    }

    @GetMapping("/course/{id}")
    public CourseBaseInfoDto queryCourseById(@PathVariable("id") Long id){

        return courseBaseInfoService.queryCourseBaseById(id);
    }

    //修改课程
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourse(@RequestBody EditCourseDto courseDto){
        Long companyId =1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,courseDto);
    }

    //删除课程
    @DeleteMapping("/course/{id}")
    public void deleteCourse(@PathVariable("id") Long id){
        Long companyId = 1232141425L;
        courseBaseInfoService.deleteCourse(companyId,id);
    }
}
