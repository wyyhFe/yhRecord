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
  children: [
    {
      path: "/business/diary",
      name: "Diary",
      component: () => import("@/views/diary/index.vue"),
      meta: { title: "日记管理", icon: "ep/notebook" }
    },
    {
      path: "/business/checkin",
      name: "Checkin",
      component: () => import("@/views/checkin/index.vue"),
      meta: { title: "打卡管理", icon: "ep/circle-check" }
    },
    {
      path: "/business/memorial",
      name: "Memorial",
      component: () => import("@/views/memorial/index.vue"),
      meta: { title: "纪念日管理", icon: "ep/calendar" }
    },
    {
      path: "/business/ledger",
      name: "Ledger",
      component: () => import("@/views/ledger/index.vue"),
      meta: { title: "记账管理", icon: "ep/money" }
    }
  ]
} satisfies RouteConfigsTable;
