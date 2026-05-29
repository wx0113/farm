package cn.jxufe.seedsystem.utils;

import cn.jxufe.seedsystem.entity.Message;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileSaver {

    public static Message save(String subPath, MultipartFile uploadFile) {
        Message message = new Message();

        if (uploadFile == null || uploadFile.isEmpty()) {
            message.setCode(400);
            message.setMsg("上传文件不能为空");
            return message;
        }

        String originalFilename = uploadFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        String allowedExtensions = ".jpg,.jpeg,.png,.gif,.bmp";
        if (!allowedExtensions.contains(fileExtension)) {
            message.setCode(400);
            message.setMsg("只支持jpg、jpeg、png、gif、bmp格式的图片");
            return message;
        }

        if (uploadFile.getSize() > 2 * 1024 * 1024) {
            message.setCode(400);
            message.setMsg("文件大小不能超过2MB");
            return message;
        }

        try {
            // 优先写到 classpath 实际运行目录，确保上传后立即可访问
            Path staticDir = Paths.get("src/main/resources/static");
            try {
                URL staticResource = FileSaver.class.getClassLoader().getResource("static/");
                if (staticResource != null && "file".equals(staticResource.getProtocol())) {
                    staticDir = Paths.get(staticResource.toURI());
                }
            } catch (Exception ignored) {}
            staticDir = staticDir.toAbsolutePath().normalize();
            Path saveDir = staticDir.resolve(subPath.replace("/", File.separator));
            Files.createDirectories(saveDir);

            String newFileName = originalFilename;
            Path targetFile = saveDir.resolve(newFileName);

            // 先写到临时文件，再 move 替换，避免 Windows 文件锁定问题
            Path tmpFile = Files.createTempFile(saveDir, "upload_", ".tmp");
            try {
                Files.copy(uploadFile.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmpFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // move 失败则清理临时文件后重新抛出
                try { Files.deleteIfExists(tmpFile); } catch (IOException ignored) {}
                throw e;
            }

            String accessUrl = "/" + subPath + newFileName;
            message.setCode(200);
            message.setMsg("上传成功");
            message.setData(accessUrl);

        } catch (IOException e) {
            e.printStackTrace();
            message.setCode(500);
            message.setMsg("文件保存失败：" + e.getMessage());
        }

        return message;
    }
}
