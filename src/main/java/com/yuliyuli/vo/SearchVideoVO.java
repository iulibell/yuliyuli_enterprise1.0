package com.yuliyuli.vo;

import com.yuliyuli.document.VideoDocument;
import lombok.Data;

import java.util.List;

@Data
public class SearchVideoVO {
    private List<VideoDocument> videoDocuments;
}
