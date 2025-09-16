package com.zwnsyw.ai_agent_backend.manager.uploadBySteam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.zwnsyw.ai_agent_backend.vo.File.UploadPictureResult;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Set;

@Service
public class UrlPictureUploadBySteam extends PictureUploadBySteamTemplate {

    @Value("${file.max-size:2097152}")
    private long MAX_FILE_SIZE;

    @Value("#{'${file.allowed-formats:jpeg,jpg,png,webp}'.split(',')}")
    private Set<String> ALLOW_FORMATS;

    // 使用ThreadLocal安全存储元数据
    private final transient ThreadLocal<Long> contentLength = new ThreadLocal<>();
    private final transient ThreadLocal<String> contentType = new ThreadLocal<>();
    private final transient ThreadLocal<String> fileExt = new ThreadLocal<>();
    private final transient ThreadLocal<HttpResponse> cachedResponse = new ThreadLocal<>();
    private final transient ThreadLocal<String> fileExtension = new ThreadLocal<>();

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        // 验证 URL 格式
        URL url;
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 校验 URL 协议
        String protocol = url.getProtocol().toLowerCase();
        ThrowUtils.throwIf(!(protocol.equals("http") || protocol.equals("https")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        HttpResponse response = null;
        try {
            // 添加SSL配置处理HTTPS
            response = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .setSSLSocketFactory(createSslSocketFactory())
                    .setReadTimeout(5000)
                    .execute();

            int statusCode = response.getStatus();
            ThrowUtils.throwIf(statusCode != HttpStatus.HTTP_OK,
                    ErrorCode.NOT_FOUND_ERROR, "文件不存在或无法访问（状态码：" + statusCode + "）");

            // 提取元数据
            String contentTypeVal = response.header("Content-Type");
            ThrowUtils.throwIf(StrUtil.isBlank(contentTypeVal),
                    ErrorCode.PARAMS_ERROR, "文件类型信息缺失");
            this.contentType.set(contentTypeVal);

            String contentLengthStr = response.header("Content-Length");
            ThrowUtils.throwIf(StrUtil.isBlank(contentLengthStr),
                    ErrorCode.PARAMS_ERROR, "文件大小信息缺失");
            long contentLengthVal = Long.parseLong(contentLengthStr);
            this.contentLength.set(contentLengthVal);

            // 验证文件扩展名
            String ext = getExtensionFromContentType(contentTypeVal);
            if (ext == null || ext.isEmpty()) {
                ext = getExtensionFromUrl(fileUrl);
            }
            ThrowUtils.throwIf(ext == null || !ALLOW_FORMATS.contains(ext.substring(1)),
                    ErrorCode.PARAMS_ERROR, "文件类型错误");

            // 验证文件大小
            ThrowUtils.throwIf(contentLengthVal <= 0 || contentLengthVal > MAX_FILE_SIZE,
                    ErrorCode.PARAMS_ERROR, "文件大小必须大于0且不超过2M");
            this.fileExt.set(ext);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "元数据解析失败: " + e.getMessage());
        } finally {
            // 关闭资源并清理
            try {
                if (response != null) response.close();
            } finally {
                response = null;
            }
        }
    }


    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        String filename = FileUtil.getName(fileUrl);
        String baseName = FileUtil.mainName(filename);

        String ext = fileExt.get();
        return baseName + (ext != null ? ext : ".unknown");
    }

    @Override
    protected InputStream getInputStream(Object inputSource) throws IOException {
        String fileUrl = (String) inputSource;
        return HttpUtil.createGet(fileUrl)
                .setSSLSocketFactory(createSslSocketFactory()) // 处理HTTPS
                .setReadTimeout(5000)
                .execute()
                .bodyStream();
    }

    @Override
    protected long getContentLength(Object inputSource) {
        Long length = contentLength.get();
        ThrowUtils.throwIf(length == null, ErrorCode.SYSTEM_ERROR, "文件大小信息缺失");
        return length;
    }

    @Override
    protected String getContentType(Object inputSource) {
        String type = contentType.get();
        ThrowUtils.throwIf(StrUtil.isBlank(type), ErrorCode.SYSTEM_ERROR, "文件类型信息缺失");
        return type;
    }

    // 扩展名解析增强
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return null;
        String type = contentType.split(";")[0].trim().toLowerCase();
        switch (type) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/webp":
                return ".webp";
            default:
                return null;
        }
    }

    private String getExtensionFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            String query = url.getQuery();

            // 优先从路径解析扩展名
            String ext = FileUtil.extName(path);
            if (ext != null && !ext.isEmpty()) return "." + ext;

            // 从查询参数中解析扩展名（如?f=webp）
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("f=")) {
                        String[] parts = param.split("=");
                        if (parts.length >= 2) {
                            return "." + parts[1].toLowerCase();
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            return null;
        }
        return null;
    }

    // SSL配置
    private SSLSocketFactory createSslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("SSL配置失败", e);
        }
    }

    @Override
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        try {
            return super.uploadPicture(inputSource, uploadPathPrefix);
        } finally {
            // 清理线程局部变量
            contentLength.remove();
            contentType.remove();
            fileExt.remove();
        }
    }
}

