package com.yuliyuli.service;

import java.util.List;
import com.yuliyuli.vo.VideoVO;

public interface InfoService {
    /**
     * 获取作者页面信息,传视频的信息，包括制作的视频
     * @param userId 作者ID
     * @return 作者页面所有视频
     */
    public List<VideoVO> getAuthorPageVideo(Long userId);

}
