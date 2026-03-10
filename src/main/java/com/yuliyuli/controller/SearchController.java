package com.yuliyuli.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yuliyuli.common.Result;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.vo.SearchVideoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索模块")
@Slf4j
public class SearchController {

    @Resource
    private SearchService searchService;

    /**
     * 获取前十热门视频
     * @return top10热门视频标题
     */
    @Operation(summary = "前十的热门视频获取")
    @GetMapping("/topTenVideo")
    public Result<List<SearchVideoVO>> getTopTenVideo() {
        try{
            List<SearchVideoVO> topTenVideoDocuments = searchService.getTopTenVideo();
            return Result.success(topTenVideoDocuments);
        }catch(Exception e){
            log.error("获取热门视频失败", e);
            throw new GlobalExceptionHandler.BusinessException("获取热门视频失败");
        }
    }

    /**
     * 搜索视频
     * @param keyword 视频标题关键词
     * @return 视频列表
     */
    @Operation(summary = "搜索视频")
    @GetMapping("/video")
    public Result<List<SearchVideoVO>> searchVideo(@RequestParam("keyword") String keyword) {
        try{
            List<SearchVideoVO> videoDocuments = searchService.findByTitleSuggest(keyword);
            return Result.success(videoDocuments);
        }catch(Exception e){
            log.error("搜索视频失败", e);
            throw new GlobalExceptionHandler.BusinessException("搜索视频失败");
        }
    }
}
