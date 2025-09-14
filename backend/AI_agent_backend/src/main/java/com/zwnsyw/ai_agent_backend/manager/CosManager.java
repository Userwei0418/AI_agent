package com.zwnsyw.ai_agent_backend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Transfer;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferProgress;
import com.qcloud.cos.transfer.Upload;
import com.zwnsyw.ai_agent_backend.config.CosClientConfig;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * cos文档：https://cloud.tencent.com/document/product/436/65935
 * todo 上线须配套cdn 提速且防止被刷存储桶
 * 评论区大佬：
 *  第一，cos必须配合cdn使用
 *  第二，cdn欠费期已经是2小时了，两小时欠费就关停
 *  第三，设置好安全策略，单IP限制qos，必要时直接拉黑（不清楚有没有这个功能）：
 *  对于静态资源做好鉴权，必要时做好文件有效期等
 *  储存桶被刷是基本操作，找腾讯也不会给你免的，直接销号跑路就行，金额不大不至于起诉你。
 *  我站点图片都是放在新浪上，部分还有哗哩哗哩上，不过哗哩哗哩的有防盗链比较麻烦。新手建议直接套上Cloudflare，
 *  储存桶用backblaze的b2，和CF配套使用可以免除流量费用。
 * <p>
 * cdn的原理是，在某个用户发生请求时，向一个源获取静态源文件，备份到当地服务器。
 * 如果有更多那个地区的用户访问，它不会再向源站请求静态文件，而是直接由当地服务器发给用户，
 * 除非流量爆了，会按照你配置的规则自动操作
 * （比如你配置的是没流量了就关站，那么它就会自动停，如果你配置的是没流量时回源，它会自动转发请求到源站）。
 * 这也是为什么套了cdn的网站ip能有几十个上百个之多。
 * 相当于你cos所消耗的下行流量是给cdn热备用的，不会直接给用户，
 * 承担的费用是便宜的cdn流量+少部分cos下行流量费用。
 * 因为cdn自带的保护机制，所以流量也没有那么容易刷爆，顶多欠费几十块钱罢了
 * （毕竞你想想，套了cdn后流量用完我就摆烂，等我上线补了流量才继续服务，谁能害我）
 */
