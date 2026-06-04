// 系统管理路由骨架
// 实际菜单由后端动态返回，这里仅保留 Layout 外壳作为兜底
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/system",
  name: "System",
  component: Layout,
  redirect: "/system/menu",
  meta: {
    icon: "ep/setting",
    title: "系统管理",
    rank: 99
  },
  children: []
} satisfies RouteConfigsTable;
