/* eslint-disable */
/* prettier-ignore */
import type { GeneratedRoute } from '@elegant-router/types';

export const generatedRoutes: GeneratedRoute[] = [
  {
    name: '403',
    path: '/403',
    component: 'layout.blank$view.403',
    meta: {
      title: '403',
      i18nKey: 'route.403',
      constant: true,
      hideInMenu: true
    }
  },
  {
    name: '404',
    path: '/404',
    component: 'layout.blank$view.404',
    meta: {
      title: '404',
      i18nKey: 'route.404',
      constant: true,
      hideInMenu: true
    }
  },
  {
    name: '500',
    path: '/500',
    component: 'layout.blank$view.500',
    meta: {
      title: '500',
      i18nKey: 'route.500',
      constant: true,
      hideInMenu: true
    }
  },
  {
    name: 'category',
    path: '/category',
    component: 'layout.base$view.category',
    meta: {
      title: '分类管理',
      icon: 'mdi:folder-outline',
      order: 3,
      i18nKey: 'route.category'
    }
  },
  {
    name: 'drug',
    path: '/drug',
    component: 'layout.base$view.drug',
    meta: {
      title: '药品管理',
      icon: 'mdi:pill',
      order: 2,
      i18nKey: 'route.drug'
    }
  },
  {
    name: 'expiry-alert',
    path: '/expiry-alert',
    component: 'layout.base$view.expiry-alert',
    meta: {
      title: '效期预警',
      icon: 'mdi:alert-circle-outline',
      order: 9,
      i18nKey: 'route.expiry-alert'
    }
  },
  {
    name: 'home',
    path: '/home',
    component: 'layout.base$view.home',
    meta: {
      title: '首页',
      icon: 'mdi:monitor-dashboard',
      order: 1,
      i18nKey: 'route.home'
    }
  },
  {
    name: 'iframe-page',
    path: '/iframe-page/:url',
    component: 'layout.base$view.iframe-page',
    props: true,
    meta: {
      title: 'iframe-page',
      i18nKey: 'route.iframe-page',
      constant: true,
      hideInMenu: true,
      keepAlive: true
    }
  },
  {
    name: 'inventory',
    path: '/inventory',
    component: 'layout.base$view.inventory',
    meta: {
      title: '库存管理',
      icon: 'mdi:warehouse',
      order: 5,
      i18nKey: 'route.inventory'
    }
  },
  {
    name: 'inventory-log',
    path: '/inventory-log',
    component: 'layout.base$view.inventory-log',
    meta: {
      title: '出入库记录',
      icon: 'mdi:clipboard-list',
      order: 6,
      i18nKey: 'route.inventory-log'
    }
  },
  {
    name: 'login',
    path: '/login/:module(pwd-login|code-login|register|reset-pwd|bind-wechat)?',
    component: 'layout.blank$view.login',
    props: true,
    meta: {
      title: 'login',
      i18nKey: 'route.login',
      constant: true,
      hideInMenu: true
    }
  },
  {
    name: 'operation-log',
    path: '/operation-log',
    component: 'layout.base$view.operation-log',
    meta: {
      title: '操作日志',
      icon: 'mdi:history',
      order: 11,
      i18nKey: 'route.operation-log'
    }
  },
  {
    name: 'purchase-order',
    path: '/purchase-order',
    component: 'layout.base$view.purchase-order',
    meta: {
      title: '采购管理',
      icon: 'mdi:cart-outline',
      order: 7,
      i18nKey: 'route.purchase-order'
    }
  },
  {
    name: 'sale',
    path: '/sale',
    component: 'layout.base$view.sale',
    meta: {
      title: '销售记录',
      icon: 'mdi:point-of-sale',
      order: 8,
      i18nKey: 'route.sale'
    }
  },
  {
    name: 'supplier',
    path: '/supplier',
    component: 'layout.base$view.supplier',
    meta: {
      title: '供应商管理',
      icon: 'mdi:truck-delivery',
      order: 4,
      i18nKey: 'route.supplier'
    }
  },
  {
    name: 'user-manage',
    path: '/user-manage',
    component: 'layout.base$view.user-manage',
    meta: {
      title: '用户管理',
      icon: 'mdi:account-group',
      order: 10,
      i18nKey: 'route.user-manage'
    }
  }
];
