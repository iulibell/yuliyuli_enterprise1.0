package com.yuliyuli.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.entity.Video;
import com.yuliyuli.vo.HotRecommendVideoVO;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;

public class VideoConvertUtil {

    /**
     * 单个视频实体类转换为视频VO类
     * @param video 视频实体类
     * @return 视频VO类
     */
    public static VideoVO convertVideoToVideoVO(Video video){

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
    public static List<VideoVO> convertVideoListToVideoVOList(List<Video> videoList){
        if(videoList == null){
            return null;
        }
        return videoList.stream()
        .map(VideoConvertUtil::convertVideoToVideoVO)
        .collect(Collectors.toList());
    }

    /**
     * 视频分页列表转换为视频VO类分页列表
     * @param pageVideoList 视频实体类分页列表
     * @return 视频VO类分页列表
     */
    public static Page<VideoVO> converPageToVideoVOList(Page<Video> pageVideoList){
        if(pageVideoList == null){
            return null;
        }
        List<VideoVO> convertToVideoVOList = convertVideoListToVideoVOList(pageVideoList.getRecords());
        Page<VideoVO> videoVOPageList = new Page<>(pageVideoList.getCurrent(), 
        pageVideoList.getSize(), pageVideoList.getTotal());
        videoVOPageList.setRecords(convertToVideoVOList);
        return videoVOPageList;
    }

    /**
     * 视频文档转换为视频VO类
     * @param map 视频文档Map
     * @return 视频VO类
     */
    public static VideoVO convertMapToVideoVO(Map<Object,Object> map){
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(map, vo);
        return vo;
    }

    /**
     * 视频搜索视频转换为视频VO类列表
     * @param videoDocumentList 搜索视频列表
     * @return 视频VO类列表
     */
    public static List<SearchVideoVO> convertVideoDocumentListToSearchVideoVOList(List<VideoDocument> videoDocumentList){
        if(videoDocumentList == null){
            return null;
        }
        return videoDocumentList.stream()
        .map(videoDocument -> {
            SearchVideoVO searchVideoVO = new SearchVideoVO();
            searchVideoVO.setVideoDocuments(List.of(videoDocument));
            return searchVideoVO;
        })
        .collect(Collectors.toList());
    }

    /**
     * 视频VO类列表转换为热门推荐视频VO类列表
     * @param videoVOList 视频VO类列表
     * @return 热门推荐视频VO类列表
     */
    public static List<HotRecommendVideoVO> convertVideoDocumentToHotRecommendVideoVO(List<VideoDocument> videoDocumentList){
        if(videoDocumentList == null){
            return null;
        }
        return videoDocumentList.stream()
        .map(videoDocument -> {
            HotRecommendVideoVO hotRecommendVideoVO = new HotRecommendVideoVO();
            hotRecommendVideoVO.setVideoDocuments(List.of(videoDocument));
            return hotRecommendVideoVO;
        })
        .collect(Collectors.toList());
    }
}
