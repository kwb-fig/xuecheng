package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description MediaFileProcess接口实现
 * @date 2023/2/19 10:44
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;


    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    //实现如下
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }


    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {

        LambdaQueryWrapper<MediaProcess> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MediaProcess::getFileId,fileId);
        MediaProcess mediaProcess = mediaProcessMapper.selectOne(lambdaQueryWrapper);
//        MediaProcess mediaProcess = mediaProcessMapper.selectById(fileId);
        if (mediaProcess == null) {
            log.warn("更新任务状态时此任务不存在 {}", fileId);
            return;
        }
        // 处理失败
        if (status.equals("3")) {
            //mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);//失败次数加1
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }

        // status = 2，处理成功，更新 url 和状态
        mediaProcess.setStatus("2");
        mediaProcess.setUrl(url);
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        // 更新文件表中的 url 字段
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        // 添加到历史记录
        MediaProcessHistory history = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, history);
        mediaProcessHistoryMapper.insert(history);

        // 删除 mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }
}