@Component
@Slf4j
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    private TransferManager transferManager;

    /**
     * 初始化COS客户端和传输管理器
     * 1. 校验COS配置参数
     * 2. 创建COS客户端实例
     * 3. 初始化线程池管理上传任务
     */
    @PostConstruct
    public void init() {
        if (cosClientConfig.getBucket() == null || cosClientConfig.getBucket().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "COS配置错误: bucket未配置");
        }
        if (cosClient == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "COS客户端未初始化");
        }
        this.cosClient = createCOSClient();
        this.transferManager = createTransferManager();
    }

    /**
     * 创建TransferManager实例
     *
     * @return 带32线程池的TransferManager
     */
    private TransferManager createTransferManager() {
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        return new TransferManager(cosClient, threadPool);
    }

    /**
     * 根据配置创建COS客户端
     *
     * @return 配置好的COSClient实例
     */
    private COSClient createCOSClient() {
        String secretId = cosClientConfig.getSecretId();
        String secretKey = cosClientConfig.getSecretKey();
        if (secretId == null || secretKey == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "COS凭证未配置");
        }
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(cosClientConfig.getRegion()));
        return new COSClient(cred, config);
    }

    /**
     * 关闭TransferManager并释放资源
     *
     * @param transferManager 要关闭的传输管理器
     */
    private void shutdownTransferManager(TransferManager transferManager) {
        transferManager.shutdownNow(true);
    }

    /**
     * 上传通用对象到COS
     *
     * @param key  对象存储路径
     * @param file 本地文件
     * @return 上传结果对象
     */
    public PutObjectResult putObject(String key, File file) {
        try {
            PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
            return cosClient.putObject(request);
        } catch (CosClientException e) {
            log.error("对象上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 从COS下载对象
     *
     * @param key 对象存储路径
     * @return COSObject对象
     */
    public COSObject getObject(String key) {
        try {
            return cosClient.getObject(cosClientConfig.getBucket(), key);
        } catch (CosClientException e) {
            log.error("对象下载失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 上传图片并执行处理规则
     *
     * @param key  存储路径
     * @param file 本地文件
     * @return 上传结果
     */
    public PutObjectResult putPictureObject(String key, File file) {
        try {
            PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
            request.setPicOperations(createPicOperations(key));
            return cosClient.putObject(request);
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通过流上传图片并执行处理规则
     *
     * @param objectKey     存储路径
     * @param inputStream   输入流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     * @return 上传结果
     */
    public UploadResult putPictureObjectStream(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        TransferManager transferManager = createTransferManager();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);
            PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), objectKey, inputStream, metadata);
            request.setPicOperations(createPicOperations(objectKey));

            Upload upload = transferManager.upload(request);
            showTransferProgress(upload);
            return upload.waitForUploadResult();
        } catch (CosClientException | InterruptedException e) {
            log.error("上传失败: {}, Content-Length: {}", e.getMessage(), contentLength, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通过流上传模型并执行处理规则
     *
     * @param objectKey     存储路径
     * @param inputStream   输入流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     * @return 上传结果
     */
    public UploadResult putModelObjectStream(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        TransferManager transferManager = createTransferManager();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);
            PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), objectKey, inputStream, metadata);

            Upload upload = transferManager.upload(request);
            showTransferProgress(upload);
            return upload.waitForUploadResult();
        } catch (CosClientException | InterruptedException e) {
            log.error("模型上传失败: {}, Content-Length: {}", e.getMessage(), contentLength, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通过流上传mp3文件并执行处理规则
     *
     * @param objectKey     存储路径
     * @param inputStream   输入流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     * @return 上传结果
     */
    public UploadResult putMp3ObjectStream(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        TransferManager transferManager = createTransferManager();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);

            PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), objectKey, inputStream, metadata);

            Upload upload = transferManager.upload(request);
            showTransferProgress(upload);
            return upload.waitForUploadResult();
        } catch (CosClientException | InterruptedException e) {
            log.error("MP3上传失败: {}, Content-Length: {}", e.getMessage(), contentLength, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 监听并记录上传进度
     *
     * @param transfer 上传任务对象
     */
    private void showTransferProgress(Transfer transfer) {
        transfer.addProgressListener(progress -> {
            TransferProgress p = transfer.getProgress();
            log.info("上传进度: {}% [{} / {}]", p.getPercentTransferred(), p.getBytesTransferred(), p.getTotalBytesToTransfer());
        });
    }

    /**
     * 获取图片对象内容流
     *
     * @param objectKey 对象存储路径
     * @return 输入流
     */
    public InputStream getPictureObject(String objectKey) {
        try {
            return cosClient.getObject(cosClientConfig.getBucket(), objectKey).getObjectContent();
        } catch (CosClientException e) {
            log.error("获取图片失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }


    /**
     * 删除COS对象
     *
     * @param key 要删除的对象路径
     */
    public void deleteObject(String key) {
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
        } catch (CosServiceException e) {
            log.error("删除失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 生成预签名访问URL
     *
     * @param objectKey         对象路径
     * @param expirationSeconds 过期时间（秒）
     * @return 带签名的URL字符串
     */
    public String generatePresignedUrl(String objectKey, long expirationSeconds) {
        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(cosClientConfig.getBucket(), objectKey);
            request.setMethod(HttpMethodName.GET);
            request.setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000));
            return cosClient.generatePresignedUrl(request).toString();
        } catch (CosServiceException e) {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 创建图片处理规则
     * 包含：
     * 1. WebP格式转换
     * 2. 缩略图生成（尺寸由配置决定）
     *
     * @param objectKey 原始文件名
     * @return PicOperations对象
     */
    // to do 添加图片压缩规则 上传缩略图 再提速80% 数据万象有动态缩略图功能 但可以手动压缩上传 静态访问 用开发时间和存储空间换钱
    // 数据库只存真实图片后缀 .webp _thumb_200x200.webp 在前端做替换
    private PicOperations createPicOperations(String objectKey) {
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);// 1 表示返回原图信息

        List<PicOperations.Rule> rules = new ArrayList<>();
        //---------- 压缩webp副本上传 -----------
        String webpKey = FileUtil.mainName(objectKey) + ".webp";
        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setRule("imageMogr2/format/webp");
        webpRule.setBucket(cosClientConfig.getBucket());
        webpRule.setFileId(webpKey);
        rules.add(webpRule);
        //--------------- end -----------------

        //---------- 生成缩略图 -----------
        String thumbnailKey = FileUtil.mainName(objectKey) + "_thumb_"
                + cosClientConfig.getThumbnailWidth() + "x"
                + cosClientConfig.getThumbnailHeight() + ".webp";
        PicOperations.Rule thumbRule = new PicOperations.Rule();
        thumbRule.setRule(String.format(
                "imageMogr2/thumbnail/!%dx%dr/gravity/Center/crop/%dx%d/quality/%d|format/webp",
                cosClientConfig.getThumbnailWidth(),
                cosClientConfig.getThumbnailHeight(),
                cosClientConfig.getThumbnailWidth(),
                cosClientConfig.getThumbnailHeight(),
                cosClientConfig.getWebpQuality()
        ));
        thumbRule.setBucket(cosClientConfig.getBucket());
        thumbRule.setFileId(thumbnailKey);
        rules.add(thumbRule);
        //--------------- end -----------------

        picOperations.setRules(rules);
        return picOperations;
    }


}
