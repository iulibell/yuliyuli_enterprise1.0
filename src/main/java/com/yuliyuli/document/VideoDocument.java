package com.yuliyuli.document;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

// 存入ES的视频文档
@Document(indexName = "video")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDocument {

  @Id private String id;

  @Field(type = FieldType.Text, analyzer = "ik_max_word")
  private String title;

  // 作者Id，用于点击作者名字后进行作者主页跳转
  @Field(type = FieldType.Integer)
  private Long userId;

  @Field(type = FieldType.Text)
  private String authorName;

  // 视频url
  @Field(type = FieldType.Text)
  private String url;

  // 视频封面url
  @Field(type = FieldType.Text)
  private String coverUrl;

  // 点击搜索后的视频会根据type_id显示出其他同一typeId的视频
  @Field(type = FieldType.Integer)
  private Integer typeId;

  @Field(type = FieldType.Integer)
  private Integer playCount;

  @Field(type = FieldType.Date)
  private Date createTime;
}
