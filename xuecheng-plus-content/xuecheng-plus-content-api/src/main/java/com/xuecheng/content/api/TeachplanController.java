package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @PostMapping("/teachplan")
    public void addTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.addTeachplan(saveTeachplanDto);
    }

    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable("id") Long id){
        teachplanService.deleteTeachplan(id);
    }

    @PostMapping("/teachplan/moveup/{id}")
    public void teachplanMoveUp(@PathVariable("id") Long id){
        teachplanService.moveUp(id);
    }

    @PostMapping("/teachplan/movedown/{id}")
    public void teachplanMoveDown(@PathVariable("id") Long id){
        teachplanService.moveDown(id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("解绑课程计划和媒资信息")
    @DeleteMapping("/teachplan/association/media/{teachplanId}/{mediaId}")
    public void dissociationMedia(@PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        teachplanService.deleteTeachplanMedia(teachplanId, mediaId);
    }
}
