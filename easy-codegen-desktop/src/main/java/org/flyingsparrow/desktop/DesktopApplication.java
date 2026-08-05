package org.flyingsparrow.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.flyingsparrow.web.CodegenWebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Easy-Codegen 桌面版：内嵌启动 Web 服务 + JavaFX WebView 加载可视化面板
 *
 * @author Siborne
 */
public class DesktopApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(DesktopApplication.class);

    /** 面板实际端口（固定 9000 或自动分配） */
    private static int port;

    private static ConfigurableApplicationContext webContext;

    public static void main(String[] args) {
        try {
            port = startEmbeddedWeb();
            launch(args);
        } catch (Exception e) {
            logger.error("Easy-Codegen 启动失败", e);
            try {
                JOptionPane.showMessageDialog(null,
                        "Easy-Codegen 启动失败:\n" + e.getMessage(),
                        "Easy-Codegen", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ignore) {
                // 无图形环境时仅打印日志
            }
            System.exit(1);
        }
    }

    /**
     * 内嵌启动 Web 服务：优先固定 9000 端口；被占用时交由 Spring 自动分配端口
     */
    private static int startEmbeddedWeb() {
        boolean fixedPort = isPortAvailable(9000);
        String[] args = {
                "--server.port=" + (fixedPort ? 9000 : 0),
                "--spring.main.banner-mode=off"
        };
        webContext = new SpringApplicationBuilder(CodegenWebApplication.class)
                .web(WebApplicationType.SERVLET)
                .headless(false)
                .run(args);
        int actualPort = fixedPort ? 9000 : Integer.parseInt(webContext.getEnvironment().getProperty("local.server.port"));
        logger.info("Easy-Codegen Web 服务已启动: http://localhost:{}", actualPort);
        return actualPort;
    }

    /**
     * 探测端口是否可用（仅探测，不持有）
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void start(Stage stage) {
        String url = "http://localhost:" + port + "/";

        WebView webView = new WebView();
        // 弹窗（window.open）在桌面壳内不支持，直接阻止
        webView.getEngine().setCreatePopupHandler(popupFeatures -> {
            logger.info("检测到弹窗请求，桌面壳内已阻止（如需完整功能请使用浏览器版: http://localhost:{}/）", port);
            return null;
        });

        webView.getEngine().load(url);

        stage.setTitle("Easy-Codegen 可视化面板");
        stage.setWidth(1280);
        stage.setHeight(860);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(new Scene(webView));
        stage.show();

        logger.info("桌面面板已打开: {}", url);
    }

    @Override
    public void stop() {
        logger.info("关闭 Easy-Codegen 桌面版");
        if (webContext != null) {
            // 后台线程关闭，避免阻塞 JavaFX Application Thread（非 daemon，JVM 会等待其完成）
            new Thread(() -> {
                try {
                    webContext.close();
                } catch (Exception e) {
                    logger.warn("关闭内嵌服务时发生异常", e);
                }
            }, "easy-codegen-web-shutdown").start();
        }
    }
}
