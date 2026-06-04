// 业务管理路由骨架
// 实际菜单由后端动态返回，这里仅保留 Layout 外壳作为兜底
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/business",
  name: "Business",
  component: Layout,
  redirect: "/business/diary",
  meta: {
    icon: "ep/document",
    title: "业务管理",
    rank: 10
  },
  children: []
} satisfies RouteConfigsTable;
