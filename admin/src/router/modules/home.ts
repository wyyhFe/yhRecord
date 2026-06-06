// 首页 + 仪表盘（所有登录用户共享，不走后端动态路由）
// 这两条路由如果由后端 sys_menu 再返回一遍，会跟这里同名冲突，所以后端 V20260604_9 已把这两条删掉
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/",
  name: "Home",
  component: Layout,
  redirect: "/dashboard",
  meta: {
    icon: "ep/home-filled",
    title: "首页",
    rank: 0
  },
  children: [
    {
      path: "/dashboard",
      name: "Dashboard",
      component: () => import("@/views/dashboard/index.vue"),
      meta: {
        title: "仪表盘",
        showLink: true
      }
    }
  ]
} satisfies RouteConfigsTable;
