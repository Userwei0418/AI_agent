package com.zwnsyw.ai_agent_backend.manager.uploadBySteam;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.zwnsyw.ai_agent_backend.config.CosClientConfig;
import com.zwnsyw.ai_agent_backend.vo.File.UploadPictureResult;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.manager.CosManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Slf4j
public abstract class PictureUploadBySteamTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    protected CosManager cosManager;

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        validPicture(inputSource);

        // 生成上传路径
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String originFilename = getOriginFilename(inputSource);
        String suffix = FileUtil.getSuffix(originFilename);
        if (suffix == null || suffix.isEmpty()) {
            suffix = "unknown";
        }
        String uploadFilename = String.format(
                "%s_%s.%s",
                DateUtil.format(new Date(), DEFAULT_DATE_FORMAT),
                uuid,
                suffix
        );
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        try (InputStream inputStream = getInputStream(inputSource)) {
            // 获取元数据
            long contentLength = getContentLength(inputSource);
            String contentType = getContentType(inputSource);

            // 执行流式上传
            UploadResult uploadResult = cosManager.putPictureObjectStream(
                    uploadPath,
                    inputStream,
                    contentLength,
                    contentType
            );
            ImageInfo imageInfo = uploadResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            return buildResult(
                    originFilename,
                    uploadPath,
                    imageInfo,
                    contentLength
            );
        } catch (IOException | BusinessException e) {
            log.error("文件传输失败, uploadPath = {}, filename = {}",
                    uploadPath, originFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件传输失败，检查网络问题");
        } catch (Exception e) {
            log.error("图片上传到对象存储失败, uploadPath = {}, filename = {}",
                    uploadPath, originFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败，请检查网络问题");
        }
    }

    // 新增抽象方法
    protected abstract InputStream getInputStream(Object inputSource) throws Exception;

    protected abstract long getContentLength(Object inputSource);

    protected abstract String getContentType(Object inputSource);

    protected abstract String getOriginFilename(Object inputSource);

    protected abstract void validPicture(Object inputSource);

    // 构建结果方法
    private UploadPictureResult buildResult(
            String originFilename,
            String uploadPath,
            ImageInfo imageInfo,
            long contentLength
    ) {
        UploadPictureResult result = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        result.setPicName(FileUtil.mainName(originFilename));
        result.setPicWidth(picWidth);
        result.setPicHeight(picHeight);
        result.setPicScale(picScale);
        result.setPicFormat(imageInfo.getFormat());
        result.setPicSize(contentLength);
        result.setUrl(uploadPath);
        return result;
    }
}
