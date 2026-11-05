# B站客户端跳转助手 · Bili Client Bridge

让外部 B 站链接转交给你选择的 Android 第三方客户端，支持 PiliNara、PiliPlus 等声明兼容链接的应用。

本工具使用大陆版哔哩哔哩包名 `tv.danmaku.bili` 接收唤起请求，再显式转交目标客户端。它是独立第三方工具，与哔哩哔哩及各客户端项目没有官方关联。

## 功能

- 自动发现兼容客户端，不限定固定包名，支持不同分支和调试版。
- 选择并保存默认客户端；默认项已卸载或不支持当前链接时重新选择。
- 接收 `bilibili://`、B 站 http/https 网址、`b23.tv` 短链、纯文本分享和手动粘贴。
- 原样保留内容 ID、分 P、时间等 URL 参数；页面处理和短链解析由目标客户端完成。
- 无网络、存储或查询全部应用权限，无第三方运行库。

## 下载与安装

从本仓库 **Releases** 下载 APK，最低支持 **Android 8.0 / API 26**。

**无法与官方哔哩哔哩 App 共存。** 包名相同但签名不同，不能覆盖安装官方版。是否卸载官方版由你自行决定，卸载可能清除其本地数据。

1. 安装 PiliNara、PiliPlus 或其他兼容客户端。
2. 安装跳转助手，打开后点击“选择 / 更换默认客户端”。
3. 点击“设置支持的链接”，按系统界面允许本助手打开 B 站网址。
4. 从 QQ、微信等入口尝试“打开哔哩哔哩”，或把分享文本分享给助手。

未设置默认项且只有一个候选时自动记住；多个候选时弹框选择。取消或没有可用客户端时保留输入。

## 兼容性与限制

- 同包名不能代替官方签名。来源若校验签名、调用未适配的私有组件、限制外部跳转或强制下载官方版，可能仍无法唤起本助手。
- 本工具没有 B 站域名控制权，无法通过官方 App Links 域名验证。Android 12+ 通常需要在系统设置中手动指定支持的链接。
- 仅转交目标客户端**声明支持当前链接**的 Activity，不会强行向其主页发送不支持的格式。客户端声明能接收链接，不代表其能解析所有具体页面。
- 设置列表用视频 scheme、视频网页、短链和直播网页探测应用。只支持其他特殊页面的应用可能不在设置列表，但收到实际支持的链接时仍可参与选择。
- 排除助手自身和仅处理通用网页的浏览器；同时声明 bilibili scheme 的应用仍会作为候选。
- 当前未完成真机、客户端端到端或实际 QQ / 微信小程序验收。不能保证所有入口、手机系统和客户端版本均有效。

## 从源码构建

工程采用原生 Java，应用版本 **1.1 / versionCode 2**，target API 36。需要 **JDK 21、Android SDK Platform 37.0、Build Tools 36.1.0**；Gradle Wrapper 固定为 9.7.1，Android Gradle Plugin 为 9.4.1。首次构建需要联网下载依赖。

在 Android Studio 打开仓库，或配置 `JAVA_HOME` 和 `ANDROID_HOME` 后，在仓库根目录运行：

```powershell
./build.ps1
./test.ps1
```

`build.ps1` 创建本机调试签名，再执行 `assembleDebug` 和 `lintDebug`。APK 输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

也可自行创建 `signing/bridge-debug.keystore`（alias / store password / key password 分别为 `androiddebugkey` / `android` / `android`），使用标准 Gradle 命令：

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

签名密钥不随源码公开。自行构建的新密钥签名 APK 无法覆盖 Release APK；需要先卸载已有助手。维护者需保留原私钥才能继续提供可覆盖更新的版本。Release 的首版 APK 沿用之前本地 1.1 版本的调试密钥。

## 验证

首版发布准备时已通过：构建、14 项链接解析检查、APK v2 签名验证；Lint 0 错误、5 项警告。解析测试覆盖链接提取与输入过滤，不覆盖 Android 客户端发现和持久化选择。

真机待验收：单客户端自动选择、多客户端选择、默认项切换及重启后持久化、卸载默认项、无候选提示、视频/番剧/直播/短链、客户端冷启动与后台运行、QQ / 微信实际入口。

## 项目结构

| 路径 | 用途 |
| --- | --- |
| `app/src/main/java/local/pilinara/bridge/MainActivity.java` | 界面、默认项保存和显式跳转 |
| `app/src/main/java/local/pilinara/bridge/ClientRegistry.java` | 系统链接处理器发现与候选筛选 |
| `app/src/main/java/local/pilinara/bridge/LinkParser.java` | 分享文本链接提取和输入过滤 |
| `tests/LinkParserTest.java` | 14 项独立 Java 解析检查 |

## 许可证与参考

本项目采用 [MIT License](LICENSE)。未复制或包含第三方客户端代码、资源或官方图标。

接口参考：[PiliNara Android Manifest](https://github.com/Starfallan/PiliNara/blob/main/android/app/src/main/AndroidManifest.xml)、[PiliPlus Android 配置](https://github.com/bggRGjQaUbCoE/PiliPlus/blob/main/android/app/build.gradle.kts)。
