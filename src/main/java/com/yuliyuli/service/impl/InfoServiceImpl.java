package com.yuliyuli.service.impl;

import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.service.InfoService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.VideoWrapper;

import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {

    @Resource
    private VideoWrapper videoWrapper;

    @Resource
    private VideoMapper videoMapper;

    /**
     * 获取作者页面信息,传视频的信息，包括制作的视频
     * @param userId 作者ID
     * @return 作者页面所有视频
     */
    @Override
    public List<VideoVO> getAuthorPageVideo(Long userId) {
        return VideoConvertUtil.convertVideoListToVideoVOList(
            videoMapper.selectList(videoWrapper.getAuthorPageVideo(userId)));
    }
}
