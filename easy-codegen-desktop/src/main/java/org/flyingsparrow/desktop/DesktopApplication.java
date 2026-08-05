package org.flyingsparrow.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.flyingsparrow.web.CodegenWebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Easy-Codegen 桌面版：内嵌启动 Web 服务 + JavaFX WebView 加载可视化面板
 *
 * @author Siborne
 */
public class DesktopApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(DesktopApplication.class);

    private static ConfigurableApplicationContext webContext;

    public static void main(String[] args) {
        int port = startEmbeddedWeb();
        System.setProperty("easy-codegen.port", String.valueOf(port));
        launch(args);
    }

    /**
     * 内嵌启动 Web 服务：优先 9000 端口，被占用则自动选择空闲端口
     */
    private static int startEmbeddedWeb() {
        int port = findPort();
        webContext = new SpringApplicationBuilder(CodegenWebApplication.class)
                .web(WebApplicationType.SERVLET)
                .headless(false)
                .run("--server.port=" + port, "--spring.main.banner-mode=off");
        logger.info("Easy-Codegen Web 服务已启动: http://localhost:{}", port);
        return port;
    }

    private static int findPort() {
        // 优先使用 9000（与浏览器版一致），被占用则自动选空闲端口
        try (ServerSocket ignored = new ServerSocket(9000)) {
            return 9000;
        } catch (IOException e) {
            // 9000 被占用，找空闲端口
        }
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return 9000;
        }
    }

    @Override
    public void start(Stage stage) {
        int port = Integer.parseInt(System.getProperty("easy-codegen.port"));
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
            webContext.close();
        }
        Platform.exit();
    }
}
