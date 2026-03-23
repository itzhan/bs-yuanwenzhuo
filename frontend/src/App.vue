<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <router-view />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#1B6B4A',
    primaryColorHover: '#2D8F65',
    primaryColorPressed: '#134F37',
    primaryColorSuppl: '#2D8F65',
    borderRadius: '8px',
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
  },
  Button: {
    borderRadiusMedium: '8px',
    borderRadiusLarge: '12px'
  },
  Card: {
    borderRadius: '12px'
  },
  Input: {
    borderRadius: '8px'
  }
}
</script>
