# v1.1.0 — 首个公开版本

B站客户端跳转助手，Android 应用版本 1.1（versionCode 2）。

- 使用 `tv.danmaku.bili` 接收外部 B 站链接。
- 自动发现 PiliNara、PiliPlus 等声明支持链接的客户端。
- 选择并记住默认客户端，支持更换默认项。
- 支持 bilibili scheme、B 站网页、b23.tv 短链接及分享文本。
- 无网络、存储或 QUERY_ALL_PACKAGES 权限。

安装附件 `BiliClientBridge-v1.1.0.apk`，最低 Android 8.0。此 APK 为本地调试密钥签名，与之前分发的 1.1 助手使用相同密钥，可覆盖安装。无法与官方哔哩哔哩 App 共存或覆盖官方版。

QQ / 微信入口若校验官方签名或限制外部跳转，本工具无法绕过。Android 12+ 可能需要在系统设置中手动允许打开支持的链接。

验证：构建成功；14 项链接解析检查通过；APK v2 签名验证通过；Lint 0 错误、5 项警告。尚未进行真机、客户端端到端或实际 QQ / 微信小程序测试。

源码采用 MIT 许可证。APK 校验值见附件 `SHA256SUMS.txt`。
