# B站客户端跳转助手 · Bili Client Bridge

将外部 B 站链接转交给你选择的 Android 客户端，支持 PiliNara、PiliPlus 等兼容应用。

## 下载与使用

在 [Releases](https://github.com/Bejukoro/BiliClientBridge/releases/latest) 下载最新 APK。最低支持 Android 8.0，当前正式版为 **1.2.0**。

1. 安装 PiliNara、PiliPlus 或其他兼容客户端。
2. 安装并打开助手，选择默认客户端。
3. 在“链接设置”中允许助手打开 B 站链接。
4. 从其他应用打开 B 站内容，或粘贴链接后点击“打开链接”。

助手使用大陆版哔哩哔哩包名 `tv.danmaku.bili`，与官方 App 签名不同，无法共存或直接覆盖官方版。已安装旧版助手的用户可直接覆盖更新，默认客户端设置会保留。

## 功能

- 自动发现兼容客户端，支持正式版、调试版和其他分支。
- 保存默认客户端，支持随时更换。
- 在选择列表中显示应用图标、名称和完整包名。
- 支持 `bilibili://`、B 站网页、`b23.tv` 短链、分享文本和手动粘贴。
- 保留内容 ID、分 P、播放时间等链接参数，由目标客户端解析内容。
- Material 3 Expressive 卡片式界面，支持动态配色与深色模式。
- 首页提供粘贴按钮，以及带图标的链接设置和使用帮助入口。
- 无网络、存储或查询全部应用权限。

## 兼容说明

部分 QQ、微信入口会校验官方签名、调用私有组件或限制外部跳转，这类入口可能无法唤起助手。Android 12 及以上系统通常需要手动允许打开支持的链接。

助手优先使用客户端声明的链接入口。PiliNara、PiliPlus 的正式版及 debug/dev 版还支持通过导出的启动入口接收链接。其他客户端通过系统链接处理声明发现，具体页面与短链支持取决于目标客户端。

客户端与助手需要安装在同一用户空间。系统分身、应用分身和工作资料可能影响应用发现。

本项目为独立第三方工具，与哔哩哔哩及相关客户端项目没有官方关联。

## 从源码构建

工程采用 Java 和官方 Material Components，当前应用版本为 `1.2.0`，versionCode 为 `10`，target API 为 `36`。

构建环境需要 JDK 21、Android SDK Platform 37.0 和 Build Tools 36.1.0。工程附带 Gradle 9.7.1 Wrapper，使用 Android Gradle Plugin 9.4.1。

配置 `JAVA_HOME` 和 `ANDROID_HOME` 后，在仓库根目录运行。

```powershell
./build.ps1
./test.ps1
```

默认构建输出到 `app/build/outputs/apk/debug/app-debug.apk`。构建 Release APK 可运行以下命令。

```powershell
./build.ps1 -Tasks ':app:assembleRelease',':app:lintRelease'
```

输出文件位于 `app/build/outputs/apk/release/app-release.apk`。

构建脚本会创建本机签名密钥。发布包沿用原签名以支持覆盖更新，私钥独立保存。自行生成新密钥构建的 APK 需要卸载原助手后安装。

## 项目结构

| 路径 | 用途 |
| --- | --- |
| `app/src/main/java/local/pilinara/bridge/MainActivity.java` | 界面、默认客户端保存和链接转交 |
| `app/src/main/java/local/pilinara/bridge/ClientRegistry.java` | 客户端发现与筛选 |
| `app/src/main/java/local/pilinara/bridge/ClientListAdapter.java` | 客户端图标、名称和包名列表 |
| `app/src/main/java/local/pilinara/bridge/LinkParser.java` | 链接提取与输入过滤 |
| `app/src/main/res/layout/` | 首页与列表布局 |
| `tests/LinkParserTest.java` | 链接解析检查 |

## 许可证

本项目采用 [MIT License](LICENSE)。

接口参考来自 [PiliNara Android Manifest](https://github.com/Starfallan/PiliNara/blob/main/android/app/src/main/AndroidManifest.xml) 和 [PiliPlus Android 配置](https://github.com/bggRGjQaUbCoE/PiliPlus/blob/main/android/app/build.gradle.kts)。
