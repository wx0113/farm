package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.utils.FileSaver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("file")
public class FileController {

    @RequestMapping(value = "saveHeadImg", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message saveHeadImg(@RequestParam("filePathName") MultipartFile uploadFile) {
        return FileSaver.save("images/avatars/", uploadFile);
    }
}
