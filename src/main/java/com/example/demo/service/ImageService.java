package com.example.demo.service;

import com.example.demo.mapper.ImageMapper;
import com.example.demo.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

	@Autowired
	private ImageMapper imageMapper;

	public Image saveImage(MultipartFile file, Long uploaderUid) throws IOException {
		// 生成唯一文件名
		String originalFilename = file.getOriginalFilename();
		String ext = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String filename = UUID.randomUUID().toString().replace("-", "") + ext;
		String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String filePath = uploadDir + File.separator + filename;
		File dest = new File(filePath);
		file.transferTo(dest);

		// 保存数据库
		Image image = new Image();
		image.setPath("/uploads/" + filename);
		image.setUploaderUid(uploaderUid);
		imageMapper.insertImage(image);
		return image;
	}

	public Image getImageById(Integer id) {
		return imageMapper.selectImageById(id);
	}
}