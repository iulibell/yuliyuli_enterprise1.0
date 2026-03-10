package com.yuliyuli.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.entity.Video;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;

public class VideoConvertUtil {

    /**
     * 单个视频实体类转换为视频VO类
     * @param video 视频实体类
     * @return 视频VO类
     */
    public static VideoVO convertToVideoVO(Video video){

        if(video == null){
            return null;
        }
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        return vo;
    }

    /**
     * 视频List列表转换为视频VO类列表
     * @param videoList 视频实体类列表
     * @return 视频VO类列表
     */
    public static List<VideoVO> convertToVideoVOList(List<Video> videoList){
        if(videoList == null){
            return null;
        }
        return videoList.stream()
        .map(VideoConvertUtil::convertToVideoVO)
        .collect(Collectors.toList());
    }

    public static Page<VideoVO> converToVideoVOList(Page<Video> pageVideoList){
        if(pageVideoList == null){
            return null;
        }
        List<VideoVO> convertToVideoVOList = convertToVideoVOList(pageVideoList.getRecords());
        Page<VideoVO> videoVOPageList = new Page<>(pageVideoList.getCurrent(), 
        pageVideoList.getSize(), pageVideoList.getTotal());
        videoVOPageList.setRecords(convertToVideoVOList);
        return videoVOPageList;
    }

    public static VideoVO convertMapToVideoVO(Map<Object,Object> map){
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(map, vo);
        return vo;
    }

    /**
     * 视频文档列表转换为视频VO类列表
     * @param videoDocumentList 视频文档列表
     * @return 视频VO类列表
     */
    public static List<SearchVideoVO> convertSearchVideoVOList(List<VideoDocument> videoDocumentList){
        if(videoDocumentList == null){
            return null;
        }
        return videoDocumentList.stream()
        .map(videoDocument -> {
            SearchVideoVO topTenVO = new SearchVideoVO();
            topTenVO.setVideoDocuments(List.of(videoDocument));
            return topTenVO;
        })
        .collect(Collectors.toList());
    }
}
