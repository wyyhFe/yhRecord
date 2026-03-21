/// <reference types="vite/client" />
/// <reference types="@dcloudio/types" />

interface ImportMetaEnv {
  readonly VITE_APP_NAME: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_OSS_BASE_URL: string
  readonly VITE_TENCENT_MAP_KEY: string
  readonly VITE_MP_DIARY_TEMPLATE_ID: string
  readonly VITE_MP_LEDGER_TEMPLATE_ID: string
  readonly VITE_MP_LEDGER_MONTHLY_TEMPLATE_ID: string
  readonly VITE_MP_MEMORIAL_TEMPLATE_ID: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
