package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.Exception.*;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void addTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //由id是否为空来判断是新增还是修改
        Long id = saveTeachplanDto.getId();
        if (id == null) {
            //新增章节
            Long courseId = saveTeachplanDto.getCourseId();
            Teachplan teachplan = new Teachplan();
            //grage为1表示加大章节,2就是添加小章节
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);

            //查询同级课程数量，为了order排序
            Long parentId = saveTeachplanDto.getParentid();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
            Integer integer = teachplanMapper.selectCount(queryWrapper);
            teachplan.setOrderby(integer+1);

            int insert = teachplanMapper.insert(teachplan);
            if(insert<1){
                throw new addCourseException("新增章节失败");
            }
        }else {
            //修改章节名称
            Teachplan teachplan = teachplanMapper.selectById(id);
            teachplan.setPname(saveTeachplanDto.getPname());
            teachplan.setIsPreview(saveTeachplanDto.getIsPreview());
            int i = teachplanMapper.updateById(teachplan);
            if(i<1){
                throw new updateCourseException("修改章节名称失败");
            }
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        //1.判断删除章节等级
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer grade = teachplan.getGrade();
        if(grade==1){
            //2.1如果是大章节，则先要删除小章节
            List<Teachplan> teachplans = teachplanMapper.selectSmallTeachplan(id);
            if(teachplans!=null && !teachplans.isEmpty()){
                throw new deleteTeachplanException("请先删除小章节");
            }
            //2.2 大章节底下为空,删除
            int i = teachplanMapper.deleteById(id);
            if(i<1){
                throw new deleteTeachplanException("删除大章节异常");
            }

        }else {
            Long courseId = teachplan.getCourseId();
            Long parentId = teachplan.getParentid();
            //2.2如果是小章节,则可以直接删除,同时将teachplanmedia关联信息删除
            int i = teachplanMapper.deleteById(id);
            LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            teachplanMediaMapper.delete(lambdaQueryWrapper);

            //并且将所有同级小章节的order减去一，方便上移和下移，保证orderby的连续性
            Integer orderby1 = teachplan.getOrderby();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);

            teachplanMapper.selectList(queryWrapper).stream().forEach(item->{
                Integer orderby = item.getOrderby();
                if(orderby>orderby1) {
                    //只有比删除的orderby大的值，才减一
                    item.setOrderby(orderby - 1);
                    teachplanMapper.updateById(item);
                }
            });
            if(i<1){
                throw new deleteTeachplanException("删除小章节异常");
            }
        }
    }

    @Override
    public void moveUp(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();
        //如果上移第一个，则不操作
        if(orderby!=1){
            LambdaQueryWrapper<Teachplan> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getOrderby,orderby-1).
                    eq(Teachplan::getParentid,parentid);

            //上一个teachplan，主要就是交换上下两个的orderby
            Teachplan teachplan1 = teachplanMapper.selectOne(lambdaQueryWrapper);
            Integer orderby1 = teachplan1.getOrderby();

            teachplan.setOrderby(orderby1);
            teachplan1.setOrderby(orderby);

            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(teachplan1);
        }
    }

    @Override
    public void moveDown(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);

        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();
        Long parentid = teachplan.getParentid();

        //查询最后一个orderby的值，如果下移最后一个，则不操作
        LambdaQueryWrapper<Teachplan> QueryWrapper=new LambdaQueryWrapper<>();
        QueryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentid);
        Integer integer = teachplanMapper.selectCount(QueryWrapper);

        if(orderby!=integer){
            LambdaQueryWrapper<Teachplan> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getOrderby,orderby+1).
                    eq(Teachplan::getParentid,parentid);

            //下一个teachplan，主要就是交换上下两个的orderby
            Teachplan teachplan1 = teachplanMapper.selectOne(lambdaQueryWrapper);
            Integer orderby1 = teachplan1.getOrderby();

            teachplan.setOrderby(orderby1);
            teachplan1.setOrderby(orderby);

            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(teachplan1);
        }
    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            throw new addCourseTeacherException("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            throw new deleteTeachplanException("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }


    @Override
    public void deleteTeachplanMedia(Long teachplanId, String mediaId) {
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachplanId)
                .eq(TeachplanMedia::getMediaId, mediaId));
        if (delete <= 0) {
            log.warn("删除课程计划{}-媒资{}信息失败", teachplanId, mediaId);
        }
    }

}
