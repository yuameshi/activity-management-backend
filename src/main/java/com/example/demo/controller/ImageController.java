package com.example.demo.controller;

import com.example.demo.model.Image;
import com.example.demo.service.ImageService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/image")
public class ImageController {

	@Autowired
	private ImageService imageService;

	private Claims parseAuth(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("missing or invalid Authorization header");
		}
		String token = authHeader.substring("Bearer ".length());
		return JwtUtil.parseToken(token);
	}

	// 上传图片
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
			@RequestHeader("Authorization") String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Long requesterId = ((Number) claims.get("id")).longValue();
			Image image = imageService.saveImage(file, requesterId);
			String relPath = image.getPath();
			String rawUrl = "/api/image/raw/" + image.getId();
			com.example.demo.util.OperationLogUtil.log(requesterId, String.format("上传图片，图片ID=%d", image.getId()),
					image.getId() != null ? image.getId().longValue() : null, "Image", request);
			return ResponseEntity.ok()
					.body(new UploadResult(image.getId(), relPath, rawUrl));
		} catch (Exception e) {
			System.out.println("上传异常: " + e.getMessage());
			return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
		}
	}

	// 查询图片路径
	@GetMapping("/path/{id}")
	public ResponseEntity<?> getPath(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
		com.example.demo.util.OperationLogUtil.log(null, String.format("获取图片路径，图片ID=%d", id),
				id != null ? id.longValue() : null, "Image", request);
		Image image = imageService.getImageById(id);
		if (image == null) {
			return ResponseEntity.notFound().build();
		}
		// 返回相对路径（无需 Host 头）
		String relPath = image.getPath();
		return ResponseEntity.ok().body(relPath);
	}

	// 返回图片二进制流
	@GetMapping("/raw/{id}")
	public ResponseEntity<Resource> getImageRaw(@PathVariable Integer id,
			jakarta.servlet.http.HttpServletRequest request) {
		com.example.demo.util.OperationLogUtil.log(null, String.format("获取图片二进制流，图片ID=%d", id),
				id != null ? id.longValue() : null, "Image", request);
		Image image = imageService.getImageById(id);
		if (image == null) {
			return ResponseEntity.notFound().build();
		}
		String path = image.getPath().replaceFirst("^/+", "");
		File file = new File(path);
		if (!file.exists()) {
			return ResponseEntity.notFound().build();
		}
		FileSystemResource resource = new FileSystemResource(file);
		String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		// 根据文件后缀设置 Content-Type
		if (path.endsWith(".png"))
			contentType = MediaType.IMAGE_PNG_VALUE;
		else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
			contentType = MediaType.IMAGE_JPEG_VALUE;
		else if (path.endsWith(".gif"))
			contentType = MediaType.IMAGE_GIF_VALUE;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
				.contentType(MediaType.parseMediaType(contentType))
				.body(resource);
	}

	// 管理员获取图片元信息
	@GetMapping("/meta/{id}")
	public ResponseEntity<?> getMeta(@PathVariable Integer id, @RequestHeader("Authorization") String authHeader,
			jakarta.servlet.http.HttpServletRequest request) {
		// 简单示例：假设管理员uid为1，实际应结合权限系统
		Long uid = null;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String uidStr = authHeader.substring(7);
			try {
				uid = Long.parseLong(uidStr);
			} catch (NumberFormatException e) {
				return ResponseEntity.status(401).body("无效的管理员身份");
			}
		}
		if (uid == null || uid != 1L) {
			return ResponseEntity.status(403).body("仅管理员可用");
		}
		com.example.demo.util.OperationLogUtil.log(uid, String.format("获取图片元信息，图片ID=%d", id),
				id != null ? id.longValue() : null, "Image", request);
		Image image = imageService.getImageById(id);
		if (image == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(image);
	}

	// 上传返回结构体
	static class UploadResult {
		private Integer id;
		private String path;
		private String rawUrl;

		public UploadResult(Integer id, String path, String rawUrl) {
			this.id = id;
			this.path = path;
			this.rawUrl = rawUrl;
		}

		public Integer getId() {
			return id;
		}

		public String getPath() {
			return path;
		}

		public String getRawUrl() {
			return rawUrl;
		}
	}
}