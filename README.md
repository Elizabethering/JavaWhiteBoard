# 🎨 Java 实时协作白板 (Java Real-Time Collaborative Whiteboard)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.java.com)
[![Build Tool](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

这是一个基于 Java Socket 和 Swing 实现的轻量级、多用户实时协作白板项目。它旨在通过一个实践项目，深入学习并应用 Java 网络编程、多线程处理以及图形界面开发的核心知识。

This is a lightweight, multi-user, real-time collaborative whiteboard project built with Java Socket and Swing. It aims to provide a hands-on opportunity to learn and apply core concepts of Java network programming, multithreading, and GUI development.

## 🌟 项目演示 (Live Demo)

*(强烈建议您在此处放置一个 GIF 动图来展示项目运行效果，例如，将录制的GIF文件放在项目下的`docs/`目录中)*

![项目动态演示](docs/demo.gif)

## ✨ 核心功能 (Core Features)

-   **实时同步**: 教师端（或任一客户端）的绘图操作能够毫秒级同步到所有其他客户端。
-   **多客户端支持**: 服务器采用多线程模型，可以稳定地处理多个客户端同时连接。
-   **画笔自定义**: 支持选择不同的画笔颜色和粗细。
-   **画布管理**: 提供一键清空画布的功能，并同步给所有用户。
-   **状态同步**: 新加入的客户端可以立即看到当前画布上的所有历史内容。

## 🛠️ 技术栈 (Tech Stack)

-   **核心语言 (Language):** ☕ Java 1.8+
-   **图形界面 (GUI):** 🖼️ Java Swing
-   **网络通信 (Networking):** 🌐 TCP Sockets & Java Object Serialization
-   **并发处理 (Concurrency):** 🧵 Java 多线程 (`Thread`, `CopyOnWriteArrayList`)
-   **项目构建 (Build):** 📦 Apache Maven
-   **日志 (Logging):** 📝 SLF4J & Logback

## 🚀 如何运行 (How to Run)

#### 环境要求
-   JDK 1.8 或更高版本
-   Apache Maven 3.6+

---

### 方式一：使用命令行 (推荐)

1.  **克隆仓库到本地**
    ```bash
    git clone [https://github.com/EEE/JavaWhiteBoard.git](https://github.com/EEE/JavaWhiteBoard.git)
    cd JavaWhiteBoard
    ```

2.  **使用 Maven 进行编译和打包**
    此命令会清除旧的构建，并打包成两个可执行的 `.jar` 文件。
    ```bash
    mvn clean package
    ```

3.  **启动服务器**
    打包成功后，在命令行中运行服务器。服务器将开始监听端口。
    ```bash
    java -jar target/whiteboard-server.jar
    ```

4.  **启动客户端**
    打开一个新的命令行窗口，运行客户端。您可以重复此步骤以启动任意多个客户端。
    ```bash
    java -jar target/whiteboard-client.jar
    ```

---

### 方式二：在 IntelliJ IDEA 中运行

1.  使用 IntelliJ IDEA 打开项目。
2.  等待 IDEA 自动加载并识别为 Maven 项目。
3.  **首先运行服务器**: 在左侧项目视图中找到 `src/main/java` 下的 `ServerApp.java`，右键点击并选择 `Run 'ServerApp.main()'`。
4.  **再运行客户端**: 找到 `ClientApp.java`，同样右键点击并选择 `Run 'ClientApp.main()'`。可多次运行以模拟多用户。

## 📂 项目结构 (Project Structure)

项目遵循标准的 Maven 目录结构，代码逻辑按职责清晰地划分为三个主要模块：

```
src/main/java/
└── com/yourcompany/whiteboard/
    ├── client/       # 客户端所有代码：GUI、网络连接、事件处理
    ├── server/       # 服务器端所有代码：监听、并发处理、消息广播
    └── shared/       # 共享代码：主要用于定义客户端与服务器之间的通信协议，如 DrawAction 数据对象
```

-   **`shared`**: 定义了通信的核心数据结构 `DrawAction`，确保客户端和服务器对绘图指令有统一的理解。
-   **`server`**: 负责监听客户端连接，为每个连接创建一个独立线程。它维护一个完整的绘图历史记录，并将收到的任何新绘图动作广播给所有已连接的客户端。
-   **`client`**: 负责图形界面的展示和用户交互。它监听用户的鼠标操作，将其封装成 `DrawAction` 对象发送给服务器，并接收从服务器广播来的指令来更新自己的画板。

## 📝 未来计划 (Future Plans)

-   [ ] 添加橡皮擦功能
-   [ ] 支持绘制不同形状（如圆形、矩形）
-   [ ] 添加文本输入工具
-   [ ] 实现用户列表显示功能
-   [ ] 实现保存/加载画布到文件的功能

## 📄 许可证 (License)

本项目采用 [MIT](https://opensource.org/licenses/MIT) 许可证。详情请见 `LICENSE` 文件。
