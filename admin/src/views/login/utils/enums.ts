import { $t } from "@/plugins/i18n";

const operates = [
  {
    title: $t("login.purePhoneLogin")
  },
  {
    title: $t("login.pureQRCodeLogin")
  },
  {
    title: $t("login.pureRegister")
  }
];

/** 默认展示的第三方登录（GitHub + Google） */
const thirdPartyPrimary = [
  {
    title: "GitHub",
    icon: "github",
    provider: "github",
    action: "oauth"
  },
  {
    title: "Google",
    icon: "google",
    provider: "google",
    action: "oauth"
  }
];

/** 展开后展示更多（微信/支付宝/QQ/微博，暂无实际逻辑） */
const thirdPartyMore = [
  {
    title: $t("login.pureWeChatLogin"),
    icon: "wechat",
    provider: "wechat",
    action: "none"
  },
  {
    title: $t("login.pureAlipayLogin"),
    icon: "alipay",
    provider: "alipay",
    action: "none"
  },
  {
    title: $t("login.pureQQLogin"),
    icon: "qq",
    provider: "qq",
    action: "none"
  },
  {
    title: $t("login.pureWeiBoLogin"),
    icon: "weibo",
    provider: "weibo",
    action: "none"
  }
];

export { operates, thirdPartyPrimary, thirdPartyMore };
