package com.record.modules.auth.controller;

import com.record.integration.wechat.WechatUrlLinkClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 二维码扫码登录落地页。
 * 微信扫一扫打开时，生成 URL Link 跳转唤起小程序；普通浏览器打开时展示提示页。
 */
@Controller
public class QrLoginPageController {

    private static final Logger log = LoggerFactory.getLogger(QrLoginPageController.class);

    private final WechatUrlLinkClient wechatUrlLinkClient;

    public QrLoginPageController(WechatUrlLinkClient wechatUrlLinkClient) {
        this.wechatUrlLinkClient = wechatUrlLinkClient;
    }

    @GetMapping("/qr-login")
    public void qrLogin(@RequestParam String token,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String userAgent = request.getHeader("User-Agent");
        boolean isWechat = userAgent != null && userAgent.toLowerCase().contains("micromessenger");

        if (isWechat) {
            // 微信浏览器 → 生成 URL Link 唤起小程序
            try {
                String urlLink = wechatUrlLinkClient.generateUrlLink("pages/home/index", "qrToken=" + token);
                log.info("[QR-Login] 微信扫码，重定向到 URL Link token={}", token);
                response.sendRedirect(urlLink);
            } catch (Exception e) {
                log.error("[QR-Login] 生成 URL Link 失败 token={}: {}", token, e.getMessage());
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write("""
                        <!DOCTYPE html>
                        <html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                        <title>登录失败</title></head>
                        <body style="font-family:sans-serif;text-align:center;padding:40px 20px;">
                        <h2>链接生成失败</h2><p>请稍后重试，或在 Life Record 小程序中使用扫码登录。</p>
                        </body></html>""");
                writer.flush();
            }
        } else {
            // 普通浏览器 → 展示提示页
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("""
                    <!DOCTYPE html>
                    <html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                    <title>扫码登录 - Life Record</title>
                    <style>
                      body{font-family:-apple-system,BlinkMacSystemFont,sans-serif;text-align:center;padding:60px 20px;background:#f5f0eb;color:#333}
                      .card{max-width:400px;margin:0 auto;background:#fff;border-radius:16px;padding:40px 24px;box-shadow:0 2px 12px rgba(0,0,0,.08)}
                      h2{font-size:20px;margin:0 0 12px} p{font-size:14px;color:#666;line-height:1.6;margin:0}
                      .icon{font-size:48px;margin-bottom:16px}
                    </style></head>
                    <body>
                    <div class="card">
                      <div class="icon">📱</div>
                      <h2>请使用微信扫一扫</h2>
                      <p>请使用微信扫描此二维码登录管理后台。<br>或打开 Life Record 小程序，在「个人中心」选择「扫码登录管理后台」。</p>
                    </div></body></html>""");
            writer.flush();
        }
    }
}
