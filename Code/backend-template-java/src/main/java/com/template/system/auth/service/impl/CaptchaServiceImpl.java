package com.template.system.auth.service.impl;

import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.system.auth.service.CaptchaService;
import com.template.system.auth.vo.CaptchaResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存图形验证码服务实现。
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int WIDTH = 112;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRES_IN_SECONDS = 300L;
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final Font FONT = new Font("Arial", Font.BOLD, 24);

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse generate() {
        clearExpired();
        String code = randomCode();
        String captchaId = UUID.randomUUID().toString();
        captchaStore.put(captchaId, new CaptchaEntry(code.toLowerCase(), Instant.now().plusSeconds(EXPIRES_IN_SECONDS)));
        return new CaptchaResponse(captchaId, toBase64Image(code), EXPIRES_IN_SECONDS);
    }

    @Override
    public void validateAndConsume(String captchaId, String captchaCode) {
        clearExpired();
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "请输入验证码");
        }
        CaptchaEntry entry = captchaStore.get(captchaId);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            captchaStore.remove(captchaId);
            throw new BusinessException(ApiCode.BAD_REQUEST, "验证码已过期，请刷新后重试");
        }
        if (!entry.code().equals(captchaCode.trim().toLowerCase())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "验证码错误");
        }
        captchaStore.remove(captchaId);
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private String toBase64Image(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 248, 252));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            drawNoise(graphics);
            drawCode(graphics, code);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception ex) {
            throw new BusinessException(ApiCode.ERROR, "验证码生成失败");
        } finally {
            graphics.dispose();
        }
    }

    private void drawNoise(Graphics2D graphics) {
        for (int i = 0; i < 8; i++) {
            graphics.setColor(randomColor(90, 190));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            graphics.drawLine(x1, y1, x2, y2);
        }
        for (int i = 0; i < 80; i++) {
            graphics.setColor(randomColor(120, 230));
            graphics.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
    }

    private void drawCode(Graphics2D graphics, String code) {
        graphics.setFont(FONT);
        for (int i = 0; i < code.length(); i++) {
            AffineTransform original = graphics.getTransform();
            double angle = Math.toRadians(random.nextInt(31) - 15);
            int x = 16 + i * 22 + random.nextInt(5);
            int y = 27 + random.nextInt(7) - 3;
            graphics.rotate(angle, x, y);
            graphics.setColor(randomColor(20, 120));
            graphics.drawString(String.valueOf(code.charAt(i)), x, y);
            graphics.setTransform(original);
        }
    }

    private Color randomColor(int min, int max) {
        int bound = Math.max(1, max - min);
        return new Color(min + random.nextInt(bound), min + random.nextInt(bound), min + random.nextInt(bound));
    }

    private void clearExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CaptchaEntry>> iterator = captchaStore.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt().isBefore(now)) {
                iterator.remove();
            }
        }
    }

    private record CaptchaEntry(String code, Instant expiresAt) {
    }
}
