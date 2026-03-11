package com.yuliyuli.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentWrapper {
  /**
   * 获取视频的评论列表
   *
   * @param videoId 视频url
   * @return LambdaQueryWrapper<Comment>获取视频的评论列表
   */
  public LambdaQueryWrapper<Comment> getCommentList(String videoUrl) {
    return new LambdaQueryWrapper<Comment>().eq(Comment::getVideoId, videoUrl);
  }

  //
  /**
   * 获取热门视频的评论列表（分页）
   *
   * @param videoUrl 视频url
   * @param lastId 上一页最后一条评论的id
   * @param pageSize 每页评论数量
   * @return LambdaQueryWrapper<Comment>获取视频的评论列表（分页）
   */
  public LambdaQueryWrapper<Comment> getCommentListByCursor(
      String videoUrl, Long lastId, int pageSize) {
    LambdaQueryWrapper<Comment> wrapper =
        new LambdaQueryWrapper<Comment>()
            .eq(Comment::getVideoId, videoUrl)
            .eq(Comment::getIsDelete, 0)
            .orderByDesc(Comment::getCreateTime);

    if (lastId != null && lastId > 0) {
      wrapper.lt(Comment::getId, lastId); // 查询ID小于lastId的
    }

    return wrapper.last("LIMIT " + pageSize);
  }
}
