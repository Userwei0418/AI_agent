package com.zwnsyw.ai_agent_backend.manager.uploadBySteam;

import cn.hutool.core.io.FileUtil;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class FilePictureUploadBySteam extends PictureUploadBySteamTemplate {

    @Value("${file.max-size:2097152}")
    private long MAX_FILE_SIZE;

    @Value("#{'${file.allowed-formats:jpeg,jpg,png,webp,glb}'.split(',')}")
    private Set<String> ALLOW_FORMATS;

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 校验文件大小
        long fileSize = file.getSize();
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件过大");

        // 校验扩展名
        String extension = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMATS.contains(extension), ErrorCode.PARAMS_ERROR, "不支持的文件类型");

    }


    @Override
    protected String getOriginFilename(Object inputSource) {
        return FileUtil.getName(((MultipartFile) inputSource).getOriginalFilename());
    }

    @Override
    protected InputStream getInputStream(Object inputSource) throws IOException {
        return ((MultipartFile) inputSource).getInputStream();
    }

    @Override
    protected long getContentLength(Object inputSource) {
        return ((MultipartFile) inputSource).getSize();
    }

    @Override
    protected String getContentType(Object inputSource) {
        return ((MultipartFile) inputSource).getContentType();
    }

}