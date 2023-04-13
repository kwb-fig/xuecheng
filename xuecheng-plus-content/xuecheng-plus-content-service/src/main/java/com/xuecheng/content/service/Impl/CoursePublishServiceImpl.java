package com.xuecheng.content.service.Impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.Exception.*;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程发布相关接口实现
 * @date 2023/2/21 10:04
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //课程基本信息,营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            throw new addMediaException("课程找不到");
        }
        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();

        //如果课程的审核状态为已提交则不允许提交
        if(auditStatus.equals("202003")){
            throw new addCourseException("课程已提交请等待审核");
        }
        //如果课程的审核状态为已通过则不允许提交
        if(auditStatus.equals("202004")){
            throw new addCourseException("课程已通过审核，无需再提交");
        }
        //本机构只能提交本机构的课程
        //todo:本机构只能提交本机构的课程

        //课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if(StringUtils.isEmpty(pic)){
            throw new addCourseException("请求上传课程图片");
        }
        //查询课程计划
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree == null || teachplanTree.size()==0){
            throw new addCourseException("请编写课程计划");
        }

        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //计划信息
        //转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //审核状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreObj==null){
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");//审核状态为已提交

        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            throw new addMediaException("课程没有审核记录，无法发布");
        }
        //状态
        String status = coursePublishPre.getStatus();
        //课程如果没有审核通过不允许发布
        if(!status.equals("202004")){
            throw new addMediaException("课程没有审核通过不允许发布");
        }

        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //修改课程发布表中发布状态为已发布
        coursePublish.setStatus("203002");
        //先查询课程发布，如果有则更新，没有再添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if(coursePublishObj == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表写入数据
        //mqMessageService.addMessage("course_publish",String.valueOf(courseId),null,null);
        saveCoursePublishMessage(courseId);

        //更新课程基础表中课程发布信息为已发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        int i = courseBaseMapper.updateById(courseBase);
        if(i<1){
            throw new updateCourseException("修改课程基础表失败");
        }
        //将预发布表数据删除
        int i1 = coursePublishPreMapper.deleteById(courseId);
        if(i1<1){
            throw new deleteTeachplanException("删除课程预发布表失败");
        }

    }

    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            throw new saveCoursePublishMessageException("添加消息记录失败");
        }
    }
    @Override
    public File generateCourseHtml(Long courseId) {
        // 静态化文件
        File file = null;
        try {
            // 配置 freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 加载模板，选指定模板路径，classpath 下 templates 下
            // 得到 classpath
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");
            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");
            // 准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            // 静态化
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(html,"utf-8");
            // 创建静态化文件
            file = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态化文件：{}", file.getAbsolutePath());
            // 输出流
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = null;
        try {
            course = mediaServiceClient.upload(multipartFile, "course/"+courseId+".html");
            if (course == null) {
                throw new uploadFileException("远程调用媒资服务上传文件失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new uploadFileException("远程调用媒资服务上传文件失败");
        }

    }
/*
根据课程id查询课程发布信息
 */
    public CoursePublish getCoursePublish(Long courseId) {

        return coursePublishMapper.selectById(courseId);
    }

    //使用redisson实现分布式锁
    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {

        //从缓存中查询
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        //缓存中有
        if (jsonObj != null) {
//            System.out.println("=============从缓存中查询=============");
            //缓存中有直接返回数据
            String jsonString = jsonObj.toString();
            if ("null".equals(jsonString)) {
                return null;
            }
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        } else {
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取分布式锁
            lock.lock();
            try {
                //再次查询一下缓存
                //从缓存中查询
                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
                //缓存中有
                if (jsonObj != null) {
                    //缓存中有直接返回数据
                    String jsonString = jsonObj.toString();
                    if ("null".equals(jsonString)) {
                        return null;
                    }
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("==查询数据库==");
                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                if (coursePublish == null) {
                       redisTemplate.opsForValue().set("course:" + courseId, "null", 30, TimeUnit.SECONDS);
                       return null;
                  }
                //查询完成再存储到redis
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300, TimeUnit.SECONDS);
                return coursePublish;

            } finally {
                //手动释放锁
                lock.unlock();
            }
        }


    }

    @Override
    public void saveToCache(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if(coursePublish==null){
            throw new addCourseException("课程信息为空");
        }
        String jsonString = JSON.toJSONString(coursePublish);
        redisTemplate.opsForValue().set("course:"+courseId,jsonString,600,TimeUnit.SECONDS);
    }

}
